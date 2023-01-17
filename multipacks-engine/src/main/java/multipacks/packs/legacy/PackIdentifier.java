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
package multipacks.packs.legacy;

import multipacks.versioning.Version;

public class PackIdentifier {
	public final String id;
	public final Version version;
	public final String folder;

	public PackIdentifier(String id, Version version) {
		this.id = id;
		this.version = version;
		this.folder = null;
	}

	public PackIdentifier(String id, String folder) {
		this.id = id;
		this.version = null;
		this.folder = folder;
	}

	public static PackIdentifier fromString(String id, String location) {
		return location.startsWith("file:")? new PackIdentifier(id, location.substring(5)) : new PackIdentifier(id, new Version(location)); 
	}

	@Override
	public String toString() {
		return id + ": " + (folder != null? "file:" + folder : version);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof PackIdentifier pid && id.equals(pid.id) && version.compareTo(pid.version) == 0;
	}
}
