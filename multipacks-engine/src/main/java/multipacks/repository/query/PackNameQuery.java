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
package multipacks.repository.query;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import multipacks.packs.meta.PackInfo;

/**
 * @author nahkd
 *
 */
public class PackNameQuery implements PackQuery {
	public static final Pattern QUERY_PATTERN = Pattern.compile("^name ([\\\"'])(.*?)\\1$");

	public final String name;

	public PackNameQuery(String name) {
		this.name = name;
	}

	@Override
	public boolean matches(PackInfo meta) {
		return meta.name.equals(name);
	}

	public static PackNameQuery parse(String queryString) {
		Matcher matcher = QUERY_PATTERN.matcher(queryString);

		if (matcher.find()) {
			String name = matcher.group(2);
			return new PackNameQuery(name);
		}

		return null;
	}

	@Override
	public String toString() {
		return "name '" + name + "'";
	}
}
