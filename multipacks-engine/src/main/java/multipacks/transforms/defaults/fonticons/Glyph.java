package multipacks.transforms.defaults.fonticons;

import com.google.gson.JsonObject;

import multipacks.utils.ResourcePath;

public class Glyph {
	public final ResourcePath id;
	public final char character;
	public final ResourcePath font;
	public final ResourcePath texture;

	public Glyph(ResourcePath id, char character, ResourcePath font, ResourcePath texture) {
		this.id = id;
		this.character = character;
		this.font = font;
		this.texture = texture;
	}

	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		if (id != null) json.addProperty("id", id.toString());
		json.addProperty("charCode", (int) character);
		json.addProperty("font", font.toString());
		json.addProperty("texture", texture.toString());
		return json;
	}
}
