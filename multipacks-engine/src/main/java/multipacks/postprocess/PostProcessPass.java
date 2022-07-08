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
package multipacks.postprocess;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Function;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import multipacks.bundling.BundleResult;
import multipacks.utils.Selects;
import multipacks.utils.logging.AbstractMPLogger;
import multipacks.vfs.VirtualFs;

/**
 * Multipacks post processing. This is used to perform files manipulation on virtual file system. An example
 * would be splitting PNG atlas into multiple PNGs.
 * @author nahkd
 *
 */
public abstract class PostProcessPass {
	public abstract void apply(VirtualFs fs, BundleResult result, AbstractMPLogger logger) throws IOException;

	public static final HashMap<String, Function<JsonObject, PostProcessPass>> REGISTRY = new HashMap<>();

	public static PostProcessPass fromJson(JsonObject json) {
		String type = Selects.nonNull(json.get("type"), "'type' is empty").getAsString();
		Function<JsonObject, PostProcessPass> ctor = REGISTRY.get(type);
		if (ctor == null) return null;
		return ctor.apply(json);
	}

	public static void apply(JsonElement json, VirtualFs fs, BundleResult result, AbstractMPLogger logger) throws IOException {
		Iterable<JsonElement> iterable;
		if (json instanceof JsonArray arr) iterable = arr;
		else iterable = Arrays.asList(json);

		for (JsonElement e : iterable) {
			JsonObject j = e.getAsJsonObject();
			PostProcessPass pass = fromJson(j);
			if (pass == null) logger.warning("Post process pass " + j.get("type") + " is not registered! Please check your post process passes and installed plug-ins");
			else pass.apply(fs, result, logger);
		}
	}

	static {
		// Basic file operations
		REGISTRY.put("include", IncludePass::new);
		REGISTRY.put("move", MovePass::new);
		REGISTRY.put("delete", DeletePass::new);
		REGISTRY.put("copy", CopyPass::new);
	}
}
