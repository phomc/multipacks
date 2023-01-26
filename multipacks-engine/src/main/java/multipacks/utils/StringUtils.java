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
package multipacks.utils;

public class StringUtils {
	private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789";
	// private static final String NON_SPECIAL = ALPHABET + "0123456789!@#$%^&*()-=_+[]{}\\|;':\"<>,./? `~";

	public static String randomString(int length) {
		char[] cs = new char[length];
		for (int i = 0; i < length; i++) cs[i] = ALPHABET.charAt((int) Math.round(Math.floor(Math.random() * ALPHABET.length())));
		return String.valueOf(cs);
	}
}
