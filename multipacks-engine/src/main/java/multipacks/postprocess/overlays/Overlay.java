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
package multipacks.postprocess.overlays;

import java.awt.image.BufferedImage;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import multipacks.utils.Selects;
import multipacks.vfs.Path;

public class Overlay {
	public final Path overlayFile;
	public final int x, y;
	public BufferedImage cachedImage;

	public Overlay(Path overlayFile, int x, int y) {
		this.overlayFile = overlayFile;
		this.x = x;
		this.y = y;
	}

	public Overlay(JsonElement json) {
		if (json.isJsonPrimitive()) {
			overlayFile = new Path(json.getAsString());
			x = 0; y = 0;
		} else if (json.isJsonObject()) {
			JsonObject obj = json.getAsJsonObject();
			overlayFile = new Path(Selects.nonNull(obj.get("path"), "'path' is empty").getAsString());
			x = Selects.nonNull(obj.get("x"), "'x' is empty").getAsInt();
			y = Selects.nonNull(obj.get("y"), "'y' is empty").getAsInt();
		} else throw new JsonSyntaxException("Invalid JSON object type: " + json.getClass().getName());
	}
}
