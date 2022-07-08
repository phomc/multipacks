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
package multipacks.spigot;

import multipacks.postprocess.font.Glyph;
import net.md_5.bungee.api.chat.ComponentBuilder;

/**
 * Utility class for messing with Bungeecord Chat API (shipped with Spigot).
 * @author nahkd
 *
 */
public class BungeeGlyphs {
	/**
	 * Convert custom font texture glyph to chat component builder. This method respect the glyph font id,
	 * so you can have a dedicated font will all of your icons without interfering "minecraft:default" font.
	 */
	public static ComponentBuilder from(Glyph glyph) {
		return new ComponentBuilder(String.valueOf(glyph.character)).font(glyph.fontId.toString());
	}
}
