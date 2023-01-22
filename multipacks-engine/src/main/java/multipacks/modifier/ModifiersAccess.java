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
package multipacks.modifier;

import java.io.DataInput;
import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

import multipacks.utils.ResourcePath;
import multipacks.utils.io.Deserializer;

/**
 * @author nahkd
 *
 */
public interface ModifiersAccess {
	Modifier createModifier(ResourcePath id);
	Modifier deserializeModifier(ResourcePath id, DataInput input) throws IOException;
	<T extends Modifier> void registerModifier(ResourcePath id, Supplier<T> supplier, Deserializer<T> deserializer);
	List<ResourcePath> getRegisteredModifiers();

	default void registerModifier(ResourcePath id, Supplier<Modifier> supplier) {
		registerModifier(id, supplier, input -> supplier.get());
	}
}
