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
package multipacks.plugins;

import java.io.File;
import java.util.HashMap;
import java.util.function.Function;

import com.google.gson.JsonObject;

import multipacks.management.legacy.LocalRepository;
import multipacks.management.legacy.PacksRepository;
import multipacks.postprocess.PostProcessPass;
import multipacks.postprocess.allocations.modelsdata.CustomModelsPass;
import multipacks.postprocess.atlas.AtlasPass;
import multipacks.postprocess.basic.CopyPass;
import multipacks.postprocess.basic.DeletePass;
import multipacks.postprocess.basic.IncludePass;
import multipacks.postprocess.basic.MovePass;
import multipacks.postprocess.font.FontIconsPass;
import multipacks.postprocess.overlays.OverlayPass;

public class MultipacksDefaultPlugin implements MultipacksPlugin {
	@Override
	public PacksRepository parseRepository(File root, String uri) {
		if (uri.startsWith("file:")) return new LocalRepository(new File(root, uri.substring(5)));
		return null;
	}

	@Override
	public void registerPostProcessPasses(HashMap<String, Function<JsonObject, PostProcessPass>> reg) {
		reg.put("include", IncludePass::new);
		reg.put("move", MovePass::new);
		reg.put("delete", DeletePass::new);
		reg.put("copy", CopyPass::new);

		reg.put("font-icons", FontIconsPass::new);
		reg.put("atlas", AtlasPass::new);
		reg.put("overlay", OverlayPass::new);
		reg.put("custom-models", CustomModelsPass::new);
	}
}
