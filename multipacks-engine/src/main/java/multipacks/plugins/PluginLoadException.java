package multipacks.plugins;

public class PluginLoadException extends RuntimeException {
	private static final long serialVersionUID = -818588508988080084L;

	public PluginLoadException(String message) {
		super(message);
	}

	public PluginLoadException(String message, Throwable from) {
		super(message, from);
	}
}
