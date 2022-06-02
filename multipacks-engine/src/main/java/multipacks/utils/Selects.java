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

import java.util.function.Function;

public class Selects {
	@SafeVarargs
	public static <T> T firstNonNull(T... values) {
		for (T t : values) if (t != null) return t;
    	return null;
    }

	public static String firstNonEmpty(String... values) {
		for (String s : values) if (s.trim().length() > 0) return s;
		return null;
	}

	public static <I, R> R getChain(I val, Function<I, R> onNotNull, R def) {
		if (val == null) return def;
		return onNotNull.apply(val);
	}

    public static <T> T nonNull(T obj, String message) {
    	if (obj == null) throw new NullPointerException(message);
    	return obj;
    }
}
