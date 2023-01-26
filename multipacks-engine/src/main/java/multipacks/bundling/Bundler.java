/*
 * Copyright (c) 2022-2023 PhoMC
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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

import multipacks.modifier.Modifier;
import multipacks.modifier.ModifiersAccess;
import multipacks.packs.Pack;
import multipacks.packs.meta.PackIdentifier;
import multipacks.platform.Platform;
import multipacks.repository.RepositoriesAccess;
import multipacks.repository.Repository;
import multipacks.repository.query.PackQuery;
import multipacks.utils.Messages;
import multipacks.utils.ResourcePath;
import multipacks.versioning.Version;
import multipacks.vfs.Vfs;

/**
 * @author nahkd
 *
 */
public class Bundler {
	public RepositoriesAccess repositories;
	public ModifiersAccess modifiers;
	public boolean includeLicenses = true;
	public String[] licenseFileNames = new String[] {
			"license", "licence", "license.txt", "licence.txt", "license.md", "licence.md"
	};

	public Bundler() {
	}

	public Bundler setRepositoriesAccess(RepositoriesAccess access) {
		this.repositories = access;
		return this;
	}

	public Bundler setModifiersAccess(ModifiersAccess access) {
		this.modifiers = access;
		return this;
	}

	public Bundler setIncludeLicenses(boolean includeLicenses) {
		this.includeLicenses = includeLicenses;
		return this;
	}

	public Bundler fromPlatform(Platform platform) {
		return this
				.setRepositoriesAccess(platform)
				.setModifiersAccess(platform);
	}

	private Vfs bundleWithoutFinish(Pack pack, Vfs licensesStore, Vfs packFinalOutput, Map<ResourcePath, Modifier> modifiersMap) {
		// TODO: Return CompletableFuture instead
		Vfs content = Vfs.createVirtualRoot();

		if (pack.getIndex().dependencies.size() > 0) {
			if (repositories == null) throw new NullPointerException("Repositories accessor is missing for this Bundler");

			for (PackQuery depQuery : pack.getIndex().dependencies) {
				for (Repository repo : repositories.getRepositories()) {
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
						Vfs.copyRecursive(bundleWithoutFinish(dep, licensesStore, null, modifiersMap), content);
					} catch (ExecutionException | InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}

		Vfs thisPack = pack.createVfs();
		List<Vfs> contentTypeDirs = Stream.of(thisPack.listFiles()).filter(v -> v.isDir()).toList();

		for (Vfs contentTypeDir : contentTypeDirs) {
			Vfs contentTypeDirOut = content.get(contentTypeDir.getName());
			if (contentTypeDirOut == null) contentTypeDirOut = content.mkdir(contentTypeDir.getName());
			Vfs.copyRecursive(contentTypeDir, contentTypeDirOut);
		}

		// TODO: Modifiers
		JsonArray modifiersConfig = pack.getModifiersConfig();
		if (modifiersConfig != null) {
			if (modifiers == null) throw new NullPointerException("Modifiers accessor is missing for this Bundler");
			Modifier.applyModifiers(pack, content, modifiersConfig, modifiers, modifiersMap);
		}

		// Licenses & pack.png
		if (includeLicenses && licensesStore != null) {
			for (String licenseFileName : licenseFileNames) {
				Vfs f = thisPack.get(licenseFileName);
				if (f != null) {
					Vfs to = licensesStore.touch("license-" + pack.getIndex().name);
					try (OutputStream streamOut = to.getOutputStream(); InputStream streamIn = f.getInputStream()) {
						streamIn.transferTo(streamOut);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}

		if (packFinalOutput != null) {
			Vfs packPng = thisPack.get("pack.png");
			if (packPng != null) {
				Vfs packPngTo = packFinalOutput.touch("pack.png");
				try (OutputStream streamOut = packPngTo.getOutputStream(); InputStream streamIn = packPng.getInputStream()) {
					streamIn.transferTo(streamOut);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}

		return content;
	}

	public BundleResult bundle(Pack pack, Version targetGameVersion) {
		HashMap<ResourcePath, Modifier> modifiersMap = new HashMap<>();

		Vfs licenses = Vfs.createVirtualRoot();
		Vfs finalOutput = Vfs.createVirtualRoot();
		Vfs content = bundleWithoutFinish(pack, licenses, finalOutput, modifiersMap);

		Vfs.copyRecursive(licenses, content);
		Vfs.copyRecursive(finalOutput, content);

		BundleResult result = new BundleResult(content);
		result.modifiers = modifiersMap;
		for (Modifier modifier : modifiersMap.values()) modifier.finalizeModifier(content, modifiers);

		Vfs packMcmeta = content.touch("pack.mcmeta");
		try (OutputStream stream = packMcmeta.getOutputStream()) {
			JsonObject json = pack.getIndex().buildPackMcmeta(targetGameVersion);
			JsonWriter writer = new JsonWriter(new OutputStreamWriter(stream, StandardCharsets.UTF_8));
			new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(json, writer);
			writer.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return result;
	}
}
