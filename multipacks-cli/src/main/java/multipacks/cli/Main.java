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
package multipacks.cli;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import multipacks.bundling.BundleIgnore;
import multipacks.bundling.BundleInclude;
import multipacks.management.PacksRepository;
import multipacks.packs.PackIdentifier;
import multipacks.plugins.MultipacksDefaultPlugin;
import multipacks.plugins.MultipacksPlugin;
import multipacks.versioning.Version;

public class Main {
	public static void main(String[] args) throws IOException {
		Platform currentPlatform = Platform.getPlatform();

		if (currentPlatform == Platform.UNKNOWN) {
			System.err.println("Unsupported platform: " + System.getProperty("os.name"));
			System.err.println("If you think this platform should be supported, please open new issue in our GitHub repository.");
			System.exit(1);
			return;
		}

		if (args.length == 0) {
			System.out.println("Multipacks CLI");
			System.out.println("Usage: java multipacks.cli.Main <subcommand...> [options...]");
			System.out.println("Subcommands:");
			System.out.println("  list            List repositories or packs");
			System.out.println("  pack            Pack command");
			System.out.println();
			System.out.println("Options:");
			System.out.println("  -F  --filter <ID> [>=, >, <=, <]<Version>");
			System.out.println("        Set filter for querying packs");
			System.out.println("  -R  --repo <'#' + Index | 'file:/path/'>");
			System.out.println("        Select repository (see index with 'list repo')");
			System.out.println("  -O  --output </path/to/output>");
			System.out.println("        Set output path");
			System.out.println("      --skip");
			System.out.println("        Skip 'pack init' prompts");
			System.out.println("      --ignore-errors");
			System.out.println("        Ignore errors as much as possible");
			System.out.println("  -I  --ignore <Pack feature>");
			System.out.println("        Ignore pack features (use -I multiple times to ignores more)");
			System.out.println("        Available features to ignore: " + String.join(", ", Stream.of(BundleIgnore.values()).map(v -> v.toString().toLowerCase()).toArray(String[]::new)));
			System.out.println("      --include <Pack type A, Pack type B...>");
			System.out.println("        Include 1 or more different pack types to output. The parameter is separated with comma (',') character");
			System.out.println("        Available pack types: " + String.join(", ", Stream.of(BundleInclude.values()).map(v -> v.toString().toLowerCase()).toArray(String[]::new)));
			System.out.println("  -W  --watch");
			System.out.println("        Watch for any changes (usable with pack build)");
			System.out.println("      --plugin </path/to/plugin.jar>");
			System.out.println("        Load Multipacks Engine plugin from a JAR file");
			System.out.println("        By default, this will not load any plugin but the integrated one");
			return;
		}

		// Load plugins
		MultipacksPlugin.loadPlugin(new MultipacksDefaultPlugin());

		MultipacksCLI cli = new MultipacksCLI(currentPlatform);
		List<String> regularArguments = new ArrayList<>();

		for (int i = 0; i < args.length; i++) {
			String s = args[i];

			if (!s.startsWith("-")) {
				regularArguments.add(s);
				continue;
			}

			// TODO: Use switch-case?

			if (s.equals("-F") || s.equals("--filter")) {
				String id = args[++i];
				String versionStr = args[++i];
				cli.filter = new PackIdentifier(id, new Version(versionStr));
			} else if (s.equals("-R") || s.equals("--repo")) {
				String repoStr = args[++i];

				if (repoStr.startsWith("#")) {
					int repoIdx = Integer.parseInt(repoStr.substring(1));

					if (repoIdx >= cli.repositories.size()) {
						System.err.println("Repository #" + repoIdx + " doesn't exists (Out of bound)");
						System.err.println("Tip: Use 'list repo' to view all repositories and its index");
						System.exit(1);
						return;
					}

					cli.selectedRepository = cli.repositories.get(repoIdx);
				} else {
					cli.selectedRepository = PacksRepository.parseRepository(null, repoStr);

					if (cli.selectedRepository == null) {
						System.err.println("Unknown repository string: " + repoStr);
						System.err.println("Valid string formats:");
						System.err.println(" - '#' + Index");
						System.err.println(" - 'file:/path/to/repository'");
						System.exit(1);
						return;
					}
				}
			} else if (s.equals("-O") || s.equals("--output")) {
				cli.outputTo = new File(args[++i].replace('/', File.separatorChar).replace('\\', File.separatorChar));
			} else if (s.equals("--skip")) {
				cli.skipPrompts = true;
			} else if (s.equals("--ignore-errors")) {
				cli.ignoreErrors = true;
			} else if (s.equals("--ignore")) {
				String featureString = args[++i];

				try {
					BundleIgnore ignore = BundleIgnore.valueOf(featureString.toUpperCase());
					cli.bundleIgnoreFeatures.add(ignore);
				} catch (IllegalArgumentException e) {
					System.err.println("Unknown feature: " + featureString);
					System.exit(1);
					return;
				}
			} else if (s.equals("--include")) {
				String includeString = args[++i];
				String[] split = includeString.split(",");
				BundleInclude[] includes = new BundleInclude[split.length];

				for (int j = 0; j < split.length; j++) {
					String includeStr = split[j];

					try {
						includes[j] = BundleInclude.valueOf(includeStr.trim().toUpperCase());
					} catch (IllegalArgumentException e) {
						System.err.println("Unknown pack type: " + includeStr);
						System.exit(1);
						return;
					}
				}

				cli.bundleIncludes = includes;
			} else if (s.equals("-W") || s.equals("--watch")) {
				cli.watchInputs = true;
			} else if (s.equals("--plugin")) {
				String plugin = args[++i];
				System.out.println("Plugin: " + plugin);
				MultipacksPlugin.loadJarPlugin(new File(plugin));
			} else {
				System.err.println("Unknown option: " + s);
				System.exit(1);
				return;
			}
		}

		cli.exec(regularArguments.toArray(String[]::new));

		// Clean up
		for (URLClassLoader loader : MultipacksPlugin.JAR_HANDLES) loader.close();
	}
}
