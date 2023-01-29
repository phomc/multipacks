/*
 * Copyright (c) 2022-2023 PhoMC
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
package multipacks.modifier.builtin.slices;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import multipacks.bundling.BundleContext;
import multipacks.modifier.Modifier;
import multipacks.modifier.ModifiersAccess;
import multipacks.utils.Constants;
import multipacks.utils.Messages;
import multipacks.utils.ResourcePath;
import multipacks.utils.Selects;
import multipacks.vfs.Path;
import multipacks.vfs.Vfs;

/**
 * @author nahkd
 *
 */
public class SlicesModifier extends Modifier<multipacks.modifier.builtin.slices.SlicesModifier.Config, Void> {
	public static class Config {
		public Path file;
		public JsonElement templateJson;
		public Template template;

		public void resolveTemplate(Vfs scoped) {
			if (template != null) return;
			template = Template.resolveTemplate(scoped, null, templateJson);
		}
	}

	public static final ResourcePath ID = new ResourcePath(Constants.SYSTEM_NAMESPACE, "builtin/slices");

	public static final String FIELD_FILE = "file";
	public static final String FIELD_TEMPLATE = "template";

	@Override
	public Config configure(JsonObject json) {
		Config config = new Config();

		if (json.has(FIELD_FILE)) {
			config.file = new Path(json.get(FIELD_FILE).getAsString());
			config.templateJson = Selects.nonNull(json.get(FIELD_TEMPLATE), Messages.missingFieldAny(FIELD_TEMPLATE));
		} else throw new JsonSyntaxException(Messages.missingFieldAny(FIELD_INCLUDE, FIELD_FILE));

		return config;
	}

	@Override
	public Void createContext() {
		return null;
	}

	@Override
	public void applyModifier(BundleContext context, Path cwd, Config config, Void modContext) {
		Vfs scoped = context.content.get(cwd);
		config.resolveTemplate(scoped);
		Vfs file = scoped.get(config.file);

		try (InputStream srcStream = file.getInputStream()) {
			BufferedImage src = ImageIO.read(srcStream);
			int scale = config.template.scale;

			for (Part part : config.template.parts) {
				BufferedImage out = part.region.slice(src, scale);
				Path destPath = new Path(part.applyNameTemplate(config.file));
				Vfs destFile = file.getParent().touch(destPath);

				try (OutputStream destStream = destFile.getOutputStream()) {
					ImageIO.write(out, "PNG", destStream);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to apply atlases modifier", e);
		}

		file.getParent().delete(file.getName());
	}

	@Override
	public void finalizeModifier(Vfs contents, ModifiersAccess access) {
	}

	public static void registerTo(ModifiersAccess access) {
		access.registerModifier(ID, SlicesModifier::new);
	}
}
