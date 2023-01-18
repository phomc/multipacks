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

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import multipacks.bundling.Bundler;
import multipacks.packs.LocalPack;
import multipacks.packs.meta.PackIndex;

/**
 * @author nahkd
 *
 */
class PacksTest {
	@Test
	void testPacksFromJar() throws Exception {
		Path rootPath = Path.of(this.getClass().getClassLoader().getResource("multipacksAssets").toURI());

		LocalPack pack = new LocalPack(rootPath);
		pack.loadFromStorage();

		PackIndex index = pack.getIndex();
		assertEquals("sample-pack", index.name);
		assertEquals("PhoMC", index.author);

		assertNotNull(pack.createVfsWithoutModifiers().get(new multipacks.vfs.Path("assets/multipacks/models/sample_model.json")));
	}

	@Test
	void testPackBundling() throws Exception {
		Path rootPath = Path.of(this.getClass().getClassLoader().getResource("multipacksAssets").toURI());
		LocalPack pack = new LocalPack(rootPath);
		pack.loadFromStorage();

		Bundler bundler = new Bundler();
		OutputStream stream = new FileOutputStream(new File("testartifact_PacksTest_001.zip"));
		bundler.bundleToStream(pack, pack.getIndex().sourceGameVersion, stream);

		FileSystem fs = FileSystems.newFileSystem(Paths.get("testartifact_PacksTest_001.zip"));
		assertTrue(Files.exists(fs.getPath("assets/multipacks/models/sample_model.json")));
	}
}