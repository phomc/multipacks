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
package multipacks.modifier;

import java.util.List;
import java.util.function.Supplier;

import multipacks.utils.ResourcePath;
import multipacks.utils.io.Deserializer;

/**
 * @author nahkd
 *
 */
public interface ModifiersAccess {
	ModifierInfo<?, ?, ?> getModifierInfo(ResourcePath id);
	<C, X, T extends Modifier<C, X>> void registerModifier(ResourcePath id, ModifierInfo<C, X, T> info);

	@SuppressWarnings("unchecked")
	default <C, X, T extends Modifier<C, X>> void registerModifier(ResourcePath id, Supplier<T> supplier, Deserializer<T> deserializer) {
		registerModifier(id, new ModifierInfo<>()
				.setConstructor((Supplier<Modifier<Object, Object>>) supplier)
				.setDeserializer((Deserializer<Modifier<Object, Object>>) deserializer));
	}

	List<ResourcePath> getRegisteredModifiers();

	default <C, X> void registerModifier(ResourcePath id, Supplier<Modifier<C, X>> supplier) {
		registerModifier(id, supplier, input -> supplier.get());
	}
}
