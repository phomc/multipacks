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
package multipacks.cli.api.tests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import multipacks.cli.api.tests.sample.CommandRoot;

class CommandTest {
	@Test
	void test() {
		CommandRoot root = new CommandRoot();
		root.execute("first", "42");
		assertEquals("first", root.firstArg);
		assertEquals(42, root.secondArg);
	}

	@Test
	void testState() {
		CommandRoot root = new CommandRoot();
		root.execute("first", "42", "--state");
		assertEquals("first", root.firstArg);
		assertEquals(42, root.secondArg);
		assertTrue(root.state);
	}

	@Test
	void testSubcommand() {
		CommandRoot root = new CommandRoot();
		root.execute("second", "1337", "subcommand", "12.34");
		assertEquals("second", root.firstArg);
		assertEquals(1337, root.secondArg);
		assertEquals(12.34, root.subcommand.seconds);
		assertEquals("default value", root.subcommand.optional);

		root.execute("second", "1337", "subcommand", "12.34", "not default");
		assertEquals("second", root.firstArg);
		assertEquals(1337, root.secondArg);
		assertEquals(12.34, root.subcommand.seconds);
		assertEquals("not default", root.subcommand.optional);
	}

	@Test
	void testOptions() {
		CommandRoot root = new CommandRoot();
		root.execute("first", "42", "--my-option=hello world");
		assertEquals("first", root.firstArg);
		assertEquals(42, root.secondArg);
		assertEquals("hello world", root.myOption);
	}

	@Test
	void testOptionsSubcommand() {
		CommandRoot root = new CommandRoot();
		root.execute("first", "42", "subcommand", "12.34", "-a=hello", "not default");
		assertEquals("first", root.firstArg);
		assertEquals(42, root.secondArg);
		assertEquals("hello", root.subcommand.anotherOption);

		root.execute("first", "42", "subcommand", "12.34", "--A=yolo", "not default");
		assertEquals("first", root.firstArg);
		assertEquals(42, root.secondArg);
		assertEquals("yolo", root.subcommand.anotherOption);
	}
}
