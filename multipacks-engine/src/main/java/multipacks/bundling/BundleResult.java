package multipacks.bundling;

import java.util.HashMap;
import java.util.function.Supplier;

public class BundleResult {
	private final HashMap<Class<?>, Object> transformResults = new HashMap<>();

	@SuppressWarnings("unchecked")
	public <T> T getOrCreate(Class<T> clazz, Supplier<T> creator) {
		Object obj = transformResults.get(clazz);

		if (obj == null) {
			obj = creator.get();
			transformResults.put(clazz, obj);
		}

		return (T) obj;
	}
}
