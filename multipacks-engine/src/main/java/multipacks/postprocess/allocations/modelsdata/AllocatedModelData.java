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

import multipacks.postprocess.allocations.Allocated;
import multipacks.utils.ResourcePath;

/**
 * Allocated custom model data.
 * @author nahkd
 *
 */
public class AllocatedModelData implements Allocated {
	public final ResourcePath itemId;
	public final int modelId;

	public AllocatedModelData(ResourcePath itemId, int modelId) {
		this.itemId = itemId;
		this.modelId = modelId;
	}
}
