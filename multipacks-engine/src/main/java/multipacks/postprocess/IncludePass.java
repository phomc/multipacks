package multipacks.postprocess;

import java.io.IOException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import multipacks.bundling.BundleResult;
import multipacks.utils.Selects;
import multipacks.utils.logging.AbstractMPLogger;
import multipacks.vfs.Path;
import multipacks.vfs.VirtualFs;

public class IncludePass extends PostProcessPass {
	private Path file;

	public IncludePass(JsonObject config) {
		file = new Path(Selects.nonNull(config.get("file"), "'file' is empty").getAsString());
	}

	@Override
	public void apply(VirtualFs fs, BundleResult result, AbstractMPLogger logger) throws IOException {
		JsonElement json = fs.readJson(file);
		PostProcessPass.apply(json, fs, result, logger);
	}
}
