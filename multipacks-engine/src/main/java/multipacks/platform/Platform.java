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
package multipacks.platform;

import multipacks.logging.LoggerAccess;
import multipacks.modifier.ModifiersAccess;
import multipacks.repository.RepositoriesAccess;

/**
 * Multipacks platform interface (some people may call this "Multipacks environment"). If you are creating your
 * own platform, you have to implement this interface. Take a look at "Multipacks for Spigot" if you need an
 * example.
 * @author nahkd
 *
 */
public interface Platform extends RepositoriesAccess, LoggerAccess, ModifiersAccess {
}
