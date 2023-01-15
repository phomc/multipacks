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

import java.io.IOException;

import multipacks.bundling.BundleResult;
import multipacks.vfs.legacy.VirtualFs;

/**
 * Dynamic packs allow you to add contents dynamically. An example would be composing .bbmodel (Blockbench
 * Model) into a resource pack with its rotations + position fixed for Minecraft (because of the dumb
 * restriction).
 * @author nahkd
 *
 */
public abstract class DynamicPack extends Pack {
	public DynamicPack(PackIndex index) {
		super(index);
	}

	/**
	 * Build the pack from configurations inside this dynamic pack.
	 */
	public abstract void build(VirtualFs outputFiles, BundleResult result) throws IOException;
}
