package multipacks.transforms.defaults.debug;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.google.gson.JsonObject;

import multipacks.bundling.BundleResult;
import multipacks.transforms.TransformPass;
import multipacks.transforms.TransformativeFileSystem;
import multipacks.utils.Selects;
import multipacks.utils.logging.AbstractMPLogger;

public class PathTestTransformPass extends TransformPass {
	private String test;

	public PathTestTransformPass(JsonObject json) {
		test = Selects.nonNull(json.get("test"), "'test' is empty").getAsString();
	}

	@Override
	public void transform(TransformativeFileSystem fs, BundleResult result, AbstractMPLogger logger) throws IOException {
		try {
			byte[] data = fs.get(test);
			logger.info("File test: '" + test + "' " + (data != null? "FOUND" : "FAILED"));
		} catch (FileNotFoundException e) {
			logger.info("FileNotFoundException thrown...");
		}
	}
}
