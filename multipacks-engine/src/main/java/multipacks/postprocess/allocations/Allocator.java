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
package multipacks.postprocess.allocations;

import java.io.IOException;
import java.util.ArrayList;

import multipacks.bundling.BundleResult;
import multipacks.vfs.Path;
import multipacks.vfs.legacy.VirtualFs;

/**
 * Allocations allow different post processing passes to obtain the same content type without introducing
 * conflicts. An example would be custom model data, where each item can have "CustomModelData" NBT value
 * and we need to use that for, let's say... multi-parts custom entity for example. This will be used
 * with {@link BundleResult#getOrCreate(Class, java.util.function.Supplier)}.
 * @author nahkd
 * 
 * @param <T> Allocated object type.
 * @param <D> Allocation data (Eg: {@link Path} to model file).
 * 
 */
public abstract class Allocator<T extends Allocated, D> {
	private int current = 0;
	public final ArrayList<AllocationSpace<T, D>> spaces = new ArrayList<>();

	/**
	 * Add another allocation space on top of all spaces. Convenient method for {@link #spaces}
	 * {@link ArrayList#add(Object)}
	 * @param space
	 */
	public void add(AllocationSpace<T, D> space) {
		this.spaces.add(space);
	}

	/**
	 * Allocate a new object from pending allocation spaces.
	 * @return An allocated object, or null if this allocator ran out of spaces. Recommended approach for
	 * allocator ran out of spaces is to add another space to ensure there always enough space (space from
	 * JSON object for example).
	 * @see #add(AllocationSpace)
	 */
	public T allocateNew(VirtualFs fs, D data) throws IOException {
		if (spaces.size() == 0) return null;
		int p = current;
		// TODO optimize me
		for (AllocationSpace<T, D> space : spaces) {
			if (p < space.size()) {
				current++;
				return space.allocate(p, fs, data);
			}

			p -= space.size();
			continue;
		}

		return null;
	}
}
