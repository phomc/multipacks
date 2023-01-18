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
package multipacks.packs.legacy;

import java.io.File;
import java.io.IOException;

import multipacks.utils.IOUtils;

public class Pack {
	private File packRoot;
	private PackIndex index;
	private PackIdentifier identifier;

	public Pack(File packRoot) throws IOException {
		this.packRoot = packRoot;
		this.index = PackIndex.fromJson(IOUtils.jsonFromFile(new File(packRoot, "multipacks.json")).getAsJsonObject());
		this.identifier = index.getIdentifier();
	}

	/**
	 * Construct a brand new pack without local root directory. Use this if you want to build a dynamic pack. Oh and
	 * to actually build a dynamic pack, you'll have to extends {@link DynamicPack}. 
	 */
	public Pack(PackIndex index) {
		this.packRoot = null;
		this.index = index;
		this.identifier = index.getIdentifier();
	}

	public PackIndex getIndex() {
		return index;
	}

	public File getRoot() {
		return packRoot;
	}

	public PackIdentifier getIdentifier() {
		return identifier;
	}
}
