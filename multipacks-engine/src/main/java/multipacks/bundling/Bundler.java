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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.FileTime;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

import multipacks.packs.Pack;
import multipacks.packs.meta.PackIdentifier;
import multipacks.platform.Platform;
import multipacks.repository.RepositoriesAccess;
import multipacks.repository.Repository;
import multipacks.repository.query.PackQuery;
import multipacks.utils.Messages;
import multipacks.versioning.Version;
import multipacks.vfs.Vfs;

/**
 * @author nahkd
 *
 */
public class Bundler {
	public RepositoriesAccess repositoriesAccess;

	public Bundler() {
	}

	public Bundler setRepositoriesAccess(RepositoriesAccess access) {
		this.repositoriesAccess = access;
		return this;
	}

	public Bundler fromPlatform(Platform platform) {
		return this
				.setRepositoriesAccess(platform);
	}

	private Vfs bundleWithoutFinish(Pack pack) {
		// TODO: Return CompletableFuture instead
		Vfs content = Vfs.createVirtualRoot();

		if (pack.getIndex().dependencies.size() > 0) {
			if (repositoriesAccess == null) throw new IllegalStateException("Repositories accessor is missing for this Bundler");

			for (PackQuery depQuery : pack.getIndex().dependencies) {
				for (Repository repo : repositoriesAccess.getRepositories()) {
					try {
						// TODO: improve dependencies resolution algorithm
						// caching is needed.
						Collection<PackIdentifier> ids = repo.search(depQuery).get();
						if (ids.size() == 0) throw new RuntimeException(Messages.cantResolveDependency(depQuery)); // TODO

						PackIdentifier latest = null;
						for (PackIdentifier id : ids) {
							if (latest == null || latest.packVersion.compareTo(id.packVersion) < 0) {
								latest = id;
							}
						}

						Pack dep = repo.obtain(latest).get();
						Vfs.copyRecursive(bundleWithoutFinish(dep), content);
					} catch (ExecutionException | InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}

		pack.applyAsDependency(content);
		return content;
	}

	public Vfs bundle(Pack pack, Version targetGameVersion) {
		Vfs content = bundleWithoutFinish(pack);

		Vfs packMcmeta = content.touch("pack.mcmeta");
		try (OutputStream stream = packMcmeta.getOutputStream()) {
			JsonObject json = pack.getIndex().buildPackMcmeta(targetGameVersion);
			JsonWriter writer = new JsonWriter(new OutputStreamWriter(stream, StandardCharsets.UTF_8));
			new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(json, writer);
			writer.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// TODO: Licenses
		// TODO: pack.png

		return content;
	}

	public void bundleToStream(Pack pack, Version targetGameVersion, OutputStream stream) throws IOException {
		ZipOutputStream zip = new ZipOutputStream(stream, StandardCharsets.UTF_8);
		FileTime bundleTime = FileTime.fromMillis(System.currentTimeMillis());
		Vfs content = bundle(pack, targetGameVersion);
		vfsAddZipEntry(content, bundleTime, zip);
		zip.finish();
	}

	private void vfsAddZipEntry(Vfs file, FileTime bundleTime, ZipOutputStream zip) throws IOException {
		if (file.isDir()) {
			for (Vfs child : file.listFiles()) vfsAddZipEntry(child, bundleTime, zip);
		} else {
			ZipEntry entry = new ZipEntry(file.getPathFromRoot().toString())
					.setCreationTime(bundleTime)
					.setLastModifiedTime(bundleTime);
			zip.putNextEntry(entry);
			zip.write(file.getContent());
			zip.closeEntry();
		}
	}
}
