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
package multipacks.transforms.defaults.debug;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.google.gson.JsonObject;

import multipacks.bundling.BundleResult;
import multipacks.transforms.TransformPass;
import multipacks.transforms.TransformativeFileSystem;
import multipacks.utils.Selects;
import multipacks.utils.logging.AbstractMPLogger;

public class PathTestTransformPass extends TransformPass {
	private String test;

	public PathTestTransformPass(JsonObject json) {
		test = Selects.nonNull(json.get("test"), "'test' is empty").getAsString();
	}

	@Override
	public void transform(TransformativeFileSystem fs, BundleResult result, AbstractMPLogger logger) throws IOException {
		try {
			byte[] data = fs.get(test);
			logger.info("File test: '" + test + "' " + (data != null? "FOUND" : "FAILED"));
		} catch (FileNotFoundException e) {
			logger.info("FileNotFoundException thrown...");
		}
	}
}
