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
package multipacks.cli.api;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import multipacks.cli.api.annotations.Argument;
import multipacks.cli.api.annotations.Option;
import multipacks.cli.api.annotations.Subcommand;
import multipacks.cli.api.console.TableDisplay;
import multipacks.cli.api.internal.ArgumentInfo;
import multipacks.cli.api.internal.OptionInfo;

/**
 * Abstract class for all commands and subcommands.
 * @author nahkd
 * @see Argument
 * @see Option
 * @see Subcommand
 * @see #onExecute()
 * @see #onExecuteWithoutSubcommand()
 * @see #postExecute()
 *
 */
public abstract class Command {
	protected String helpName, helpDescription;
	protected boolean allowHelp = true;

	/**
	 * Called when this command is executed. This method will be called after all arguments and options are
	 * populated with user's data and before subcommand (if any).
	 * @throws CommandException
	 */
	protected void onExecute() throws CommandException {
		// NO-OP
	}

	/**
	 * Called after subcommand is executed, or after {@link #onExecute()} if there's no subcommands.
	 * @throws CommandException
	 */
	protected void postExecute() throws CommandException {
		// NO-OP
	}

	/**
	 * Called when this command is executed without subcommand.
	 * @throws CommandException
	 */
	protected void onExecuteWithoutSubcommand() throws CommandException {
		// NO-OP
	}

	private boolean isBuilt = false;
	private ArgumentInfo[] arguments;
	private Map<String, OptionInfo> options;
	private Map<String, Command> subcommands;

	private void build() {
		if (isBuilt) return;

		boolean hasOptionalArgs = false;

		this.options = new HashMap<>();
		this.subcommands = new HashMap<>();
		List<ArgumentInfo> arguments = new ArrayList<>();

		for (Field f : getClass().getDeclaredFields()) {
			Argument argAnnotation = f.getDeclaredAnnotation(Argument.class);
			if (argAnnotation != null) {
				arguments.add(new ArgumentInfo(this, argAnnotation, f));
				if (argAnnotation.optional()) hasOptionalArgs = true;
			}

			Option optAnnotation = f.getDeclaredAnnotation(Option.class);
			if (optAnnotation != null) {
				OptionInfo optInfo = new OptionInfo(this, optAnnotation, f);
				for (String opt : optAnnotation.value()) options.put(opt, optInfo);
			}

			Subcommand cmdAnnotation = f.getDeclaredAnnotation(Subcommand.class);
			if (cmdAnnotation != null) {
				try {
					subcommands.put(cmdAnnotation.value(), (Command) f.get(this));
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		}

		for (Method m : getClass().getDeclaredMethods()) {
			Argument argAnnotation = m.getDeclaredAnnotation(Argument.class);
			if (argAnnotation != null) arguments.add(new ArgumentInfo(this, argAnnotation, m));

			Option optAnnotation = m.getDeclaredAnnotation(Option.class);
			if (optAnnotation != null) {
				OptionInfo optInfo = new OptionInfo(this, optAnnotation, m);
				for (String opt : optAnnotation.value()) options.put(opt, optInfo);
			}
		}

		Collections.sort(arguments);
		this.arguments = arguments.toArray(ArgumentInfo[]::new);

		// Validate rules:
		// If there's no subcommands, optional arguments must be placed after required arguments.
		// If there's at least 1 subcommand, no optional arguments are allowed.
		if (hasOptionalArgs) {
			if (subcommands.size() > 0) throw new RuntimeException("No optional arguments are allowed when there's at least 1 subcommand.");
			boolean currentOptionalState = false;

			for (ArgumentInfo info : arguments) {
				if (currentOptionalState && !info.declared.optional()) throw new RuntimeException("Argument #" + info.declared.value() + " is required, but there's at least 1 optional argument before it.");
				if (info.declared.optional()) currentOptionalState = true;
			}
		}

		isBuilt = true;
	}

	public void execute(Parameters params) throws CommandException {
		build();

		if (allowHelp && !params.endOfParams() && (params.getCurrent().equals("-h") || params.getCurrent().equals("-?") || params.getCurrent().equals("--help"))) {
			printHelp(System.out);
			return;
		}

		checkOptions(params);

		for (ArgumentInfo arg : arguments) {
			checkOptions(params);

			if (params.endOfParams()) {
				if (arg.declared.optional()) break;
				throw new CommandException("Argument " + (arg.declared.helpName().length() > 0? arg.declared.helpName() : ("#" + arg.declared.value())) + " is required");
			}

			arg.set(params.getThenAdvance());
		}

		checkOptions(params);
		onExecute();

		if (!params.endOfParams() && subcommands.size() > 0) {
			Command subcommand = subcommands.get(params.getCurrent());
			if (subcommand == null) throw new CommandException("Subcommand '" + params.getCurrent() + "' not found");

			params.getThenAdvance();
			subcommand.execute(params);
		} else {
			onExecuteWithoutSubcommand();
		}

		postExecute();
	}

	public void execute(String... args) {
		execute(new Parameters(args));
	}

	public void printHelp(PrintStream out) {
		if (helpDescription != null) out.println(helpDescription);
		out.print("Usage: " + (helpName != null? helpName : "<command>"));
		if (options.size() > 0) out.print(" [-<Options>]");
		if (subcommands.size() > 0) out.print(" (subcommand...)");

		for (ArgumentInfo arg : arguments) {
			String name = arg.declared.helpName();
			out.print(" " + (arg.declared.optional()? "[" : "<") + (name.length() > 0? name : "argument") + (arg.declared.optional()? "]" : ">"));
		}

		out.println();

		TableDisplay table = new TableDisplay();
		if (arguments.length > 0) {
			table.add("Arguments:", null);
			for (ArgumentInfo arg : arguments) {
				table.add("  " + arg.declared.helpName() + " ", arg.declared.optional()? "(optional)" : null);
			}
		}

		if (options.size() > 0) {
			table.add("Options:", null);
			options.values().stream().distinct().forEachOrdered(opt -> {
				String head = "  ";
				for (String variant : opt.declared.value()) head += "  " + variant;
				table.add(head + "=<value> ", opt.declared.helpDescription().length() > 0? opt.declared.helpDescription() : null);
			});
		}

		if (subcommands.size() > 0) {
			table.add("Subcommands:", null);
			for (Map.Entry<String, Command> e : subcommands.entrySet()) {
				table.add("  " + e.getKey() + " ", e.getValue().helpDescription != null? e.getValue().helpDescription : null);
			}
		}

		table.print(out);
	}

	private void checkOptions(Parameters params) throws CommandException {
		String current;

		while ((current = params.getCurrent()) != null && current.startsWith("-")) {
			params.getThenAdvance();
			String[] split = current.split("=", 2);

			OptionInfo opt = options.get(split[0]);
			if (opt == null) break;

			if (split.length == 1) {
				if (opt.type.isAssignableFrom(boolean.class)) opt.set("true");
			} else {
				opt.set(split[1]);
			}
		}
	}

	public static Object parse(String data, Class<?> type) {
		Object val;
		if (type.isAssignableFrom(String.class)) val = data;
		else if (type.isAssignableFrom(long.class)) val = Long.parseLong(data);
		else if (type.isAssignableFrom(int.class)) val = Integer.parseInt(data);
		else if (type.isAssignableFrom(short.class)) val = Short.parseShort(data);
		else if (type.isAssignableFrom(byte.class)) val = Byte.parseByte(data);
		else if (type.isAssignableFrom(double.class)) val = Double.parseDouble(data);
		else if (type.isAssignableFrom(float.class)) val = Float.parseFloat(data);
		else if (type.isAssignableFrom(boolean.class)) val = Boolean.parseBoolean(data);
		else throw new RuntimeException("Can't convert String to " + type);
		return val;
	}
}
