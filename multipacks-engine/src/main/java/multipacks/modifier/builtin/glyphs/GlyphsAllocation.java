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

import java.util.Map;

import com.google.gson.JsonObject;

import multipacks.utils.Messages;
import multipacks.utils.ResourcePath;
import multipacks.utils.Selects;

/**
 * @author nahkd
 *
 */
public class GlyphsAllocation {
	public static final String FIELD_FONT = "font";
	public static final String FIELD_START = "start";
	public static final String FIELD_END = "end";

	public final FontInfo font;
	public final char startChar, endChar;
	public final int maxChars;
	public int currentIdx = 0;

	public GlyphsAllocation(FontInfo font, char startChar, char endChar) {
		if (startChar > endChar) throw new IllegalArgumentException("endChar is larger than startChar");
		this.font = font;
		this.startChar = startChar;
		this.endChar = endChar;
		this.maxChars = endChar - startChar + 1;
	}

	public GlyphsAllocation(Map<ResourcePath, FontInfo> fonts, JsonObject json) {
		ResourcePath fontId = new ResourcePath(Selects.nonNull(json.get(FIELD_FONT), Messages.missingFieldAny(FIELD_FONT)).getAsString());
		fonts.putIfAbsent(fontId, new FontInfo(fontId));
		this.font = fonts.get(fontId);
		this.startChar = Selects.nonNull(json.get(FIELD_START), Messages.missingFieldAny(FIELD_START)).getAsCharacter();
		this.endChar = Selects.nonNull(json.get(FIELD_END), Messages.missingFieldAny(FIELD_END)).getAsCharacter();
		this.maxChars = endChar - startChar + 1;
	}

	public boolean isFilled() {
		return currentIdx >= maxChars;
	}

	public Character getNext() {
		if (isFilled()) return null;
		char ch = (char) (startChar + currentIdx);
		currentIdx++;
		return ch;
	}

	public Character findSuitableNextChar() {
		Character ch;
		do {
			ch = getNext();
		} while (ch != null && font.assignedGlyphs.containsKey(ch));
		return ch;
	}
}
