package multipacks.transforms;

import java.io.IOException;
import java.util.HashMap;
import java.util.function.Function;

import com.google.gson.JsonObject;

import multipacks.bundling.BundleResult;
import multipacks.transforms.defaults.fonticons.FontIconsTransformPass;
import multipacks.utils.Selects;
import multipacks.utils.logging.AbstractMPLogger;

public abstract class TransformPass {
	/**
	 * Transform files inside virtual file system into another set of files. An example would
	 * be composing multiple font icons into a single font.
	 */
	public abstract void transform(TransformativeFileSystem fs, BundleResult result, AbstractMPLogger logger) throws IOException;

	public static final HashMap<String, Function<JsonObject, TransformPass>> REGISTRY = new HashMap<>();

	public static TransformPass fromJson(JsonObject json) {
		String type = Selects.nonNull(json.get("type"), "'type' is empty").getAsString();
		Function<JsonObject, TransformPass> ctor = REGISTRY.get(type);
		if (ctor == null) return null;
		return ctor.apply(json);
	}

	static {
		REGISTRY.put("font-icons", FontIconsTransformPass::new);
	}
}
