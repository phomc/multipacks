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
package multipacks.modifier.builtin.atlases;

import com.google.gson.JsonObject;

import multipacks.utils.Messages;
import multipacks.utils.Selects;
import multipacks.utils.TemplatedString;
import multipacks.vfs.Path;

/**
 * @author nahkd
 *
 */
public class Part {
	public static final String FIELD_STOREAT = "storeAt";
	public static final String FIELD_NAME = "name";
	public static final String FIELD_REGION = "region";

	public static final String TEMPLATE_BASENAME = "basename";
	public static final String TEMPLATE_EXTNAME = "extname";
	public static final String TEMPLATE_DIRNAME = "dirname";
	public static final String[] TEMPLATES = new String[] { TEMPLATE_BASENAME, TEMPLATE_EXTNAME, TEMPLATE_DIRNAME };

	public final Path storeAt;
	public final TemplatedString name;
	public final Region region;

	public Part(Path storeAt, String name, Region region) {
		this.storeAt = storeAt;
		this.name = new TemplatedString(name, TEMPLATES);
		this.region = region;
	}

	public Part(JsonObject json) {
		this.storeAt = Selects.getChain(json.get(FIELD_STOREAT), j -> new Path(j.getAsString()), new Path("."));
		this.name = new TemplatedString(Selects.nonNull(json.get(FIELD_NAME), Messages.missingFieldAny(FIELD_NAME)).getAsString(), TEMPLATES);
		this.region = new Region(Selects.nonNull(json.get(FIELD_REGION), Messages.missingFieldAny(FIELD_REGION)).getAsJsonObject());
	}

	public String applyNameTemplate(Path targetPath) {
		String fileName = targetPath.fileName();
		String[] split = fileName.split("\\.");
		String extname = split[split.length - 1];
		String basename = fileName.substring(0, fileName.length() - extname.length() - 1);
		String dirname = targetPath.join("..").toString();
		return name.get(basename, extname, dirname);
	}
}
