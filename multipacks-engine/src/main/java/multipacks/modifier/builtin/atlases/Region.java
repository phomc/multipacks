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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import multipacks.utils.Messages;
import multipacks.utils.Selects;

/**
 * @author nahkd
 *
 */
public class Region {
	public static final String FIELD_FROM = "from";
	public static final String FIELD_TO = "to";

	public final int[] from;
	public final int[] to;
	public final int[] area;

	public Region(int x1, int y1, int x2, int y2) {
		this.from = new int[] { Math.min(x1, x2), Math.min(y1, y2) };
		this.to = new int[] { Math.max(x1, x2), Math.max(y1, y2) };
		this.area = new int[] { this.to[0] - this.from[0], this.to[1] - this.from[1] };
	}

	public Region(JsonObject json) {
		JsonArray from = Selects.nonNull(json.get(FIELD_FROM), Messages.missingFieldAny(FIELD_FROM)).getAsJsonArray();
		JsonArray to = Selects.nonNull(json.get(FIELD_TO), Messages.missingFieldAny(FIELD_TO)).getAsJsonArray();
		int x1 = from.get(0).getAsInt(), y1 = from.get(1).getAsInt();
		int x2 = to.get(0).getAsInt(), y2 = to.get(1).getAsInt();
		this.from = new int[] { Math.min(x1, x2), Math.min(y1, y2) };
		this.to = new int[] { Math.max(x1, x2), Math.max(y1, y2) };
		this.area = new int[] { this.to[0] - this.from[0], this.to[1] - this.from[1] };
	}

	public BufferedImage slice(BufferedImage src, int scale) {
		BufferedImage out = new BufferedImage(area[0] * scale, area[1] * scale, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = out.createGraphics();
		g.drawImage(src, 0, 0, area[0] * scale, area[1] * scale, from[0] * scale, from[1] * scale, to[0] * scale, to[1] * scale, null);
		g.dispose();
		return out;
	}
}
