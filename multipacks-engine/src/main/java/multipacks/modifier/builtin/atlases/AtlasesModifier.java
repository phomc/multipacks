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
package multipacks.modifier.builtin.atlases;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import multipacks.modifier.ModifiersAccess;
import multipacks.modifier.builtin.BuiltinModifierBase;
import multipacks.packs.Pack;
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
public class AtlasesModifier extends BuiltinModifierBase<Void> {
	public static final ResourcePath ID = new ResourcePath(Constants.SYSTEM_NAMESPACE, "builtin/atlases");

	public static final String FIELD_FILE = "file";
	public static final String FIELD_TEMPLATE = "template";

	@Override
	protected void applyWithScopedConfig(Pack fromPack, Vfs root, Vfs scoped, JsonElement config, Void data, ModifiersAccess access) {
		if (config.isJsonObject()) {
			JsonObject obj = config.getAsJsonObject();

			if (obj.has(FIELD_FILE)) {
				Path filePath = new Path(obj.get(FIELD_FILE).getAsString());
				Vfs file = scoped.get(filePath);
				Template template = Template.resolveTemplate(scoped, null, Selects.nonNull(obj.get(FIELD_TEMPLATE), Messages.missingFieldAny(FIELD_TEMPLATE)));

				try (InputStream srcStream = file.getInputStream()) {
					BufferedImage src = ImageIO.read(srcStream);
					int scale = template.scale;

					for (Part part : template.parts) {
						BufferedImage out = part.region.slice(src, scale);
						Path destPath = new Path(part.applyNameTemplate(filePath));
						Vfs destFile = file.getParent().touch(destPath);

						try (OutputStream destStream = destFile.getOutputStream()) {
							ImageIO.write(out, "PNG", destStream);
						}
					}
				} catch (IOException e) {
					throw new RuntimeException("Failed to apply atlases modifier", e);
				}

				file.getParent().delete(file.getName());
			} else throw new JsonSyntaxException(Messages.missingFieldAny(FIELD_INCLUDE, FIELD_FILE));
		}
	}

	@Override
	protected Void createLocalData() {
		return null;
	}

	@Override
	public void finalizeModifier(Vfs contents, ModifiersAccess access) {
	}

	public static void registerTo(ModifiersAccess access) {
		access.registerModifier(ID, AtlasesModifier::new);
	}
}
