/*
 * Copyright (c) 2020-2022 MangoPlex
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package multipacks.bundling;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import multipacks.management.PacksRepository;
import multipacks.packs.DynamicPack;
import multipacks.packs.Pack;
import multipacks.packs.PackIdentifier;
import multipacks.packs.PackIndex;
import multipacks.plugins.MultipacksPlugin;
import multipacks.postprocess.PostProcessPass;
import multipacks.utils.Selects;
import multipacks.utils.logging.AbstractMPLogger;
import multipacks.vfs.Path;
import multipacks.vfs.legacy.VirtualFs;

public class PackBundler {
	public final List<PacksRepository> repositories = new ArrayList<>();
	public final List<BundleIgnore> bundlingIgnores = new ArrayList<>();
	public boolean ignoreResolveFail = false;
	public AbstractMPLogger logger;

	public PackBundler(AbstractMPLogger logger) {
		this.logger = logger;
	}

	private Pack getPack(PackIdentifier id, File parentRoot, String parentName) {
		if (id.folder != null) {
			File packRoot = new File(parentRoot, id.folder.replace('/', File.separatorChar));
			if (!packRoot.exists()) throw new PackagingFailException(parentName + ": Folder does not exists: " + packRoot);
			if (!packRoot.isDirectory()) throw new PackagingFailException(parentName + ": Not a folder: " + packRoot);

			try {
				return new Pack(packRoot);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}

		for (PacksRepository repo : repositories) {
			PackIndex latest = null;
			Iterator<PackIndex> iter = repo.queryPacks(id);

			while (iter.hasNext()) {
				PackIndex index = iter.next();
				if (id.version.check(index.packVersion) && (latest == null || latest.packVersion.compareTo(index.packVersion) < 0)) latest = index;
			}

			if (latest != null) return repo.getPack(latest);
		}

		return null;
	}

	private void resolveDependencies(String parent, Pack source, HashMap<String, Pack> resolvedMap, List<Pack> resolvedList) {
		for (PackIdentifier dependency : source.getIndex().include) {
			if (resolvedMap.containsKey(dependency.id)) continue; // TODO: Grab the latest version if requested
			String dependencyDisplayPath = parent + "/" + dependency.id + " " + (dependency.folder != null? "Local file: " + dependency.folder : dependency.version);

			Pack pack = getPack(dependency, source.getRoot(), parent);
			if (pack == null) {
				// 2nd pass: plugins
				for (MultipacksPlugin plug : MultipacksPlugin.PLUGINS) {
					pack = plug.packsResolutionFailback(dependency);
					if (pack != null) break;
				}

				if (pack == null) {
					if (!ignoreResolveFail) throw new PackagingFailException((dependencyDisplayPath + " -> FAILED while resolving this dependency"));
					logger.warning(dependencyDisplayPath + " -> FAILED while resolving (ignored)");
					continue;
				}
			}

			logger.info(dependencyDisplayPath + " -> " + pack.getIndex().packVersion + (pack instanceof DynamicPack? " (Dynamic)" : ""));
			resolvedMap.put(dependency.id, pack);
			resolvedList.add(pack);
			resolveDependencies(parent + "/" + dependency.id, pack, resolvedMap, resolvedList);
		}
	}

	private boolean isIncluded(BundleInclude[] includes, BundleInclude... b) {
		if (includes == null) return true;
		for (BundleInclude i : b) {
			for (BundleInclude included : includes) if (i == included) return true;
		}
		return false;
	}

	private void apply(Pack pack, HashMap<String, Pack> resolvedMap, VirtualFs destination, BundleResult result, BundleInclude[] included) throws IOException {
		VirtualFs packFiles = new VirtualFs(pack.getRoot());
		if (pack.getIndex().include != null) for (PackIdentifier dependency : pack.getIndex().include) {
			Pack p = resolvedMap.get(dependency.id);
			if (p != null) apply(p, resolvedMap, packFiles, result, new BundleInclude[] { BundleInclude.RESOURCES, BundleInclude.DATA });
		}

		// Post processing (if any)
		if (pack.getIndex().postProcess != null) PostProcessPass.apply(pack.getIndex().postProcess, packFiles, result, logger);

		// Dynamic packs are not allowed to use exports field!
		if (pack instanceof DynamicPack dynamic) {
			dynamic.build(packFiles, result);
			return;
		}

		// Export all exported contents (defined in index)
		Map<Path, BundleInclude[]> exports = Selects.firstNonNull(pack.getIndex().exports, Map.of(
				new multipacks.vfs.Path("assets"), new BundleInclude[] { BundleInclude.RESOURCES },
				new multipacks.vfs.Path("data"), new BundleInclude[] { BundleInclude.DATA }
				));
		for (Entry<Path, BundleInclude[]> e : exports.entrySet()) {
			if (!isIncluded(included, e.getValue())) continue;
			Path exportedDir = e.getKey();
			for (Path file : packFiles.ls(exportedDir)) destination.write(file, packFiles.read(file));
		}

		// Write licenses
		for (Path licFile : packFiles.ls(new Path("licenses"))) destination.write(licFile, packFiles.read(licFile));
		final String[] LICENSE_FILES = { "license", "licence", "license.txt", "licence.txt", "license.md", "licence.md" };
		if (!bundlingIgnores.contains(BundleIgnore.LICENSES)) for (String f : LICENSE_FILES) {
			Path licFile = new Path(f);
			if (packFiles.isExists(licFile)) {
				byte[] data = packFiles.read(licFile);
				destination.write(new Path("licenses", pack.getIdentifier().id + ".txt"), data);
			}
		}
	}

	public BundleResult bundle(Pack pack, OutputStream out, BundleInclude[] included) throws IOException {
		BundleResult result = new BundleResult();
		ArrayList<Pack> dependencies = new ArrayList<>();
		HashMap<String, Pack> resolvedMap = new HashMap<>();
		resolveDependencies(pack.getIndex().id, pack, resolvedMap, dependencies);

		VirtualFs outputFs = new VirtualFs(null);
		apply(pack, resolvedMap, outputFs, result, included);
		result.files = outputFs;

		// Writing generated files
		outputFs.writeJson(new Path("pack.mcmeta"), pack.getIndex().getMcMeta());

		if (out != null) {
			ZipOutputStream zip = new ZipOutputStream(out, StandardCharsets.UTF_8);
			FileTime time = FileTime.fromMillis(System.currentTimeMillis());
			for (Entry<Path, byte[]> e : outputFs.emulatedFs.entrySet()) {
				zip.putNextEntry(new ZipEntry(e.getKey().toString()).setCreationTime(time).setLastModifiedTime(time));
				zip.write(e.getValue());
				zip.closeEntry();
			}
			zip.close();
		}

		return result;
	}
}
