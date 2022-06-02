package multipacks.versioning;

public enum VersionPrefix {
	EXACT(""),
	NEWER(">"),
	NEWER_OR_EXACT(">="),
	OLDER("<"),
	OLDER_OR_EXACT("<=");

	public static final VersionPrefix[] CHECK_ORDER = { NEWER_OR_EXACT, NEWER, OLDER_OR_EXACT, OLDER, EXACT };

	public final String prefix;

	private VersionPrefix(String prefix) {
		this.prefix = prefix;
	}
}
