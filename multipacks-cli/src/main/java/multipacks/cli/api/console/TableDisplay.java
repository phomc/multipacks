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
package multipacks.cli.api.console;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author nahkd
 *
 */
public class TableDisplay {
	public final List<String> left = new ArrayList<>();
	public final List<String> right = new ArrayList<>();

	public int columnWidth = 0;

	public void add(String left, String right) {
		this.left.add(left);
		this.right.add(right);

		if (columnWidth < left.length()) columnWidth = left.length() + 1;
	}

	public void print(PrintStream out) {
		for (int i = 0; i < left.size(); i++) {
			String l = left.get(i);
			String r = right.get(i);
			if (l == null) l = "";
			if (r == null) r = "";

			while (l.length() < columnWidth) l += ' ';

			out.println(l + r);
		}
	}
}
