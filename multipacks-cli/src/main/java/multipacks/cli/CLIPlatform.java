package multipacks.cli;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import multipacks.logging.Logger;
import multipacks.modifier.Modifier;
import multipacks.platform.Platform;
import multipacks.plugins.Plugin;
import multipacks.repository.Repository;
import multipacks.utils.ResourcePath;
import multipacks.utils.io.Deserializer;

public class CLIPlatform implements Platform {
	private Logger logger;
	private Map<ResourcePath, Supplier<Modifier>> modifierCtors = new HashMap<>();
	private Map<ResourcePath, Deserializer<Modifier>> modifierDeserializers = new HashMap<>();

	private Map<ResourcePath, Plugin> plugins = new HashMap<>();
	private boolean pluginsLoadFinalized = false;

	protected List<Repository> repositories = new ArrayList<>();

	public CLIPlatform(Logger logger) {
		this.logger = logger;
	}

	public void loadPlugin(ResourcePath id, Plugin plugin) {
		if (pluginsLoadFinalized) throw new IllegalStateException("Plugins registration is closed");
		if (plugins.containsKey(id)) throw new IllegalArgumentException("Plugin is already loaded: " + id);

		try {
			plugin.onInit(this);
		} catch (Exception e) {
			throw new RuntimeException("Plugin load failed: " + id, e);
		}

		plugins.put(id, plugin);
	}

	public void finalizePluginsLoad() {
		if (pluginsLoadFinalized) return;
		pluginsLoadFinalized = true;

		for (Plugin p : plugins.values()) {
			Collection<Repository> repos = p.getPluginRepositories();
			if (repos != null) repositories.addAll(repos);
		}
	}

	@Override
	public Collection<Repository> getRepositories() {
		return Collections.unmodifiableCollection(repositories);
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

	@Override
	public Modifier createModifier(ResourcePath id) {
		Supplier<Modifier> ctor = modifierCtors.get(id);
		if (ctor != null) ctor.get();
		return null;
	}

	@Override
	public Modifier deserializeModifier(ResourcePath id, DataInput input) throws IOException {
		Deserializer<Modifier> deserializer = modifierDeserializers.get(id);
		if (deserializer != null) return deserializer.deserialize(input);
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Modifier> void registerModifier(ResourcePath id, Supplier<T> supplier, Deserializer<T> deserializer) {
		if (modifierCtors.containsKey(id)) throw new IllegalArgumentException("Modifier is already registered: " + id);
		modifierCtors.put(id, (Supplier<Modifier>) supplier);
		modifierDeserializers.put(id, (Deserializer<Modifier>) deserializer);
	}

	@Override
	public List<ResourcePath> getRegisteredModifiers() {
		return new ArrayList<>(modifierCtors.keySet());
	}
}
