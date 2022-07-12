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
package multipacks.postprocess.allocations.modelsdata;

import java.util.HashMap;

import multipacks.postprocess.allocations.Allocator;
import multipacks.utils.ResourcePath;
import multipacks.vfs.Path;

public class ModelDataAllocator extends Allocator<AllocatedModelData, Path> {
	/**
	 * Mapped models with its named id assigned to allocated model numerical id. An use case would be a custom
	 * Spigot plugin that reads all mapped models and convert them to numerical id, something like <code>"/give
	 * player ${model(namespace:id).gameId}{CustomModelData:${model(namespace:id).modelId}}"</code>. Currently
	 * this map is limited to calls on {@link CustomModelsPass}. 
	 */
	public final HashMap<ResourcePath, AllocatedModelData> mappedModels = new HashMap<>();
}
