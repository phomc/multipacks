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

import java.util.List;
import java.util.stream.Stream;

import multipacks.packs.meta.PackIndex;
import multipacks.vfs.Vfs;

/**
 * @author nahkd
 *
 */
public interface Pack {
	PackIndex getIndex();

	/**
	 * Create virtual file system with pack contents. Modifiers will not be applied to VFS contents.
	 * @return Virtual file system.
	 */
	Vfs createVfsWithoutModifiers();

	default Vfs createVfs(boolean applyModifiers) {
		Vfs vfs = createVfsWithoutModifiers();
		if (!applyModifiers) return vfs;

		// TODO: implement modifiers here
		return vfs;
	}

	/**
	 * Apply contents from this pack (with modifiers) to output VFS.
	 * @param outputVfs VFS that will be applied to.
	 */
	default void applyAsDependency(Vfs outputVfs) {
		Vfs thisPack = createVfs(true);
		List<Vfs> contentTypeDirs = Stream.of(thisPack.listFiles()).filter(v -> v.isDir()).toList();

		for (Vfs contentTypeDir : contentTypeDirs) {
			Vfs contentTypeDirOut = outputVfs.get(contentTypeDir.getName());
			if (contentTypeDirOut == null) contentTypeDirOut = outputVfs.mkdir(contentTypeDir.getName());
			Vfs.copyRecursive(contentTypeDir, contentTypeDirOut, true);
		}
	}
}
