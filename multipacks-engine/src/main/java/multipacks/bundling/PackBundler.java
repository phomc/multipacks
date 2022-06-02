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

	private Pack getPack(PackIdentifier id) {
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

			Pack pack = getPack(dependency);
			if (pack == null) {
				if (!ignoreResolveFail) throw new PackagingFailException((parent + "/" + dependency.id + " " + dependency.version + " -> FAILED while resolving this dependency"));
				logger.warning(parent + "/" + dependency.id + " " + dependency.version + " -> FAILED while resolving (ignored)");
				continue;
			}

			logger.info(parent + "/" + dependency.id + " " + dependency.version + " -> " + pack.getIndex().packVersion);
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
		TransformativeFileSystem root = new TransformativeFileSystem(null);
		BundleResult result = new BundleResult();

		for (Pack pack : resolvedList) {
			TransformativeFileSystem fs = new TransformativeFileSystem(pack.getRoot());

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
					if (fs.markDelete.contains(path)) return;

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

					if (shouldWrite(includesFinal, path)) root.put(path, t.getAsBytes());
				} catch (IOException e) {
					e.printStackTrace();
					System.err.println("Failed to get data from " + t.path + " in TFS");
				}
			});
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
