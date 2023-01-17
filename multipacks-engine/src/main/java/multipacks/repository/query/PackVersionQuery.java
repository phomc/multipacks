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
package multipacks.repository.query;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import multipacks.packs.meta.PackInfo;
import multipacks.versioning.Version;

/**
 * @author nahkd
 *
 */
public class PackVersionQuery implements PackQuery {
	public static final Pattern QUERY_PATTERN = Pattern.compile("^version\\s*(>=|==|<=|>|<)\\s*((\\d+\\.?)+)$");

	public final Version comparison;

	public PackVersionQuery(Version comparison) {
		this.comparison = comparison;
	}

	@Override
	public boolean matches(PackInfo meta) {
		return comparison.check(meta.packVersion);
	}

	@Override
	public String toString() {
		return "version " + comparison.toString();
	}

	public static PackVersionQuery parse(String queryString) {
		Matcher matcher = QUERY_PATTERN.matcher(queryString);

		if (matcher.find()) {
			String prefix = matcher.group(1);
			String versionDigits = matcher.group(2);
			return new PackVersionQuery(new Version(prefix + versionDigits));
		}

		return null;
	}
}
