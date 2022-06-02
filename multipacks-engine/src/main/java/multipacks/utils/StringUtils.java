package multipacks.utils;

public class StringUtils {
	private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789";
	// private static final String NON_SPECIAL = ALPHABET + "0123456789!@#$%^&*()-=_+[]{}\\|;':\"<>,./? `~";

	public static String randomString(int length) {
		char[] cs = new char[length];
		for (int i = 0; i < length; i++) cs[i] = ALPHABET.charAt((int) Math.round(Math.floor(Math.random() * ALPHABET.length())));
		return String.valueOf(cs);
	}
}
