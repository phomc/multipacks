package multipacks.transforms.defaults.singles;

import java.io.IOException;

import com.google.gson.JsonObject;

import multipacks.bundling.BundleResult;
import multipacks.transforms.TransformPass;
import multipacks.transforms.TransformativeFileSystem;
import multipacks.utils.Selects;
import multipacks.utils.logging.AbstractMPLogger;

public class RemapTransformPass extends TransformPass {
	public String from;
	public String to;

	public RemapTransformPass(JsonObject json) {
		from = Selects.nonNull(json.get("from"), "'from' is empty").getAsString();
		to = Selects.nonNull(json.get("to"), "'to' is empty").getAsString();
	}

	@Override
	public void transform(TransformativeFileSystem fs, BundleResult result, AbstractMPLogger logger) throws IOException {
		for (String fileName : fs.ls(from)) {
			byte[] data = fs.get(from + "/" + fileName);
			fs.put(to + "/" + fileName, data);
			fs.delete(from + "/" + fileName);
		}
	}
}
