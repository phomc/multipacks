package multipacks.cli;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import multipacks.bundling.BundleIgnore;
import multipacks.bundling.PackBundler;
import multipacks.bundling.PackagingFailException;
import multipacks.management.LocalRepository;
import multipacks.management.PacksRepository;
import multipacks.management.PacksUploadable;
import multipacks.packs.Pack;
import multipacks.packs.PackIdentifier;
import multipacks.packs.PackIndex;
import multipacks.utils.IOUtils;
import multipacks.utils.Selects;
import multipacks.utils.logging.SystemLogger;
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
	public File outputTo = null;
	public List<BundleIgnore> bundleIgnoreFeatures = new ArrayList<>();

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
				for (PackIdentifier i : index.include) System.out.println("   + " + i.id + " " + i.version);
			}

			File assets = pack.getAssetsRoot();
			File data = pack.getDataRoot();

			System.out.println("Pack content locations:");
			System.out.println(" - Pack root: " + packDir.getAbsolutePath());
			if (assets != null) System.out.println(" - Assets: " + assets.getAbsolutePath());
			if (data != null) System.out.println(" - Data: " + data.getAbsolutePath());

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
			bundler.bundle(pack, out);

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
