package multipacks.spigot.serving;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.bukkit.entity.Player;

import com.google.common.io.Files;
import com.google.gson.JsonObject;

import multipacks.utils.logging.AbstractMPLogger;

public class LocalPackServer implements PackServer {
	private static final String FILE_NAME = "multipacks-spigot-generated.zip";
	private File resourcesFile;

	public LocalPackServer(AbstractMPLogger logger, JsonObject config) {
		String osName = System.getProperty("os.name", "Unix").toLowerCase();
		Map<String, String> env = System.getenv();

		// Windows
		if (osName.startsWith("windows")) {
			if (env.containsKey("APPDATA")) resourcesFile = new File(env.get("APPDATA") + "\\.minecraft\\resourcepacks");
			else resourcesFile = new File(System.getProperty("user.home", "C:\\Users\\Public") + "\\AppData\\Roaming\\.minecraft\\resourcepacks");
		}

		// Unix-like
		// We assume the game data directory is stored at homedir
		else if (osName.contains("nix")) resourcesFile = new File(System.getProperty("user.home", "/.minecraft/resourcepacks"));

		// What else?
		else resourcesFile = null;

		if (resourcesFile == null) {
			logger.warning("Cannot identify platform: " + System.getProperty("os.name"));
			logger.warning("If you are using Unix-like platform, please open a new issue at our GitHub repository");
			resourcesFile = new File(FILE_NAME);
		} else if (!resourcesFile.exists()) {
			logger.warning("Missing " + resourcesFile + " folder!");
			logger.warning("Local serving is only available for, well, locally hosted server. Please start this server in your local machine to continue.");
			logger.warning("File will be deployed at current working directory.");
			resourcesFile = new File(FILE_NAME);
		} else resourcesFile = new File(resourcesFile, FILE_NAME);
	}

	@Override
	public CompletableFuture<Boolean> serve(Player player, InputStream stream) {
		try (FileOutputStream s = new FileOutputStream(resourcesFile)) {
			stream.transferTo(s);
			s.flush();
			s.close();
			return CompletableFuture.completedFuture(true);
		} catch (Exception e) {
			return CompletableFuture.failedFuture(e);
		}
	}

	@Override
	public CompletableFuture<Boolean> serve(Player player, File artifact) {
		try {
			Files.copy(artifact, resourcesFile);
			return CompletableFuture.completedFuture(true);
		} catch (Exception e) {
			return CompletableFuture.failedFuture(e);
		}
	}
}
