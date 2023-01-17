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
package multipacks.packs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import multipacks.packs.meta.PackIndex;
import multipacks.utils.IOUtils;
import multipacks.vfs.Vfs;

/**
 * @author nahkd
 *
 */
public class LocalPack implements Pack {
	public static final String FILE_INDEX = "multipacks.index.json";
	public static final String FILE_INDEX_LEGACY = "multipacks.json";

	public final Path packRoot;
	private PackIndex index;

	public LocalPack(Path packRoot) {
		this.packRoot = packRoot;
	}

	public void loadFromStorage() throws IOException {
		if (!Files.exists(packRoot)) throw new FileNotFoundException(packRoot.toString());
		Path indexFile = packRoot.resolve(FILE_INDEX);

		if (!Files.exists(indexFile)) {
			// TODO Attempt to load legacy file
			throw new FileNotFoundException(indexFile.toString());
		}

		index = new PackIndex(IOUtils.jsonFromPath(indexFile).getAsJsonObject());
	}

	@Override
	public PackIndex getIndex() {
		if (index == null) throw new IllegalStateException("This local pack is not loaded yet! Use LocalPack#loadFromStorage() to load.");
		return index;
	}

	@Override
	public Vfs createVfs(boolean applyModifiers) {
		Vfs vfs = Vfs.createRoot(packRoot);
		// TODO apply modifiers
		return vfs;
	}
}
