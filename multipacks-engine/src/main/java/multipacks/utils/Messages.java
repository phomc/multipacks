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

import multipacks.packs.meta.PackIdentifier;

/**
 * @author nahkd
 *
 */
public class Messages {
	public static String missingField(String fieldName) {
		return "Missing '" + fieldName + "' field";
	}

	public static String packNotFoundRepo(PackIdentifier id) {
		return "Pack not found in repository: " + id.name + " version " + id.packVersion.toStringNoPrefix();
	}

	public static final String FILE_ISDIR = "Is a directory";
	public static final String FILE_ISNOTDIR = "Is not a directory";
}
