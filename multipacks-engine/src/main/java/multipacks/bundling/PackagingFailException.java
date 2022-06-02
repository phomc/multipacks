package multipacks.bundling;

public class PackagingFailException extends RuntimeException {
	private static final long serialVersionUID = 2251765351034996381L;

	public PackagingFailException(String message) {
		super(message);
	}
}
