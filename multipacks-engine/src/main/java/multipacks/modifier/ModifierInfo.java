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

import java.util.function.Supplier;

import multipacks.utils.io.Deserializer;

/**
 * @author nahkd
 *
 */
public class ModifierInfo<C, X, T extends Modifier<C, X>> {
	public Supplier<T> supplier;
	public Deserializer<T> deserializer;

	public ModifierInfo<C, X, T> setConstructor(Supplier<T> ctor) {
		supplier = ctor;
		return this;
	}

	public ModifierInfo<C, X, T> setDeserializer(Deserializer<T> deserializer) {
		this.deserializer = deserializer;
		return this;
	}
}
