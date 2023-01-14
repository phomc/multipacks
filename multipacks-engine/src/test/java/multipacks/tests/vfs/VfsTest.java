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
package multipacks.tests.vfs;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

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

		root.delete("dirA");
		assertNull(root.get("dirA"));
	}
}