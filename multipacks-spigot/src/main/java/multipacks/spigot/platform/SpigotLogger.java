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
package multipacks.spigot.platform;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.regex.Matcher;

import multipacks.logging.Logger;
import multipacks.logging.LoggingLevel;
import multipacks.logging.SimpleLogger;

/**
 * @author nahkd
 *
 */
public class SpigotLogger implements Logger {
	private java.util.logging.Logger backed;
	private boolean[] enabledLoggingLevels;

	public SpigotLogger(java.util.logging.Logger backed) {
		this.backed = backed;

		enabledLoggingLevels = new boolean[LoggingLevel.values().length];
		for (int i = 0; i < enabledLoggingLevels.length; i++) enabledLoggingLevels[i] = true;
	}

	@Override
	public void log(LoggingLevel level, String message, Object... objs) {
		if (!enabledLoggingLevels[level.ordinal()]) return;

		AtomicInteger i = new AtomicInteger(0);
		String out = SimpleLogger.PATTERN.matcher(message).replaceAll(r -> i.get() < objs.length? Matcher.quoteReplacement(Objects.toString(objs[i.getAndAdd(1)])) : "{}");
		backed.log(level == LoggingLevel.DEBUG? Level.FINE : level == LoggingLevel.INFO? Level.INFO : level == LoggingLevel.WARNING? Level.WARNING : Level.SEVERE, out);
	}

	@Override
	public void toggleLoggingLevel(LoggingLevel level, boolean enable) {
		enabledLoggingLevels[level.ordinal()] = enable;
	}
}
