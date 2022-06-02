package multipacks.transforms.defaults.fonticons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonArray;

import multipacks.utils.ResourcePath;

public class GlyphsResult {
	public final List<Glyph> glyphs = new ArrayList<>();

	public HashMap<ResourcePath, Glyph> toIdKeyMap() {
		HashMap<ResourcePath, Glyph> map = new HashMap<>();
		for (Glyph g : glyphs) if (g.id != null) map.put(g.id, g);
		return map;
	}

	public JsonArray toJson() {
		JsonArray arr = new JsonArray();
		for (Glyph g : glyphs) arr.add(g.toJson());
		return arr;
	}
}
