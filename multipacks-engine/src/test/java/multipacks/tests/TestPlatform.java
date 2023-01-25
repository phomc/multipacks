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
package multipacks.tests;

import java.io.DataInput;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

import multipacks.logging.Logger;
import multipacks.logging.SimpleLogger;
import multipacks.modifier.Modifier;
import multipacks.platform.Platform;
import multipacks.plugins.InternalSystemPlugin;
import multipacks.repository.LocalRepository;
import multipacks.repository.Repository;
import multipacks.utils.ResourcePath;
import multipacks.utils.Selects;
import multipacks.utils.io.Deserializer;

/**
 * @author nahkd
 *
 */
public class TestPlatform implements Platform {
	private LocalRepository repo;
	private HashMap<ResourcePath, Supplier<Modifier>> modifierCtors = new HashMap<>();
	private HashMap<ResourcePath, Deserializer<Modifier>> modifierDeserializers = new HashMap<>();

	{
		try {
			repo = new LocalRepository(Path.of(this.getClass().getClassLoader().getResource("testRepo").toURI()));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	public TestPlatform() {
		new InternalSystemPlugin().onInit(this);
	}

	@Override
	public Collection<Repository> getRepositories() {
		return Arrays.asList(repo);
	}

	@Override
	public Logger getLogger() {
		return new SimpleLogger();
	}

	@Override
	public Modifier createModifier(ResourcePath id) {
		return Selects.getChain(modifierCtors.get(id), v -> v.get(), null);
	}

	@Override
	public Modifier deserializeModifier(ResourcePath id, DataInput input) throws IOException {
		Deserializer<Modifier> deserializer = modifierDeserializers.get(id);
		if (deserializer == null) return null;
		return deserializer.deserialize(input);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Modifier> void registerModifier(ResourcePath id, Supplier<T> supplier, Deserializer<T> deserializer) {
		modifierCtors.put(id, (Supplier<Modifier>) supplier);
		modifierDeserializers.put(id, (Deserializer<Modifier>) deserializer);
	}

	@Override
	public List<ResourcePath> getRegisteredModifiers() {
		return new ArrayList<>(modifierCtors.keySet());
	}
}
