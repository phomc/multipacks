package multipacks.transforms.defaults.singles;

import java.io.IOException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import multipacks.bundling.BundleResult;
import multipacks.transforms.TransformPass;
import multipacks.transforms.TransformativeFileSystem;
import multipacks.utils.Selects;
import multipacks.utils.logging.AbstractMPLogger;

public class DeleteTransformPass extends TransformPass {
	private JsonArray target;

	public DeleteTransformPass(JsonObject json) {
		target = Selects.nonNull(json.get("target"), "'target' is empty").getAsJsonArray();
	}

	@Override
	public void transform(TransformativeFileSystem fs, BundleResult result, AbstractMPLogger logger) throws IOException {
		for (int i = 0; i < target.size(); i++) fs.delete(target.get(i).getAsString());
	}
}
