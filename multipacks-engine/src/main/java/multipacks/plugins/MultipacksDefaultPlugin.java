package multipacks.plugins;

import java.io.File;
import java.util.HashMap;
import java.util.function.Function;

import com.google.gson.JsonObject;

import multipacks.management.LocalRepository;
import multipacks.management.PacksRepository;
import multipacks.postprocess.CopyPass;
import multipacks.postprocess.DeletePass;
import multipacks.postprocess.IncludePass;
import multipacks.postprocess.MovePass;
import multipacks.postprocess.PostProcessPass;

public class MultipacksDefaultPlugin implements MultipacksPlugin {
	@Override
	public PacksRepository parseRepository(File root, String uri) {
		if (uri.startsWith("file:")) return new LocalRepository(new File(uri.substring(5)));
		return null;
	}

	@Override
	public void registerPostProcessPasses(HashMap<String, Function<JsonObject, PostProcessPass>> reg) {
		reg.put("include", IncludePass::new);
		reg.put("move", MovePass::new);
		reg.put("delete", DeletePass::new);
		reg.put("copy", CopyPass::new);
	}
}
