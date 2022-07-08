package multipacks.postprocess;

import java.io.IOException;

import com.google.gson.JsonObject;

import multipacks.bundling.BundleResult;
import multipacks.utils.Selects;
import multipacks.utils.logging.AbstractMPLogger;
import multipacks.vfs.Path;
import multipacks.vfs.VirtualFs;

public class MovePass extends PostProcessPass {
	private Path from;
	private Path to;

	public MovePass(JsonObject config) {
		from = new Path(Selects.nonNull(config.get("from"), "'from' is empty").getAsString());
		to = new Path(Selects.nonNull(config.get("to"), "'to' is empty").getAsString());
	}

	@Override
	public void apply(VirtualFs fs, BundleResult result, AbstractMPLogger logger) throws IOException {
		for (Path pFrom : fs.ls(from)) {
			String tail = pFrom.toString().substring(from.toString().length() + 1);
			Path pTo = Path.join(to, new Path(tail));
			byte[] bs = fs.read(pFrom);
			fs.write(pTo, bs);
			fs.delete(pFrom);
		}
	}
}
