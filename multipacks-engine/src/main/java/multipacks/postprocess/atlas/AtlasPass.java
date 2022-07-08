package multipacks.postprocess.atlas;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import multipacks.bundling.BundleResult;
import multipacks.postprocess.PostProcessPass;
import multipacks.utils.Selects;
import multipacks.utils.logging.AbstractMPLogger;
import multipacks.vfs.Path;
import multipacks.vfs.VirtualFs;

public class AtlasPass extends PostProcessPass {
	private Path from, to;
	private AtlasTemplate[] templates;

	public AtlasPass(JsonObject config) {
		from = new Path(Selects.nonNull(config.get("from"), "'from' is empty").getAsString());
		to = new Path(Selects.nonNull(config.get("to"), "'to' is empty").getAsString());

		JsonObject templatesMap = Selects.nonNull(config.get("templates"), "'templates' is empty").getAsJsonObject();
		templates = new AtlasTemplate[templatesMap.size()];
		int pointer = 0;

		for (Entry<String, JsonElement> e : templatesMap.entrySet()) {
			templates[pointer++] = new AtlasTemplate(e.getKey(), e.getValue().getAsJsonObject());
		}
	}

	@Override
	public void apply(VirtualFs fs, BundleResult result, AbstractMPLogger logger) throws IOException {
		for (Path src : fs.ls(from)) {
			String[] split = src.fileName().split("\\.");
			Path base = split.length > 1? new Path(src.toString().substring(0, src.toString().length() - split[split.length - 1].length() - 1)) : src;

			InputStream in = fs.getStream(src);
			BufferedImage srcImg = ImageIO.read(in);
			in.close();

			for (AtlasTemplate t : templates) {
				Path dest = to.join(base.fileName() + t.suffix + ".png");
				BufferedImage dstImg = t.getFrom(srcImg);

				ByteArrayOutputStream o = new ByteArrayOutputStream();
				ImageIO.write(dstImg, "PNG", o);
				o.flush();
				fs.write(dest, o.toByteArray());
			}
		}

		fs.delete(from);
	}
}
