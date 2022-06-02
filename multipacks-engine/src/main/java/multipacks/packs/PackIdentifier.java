package multipacks.packs;

import multipacks.versioning.Version;

public class PackIdentifier {
	public final String id;
	public final Version version;

	public PackIdentifier(String id, Version version) {
		this.id = id;
		this.version = version;
	}

	@Override
	public String toString() {
		return id + ": " + version;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof PackIdentifier pid && id.equals(pid.id) && version.compareTo(pid.version) == 0;
	}
}
