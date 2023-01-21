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
package multipacks.modifier.builtin.glyphs;

import multipacks.utils.ResourcePath;

/**
 * @author nahkd
 *
 */
public class Glyph {
	/**
	 * The id of this glyph.
	 */
	public final ResourcePath glyphId;

	/**
	 * The font that holds this glyph.
	 */
	public final FontInfo font;

	public final char assigned;

	public Glyph(ResourcePath glyphId, FontInfo font, char assigned) {
		this.glyphId = glyphId;
		this.font = font;
		this.assigned = assigned;
	}

	/**
	 * Get the string of length containing the character assigned to this glyph. This method is overridden
	 * so you can append it with strings using {@code +} operator.  
	 */
	@Override
	public String toString() {
		return String.valueOf(assigned);
	}
}
