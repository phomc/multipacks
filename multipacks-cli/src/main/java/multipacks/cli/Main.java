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
package multipacks.cli;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import multipacks.cli.api.console.FancyStackTrace;
import multipacks.cli.commands.MultipacksCommand;
import multipacks.logging.LoggingLevel;
import multipacks.logging.LoggingStage;
import multipacks.logging.SimpleLogger;
import multipacks.platform.PlatformConfig;
import multipacks.plugins.InternalSystemPlugin;
import multipacks.utils.Constants;
import multipacks.utils.ResourcePath;
import multipacks.utils.io.IOUtils;

public class Main {
	public static void main(String[] args) throws IOException {
		SystemEnum currentSystem = SystemEnum.getPlatform();
		SimpleLogger logger = new SimpleLogger();
		boolean isDebug = Boolean.parseBoolean(System.getenv().getOrDefault("MULTIPACKS_DEBUG", "false"));

		if (isDebug) {
			logger.debug("Debug logging level is enabled");
		} else {
			logger.toggleLoggingLevel(LoggingLevel.DEBUG, false);
		}

		if (currentSystem == SystemEnum.UNKNOWN) {
			System.err.println("Unsupported platform: " + System.getProperty("os.name"));
			System.err.println("If you think this platform should be supported, please open new issue in our GitHub repository.");
			System.exit(1);
			return;
		}

		if (currentSystem.isLegacy()) {
			System.err.println("Warning: Legacy Multipacks detected");
			System.err.println("Your previous Multipacks folder is considered as 'legacy' because " + currentSystem.getMultipacksDir().resolve(PlatformConfig.FILENAME) + " is missing.");
			System.err.println("Moving previous Multipacks folder to .multipacks-backup...");

			try (LoggingStage stage = logger.newStage("Backing up", ".multipacks to .multipacks-backup")) {
				Path dest = currentSystem.getHomeDir().resolve(".multipacks-backup");
				Files.move(currentSystem.getMultipacksDir(), dest, StandardCopyOption.REPLACE_EXISTING);
			}
		}

		if (Files.notExists(currentSystem.getMultipacksDir())) {
			System.out.println("Creating Multipacks data...");

			try (LoggingStage stage = logger.newStage("Multipacks Init", "Preparation", 2)) {
				Files.createDirectories(currentSystem.getMultipacksDir());
				Files.createDirectories(currentSystem.getMultipacksDir().resolve("repository"));

				stage.newStage(PlatformConfig.FILENAME);
				try (OutputStream stream = Files.newOutputStream(currentSystem.getMultipacksDir().resolve(PlatformConfig.FILENAME))) {
					IOUtils.jsonToStream(PlatformConfig.createConfigForHome().toJson(), stream);
				}
			}
		}

		CLIPlatform platform = new CLIPlatform(logger, new PlatformConfig(IOUtils.jsonFromPath(currentSystem.getMultipacksDir().resolve(PlatformConfig.FILENAME)).getAsJsonObject()), currentSystem);
		platform.loadPlugin(new ResourcePath(Constants.SYSTEM_NAMESPACE, "builtin/internal_system_plugin"), new InternalSystemPlugin());

		try {
			new MultipacksCommand(platform).execute(args);
		} catch (Exception e) {
			System.out.println("An error occured:");
			System.out.println("---");
			FancyStackTrace.print(e, System.out, isDebug);

			if (!isDebug) {
				System.out.println("---");
				System.out.println("Tip: Toggle stack traces by adding MULTIPACKS_DEBUG=true environment variable.");
			}
		}
	}
}
