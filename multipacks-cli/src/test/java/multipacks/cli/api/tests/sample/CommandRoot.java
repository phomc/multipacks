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
package multipacks.cli.api.tests.sample;

import multipacks.cli.api.Command;
import multipacks.cli.api.CommandException;
import multipacks.cli.api.annotations.Argument;
import multipacks.cli.api.annotations.Option;
import multipacks.cli.api.annotations.Subcommand;

public class CommandRoot extends Command {
	@Argument(0) public String firstArg;
	@Argument(1) public int secondArg;

	@Option("--my-option") public String myOption;
	@Option("--state") public boolean state = false;

	@Subcommand("subcommand") public final SubcommandCommand subcommand = new SubcommandCommand();

	@Override
	protected void onExecute() throws CommandException {
		System.out.println("firstArg = " + firstArg);
		System.out.println("secondArg = " + secondArg);
		System.out.println("myOption = " + myOption);
		System.out.println("state = " + state);
	}
}
