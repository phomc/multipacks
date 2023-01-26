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
package multipacks.packs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.JsonArray;

import multipacks.packs.meta.PackIndex;
import multipacks.utils.Messages;
import multipacks.utils.io.IOUtils;
import multipacks.vfs.Vfs;

/**
 * @author nahkd
 *
 */
public class LocalPack implements Pack {
	public static final String ERROR_UNLOADED = "This local pack is not loaded yet! Use LocalPack#loadFromStorage() to load.";

	public static final String FILE_INDEX = "multipacks.index.json";
	public static final String FILE_INDEX_LEGACY = "multipacks.json";

	public static final String FILE_MODIFIERS = "multipacks.modifiers.json";

	public final Path packRoot;
	private PackIndex index;
	private JsonArray modifiers;

	public LocalPack(Path packRoot) {
		this.packRoot = packRoot;
	}

	public static LocalPack fromClassLoader(ClassLoader clsLoader, String pathToRepo) {
		try {
			return new LocalPack(Path.of(clsLoader.getResource(pathToRepo).toURI()));
		} catch (URISyntaxException e) {
			// It SHOULD NOT throw URISyntaxException
			throw new RuntimeException(Messages.INTERNAL_ERROR, e);
		}
	}

	public void loadFromStorage() throws IOException {
		if (!Files.exists(packRoot)) throw new FileNotFoundException(packRoot.toString());
		Path indexFile = packRoot.resolve(FILE_INDEX);

		if (!Files.exists(indexFile)) {
			// TODO Attempt to load legacy file
			throw new FileNotFoundException(indexFile.toString());
		}

		index = new PackIndex(IOUtils.jsonFromPath(indexFile).getAsJsonObject());
		if (Files.exists(packRoot.resolve(FILE_MODIFIERS))) modifiers = IOUtils.jsonFromPath(packRoot.resolve(FILE_MODIFIERS)).getAsJsonArray();
	}

	private void ensureLoaded() {
		if (index == null) throw new IllegalStateException(ERROR_UNLOADED);
	}

	@Override
	public PackIndex getIndex() {
		ensureLoaded();
		return index;
	}

	@Override
	public JsonArray getModifiersConfig() {
		ensureLoaded();
		return modifiers;
	}

	@Override
	public Vfs createVfs() {
		Vfs vfs = Vfs.createRoot(packRoot);
		// TODO apply modifiers
		return vfs;
	}
}
