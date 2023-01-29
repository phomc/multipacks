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
package multipacks.tests;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import multipacks.logging.Logger;
import multipacks.logging.SimpleLogger;
import multipacks.modifier.Modifier;
import multipacks.modifier.ModifierInfo;
import multipacks.platform.Platform;
import multipacks.plugins.InternalSystemPlugin;
import multipacks.repository.LocalRepository;
import multipacks.repository.Repository;
import multipacks.utils.ResourcePath;

/**
 * @author nahkd
 *
 */
public class TestPlatform implements Platform {
	private LocalRepository repo;
	private HashMap<ResourcePath, ModifierInfo<?, ?, ?>> modifiers = new HashMap<>();

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
	public List<ResourcePath> getRegisteredModifiers() {
		return new ArrayList<>(modifiers.keySet());
	}

	@Override
	public ModifierInfo<?, ?, ?> getModifierInfo(ResourcePath id) {
		return modifiers.get(id);
	}

	@Override
	public <C, X, T extends Modifier<C, X>> void registerModifier(ResourcePath id, ModifierInfo<C, X, T> info) {
		modifiers.put(id, info);
	}
}
