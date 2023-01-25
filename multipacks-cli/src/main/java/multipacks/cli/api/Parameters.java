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
package multipacks.cli.api;

public class Parameters {
	public final String[] params;
	private int index = 0;

	public Parameters(String[] params) {
		this.params = new String[params.length];
		System.arraycopy(params, 0, this.params, 0, params.length);
	}

	public boolean endOfParams() {
		return index >= params.length;
	}

	public String getCurrent() {
		if (endOfParams()) return null;
		return params[index];
	}

	public String getThenAdvance() {
		if (endOfParams()) return null;
		return params[index++];
	}
}
