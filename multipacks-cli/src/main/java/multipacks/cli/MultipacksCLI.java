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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import multipacks.bundling.legacy.BundleIgnore;
import multipacks.bundling.legacy.BundleInclude;
import multipacks.bundling.legacy.PackBundler;
import multipacks.bundling.legacy.PackagingFailException;
import multipacks.logging.SystemLogger;
import multipacks.management.legacy.LocalRepository;
import multipacks.management.legacy.PacksRepository;
import multipacks.management.legacy.PacksUploadable;
import multipacks.packs.legacy.Pack;
import multipacks.packs.legacy.PackIdentifier;
import multipacks.packs.legacy.PackIndex;
import multipacks.utils.Selects;
import multipacks.utils.io.IOUtils;
import multipacks.versioning.GameVersions;
import multipacks.versioning.Version;

public class MultipacksCLI {
	public final Platform platform;
	public final List<PacksRepository> repositories = new ArrayList<>();
	public final SystemLogger cliLogger = new SystemLogger();

	// Command arguments
	public PackIdentifier filter;
	public PacksRepository selectedRepository;
	public boolean skipPrompts = false;
	public boolean ignoreErrors = false;
	public boolean watchInputs = false;
	public File outputTo = null;
	public List<BundleIgnore> bundleIgnoreFeatures = new ArrayList<>();
	public BundleInclude[] bundleIncludes = null;

	public MultipacksCLI(Platform platform) {
		this.platform = platform;
		this.repositories.add(selectedRepository = new LocalRepository(new File(platform.getHomeDir(), ".multipacks")));
	}

	// Exec
	public void exec(String... args) {
		if (args.length == 0) {
			System.out.println("No subcommand");
			System.out.println("All subcommands:");
			System.out.println("  list            List repositories or packs");
			System.out.println("  pack            Pack command");
			return;
		}

		switch (args[0]) {
		case "list": exec$list(args); return;
		case "pack": exec$pack(args); return;
		default:
			System.err.println("Unknown subcommand: " + args[0]);
			System.exit(1);
			break;
		}
	}

	private void exec$list(String... args) {
		if (args.length < 2) {
			System.out.println("No subcommand for 'list'");
			System.out.println("All subcommands for 'list':");
			System.out.println("  repo            List repositories");
			System.out.println("  pack            List installed packs in repository");
			return;
		}

		switch (args[1]) {
		case "repo":
		case "repos":
		case "repository":
		case "repositories": exec$listRepo(); return;
		case "pack":
		case "packs": exec$listPacks(); return;
		default:
			System.err.println("Unknown subcommand for 'list': " + args[1]);
			System.exit(1);
			return;
		}
	}

	private void exec$listRepo() {
		System.out.println("Listing all repositories:");

		for (int i = 0; i < repositories.size(); i++) {
			PacksRepository repo = repositories.get(i);
			System.out.println("#" + i + ": " + repo + (repo == selectedRepository? " (Already Selected)" : ""));
			System.out.println("    Select with -R #" + i);
		}
	}

	private void exec$listPacks() {
		System.out.println("Listing all packs: (Selected repository: " + selectedRepository + ")");
		boolean exists = false;
		Iterator<PackIndex> iter = selectedRepository.queryPacks(filter);

		while (iter.hasNext()) {
			PackIndex pack = iter.next();
			if (pack == null) continue;
			exists = true;

			System.out.println("- id = " + pack.id + ": " + pack.name + " v" + pack.packVersion + " for " + pack.gameVersion + (pack.author != null? (" by " + pack.author) : ""));
			if (pack.description != null) System.out.println("  " + pack.description);

			if (pack.include.length > 0) {
				System.out.println("  Depends on " + pack.include.length + " other pack" + (pack.include.length == 1? "" : "s") + ":");
				for (PackIdentifier i : pack.include) System.out.println("   + " + i.id + " " + i.version);
			}
		}

		if (!exists) {
			System.out.println("- ** No packs :( **");
		}
	}

	private void exec$pack(String... args) {
		if (args.length < 2) {
			System.out.println("No subcommand for 'pack'");
			System.out.println("All subcommands for 'pack':");
			System.out.println("  init [path]     Create an empty Multipacks pack");
			System.out.println("  info [path]     Gather Multipacks pack informations");
			System.out.println("  build [path]    Build Multipacks pack to game resources or data pack");
			System.out.println("  install [path]  Install Multipacks pack to selected repository");
			return;
		}

		switch (args[1]) {
		case "init": exec$packInit(args.length < 3? null : args[2]); return;
		case "info": exec$packInfo(args.length < 3? null : args[2]); return;
		case "build": exec$packBuild(args.length < 3? null : args[2]); return;
		case "install": exec$packInstall(args.length < 3? null : args[2]); return;
		default:
			System.err.println("Unknown subcommand for 'pack': " + args[1]);
			System.exit(1);
			return;
		}
	}

	private void exec$packInit(String path) {
		if (path == null) path = ".";
		File outDir = new File(path);

		if (new File(outDir, "multipacks.json").exists()) {
			System.err.println("Cannot init: multipacks.json already exists in " + outDir);
			System.err.println("Please find a different directory and run init command in there.");
			System.exit(1);
			return;
		}

		PackIndex packIndex = new PackIndex();
		packIndex.id = outDir.getName().toLowerCase().replace(' ', '-');
		packIndex.name = outDir.getName();
		packIndex.exports = Map.of(
				new multipacks.vfs.Path("assets"), new BundleInclude[] { BundleInclude.RESOURCES },
				new multipacks.vfs.Path("data"), new BundleInclude[] { BundleInclude.DATA }
				);

		if (!skipPrompts) {
			System.out.println("Fill informations to initialize: (some fields are optional)");
			Scanner scanner = new Scanner(System.in);

			System.out.print("Multipacks pack ID (" + packIndex.id + "): ");
			packIndex.id = Selects.firstNonEmpty(scanner.nextLine(), packIndex.id).trim();

			System.out.print("Pack name (" + packIndex.name + "): ");
			packIndex.name = Selects.firstNonEmpty(scanner.nextLine(), packIndex.name).trim();

			System.out.print("Author: ");
			packIndex.author = scanner.nextLine().trim();
			if (packIndex.author.length() == 0) packIndex.author = null;

			System.out.print("Pack description (optional): ");
			packIndex.description = scanner.nextLine().trim();
			if (packIndex.description.length() == 0) packIndex.description = null;

			System.out.print("Pack version (1.0.0) [NOT game version]: ");
			packIndex.packVersion = new Version(Selects.firstNonEmpty(scanner.nextLine(), "1.0.0").trim());

			System.out.print("Game version (>=1.18.2): ");
			packIndex.gameVersion = new Version(Selects.firstNonEmpty(scanner.nextLine(), ">=1.18.2").trim());

			scanner.close();
		} else {
			packIndex.packVersion = new Version("1.0.0");
			packIndex.gameVersion = new Version(">=1.18.2");
		}

		System.out.println("Initializing Multipacks pack in '" + outDir + "'...");

		try {
			if (!outDir.exists()) outDir.mkdirs();
			IOUtils.jsonToFile(packIndex.toJson(), new File(outDir, "multipacks.json"));
			new File(outDir, "assets").mkdirs();
			new File(outDir, "data").mkdirs();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Cannot write to file: IOException");
			System.exit(1);
			return;
		}

		System.out.println("Done!");
	}

	private void exec$packInfo(String path) {
		if (path == null) path = ".";
		File packDir = new File(path);

		if (!new File(packDir, "multipacks.json").exists()) {
			System.err.println("Cannot find multipacks.json");
			System.err.println("Invalid pack.");
			System.exit(1);
			return;
		}

		try {
			Pack pack = new Pack(packDir);

			PackIndex index = pack.getIndex();
			System.out.println("Pack index: (multipacks.json)");
			System.out.println(" - Pack ID: " + index.id);
			if (index.name != null) System.out.println(" - Pack name: " + index.name);
			if (index.author != null) System.out.println(" - Author: " + index.author);
			if (index.description != null) System.out.println(" - Description: " + index.description);
			System.out.println(" - Pack version: " + index.packVersion.toStringNoPrefix());
			System.out.println(" - Game version: " + index.gameVersion + " (pack format = " + GameVersions.getPackFormat(index.gameVersion) + ")");

			if (index.include != null && index.include.length > 0) {
				System.out.println(" - " + index.include.length + (index.include.length == 1? " depenency" : " depenencies"));
				for (PackIdentifier i : index.include) System.out.println("   + " + i.id + " " + (i.folder != null? "Local(" + i.folder + ")" : i.version));
			}

			System.out.println(" - Pack root: " + packDir.getAbsolutePath());

			if (pack.getIndex().exports != null) {
				System.out.println("Pack exports:");
				for (Entry<multipacks.vfs.Path, BundleInclude[]> e : pack.getIndex().exports.entrySet()) {
					System.out.println(" - " + e.getKey().toString() + ": " + (e.getValue().length == 2? "* (all)" : e.getValue()[0].toString()));
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Cannot read pack: IOException");
			System.exit(1);
			return;
		}
	}

	private void exec$packBuild(String path) {
		if (path == null) path = ".";
		File packDir = new File(path);

		if (watchInputs) {
			System.out.println("Watch mode enabled. Make changes to the directory to rebuild");
			exec$packBuild$watch(packDir);
		}

		if (!new File(packDir, "multipacks.json").exists()) {
			System.err.println("Cannot find multipacks.json");
			System.err.println("Invalid pack.");
			System.exit(1);
			return;
		}

		try {
			Pack pack = new Pack(packDir);
			File outputTo = this.outputTo != null? this.outputTo : new File(pack.getIndex().id + "-v" + pack.getIndex().packVersion.toStringNoPrefix() + ".zip");
			System.out.println("Preparing bundler...");
			System.out.println("(File will be written to " + outputTo.getAbsolutePath() + ")");
			if (bundleIgnoreFeatures.size() > 0) System.out.println("Features ignored: " + String.join(", ", bundleIgnoreFeatures.stream().map(v -> v.toString().toLowerCase()).toArray(String[]::new)));
			
			PackBundler bundler = new PackBundler(cliLogger);
			bundler.repositories.add(selectedRepository); // TODO: Add all loaded repositories?
			bundler.ignoreResolveFail = ignoreErrors;
			bundler.bundlingIgnores.addAll(bundleIgnoreFeatures);

			System.out.println("Bundling...");
			FileOutputStream out = new FileOutputStream(outputTo);
			bundler.bundle(pack, out, bundleIncludes);

			System.out.println("Done.");
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Cannot read pack: IOException");
			System.exit(1);
			return;
		} catch (PackagingFailException e) {
			System.err.println("Packaging failed: " + e.getMessage());
			System.err.println("Please resolve all errors and run the command again.");
			System.exit(1);
			return;
		}
	}

	private void exec$packBuild$watch(File packDir) {
		WatchService watcher;
		HashMap<WatchKey, Path> keys = new HashMap<>();

		try {
			watcher = FileSystems.getDefault().newWatchService();
			exec$patchBuild$watch$registerAll(packDir.toPath(), watcher, keys);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Cannot initialize files watcher");
			System.exit(1);
			return;
		}

		PackBundler bundler = new PackBundler(cliLogger);
		bundler.repositories.add(selectedRepository);
		bundler.ignoreResolveFail = ignoreErrors;
		bundler.bundlingIgnores.addAll(bundleIgnoreFeatures);
		boolean firstRun = true;

		try {
			while (true) {
				WatchKey keyA = firstRun? null : watcher.poll();
				Path dir = keyA != null? keys.get(keyA) : null;

				if (!firstRun && keyA == null) {
					Thread.sleep(1000);
					continue;
				}

				try {
					Pack pack = new Pack(packDir);
					File outputTo = this.outputTo != null? this.outputTo : new File(pack.getIndex().id + "-v" + pack.getIndex().packVersion.toStringNoPrefix() + ".zip");

					FileOutputStream out = new FileOutputStream(outputTo);
					bundler.bundle(pack, out, bundleIncludes);

					// Watch
					if (!firstRun) for (WatchEvent<?> event : keyA.pollEvents()) {
						if (event.kind() == StandardWatchEventKinds.OVERFLOW) continue;
						@SuppressWarnings("unchecked")
						WatchEvent<Path> ev = (WatchEvent<Path>) event;
						Path name = ev.context();
						Path child = dir.resolve(name);

						if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
							try {
								if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) exec$patchBuild$watch$registerAll(child, watcher, keys);
								System.out.println("+ " + child);
							} catch (IOException e) {
								e.printStackTrace();
								System.err.println("IOException while trying to add " + child + " to watching list");
							}
						}
					}

					System.out.println("Bundle built, awaiting next update...");
				} catch (IOException e) {
					e.printStackTrace();
					System.err.println("IOException thrown while reading pack data");
				} catch (PackagingFailException e) {
					System.err.println("Packaging failed: " + e.getMessage());
				} catch (Exception e) {
					e.printStackTrace();
					System.err.println("Asdasjlkdakdal");
				} finally {
					firstRun = false;
					boolean valid = keyA != null? keyA.reset() : true;

					if (!valid) {
						keys.remove(keyA);
						System.out.println("- " + dir);
					}
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.out.println("Multipacks buildwatch interruped while waiting, stopping buildwatch...");
			return;
		}
	}

	private void exec$packBuild$watch$register(Path dir, WatchService ws, HashMap<WatchKey, Path> keys) throws IOException {
		WatchKey key = dir.register(ws, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
		keys.put(key, dir);
	}

	private void exec$patchBuild$watch$registerAll(Path start, WatchService ws, HashMap<WatchKey, Path> keys) throws IOException {
		Files.walkFileTree(start, new FileVisitor<>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				exec$packBuild$watch$register(dir, ws, keys);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}
		});
	}

	private void exec$packInstall(String path) {
		if (!(selectedRepository instanceof PacksUploadable uploadable)) {
			System.err.println("Cannot upload to selected repository: Not uploadable");
			System.out.println("Current repository is " + selectedRepository);
			System.out.println("Select other repository with -R <RepoString> option");
			System.exit(1);
			return;
		}

		if (path == null) path = ".";
		File packDir = new File(path);

		if (!new File(packDir, "multipacks.json").exists()) {
			System.err.println("Cannot find multipacks.json");
			System.err.println("Invalid pack.");
			System.exit(1);
			return;
		}

		try {
			Pack pack = new Pack(packDir);

			if (pack.getIndex().hasLocalReference()) {
				System.err.println("Cannot install pack with local references");
				System.err.println("Please check multipacks.json and ensure that 'include' section does not contains 'file:...' in dependency version field");
				System.exit(1);
				return;
			}

			System.out.println("Installing " + pack.getIndex().id + " v" + pack.getIndex().packVersion + " to your repository...");
			System.out.println("Current repository is " + selectedRepository);
			uploadable.putPack(pack);
			System.out.println("Pack installed! Use '\"include\": {\"" + pack.getIndex().id + "\": \"" + pack.getIndex().packVersion.toStringNoPrefix() + "\"}' in your other pack to includes this pack.");
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Cannot read pack: IOException");
			System.exit(1);
			return;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			for (int i = 0; i < 5; i++) System.err.println();
			System.err.println("Look like you don't have permission to upload this package to repository");
			System.exit(1);
			return;
		}
	}
}
