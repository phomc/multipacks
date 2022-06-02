package multipacks.versioning;

/**
 * Represent a version.
 * @author nahkd
 *
 */
public class Version implements Comparable<Version> {
	public final VersionPrefix prefix;
	private int[] versionArray;

	public Version(String version) {
		VersionPrefix prefix = VersionPrefix.EXACT;

		for (VersionPrefix p : VersionPrefix.CHECK_ORDER) if (version.startsWith(p.prefix)) {
			prefix = p;
			version = version.substring(p.prefix.length());
			break;
		}

		this.prefix = prefix;

		versionArray = getAsVersionArray(version);
	}

	public int getVersionIndex(int level) {
		if (level >= versionArray.length) return 0;
		return versionArray[level];
	}

	@Override
	public int compareTo(Version o) {
		int level = 0;

		while (level < Math.max(versionArray.length, o.versionArray.length)) {
			int a = getVersionIndex(level);
			int b = o.getVersionIndex(level);
			level++;

			if (a > b) return 1;
			if (a < b) return -1;
		}

		return 0;
	}

	public boolean check(Version ver) {
		int compare = -this.compareTo(ver);
		return switch (compare) {
		case -1 -> prefix == VersionPrefix.OLDER || prefix == VersionPrefix.OLDER_OR_EXACT;
		case 0 -> prefix == VersionPrefix.EXACT || prefix == VersionPrefix.OLDER_OR_EXACT || prefix == VersionPrefix.NEWER_OR_EXACT;
		case 1 -> prefix == VersionPrefix.NEWER || prefix == VersionPrefix.NEWER_OR_EXACT;
		default -> throw new RuntimeException("Somehow compareTo returned " + compare + " that's not -1, 0 or 1... (Volatile memory?)");
		};
	}

	private static int[] getAsVersionArray(String str) {
		String[] split = str.split("\\.");
		int[] arr = new int[split.length];
		for (int i = 0; i < arr.length; i++) arr[i] = Integer.parseInt(split[i]);
		return arr;
	}

	@Override
	public String toString() {
		return prefix.prefix + toStringNoPrefix();
	}

	public String toStringNoPrefix() {
		String str = "";
		for (int i = 0; i < versionArray.length; i++) str += versionArray[i] + (i == versionArray.length - 1? "" : ".");
		return str;
	}
}
