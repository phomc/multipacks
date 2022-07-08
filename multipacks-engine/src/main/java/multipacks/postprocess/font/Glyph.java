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
package multipacks.postprocess.font;

import multipacks.utils.ResourcePath;

public class Glyph {
	public final ResourcePath fontId, glyphId;
	public final char character;
	public final int ascent, height;

	public Glyph(ResourcePath fontId, ResourcePath glyphId, char character, int ascent, int height) {
		this.fontId = fontId;
		this.glyphId = glyphId;
		this.character = character;
		this.ascent = ascent;
		this.height = height;
	}

	@Override
	public String toString() {
		return "Glyph(" + fontId + "/" + glyphId + " => " + Integer.toUnsignedString(character, 16) + ")";
	}
}
