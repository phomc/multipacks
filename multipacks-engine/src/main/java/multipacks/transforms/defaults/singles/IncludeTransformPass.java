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
package multipacks.transforms.defaults.singles;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import multipacks.bundling.BundleResult;
import multipacks.transforms.TransformFailException;
import multipacks.transforms.TransformPass;
import multipacks.transforms.TransformativeFileSystem;
import multipacks.utils.Selects;
import multipacks.utils.logging.AbstractMPLogger;

public class IncludeTransformPass extends TransformPass {
	private String include;

	public IncludeTransformPass(JsonObject json) {
		include = Selects.nonNull(json.get("include"), "'include' is empty").getAsString();
	}

	@Override
	public void transform(TransformativeFileSystem fs, BundleResult result, AbstractMPLogger logger) throws IOException {
		InputStream stream = Selects.nonNull(fs.openRead(include), "File doesn't exists: " + include);
		JsonElement json = new JsonParser().parse(new InputStreamReader(stream));
		stream.close();

		if (!json.isJsonArray()) throw new TransformFailException("Invalid JSON type (Must be an array)");

		JsonArray arr = json.getAsJsonArray();
		TransformPass[] passes = new TransformPass[arr.size()];

		for (int i = 0; i < passes.length; i++) passes[i] = TransformPass.fromJson(arr.get(i).getAsJsonObject());
		for (TransformPass pass : passes) pass.transform(fs, result, logger);
	}
}
