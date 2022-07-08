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
package multipacks.vfs;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Immutable path object. To be fair it is actually mutable, but you'll have to use reflections and crap
 * just to modify its internal segments array.
 * @author nahkd
 *
 */
public class Path {
	private String[] segments;

	/**
	 * Construct a new path from a single string. This string may contains forward slashes ("/") as separators.
	 */
	public Path(String p) {
		this(p.split("\\/"));
	}

	/**
	 * Construct a new path from multiple segments. Basically {@link Path#Path(String)} but with separators being
	 * inserted between each segments.
	 */
	public Path(String... segments) {
		List<String> stack = new ArrayList<>();
		for (String s : segments) {
			if (s.equals(".")) continue;
			else if (s.equals("..")) {
				if (stack.size() == 0 || stack.get(stack.size() - 1).equals("..")) stack.add("..");
				else stack.remove(stack.size() - 1);
			} else if (s.matches("[A-Za-z0-9\\-_\\. ]+")) stack.add(s);
			else throw new IllegalArgumentException("Illegal path segment name: '" + s + "' (Must matches \"[A-Za-z0-9\\\\-_ ]+\")");
		}
		this.segments = stack.toArray(String[]::new);
	}

	/**
	 * Return true if this path object accesses parent directory (using ".."). Primarily used to prevent path
	 * traversal.
	 */
	public boolean isAccessingParent() {
		return segments.length > 0 && segments[0].equals("..");
	}

	public Path parent() {
		return Path.join(this, new Path(".."));
	}

	/**
	 * Join this path with another path.
	 */
	public Path join(Path p) {
		return new Path(toString() + "/" + p.toString());
	}

	/**
	 * Join this path with another path.
	 */
	public Path join(String p) {
		return join(new Path(p));
	}

	/**
	 * Get all path segments from this path object. The result array has all unnecessary segments trimmed (Eg:
	 * "a/b/../c" becomes "a/c").
	 */
	public String[] getSegments() {
		String[] out = new String[segments.length];
		System.arraycopy(segments, 0, out, 0, out.length);
		return out;
	}

	/**
	 * Joins all segments from {@link #getSegments()} with separators between them and return as string.
	 */
	@Override
	public String toString() {
		return String.join("/", segments);
	}

	public File joinWith(File root) {
		return new File(root, String.join(File.separator, segments));
	}

	public boolean isParentOf(Path child) {
		if (segments.length >= child.segments.length) return false;
		for (int i = 0; i < segments.length; i++) if (!segments[i].equals(child.segments[i])) return false;
		return true;
	}

	@Override
	public int hashCode() {
		return 7 + 13 * this.toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj instanceof Path p && Arrays.equals(segments, p.segments);
	}

	public static Path join(Path... p) {
		return new Path(String.join("/", Stream.of(p).map(v -> v.toString()).toArray(String[]::new)));
	}

	public static Path join(String... p) {
		return new Path(String.join("/", Stream.of(p).map(v -> new Path(v).toString()).toArray(String[]::new)));
	}
}
