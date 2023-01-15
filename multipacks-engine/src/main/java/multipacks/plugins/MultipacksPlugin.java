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

import multipacks.bundling.PackBundler;
import multipacks.management.legacy.PacksRepository;
import multipacks.packs.DynamicPack;
import multipacks.packs.Pack;
import multipacks.packs.PackIdentifier;
import multipacks.postprocess.PostProcessPass;
import multipacks.utils.Selects;

public interface MultipacksPlugin {
	default void onLoad() {}

	/**
	 * Attempt to parse repository string. If this plugin can't parse the string, this method will returns
	 * null.
	 */
	default PacksRepository parseRepository(File root, String uri) { return null; }

	/**
	 * Register post processing passed that can be used for packs. It is recommended to use "namespace:value"
	 * for pass id to avoid conflicts.
	 */
	default void registerPostProcessPasses(HashMap<String, Function<JsonObject, PostProcessPass>> reg) {}

	/**
	 * Called when the pack is failed to resolve from {@link PackBundler}. You can, for example, returns an
	 * empty {@link DynamicPack}, which will effectively works as error ignoring.
	 */
	default Pack packsResolutionFailback(PackIdentifier id) { return null; }

	ArrayList<MultipacksPlugin> PLUGINS = new ArrayList<>();
	ArrayList<URLClassLoader> JAR_HANDLES = new ArrayList<>();

	static void loadPlugin(MultipacksPlugin plugin) {
		plugin.onLoad();
		plugin.registerPostProcessPasses(PostProcessPass.REGISTRY);
		PLUGINS.add(plugin);
	}

	static void loadJarPlugin(URL jarUrl) throws IOException {
		URLClassLoader clsLoader = new URLClassLoader(new URL[] { jarUrl }, MultipacksPlugin.class.getClassLoader());
		InputStream in = clsLoader.getResourceAsStream("multipacks.plugin.json");
		if (in == null) {
			clsLoader.close();
			throw new PluginLoadException("Missing multipacks.plugin.json inside " + jarUrl + " plugin");
		}
		JsonElement json = new JsonParser().parse(new InputStreamReader(in));
		in.close();

		String mainClass;

		if (json.isJsonPrimitive()) {
			mainClass = json.getAsString();
		} else if (json.isJsonObject()) {
			mainClass = Selects.nonNull(json.getAsJsonObject().get("main"), "Missing 'main' in multipacks.plugin.json").getAsString();
		} else {
			clsLoader.close();
			throw new PluginLoadException("Cannot read main class inside multipacks.plugin.json in " + jarUrl + " plugin");
		}

		try {
			Class<?> cls = Class.forName(mainClass, true, clsLoader);
			Constructor<?> ctor = cls.getDeclaredConstructor();
			Object obj = ctor.newInstance();

			if (obj instanceof MultipacksPlugin plugin) loadPlugin(plugin);
			else System.err.println("Warning: Plugin '" + jarUrl + "' have its main class not an instance of MultipacksPlugin");
			JAR_HANDLES.add(clsLoader);
		} catch (Exception e) {
			throw new PluginLoadException("Cannot load plugin " + jarUrl, e);
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
