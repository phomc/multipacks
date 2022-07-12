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

import multipacks.vfs.Path;
import multipacks.vfs.VirtualFs;

/**
 * Allocation space.
 * @author nahkd
 *
 * @param <T> Allocated object type.
 * @param <D> Allocation data (Eg: {@link Path} to model file).
 */
public abstract class AllocationSpace<T extends Allocated, D> {
	/**
	 * Get the size of this allocation space.
	 */
	public abstract int size();

	/**
	 * Get the allocated object at specified index.
	 * @param index Index value. This value is always less than the {@link #size()} of this allocation
	 * space.
	 * @param fs Virtual file system of the targeted pack, mainly used for processing allocation.
	 * @param data Allocation data.
	 * @return Allocated object at specified index.
	 */
	public abstract T allocate(int index, VirtualFs fs, D data) throws IOException;
}
