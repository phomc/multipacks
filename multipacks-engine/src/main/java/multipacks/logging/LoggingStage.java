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
package multipacks.logging;

/**
 * Current stage of the software. For example, in Multipacks, it could be "modifiers apply stage" or
 * "bundling stage".
 * @author nahkd
 *
 */
public class LoggingStage implements AutoCloseable {
	private Logger logger;
	private String display, currentName;
	private int maxStages, currentStage;
	private long nanosecStart;

	public LoggingStage(Logger logger, String display, String currentName, int maxStages) {
		this.logger = logger;
		this.display = display;
		this.currentName = currentName;
		this.maxStages = maxStages;
		logCurrentStage();
		nanosecStart = System.nanoTime();
	}

	private void logCurrentStage() {
		logger.info("{}/{} [Stage {} of {}] ---", display, currentName, currentStage + 1, maxStages != -1? maxStages : "?");
	}

	public void newStage(String stageName) {
		this.currentName = stageName;
		currentStage++;
		logCurrentStage();
	}

	@Override
	public void close() {
		long elapsed = System.nanoTime() - nanosecStart;
		logger.info("{} [Completed in {}ms] \u2713", display, elapsed * Math.pow(10, -6));
		logger = null;
	}
}
