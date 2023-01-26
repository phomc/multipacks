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
package multipacks.cli.commands.management;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

import multipacks.cli.CLIPlatform;
import multipacks.cli.api.Command;
import multipacks.cli.api.CommandException;
import multipacks.cli.api.annotations.Argument;
import multipacks.cli.api.annotations.Option;
import multipacks.cli.api.annotations.Subcommand;
import multipacks.cli.commands.MultipacksCommand;
import multipacks.repository.AuthorizedRepository;
import multipacks.repository.Repository;

/**
 * @author nahkd
 *
 */
public class RemoteCommand extends Command {
	public static final String ANONYMOUS_USER = "multipacks_anonymous";

	public final CLIPlatform platform;

	@Argument(value = 0, helpName = "connectionString")
	public String connectionString;

	@Option(value = { "--username", "-U" }, helpDescription = "Username to login to remote repository")
	public String username = ANONYMOUS_USER;

	@Option(value = { "--password", "-P" }, helpDescription = "Password to login to remote repository, using UTF-8 encoding")
	public String password;

	@Option(value = { "--secret", "-S" }, helpDescription = "Secret to login to remote repository, using hexadecimal encoding")
	public String secret;

	@Subcommand("upload") public final UploadCommand upload;
	@Subcommand("delete") public final DeleteCommand delete;

	protected AuthorizedRepository repository;

	public RemoteCommand(MultipacksCommand parent) {
		this.platform = parent.platform;

		upload = new UploadCommand(this);
		delete = new DeleteCommand(this);

		helpName = "remote";
		helpDescription = "Upload or delete pack from remote repository";
	}

	private byte[] getSecret() {
		if (password != null) return password.getBytes(StandardCharsets.UTF_8);
		if (secret != null) {
			if ((secret.length() % 2) == 1) throw new CommandException("Secret length is not even");
			byte[] bs = new byte[secret.length() / 2];
			for (int i = 0; i < bs.length; i++) bs[i] = (byte) ((Integer.parseInt(secret.charAt(i * 2) + "", 16) << 4) | (Integer.parseInt(secret.charAt(i * 2 + 1) + "", 16)));
			return bs;
		}

		return new byte[0];
	}

	@Override
	protected void onExecute() throws CommandException {
		Repository repo = Repository.fromConnectionString(connectionString, new File(".").toPath());

		System.out.println("Connecting to remote...");
		try {
			repository = repo.login(username, getSecret()).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new CommandException("An error occured", e);
		}

		System.out.println("Connected to remote repository: " + repo);
	}

	@Override
	protected void onExecuteWithoutSubcommand() throws CommandException {
		System.out.println("Subcommand is not supplied. Aborting.");
	}
}
