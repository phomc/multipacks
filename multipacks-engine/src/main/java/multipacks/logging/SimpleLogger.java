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
package multipacks.logging;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * @author nahkd
 *
 */
public class SimpleLogger implements Logger {
	public static final Pattern PATTERN = Pattern.compile("\\{\\}");

	@Override
	public void log(LoggingLevel level, String message, Object... objs) {
		AtomicInteger i = new AtomicInteger(0);
		String out = PATTERN.matcher(message).replaceAll(r -> i.get() < objs.length? Objects.toString(objs[i.getAndAdd(1)]) : "{}");
		System.err.println("[" + level.toString() + "] " + out);
	}
}
