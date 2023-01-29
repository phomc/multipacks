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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import multipacks.platform.PlatformConfig;

public enum SystemEnum {
	WINDOWS {
		@Override
		public Path getMinecraftDir() {
			return new File(System.getenv("APPDATA")).toPath().resolve(".minecraft");
		}
	},
	UNIX_LIKE {
	},
	UNKNOWN {
	};

	public static SystemEnum getPlatform() {
		String osProp = System.getProperty("os.name").toLowerCase();
		if (osProp.contains("windows")) return WINDOWS;
		if (osProp.contains("linux") || osProp.contains("unix") || osProp.contains("darwin") || osProp.contains("mac")) return UNIX_LIKE;
		return UNKNOWN;
	}

	public Path getHomeDir() {
		return new File(System.getProperty("user.home")).toPath();
	}

	public Path getMultipacksDir() {
		return getHomeDir().resolve(".multipacks");
	}

	public Path getMinecraftDir() {
		return getHomeDir().resolve(".minecraft");
	}

	public boolean isLegacy() {
		return Files.exists(getMultipacksDir()) && Files.notExists(getMultipacksDir().resolve(PlatformConfig.FILENAME));
	}
}
