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
package multipacks.utils.logging;

import java.io.PrintStream;
import java.util.stream.Stream;

public class SystemLogger extends AbstractLogger {
	private void log(PrintStream printer, Object... objs) {
		printer.println(String.join("", Stream.of(objs).map(v -> v.toString()).toArray(String[]::new)));
	}

	@Override
	public void info(Object... objs) {
		log(System.out, objs);
	}

	@Override
	public void warning(Object... objs) {
		log(System.err, objs);
	}

	@Override
	public void error(Object... objs) {
		log(System.err, objs);
	}

	@Override
	public void critical(Object... objs) {
		log(System.err, objs);
	}
}
