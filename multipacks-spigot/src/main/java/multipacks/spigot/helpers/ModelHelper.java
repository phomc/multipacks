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
package multipacks.spigot.helpers;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import multipacks.modifier.builtin.models.overrides.CustomModelOverride;
import multipacks.modifier.builtin.models.overrides.ModelOverride;
import multipacks.utils.PlatformAPI;

/**
 * @author nahkd
 *
 */
@PlatformAPI
public class ModelHelper {
	public static ItemStack createItemStack(ModelOverride override) {
		// TODO: https://openjdk.org/jeps/406
		if (override instanceof CustomModelOverride cmo) return Bukkit.getItemFactory().createItemStack(override.base.itemId + "{CustomModelData:" + cmo.modelId + "}");
		return null;
	}
}
