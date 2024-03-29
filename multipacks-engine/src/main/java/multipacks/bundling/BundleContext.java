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

import java.util.HashMap;
import java.util.Map;

import multipacks.modifier.Modifier;
import multipacks.packs.Pack;
import multipacks.utils.ResourcePath;
import multipacks.vfs.Vfs;

/**
 * @author nahkd
 *
 */
public class BundleContext {
	public final Bundler bundler;
	public final Pack pack;
	public final Vfs content;
	public final Map<ResourcePath, Modifier<?, ?>> modifiers = new HashMap<>();

	public BundleContext(Bundler bundler, Pack pack, Vfs content) {
		this.bundler = bundler;
		this.pack = pack;
		this.content = content;
	}

	public Modifier<?, ?> getOrCreateModifier(ResourcePath id) {
		Modifier<?, ?> mod = modifiers.get(id);
		if (mod == null) modifiers.put(id, mod = bundler.modifiers.getModifierInfo(id).supplier.get());
		return mod;
	}
}
