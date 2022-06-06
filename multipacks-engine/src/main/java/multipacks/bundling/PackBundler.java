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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;

import multipacks.management.PacksRepository;
import multipacks.packs.Pack;
import multipacks.packs.PackIdentifier;
import multipacks.packs.PackIndex;
import multipacks.packs.PackType;
import multipacks.transforms.TransformPass;
import multipacks.transforms.TransformativeFileSystem;
import multipacks.utils.logging.AbstractMPLogger;

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
				if (!ignoreResolveFail) throw new PackagingFailException((dependencyDisplayPath + " -> FAILED while resolving this dependency"));
				logger.warning(dependencyDisplayPath + " -> FAILED while resolving (ignored)");
				continue;
			}

			logger.info(dependencyDisplayPath + " -> " + pack.getIndex().packVersion + (pack.getIndex().type == PackType.LIBRARY? " (Library)" : ""));
			resolvedMap.put(dependency.id, pack);
			resolvedList.add(pack);
			resolveDependencies(parent + "/" + dependency.id, pack, resolvedMap, resolvedList);
		}
	}

	private boolean shouldWrite(BundleInclude[] includes, String path) {
		for (BundleInclude incl : includes) {
			if (incl == BundleInclude.RESOURCES && path.startsWith("assets/")) return true;
			if (incl == BundleInclude.DATA && path.startsWith("data/")) return true;
		}

		return false;
	}

	private static final String[] GLOBALLY_IGNORED = { ".git" };

	private TransformativeFileSystem transform(HashMap<String, Pack> resolvedMap, HashMap<String, TransformativeFileSystem> libraryCache, TransformativeFileSystem root, Pack pack, BundleResult result, BundleInclude[] includes) throws IOException {
		TransformativeFileSystem fs = new TransformativeFileSystem(pack.getRoot());

		// Resolving libraries
		if (pack.getIndex().include != null) for (PackIdentifier dependency : pack.getIndex().include) {
			Pack dependencyPack = resolvedMap.get(dependency.id);
			if (dependencyPack == null || dependencyPack.getIndex().type != PackType.LIBRARY) continue;

			TransformativeFileSystem dependencyFs;

			if ((dependencyFs = libraryCache.get(dependency.id)) == null) {
				dependencyFs = transform(resolvedMap, libraryCache, fs, dependencyPack, result, includes);
				libraryCache.put(dependency.id, fs);
			} else dependencyFs.forEach(l -> {
				try {
					byte[] data = l.getAsBytes();
					fs.put(l.path, data);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}

		// Transformations main
		InputStream transformations = fs.openRead("transformations.json");

		if (transformations != null) {
			JsonArray arr = new JsonParser().parse(new InputStreamReader(transformations)).getAsJsonArray();
			arr.forEach(element -> {
				TransformPass pass = TransformPass.fromJson(element.getAsJsonObject());

				if (pass == null) {
					System.err.println("WARNING: Transformation pass with type '" + element.getAsJsonObject().get("type").getAsString() + "' does not exists in this version of Multipacks");
					return;
				}

				try {
					pass.transform(fs, result, logger);
				} catch (IOException e) {
					e.printStackTrace();
					System.err.println("WARNING: Transformation pass with type '" + element.getAsJsonObject().get("type").getAsString() + "' failed while transforming " + pack.getIndex().id);
				}
			});
			transformations.close();
		}

		fs.forEach(t -> {
			try {
				String path = t.path;
				if (fs.isDeleted(path)) return;
				if (pack.getIndex().isIgnored(path)) return;
				for (String globalIgnore : GLOBALLY_IGNORED) if (path.startsWith(globalIgnore)) return;

				String[] splits = t.path.split("/");
				if (splits.length == 1) switch (splits[0].toLowerCase()) {
				case "license":
				case "license.md":
				case "license.txt":
				case "licence":
				case "licence.md":
				case "licence.txt":
					if (this.bundlingIgnores.contains(BundleIgnore.LICENSES)) return;
					path = "licenses/" + pack.getIndex().id;
					break;
				default:
					return;
				}

				if (shouldWrite(includes, path)) root.put(path, t.getAsBytes());
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Failed to get data from " + t.path + " in TFS");
			}
		});

		return fs;
	}

	/**
	 * Bundle the pack and write pack data as ZIP package to stream.
	 * @param source The source pack.
	 * @param stream The stream to write.
	 * @param includes Pack types to includes. If this array contains nothing, it will includes everything.
	 * @return The bundling result, usually contains some informations generated from data transformers (font
	 * icons for example).
	 */
	public BundleResult bundle(Pack source, OutputStream stream, BundleInclude... includes) throws IOException {
		if (includes == null || includes.length == 0) includes = BundleInclude.values();
		final BundleInclude[] includesFinal = includes;

		// Resolving
		HashMap<String, Pack> resolvedMap = new HashMap<>();
		List<Pack> resolvedList = new ArrayList<>();
		resolveDependencies(source.getIdentifier().id, source, resolvedMap, resolvedList);
		resolvedList.add(source);

		// Transforming
		HashMap<String, TransformativeFileSystem> libraryCache = new HashMap<>();
		TransformativeFileSystem root = new TransformativeFileSystem(null);
		BundleResult result = new BundleResult();

		for (Pack pack : resolvedList) {
			if (pack.getIndex().type == PackType.LIBRARY && pack != source) continue;
			transform(resolvedMap, libraryCache, root, pack, result, includesFinal);
		}

		// Packaging
		ZipOutputStream zip = new ZipOutputStream(stream);
		long creationTime = System.currentTimeMillis();

		root.forEach(t -> {
			try {
				zip.putNextEntry(new ZipEntry(t.path).setCreationTime(FileTime.fromMillis(creationTime)));
				zip.write(t.transformed);
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Failed to write " + t.path + " to ZipOutputStream, skipping");
			}
		});

		zip.putNextEntry(new ZipEntry("pack.mcmeta").setCreationTime(FileTime.fromMillis(creationTime)));
		JsonWriter mcMetaWriter = new JsonWriter(new OutputStreamWriter(zip));
		mcMetaWriter.setIndent("    ");
		new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(source.getIndex().getMcMeta(), mcMetaWriter);
		mcMetaWriter.flush();

		File packPng = new File(source.getRoot(), "pack.png");

		if (packPng.exists()) {
			zip.putNextEntry(new ZipEntry("pack.png").setCreationTime(FileTime.fromMillis(creationTime)));
			InputStream packPngIn = new FileInputStream(packPng);
			packPngIn.transferTo(zip);
			packPngIn.close();
		}

		zip.closeEntry();
		zip.close();

		return result;
	}
}
