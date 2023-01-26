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
package multipacks.modifier.builtin.models;

/**
 * Represent a model that's attached to an item. You can obtain the item with custom model by giving yourself
 * an item with id = {@link ItemModels#itemId} and {@code CustomModelData} = {@link #modelId}.
 * @author nahkd
 *
 */
public class Model {
	public final ItemModels item;
	public final int modelId;

	public Model(ItemModels item, int modelId) {
		this.item = item;
		this.modelId = modelId;
	}

	@Override
	public String toString() {
		return item.itemId + " -> #" + modelId;
	}
}
