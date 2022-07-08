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

import com.google.gson.JsonObject;

import multipacks.bundling.BundleResult;
import multipacks.postprocess.PostProcessPass;
import multipacks.utils.Selects;
import multipacks.utils.logging.AbstractMPLogger;
import multipacks.vfs.Path;
import multipacks.vfs.VirtualFs;

public class CopyPass extends PostProcessPass {
	private Path from;
	private Path to;

	public CopyPass(JsonObject config) {
		from = new Path(Selects.nonNull(config.get("from"), "'from' is empty").getAsString());
		to = new Path(Selects.nonNull(config.get("to"), "'to' is empty").getAsString());
	}

	@Override
	public void apply(VirtualFs fs, BundleResult result, AbstractMPLogger logger) throws IOException {
		for (Path pFrom : fs.ls(from)) {
			String tail = pFrom.toString().substring(from.toString().length() + 1);
			Path pTo = Path.join(to, new Path(tail));
			byte[] bs = fs.read(pFrom);
			fs.write(pTo, bs);
		}
	}
}
