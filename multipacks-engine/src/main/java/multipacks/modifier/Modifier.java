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
package multipacks.modifier;

import multipacks.packs.Pack;
import multipacks.platform.Platform;
import multipacks.vfs.Vfs;

/**
 * @author nahkd
 *
 */
public interface Modifier<T> {
	/**
	 * Apply this modifier to VFS.
	 * @param fromPack Pack that configured this modifier.
	 * @param contents Input and output contents.
	 * @param platform Platform (mainly for logging).
	 * @return Modifier output. Use type {@link Void} and return {@code null} if this modifier doesn't return
	 * anything for future.
	 */
	T applyModifier(Pack fromPack, Vfs contents, Platform platform);
}
