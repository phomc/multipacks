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

/**
 * @author nahkd
 *
 */
public interface Logger {
	void log(LoggingLevel level, String message, Object... objs);
	void toggleLoggingLevel(LoggingLevel level, boolean enable);

	default LoggingStage newStage(String display, String stageName, int maxStages) {
		return new LoggingStage(this, display, stageName, maxStages);
	}

	default LoggingStage newStage(String display, String stageName) {
		return new LoggingStage(this, display, stageName, -1);
	}

	default void debug(String message, Object... objs) {
		log(LoggingLevel.DEBUG, message, objs);
	}

	default void info(String message, Object... objs) {
		log(LoggingLevel.INFO, message, objs);
	}

	default void warning(String message, Object... objs) {
		log(LoggingLevel.WARNING, message, objs);
	}

	default void error(String message, Object... objs) {
		log(LoggingLevel.ERROR, message, objs);
	}

	default void critical(String message, Object... objs) {
		log(LoggingLevel.CRITICAL, message, objs);
	}
}
