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
package multipacks.transforms.defaults.overlay;

import java.awt.image.BufferedImage;

import com.google.gson.JsonObject;

import multipacks.utils.Selects;

public class OverlaySprite {
	public final String path;
	public final int x, y;
	protected BufferedImage overlayCache;

	public OverlaySprite(String path, int x, int y) {
		this.path = path;
		this.x = x;
		this.y = y;
	}

	public OverlaySprite(JsonObject json) {
		this.path = Selects.nonNull(json.get("path"), "'path' is empty").getAsString();
		this.x = Selects.nonNull(json.get("x"), "'x' is empty").getAsInt();
		this.y = Selects.nonNull(json.get("y"), "'y' is empty").getAsInt();
	}
}
