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
package multipacks.tests.modifiers;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import multipacks.bundling.BundleResult;
import multipacks.bundling.Bundler;
import multipacks.modifier.Modifier;
import multipacks.modifier.builtin.atlases.AtlasesModifier;
import multipacks.modifier.builtin.glyphs.GlyphsModifier;
import multipacks.modifier.builtin.models.ModelsModifier;
import multipacks.modifier.builtin.slices.SlicesModifier;
import multipacks.packs.Pack;
import multipacks.tests.TestPlatform;
import multipacks.tests.TestUtils;
import multipacks.utils.ResourcePath;
import multipacks.versioning.Version;
import multipacks.vfs.Path;
import multipacks.vfs.Vfs;

/**
 * @author nahkd
 *
 */
class ModifiersTest {
	BundleResult obtainBundle() {
		Pack pack = TestUtils.getSamplePack();
		Bundler bundler = new Bundler().fromPlatform(new TestPlatform());
		return bundler.bundle(pack, new Version("1.19.3"));
	}

	@Test
	void testGlyphsModifier() {
		BundleResult result = obtainBundle();
		Modifier mod = result.getModifiers().get(GlyphsModifier.ID);

		if (mod instanceof GlyphsModifier gmod) {
			assertNotNull(gmod.glyphs.get(new ResourcePath("sample", "my_glyph")));
			assertNotNull(gmod.glyphs.get(new ResourcePath("sample", "sample_glyph")));
		} else fail("Not an instance of GlyphsModifier");
	}

	@Test
	void testModelsModifier() {
		BundleResult result = obtainBundle();
		Modifier mod = result.getModifiers().get(ModelsModifier.ID);

		if (mod instanceof ModelsModifier mmod) {
			assertNotNull(mmod.models.get(new ResourcePath("sample", "my_cool_item")));
		} else fail("Not an instance of ModelsModifier");
	}

	@Test
	void testSlicesModifier() {
		BundleResult result = obtainBundle();
		Modifier mod = result.getModifiers().get(SlicesModifier.ID);

		if (mod instanceof SlicesModifier) {
			Vfs content = result.contents;
			assertNotNull(content.get(new Path("assets/multipacks/textures/sample_atlas_up.png")));
			assertNotNull(content.get(new Path("assets/multipacks/textures/sample_atlas_down.png")));
			assertNotNull(content.get(new Path("assets/multipacks/textures/sample_atlas_north.png")));
			assertNotNull(content.get(new Path("assets/multipacks/textures/sample_atlas_east.png")));
			assertNotNull(content.get(new Path("assets/multipacks/textures/sample_atlas_south.png")));
			assertNotNull(content.get(new Path("assets/multipacks/textures/sample_atlas_west.png")));
		} else fail("Not an instance of SlicesModifier");
	}

	@Test
	void testAtlasesModifier() {
		BundleResult result = obtainBundle();
		Modifier mod = result.getModifiers().get(AtlasesModifier.ID);

		if (mod instanceof AtlasesModifier amod) {
			assertNotNull(amod.atlases.get(new ResourcePath("minecraft:armor_trims")));
			assertNotNull(amod.atlases.get(new ResourcePath("sample:sample_atlas")));
		} else fail("Not an instance of AtlasesModifier");
	}
}
