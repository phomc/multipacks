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
package multipacks.postprocess.basic;

import java.io.IOException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import multipacks.bundling.legacy.BundleResult;
import multipacks.postprocess.PostProcessPass;
import multipacks.utils.Selects;
import multipacks.utils.logging.AbstractLogger;
import multipacks.vfs.Path;
import multipacks.vfs.legacy.VirtualFs;

public class IncludePass extends PostProcessPass {
	private Path file;

	public IncludePass(JsonObject config) {
		file = new Path(Selects.nonNull(config.get("file"), "'file' is empty").getAsString());
	}

	public IncludePass(Path file) {
		this.file = file;
	}

	@Override
	public void apply(VirtualFs fs, BundleResult result, AbstractLogger logger) throws IOException {
		JsonElement json = fs.readJson(file);
		PostProcessPass.apply(json, fs, result, logger);
	}
}
