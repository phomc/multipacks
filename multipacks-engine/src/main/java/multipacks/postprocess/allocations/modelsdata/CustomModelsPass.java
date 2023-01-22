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

import java.io.IOException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import multipacks.bundling.legacy.BundleResult;
import multipacks.bundling.legacy.PackagingFailException;
import multipacks.logging.AbstractLogger;
import multipacks.postprocess.PostProcessPass;
import multipacks.utils.ResourcePath;
import multipacks.utils.Selects;
import multipacks.vfs.Path;
import multipacks.vfs.legacy.VirtualFs;

public class CustomModelsPass extends PostProcessPass {
	private Path from;
	private ModelDataAllocationSpace[] allocateOutOfSpace;

	public CustomModelsPass(JsonObject config) {
		from = new Path(Selects.nonNull(config.get("from"), "'from' is empty").getAsString());
		if (config.has("outOfSpace")) {
			JsonArray outOfSpace = config.get("outOfSpace").getAsJsonArray();
			allocateOutOfSpace = new ModelDataAllocationSpace[outOfSpace.size()];
			for (int i = 0; i < allocateOutOfSpace.length; i++) allocateOutOfSpace[i] = new ModelDataAllocationSpace(outOfSpace.get(i).getAsJsonObject());
		}
	}

	@Override
	public void apply(VirtualFs fs, BundleResult result, AbstractLogger logger) throws IOException {
		ModelDataAllocator allocator = result.getOrCreate(ModelDataAllocator.class, ModelDataAllocator::new);
		boolean alreadyAdded = false;

		for (Path p : fs.ls(from)) {
			if (allocator.isAlreadyAllocated(p)) continue;

			AllocatedModelData data = allocator.allocateNew(fs, p);
			if (data == null) {
				if (alreadyAdded || allocateOutOfSpace.length == 0) throw new PackagingFailException("Custom Model Data: Out of space to allocate new model id");
				logger.info("Custom Model Data: Adding " + allocateOutOfSpace.length + " space" + (allocateOutOfSpace.length == 1? "" : "s"));
				for (ModelDataAllocationSpace s : allocateOutOfSpace) allocator.add(s);
				data = allocator.allocateNew(fs, p);
				alreadyAdded = true;
			}
			if (data == null) {
				// 2nd try: We ran out of space again :(
				throw new PackagingFailException("Custom Model Data: Out of space to allocate new model id. Maybe you need to allocate more...");
			}

			try {
				JsonObject json = fs.readJson(p).getAsJsonObject();
				ResourcePath id = Selects.getChain(json.get("multipacks:id"), j -> new ResourcePath(j.getAsString()), null);
				if (id == null) logger.warning("Custom Model Data: " + p + ": Missing 'multipacks:id' field");
				else allocator.mappedModels.put(id, data);
			} catch (Exception e) {
				e.printStackTrace();
				logger.warning("Custom Model Data: " + p + ": Not a valid JSON file");
			}
		}
	}
}
