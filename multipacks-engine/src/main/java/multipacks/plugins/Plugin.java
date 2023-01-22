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

import multipacks.platform.Platform;
import multipacks.repository.RepositoriesAccess;
import multipacks.repository.Repository;

/**
 * Plugins (as known as "add-ons") are used to add extra features to Multipacks, without having to fork the
 * project just to add a single thing.
 * @author nahkd
 *
 */
public interface Plugin {
	void onInit(Platform platform);

	/**
	 * Get repositories from this plugin. These repositories will be present in {@link RepositoriesAccess#getRepositories()}.
	 * @return A collection of repositories, or {@code null} if there is none.
	 */
	default Collection<Repository> getPluginRepositories() {
		return null;
	}
}
