package multipacks.cli;

import java.io.File;

public enum Platform {
	WINDOWS,
	UNIX_LIKE,
	UNKNOWN;

	public static Platform getPlatform() {
		String osProp = System.getProperty("os.name").toLowerCase();
		if (osProp.contains("windows")) return WINDOWS;
		if (osProp.contains("linux") || osProp.contains("unix") || osProp.contains("darwin") || osProp.contains("mac")) return UNIX_LIKE;
		return UNKNOWN;
	}

	public File getHomeDir() {
		return new File(System.getProperty("user.home", switch (this) {
		case WINDOWS -> "C:\\Users\\" + System.getProperty("user.name");
		case UNIX_LIKE -> "/home/" + System.getProperty("user.name");
		default -> "/";
		}));
	}
}
