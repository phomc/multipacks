package multipacks.versioning;

import java.util.ArrayList;
import java.util.List;

public class GameVersions {
	private static class VersionEntry {
		int packFormat;
		Version versionFrom, versionTo;

		public VersionEntry(int format, String versionFrom, String versionTo) {
			this.packFormat = format;
			this.versionFrom = new Version(versionFrom);
			this.versionTo = new Version(versionTo);
		}

		public boolean check(Version version) {
			return versionFrom.check(version) && versionTo.check(version);
		}
	}

	private static final List<VersionEntry> VERSIONS = new ArrayList<>();

	static {
		VERSIONS.add(new VersionEntry(1, ">=1.6", "<1.9"));
		VERSIONS.add(new VersionEntry(2, ">=1.9", "<1.11"));
		VERSIONS.add(new VersionEntry(3, ">=1.11", "<1.13"));
		VERSIONS.add(new VersionEntry(4, ">=1.13", "<1.15"));
		VERSIONS.add(new VersionEntry(5, ">=1.15", "<1.16.2"));
		VERSIONS.add(new VersionEntry(6, "1.16.2", "1.16.2"));
		VERSIONS.add(new VersionEntry(7, ">=1.17", "<1.18"));
		VERSIONS.add(new VersionEntry(8, ">=1.18", "<1.19")); // TODO: 1.18.2 uses different pack format for data

		VERSIONS.add(new VersionEntry(9, ">=1.19", ">=1.19"));
	}

	public static int getPackFormat(Version gameVersion) {
		for (VersionEntry e : VERSIONS) if (e.check(gameVersion)) return e.packFormat;
		return -1;
	}
}
