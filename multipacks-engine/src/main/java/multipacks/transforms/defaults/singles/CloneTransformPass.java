package multipacks.transforms.defaults.singles;

import java.io.IOException;

import com.google.gson.JsonObject;

import multipacks.bundling.BundleResult;
import multipacks.transforms.TransformPass;
import multipacks.transforms.TransformativeFileSystem;
import multipacks.utils.Selects;
import multipacks.utils.logging.AbstractMPLogger;

public class CloneTransformPass extends TransformPass {
	private String from;
	private String to;

	public CloneTransformPass(JsonObject json) {
		from = Selects.nonNull(json.get("from"), "'from' is empty").getAsString();
		to = Selects.nonNull(json.get("to"), "'to' is empty").getAsString();
	}

	@Override
	public void transform(TransformativeFileSystem fs, BundleResult result, AbstractMPLogger logger) throws IOException {
		for (String fileName : fs.ls(from)) {
			byte[] data = fs.get(from + "/" + fileName);
			if (data == null) continue;
			fs.put(to + "/" + fileName, data);
		}
	}

}
