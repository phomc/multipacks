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
package multipacks.spigot;

import java.util.logging.Logger;
import java.util.stream.Stream;

import multipacks.logging.legacy.AbstractLogger;

class JavaMPLogger extends AbstractLogger {
	private Logger logger;

	public JavaMPLogger(Logger logger) {
		this.logger = logger;
	}

	private String concat(Object... objs) {
		return String.join("", Stream.of(objs).map(v -> v.toString()).toArray(String[]::new));
	}

	@Override
	public void info(Object... objs) {
		logger.info(concat(objs));
	}

	@Override
	public void warning(Object... objs) {
		logger.warning(concat(objs));
	}

	@Override
	public void error(Object... objs) {
		logger.severe(concat(objs));
	}

	@Override
	public void critical(Object... objs) {
		logger.severe(concat(objs));
	}
}
