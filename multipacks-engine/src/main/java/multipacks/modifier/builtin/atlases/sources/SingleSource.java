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
package multipacks.modifier.builtin.atlases.sources;

import com.google.gson.JsonObject;

import multipacks.utils.Messages;
import multipacks.utils.ResourcePath;
import multipacks.utils.Selects;
import multipacks.vfs.Path;

/**
 * @author nahkd
 *
 */
public class SingleSource extends AtlasSource {
	public static final String SOURCE_NAME = "single";

	public static final String FIELD_RESOURCE = "resource";
	public static final String FIELD_SPRITE = "sprite";

	public final Path resource;
	public final ResourcePath sprite;

	public SingleSource(Path resource, ResourcePath sprite) {
		super(SOURCE_NAME);
		this.resource = resource;
		this.sprite = sprite;
	}

	@Override
	public JsonObject toOutputSource() {
		JsonObject json = super.toOutputSource();
		String[] resSegments = resource.getSegments();
		String namespace = resSegments[1]; // assets/namespace/textures/...
		String[] pathSegments = new String[resSegments.length - 3];
		System.arraycopy(resSegments, 3, pathSegments, 0, pathSegments.length);

		json.addProperty("resource", new ResourcePath(namespace, String.join("/", pathSegments)).toString());
		if (sprite != null) json.addProperty("sprite", sprite.toString());
		return json;
	}

	public static SingleSource sourceFromConfig(JsonObject json) {
		Path resource = new Path(Selects.nonNull(json.get(FIELD_RESOURCE), Messages.missingFieldAny(FIELD_RESOURCE)).getAsString());
		ResourcePath sprite = Selects.getChain(json.get(FIELD_SPRITE), j -> new ResourcePath(j.getAsString()), null);
		return new SingleSource(resource, sprite);
	}
}
