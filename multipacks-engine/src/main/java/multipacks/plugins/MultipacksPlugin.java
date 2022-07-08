package multipacks.plugins;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import multipacks.management.PacksRepository;
import multipacks.postprocess.PostProcessPass;
import multipacks.utils.Selects;

public interface MultipacksPlugin {
	default void onLoad() {}

	default PacksRepository parseRepository(File root, String uri) { return null; }
	default void registerPostProcessPasses(HashMap<String, Function<JsonObject, PostProcessPass>> reg) {}

	ArrayList<MultipacksPlugin> PLUGINS = new ArrayList<>();

	static void loadPlugin(MultipacksPlugin plugin) {
		plugin.onLoad();
		plugin.registerPostProcessPasses(PostProcessPass.REGISTRY);
		PLUGINS.add(plugin);
	}

	static void loadJarPlugin(URL jarUrl) throws IOException {
		try (URLClassLoader clsLoader = new URLClassLoader(new URL[] { jarUrl }, MultipacksPlugin.class.getClassLoader())) {
			InputStream in = clsLoader.getResourceAsStream("multipacks.plugin.json");
			if (in == null) throw new PluginLoadException("Missing multipacks.plugin.json inside " + jarUrl + " plugin");
			JsonElement json = new JsonParser().parse(new InputStreamReader(in));
			in.close();

			String mainClass;

			if (json.isJsonPrimitive()) {
				mainClass = json.getAsString();
			} else if (json.isJsonObject()) {
				mainClass = Selects.nonNull(json.getAsJsonObject().get("main"), "Missing 'main' in multipacks.plugin.json").getAsString();
			} else throw new PluginLoadException("Cannot read main class inside multipacks.plugin.json in " + jarUrl + " plugin");

			try {
				Class<?> cls = Class.forName(mainClass, true, clsLoader);
				Constructor<?> ctor = cls.getDeclaredConstructor();
				Object obj = ctor.newInstance();

				if (obj instanceof MultipacksPlugin plugin) loadPlugin(plugin);
				else System.err.println("Warning: Plugin '" + jarUrl + "' have its main class not an instance of MultipacksPlugin");
			} catch (Exception e) {
				throw new PluginLoadException("Cannot load plugin " + jarUrl, e);
			}
		}
	}

	static void loadJarPlugin(File jar) {
		try {
			loadJarPlugin(jar.toURI().toURL());
		} catch (Exception e) {
			throw new PluginLoadException("Cannot load plugin " + jar.toString(), e);
		}
	}
}
