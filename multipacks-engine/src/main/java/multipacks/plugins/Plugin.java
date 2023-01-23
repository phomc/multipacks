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

import java.util.Collection;

import multipacks.packs.LocalPack;
import multipacks.platform.Platform;
import multipacks.repository.LocalRepository;
import multipacks.repository.RepositoriesAccess;
import multipacks.repository.Repository;
import multipacks.repository.SimpleRepository;

/**
 * Plugins (as known as "add-ons") are used to add extra features to Multipacks, without having to fork the
 * project just to add a single thing.
 * @author nahkd
 *
 */
public abstract class Plugin {
	public abstract void onInit(Platform platform);

	/**
	 * Get repositories from this plugin. These repositories will be present in {@link RepositoriesAccess#getRepositories()}.
	 * @return A collection of repositories, or {@code null} if there is none.
	 * @implNote You may override this method to return a collection of repositories.
	 * @see #repositoryFromPlugin(String)
	 */
	public Collection<Repository> getPluginRepositories() {
		return null;
	}

	// Plugin APIs
	/**
	 * Get local repository from this plugin resources. This method was meant to be used inside your plugin's
	 * {@link #getPluginRepositories()} method.
	 * @param pathToRepo Path to repository directory, which is placed inside your plugin JAR (or {@code src/main/resources}
	 * if you are in development environment).
	 * @return The local repository obtained from this plugin resources.
	 * @see SimpleRepository#SimpleRepository(multipacks.packs.Pack...)
	 * @see #packFromPlugin(String)
	 */
	protected LocalRepository repositoryFromPlugin(String pathToRepo) {
		return LocalRepository.fromClassLoader(this.getClass().getClassLoader(), pathToRepo);
	}

	/**
	 * Get local pack from this plugin resources.
	 * @param pathToPack Path to pack directory, which is placed inside your plugin JAR (or {@code src/main/resources} if
	 * you are in development environment).
	 * @return The local pack obtained from this plugin resources.
	 */
	protected LocalPack packFromPlugin(String pathToPack) {
		return LocalPack.fromClassLoader(this.getClass().getClassLoader(), pathToPack);
	}
}
