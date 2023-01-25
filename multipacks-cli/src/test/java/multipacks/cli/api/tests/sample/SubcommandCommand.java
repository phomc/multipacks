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
package multipacks.cli.api.tests.sample;

import multipacks.cli.api.Command;
import multipacks.cli.api.CommandException;
import multipacks.cli.api.annotations.Argument;
import multipacks.cli.api.annotations.Option;

public class SubcommandCommand extends Command {
	@Argument(0) public double seconds;
	@Argument(value = 1, optional = true) public String optional = "default value";

	@Option({ "-a", "--A" }) public String anotherOption = "default value";

	@Override
	protected void onExecute() throws CommandException {
		System.out.println(seconds + " seconds");
		System.out.println("optional = " + optional);
		System.out.println("anotherOption = " + anotherOption);
	}
}
