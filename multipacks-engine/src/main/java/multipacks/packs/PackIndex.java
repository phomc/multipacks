package multipacks.packs;

import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import multipacks.utils.Selects;
import multipacks.versioning.GameVersions;
import multipacks.versioning.Version;

public class PackIndex {
	public String id;
	public String name;
	public String author;
	public String description;
	public Version packVersion;
	public Version gameVersion; // TODO: Use pack_format value
	public PackIdentifier[] include;

	public PackIdentifier getIdentifier() {
		return new PackIdentifier(id, packVersion);
	}

	public static PackIndex fromJson(JsonObject json) {
		PackIndex out = new PackIndex();
		out.id = Selects.nonNull(json.get("id"), "Missing 'id' in pack index file").getAsString();
		out.name = Selects.getChain(json.get("name"), j -> j.getAsString(), out.id);
		out.author = Selects.getChain(json.get("author"), j -> j.getAsString(), null);
		out.description = Selects.getChain(json.get("description"), j -> j.getAsString(), null);
		out.packVersion = Selects.getChain(json.get("version"), j -> new Version(j.getAsString()), new Version("1"));
		out.gameVersion = Selects.getChain(json.get("gameVersion"), j -> new Version(j.getAsString()), new Version(">=1.18.2"));
		out.include = Selects.getChain(json.get("include"), j -> {
			JsonObject includeMap = j.getAsJsonObject();
			PackIdentifier[] ids = new PackIdentifier[includeMap.size()];
			int i = 0;

			for (Entry<String, JsonElement> entry : includeMap.entrySet()) {
				ids[i++] = new PackIdentifier(entry.getKey(), new Version(entry.getValue().getAsString()));
			}

			return ids;
		}, new PackIdentifier[0]);
		return out;
	}

	public JsonObject toJson() {
		JsonObject obj = new JsonObject();
		obj.addProperty("id", id);
		if (name != null) obj.addProperty("name", name);
		if (author != null) obj.addProperty("author", author);
		if (description != null) obj.addProperty("description", description);
		obj.addProperty("version", packVersion.toStringNoPrefix());
		obj.addProperty("gameVersion", gameVersion.toString());

		if (include != null && include.length > 0) {
			JsonObject includeMap = new JsonObject();
			for (PackIdentifier i : include) includeMap.addProperty(i.id, i.version.toString());
		}

		return obj;
	}

	public JsonObject getMcMeta() {
		JsonObject obj = new JsonObject();
		JsonObject pack = new JsonObject();
		pack.addProperty("pack_format", GameVersions.getPackFormat(gameVersion));
		pack.addProperty("description", Selects.firstNonNull(description, "Generated from Multipacks"));
		obj.add("pack", pack);
		return obj;
	}
}
