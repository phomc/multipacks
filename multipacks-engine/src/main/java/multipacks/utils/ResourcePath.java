package multipacks.utils;

import java.util.Objects;

public class ResourcePath {
	public final String namespace;
	public final String path;

	public ResourcePath(String namespace, String path) {
		this.namespace = namespace;
		this.path = path;
	}

	public ResourcePath(String str) {
		if (str.contains(":")) {
			String[] split = str.split(":");
			namespace = split[0];
			path = str.substring(split[0].length() + 1);
		} else {
			namespace = "minecraft";
			path = str;
		}
	}

	@Override
	public String toString() {
		return namespace + ":" + path;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof ResourcePath rp && namespace.equals(rp.namespace) && path.equals(rp.path);
	}

	@Override
	public int hashCode() {
		return Objects.hash(namespace, path);
	}
}
