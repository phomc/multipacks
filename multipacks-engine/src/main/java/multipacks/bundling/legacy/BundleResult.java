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
package multipacks.bundling.legacy;

import java.util.HashMap;
import java.util.function.Supplier;

import multipacks.postprocess.allocations.Allocator;
import multipacks.vfs.legacy.VirtualFs;

public class BundleResult {
	private final HashMap<Class<?>, Object> transformResults = new HashMap<>();
	public VirtualFs files;

	@SuppressWarnings("unchecked")
	public <T> T getOrCreate(Class<T> clazz, Supplier<T> creator) {
		if (clazz == Allocator.class) throw new IllegalArgumentException("Cannot take in Allocator class. Please extends Allocator.");

		Object obj = transformResults.get(clazz);

		if (obj == null) {
			obj = creator.get();
			transformResults.put(clazz, obj);
		}

		return (T) obj;
	}
}
