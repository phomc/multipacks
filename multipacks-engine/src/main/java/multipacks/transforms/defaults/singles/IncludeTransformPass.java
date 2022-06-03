package multipacks.transforms.defaults.singles;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import multipacks.bundling.BundleResult;
import multipacks.transforms.TransformFailException;
import multipacks.transforms.TransformPass;
import multipacks.transforms.TransformativeFileSystem;
import multipacks.utils.Selects;
import multipacks.utils.logging.AbstractMPLogger;

public class IncludeTransformPass extends TransformPass {
	private String include;

	public IncludeTransformPass(JsonObject json) {
		include = Selects.nonNull(json.get("include"), "'include' is empty").getAsString();
	}

	@Override
	public void transform(TransformativeFileSystem fs, BundleResult result, AbstractMPLogger logger) throws IOException {
		InputStream stream = Selects.nonNull(fs.openRead(include), "File doesn't exists: " + include);
		JsonElement json = new JsonParser().parse(new InputStreamReader(stream));
		stream.close();

		if (!json.isJsonArray()) throw new TransformFailException("Invalid JSON type (Must be an array)");

		JsonArray arr = json.getAsJsonArray();
		TransformPass[] passes = new TransformPass[arr.size()];

		for (int i = 0; i < passes.length; i++) passes[i] = TransformPass.fromJson(arr.get(i).getAsJsonObject());
		for (TransformPass pass : passes) pass.transform(fs, result, logger);
	}
}
