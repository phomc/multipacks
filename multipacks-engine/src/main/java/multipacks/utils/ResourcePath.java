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
package multipacks.utils;

import java.util.Objects;

import multipacks.vfs.Path;

public class ResourcePath {
	public final String namespace;
	public final String path;

	public ResourcePath(String namespace, String path) {
		this.namespace = namespace;
		this.path = path;
	}

	public ResourcePath(String str) {
		if (str.contains(":")) {
			String[] split = str.split(":");
			namespace = split[0];
			path = str.substring(split[0].length() + 1);
		} else {
			namespace = "minecraft";
			path = str;
		}
	}

	public ResourcePath noFileExtension() {
		Path p = new Path(path);
		String[] splits = p.fileName().split("\\.");
		if (splits.length == 1) return this;
		return new ResourcePath(namespace, path.substring(0, path.length() - splits[splits.length - 1].length() - 1));
	}

	@Override
	public String toString() {
		return namespace + ":" + path;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof ResourcePath rp && namespace.equals(rp.namespace) && path.equals(rp.path);
	}

	@Override
	public int hashCode() {
		return Objects.hash(namespace, path);
	}
}
