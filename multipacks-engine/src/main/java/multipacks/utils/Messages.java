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
import multipacks.repository.query.PackQuery;

/**
 * @author nahkd
 *
 */
public class Messages {
	public static String missingFieldAny(String... fieldName) {
		String msg = "Missing '" + fieldName[0] + "' ";
		for (int i = 1; i < fieldName.length; i++) msg += "or '" + fieldName[i] + "' ";
		return msg + "field";
	}

	public static String missingFieldAll(String... fieldName) {
		String msg = "Missing '" + fieldName[0] + "' ";
		for (int i = 1; i < fieldName.length; i++) msg += "and '" + fieldName[i] + "' ";
		return msg + "field" + (fieldName.length != 1? "s" : "");
	}

	public static String packNotFoundRepo(PackIdentifier id) {
		return "Pack not found in repository: " + id.name + " version " + id.packVersion.toStringNoPrefix();
	}

	public static final String FILE_ISDIR = "Is a directory";
	public static final String FILE_ISNOTDIR = "Is not a directory";

	public static String cantResolveDependency(PackQuery query) {
		return "Failed to find dependency with query: " + query;
	}

	public static String missingFile(Object fileHandle) {
		return "File not found: " + fileHandle;
	}

	public static String missingFile(Object fileHandle, Object scope) {
		return "File not found: " + fileHandle + " (scope = " + scope + ")";
	}
}
