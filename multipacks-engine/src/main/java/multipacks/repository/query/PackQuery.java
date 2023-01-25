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

import multipacks.packs.meta.PackInfo;

/**
 * Packs query information. Used for searching pack ids with given conditions. 
 * @author nahkd
 *
 */
public interface PackQuery {
	public static final String SPLIT_PATTERN = "\\s*;\\s*";

	boolean matches(PackInfo meta);

	static PackQuery parse(String queryString) {
		String[] queries = queryString.split(SPLIT_PATTERN);
		if (queries.length == 1) return parseSingle(queries[0]);

		PackQuery[] pq = new PackQuery[queries.length];
		for (int i = 0; i < pq.length; i++) if ((pq[i] = parseSingle(queries[i])) == null) return null;
		return new PackMultipleQueries(pq);
	}

	static PackQuery parseSingle(String singleQueryString) {
		singleQueryString = singleQueryString.trim();
		PackQuery ret;

		if ((ret = PackVersionQuery.parse(singleQueryString)) != null) return ret;
		if ((ret = PackNameQuery.parse(singleQueryString)) != null) return ret;
		return null;
	}
}
