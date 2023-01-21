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
package multipacks.tests.packs;

import static org.junit.jupiter.api.Assertions.*;

import java.io.DataInput;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import multipacks.bundling.Bundler;
import multipacks.modifier.Modifier;
import multipacks.modifier.ModifiersAccess;
import multipacks.modifier.builtin.glyphs.GlyphsModifier;
import multipacks.packs.Pack;
import multipacks.packs.meta.PackIndex;
import multipacks.tests.TestUtils;
import multipacks.utils.ResourcePath;
import multipacks.utils.io.Deserializer;

/**
 * @author nahkd
 *
 */
class PacksTest {
	@Test
	void testPacksFromJar() throws Exception {
		Pack pack = TestUtils.getSamplePack();

		PackIndex index = pack.getIndex();
		assertEquals("sample-pack", index.name);
		assertEquals("PhoMC", index.author);

		assertNotNull(pack.createVfs().get(new multipacks.vfs.Path("assets/multipacks/models/sample_model.json")));
	}

	@Test
	void testPackBundling() throws Exception {
		Pack pack = TestUtils.getSamplePack();

		Bundler bundler = new Bundler()
				.setModifiersAccess(new ModifiersAccess() {
					@Override
					public <T extends Modifier> void registerModifier(ResourcePath id, Supplier<T> supplier, Deserializer<T> deserializer) {
					}

					@Override
					public List<ResourcePath> getRegisteredModifiers() {
						return Arrays.asList(new ResourcePath("multipacks:builtin/glyphs"));
					}

					@Override
					public Modifier createModifier(ResourcePath id) {
						if (id.equals(new ResourcePath("multipacks:builtin/glyphs"))) return new GlyphsModifier();
						return null;
					}

					@Override
					public Modifier deserializeModifier(ResourcePath id, DataInput input) throws IOException {
						return createModifier(id);
					}
				});
		OutputStream stream = new FileOutputStream(new File("testartifact_PacksTest_001.zip"));
		bundler.bundle(pack, pack.getIndex().sourceGameVersion).writeZipData(stream);

		FileSystem fs = FileSystems.newFileSystem(Paths.get("testartifact_PacksTest_001.zip"));
		assertTrue(Files.exists(fs.getPath("assets/multipacks/models/sample_model.json")));
	}
}
