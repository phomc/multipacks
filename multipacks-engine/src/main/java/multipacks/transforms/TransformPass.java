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
package multipacks.transforms;

import java.io.IOException;
import java.util.HashMap;
import java.util.function.Function;

import com.google.gson.JsonObject;

import multipacks.bundling.BundleResult;
import multipacks.transforms.defaults.fonticons.FontIconsTransformPass;
import multipacks.transforms.defaults.multisprites.MultiSpritesTransformPass;
import multipacks.transforms.defaults.overlay.OverlayTransformPass;
import multipacks.transforms.defaults.singles.IncludeTransformPass;
import multipacks.transforms.defaults.singles.RemapTransformPass;
import multipacks.utils.Selects;
import multipacks.utils.logging.AbstractMPLogger;

public abstract class TransformPass {
	/**
	 * Transform files inside virtual file system into another set of files. An example would
	 * be composing multiple font icons into a single font.
	 */
	public abstract void transform(TransformativeFileSystem fs, BundleResult result, AbstractMPLogger logger) throws IOException;

	public static final HashMap<String, Function<JsonObject, TransformPass>> REGISTRY = new HashMap<>();

	public static TransformPass fromJson(JsonObject json) {
		String type = Selects.nonNull(json.get("type"), "'type' is empty").getAsString();
		Function<JsonObject, TransformPass> ctor = REGISTRY.get(type);
		if (ctor == null) return null;
		return ctor.apply(json);
	}

	static {
		REGISTRY.put("font-icons", FontIconsTransformPass::new);
		REGISTRY.put("multi-sprites", MultiSpritesTransformPass::new);
		REGISTRY.put("remap", RemapTransformPass::new);
		REGISTRY.put("include", IncludeTransformPass::new);
		REGISTRY.put("overlay", OverlayTransformPass::new);
	}
}
