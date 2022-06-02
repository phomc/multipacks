package multipacks.utils;

import java.util.function.Function;

public class Selects {
	@SafeVarargs
	public static <T> T firstNonNull(T... values) {
		for (T t : values) if (t != null) return t;
    	return null;
    }

	public static String firstNonEmpty(String... values) {
		for (String s : values) if (s.trim().length() > 0) return s;
		return null;
	}

	public static <I, R> R getChain(I val, Function<I, R> onNotNull, R def) {
		if (val == null) return def;
		return onNotNull.apply(val);
	}

    public static <T> T nonNull(T obj, String message) {
    	if (obj == null) throw new NullPointerException(message);
    	return obj;
    }
}
