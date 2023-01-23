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
