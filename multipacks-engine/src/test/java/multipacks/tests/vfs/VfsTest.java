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
package multipacks.tests.vfs;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import multipacks.vfs.Path;
import multipacks.vfs.Vfs;

/**
 * @author nahkd
 *
 */
class VfsTest {
	@Test
	void testFullVirtual() throws Exception {
		Vfs root = Vfs.createVirtualRoot();
		Vfs dirA = root.mkdir("dirA");
		Vfs dirB = root.mkdir("dirB");
		Vfs dirC = root.mkdir("dirC");

		assertEquals(3, root.listFiles().length);
		assertEquals(dirA, root.get("dirA"));
		assertEquals(dirB, root.get("dirB"));
		assertEquals(dirC, root.get("dirC"));

		Vfs helloTxt = dirA.touch("hello.txt");
		String testString = "Hello world!";

		try (OutputStream s = helloTxt.getOutputStream()) { s.write(testString.getBytes(StandardCharsets.UTF_8)); }
		try (InputStream s = helloTxt.getInputStream()) { assertEquals(testString, new String(s.readAllBytes(), StandardCharsets.UTF_8)); }

		assertEquals(helloTxt, dirA.get("hello.txt"));
		assertEquals(helloTxt, root.get(new Path("dirA/hello.txt")));

		root.delete("dirA");
		assertNull(root.get("dirA"));
	}

	@Test
	void testSimplePathTraversal() throws Exception {
		Vfs root = Vfs.createRoot(new File("."));
		Vfs parent = root.get(new Path(".."));
		assertEquals(root, parent);
	}

	@Test
	void testResourcesInJar() throws Exception {
		URI jar = this.getClass().getClassLoader().getResource("multipacksAssets").toURI();
		Vfs vfs = Vfs.createRoot(java.nio.file.Path.of(jar));
		assertNotNull(vfs.get("multipacks.index.json"));
	}

	@Test
	void testRecursiveCopy() throws Exception {
		Vfs vfsA = Vfs.createVirtualRoot();
		Vfs vfsADir = vfsA.mkdir("dir");
		vfsADir.touch("a.txt");

		Vfs vfsB = Vfs.createVirtualRoot();
		Vfs vfsBDir = vfsA.mkdir("dir");
		vfsBDir.touch("b.txt");

		Vfs.copyRecursive(vfsA, vfsB);
		assertNotNull(vfsA.get(new Path("dir/a.txt")));
		assertNotNull(vfsA.get(new Path("dir/b.txt")));
	}
}
