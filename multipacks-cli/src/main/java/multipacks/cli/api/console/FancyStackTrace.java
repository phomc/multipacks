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
package multipacks.cli.api.console;

import java.io.PrintStream;
import java.util.concurrent.ExecutionException;

import multipacks.cli.api.CommandException;

/**
 * @author nahkd
 *
 */
public class FancyStackTrace {
	public static String trimSystemPackages(String str) {
		return str.replaceFirst("java\\.lang\\.", "");
	}

	public static void print(Throwable e, PrintStream out, boolean details, boolean causedBy, String prefix) {
		if (e instanceof CommandException cmdException) out.println(prefix + "Command: " + e.getLocalizedMessage());
		else out.println(prefix + (causedBy? "Caused by " : "") + trimSystemPackages(e.getClass().getCanonicalName()) + ": " + e.getLocalizedMessage());

		if (details) {
			for (StackTraceElement st : e.getStackTrace()) out.println(prefix + "  at " + st.toString());
		}

		if (e.getCause() != null) {
			if (e.getCause() instanceof ExecutionException exec) e = exec;
			print(e.getCause(), out, details, true, prefix + "  ");
		}
	}

	public static void print(Throwable e, PrintStream out, boolean details) {
		print(e, out, details, false, "");
	}
}
