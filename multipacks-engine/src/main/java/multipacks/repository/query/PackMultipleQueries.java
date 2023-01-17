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
 * @author nahkd
 *
 */
public class PackMultipleQueries implements PackQuery {
	public final PackQuery[] queries;

	public PackMultipleQueries(PackQuery[] queries) {
		this.queries = queries;
	}

	@Override
	public boolean matches(PackInfo meta) {
		for (PackQuery q : queries) if (!q.matches(meta)) return false;
		return true;
	}

	@Override
	public String toString() {
		String str = queries[0].toString();
		for (int i = 1; i < queries.length; i++) str += "; " + queries[i];
		return str;
	}
}
