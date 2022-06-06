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
package multipacks.packs;

import java.util.Map.Entry;
import java.util.stream.Stream;

import com.google.gson.JsonArray;
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
	public PackType type = PackType.STANDARD;
	public Version packVersion;
	public Version gameVersion; // TODO: Use pack_format value
	public PackIdentifier[] include;
	public String[] ignore;

	public PackIdentifier getIdentifier() {
		return new PackIdentifier(id, packVersion);
	}

	public static PackIndex fromJson(JsonObject json) {
		PackIndex out = new PackIndex();
		out.id = Selects.nonNull(json.get("id"), "Missing 'id' in pack index file").getAsString();
		out.name = Selects.getChain(json.get("name"), j -> j.getAsString(), out.id);
		out.author = Selects.getChain(json.get("author"), j -> j.getAsString(), null);
		out.description = Selects.getChain(json.get("description"), j -> j.getAsString(), null);
		out.type = Selects.getChain(json.get("type"), j -> PackType.valueOf(j.getAsString().toUpperCase()), PackType.STANDARD);
		out.packVersion = Selects.getChain(json.get("version"), j -> new Version(j.getAsString()), new Version("1"));
		out.gameVersion = Selects.getChain(json.get("gameVersion"), j -> new Version(j.getAsString()), new Version(">=1.18.2"));
		out.include = Selects.getChain(json.get("include"), j -> {
			JsonObject includeMap = j.getAsJsonObject();
			PackIdentifier[] ids = new PackIdentifier[includeMap.size()];
			int i = 0;

			for (Entry<String, JsonElement> entry : includeMap.entrySet()) {
				ids[i++] = PackIdentifier.fromString(entry.getKey(), entry.getValue().getAsString());
			}

			return ids;
		}, new PackIdentifier[0]);
		out.ignore = Selects.getChain(json.get("ignore"), j -> {
			JsonArray arr = j.getAsJsonArray();
			String[] ignore = new String[arr.size()];

			for (int i = 0; i < arr.size(); i++) {
				String processed = arr.get(i).getAsString();
				while (processed.startsWith("/")) processed = processed.substring(1);
				while (processed.endsWith("/")) processed = processed.substring(0, processed.length() - 1);
				ignore[i] = processed;
			}

			return ignore;
		}, null);
		return out;
	}

	/**
	 * Check if the pack index have local references. Pack with local references are not allowed to upload
	 * to packs repository, simply because it's impossible (duh).
	 */
	public boolean hasLocalReference() {
		return Stream.of(include).anyMatch(v -> v.folder != null);
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

	public boolean isIgnored(String path) {
		if (ignore == null) return false;
		while (path.startsWith("/")) path = path.substring(1);
		while (path.endsWith("/")) path.substring(0, path.length() - 1);

		for (String pattern : ignore) {
			if (pattern.endsWith("*") && path.startsWith(pattern.substring(0, pattern.length() - 1))) return true;
			if (path.equals(pattern)) return true;
		}

		return false;
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
