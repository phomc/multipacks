package multipacks.transforms.defaults.multisprites;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import multipacks.bundling.BundleResult;
import multipacks.transforms.TransformPass;
import multipacks.transforms.TransformativeFileSystem;
import multipacks.utils.Selects;
import multipacks.utils.logging.AbstractMPLogger;

public class MultiSpritesTransformPass extends TransformPass {
	private String from;
	private String to;
	private SpriteTemplate[] templates;

	public MultiSpritesTransformPass(JsonObject json) {
		from = Selects.nonNull(json.get("from"), "'from' is empty").getAsString();
		to = Selects.nonNull(json.get("to"), "'to' is empty").getAsString();
		JsonObject templatesMap = Selects.nonNull(json.get("templates"), "'templates' is empty").getAsJsonObject();
		templates = new SpriteTemplate[templatesMap.size()];
		int pointer = 0;

		for (Entry<String, JsonElement> e : templatesMap.entrySet()) {
			templates[pointer++] = new SpriteTemplate(e.getKey(), e.getValue().getAsJsonObject());
		}
	}

	@Override
	public void transform(TransformativeFileSystem fs, BundleResult result, AbstractMPLogger logger) throws IOException {
		for (String pathFrom : fs.lsFullPath(from)) {
			String[] split = pathFrom.split("/");
			String outFileNamePrefix = split[split.length - 1];
			while (outFileNamePrefix.endsWith(".png")) outFileNamePrefix = outFileNamePrefix.substring(0, outFileNamePrefix.length() - 4);
			String pathTo = to + "/" + outFileNamePrefix;

			InputStream in = fs.openRead(pathFrom);
			BufferedImage source = ImageIO.read(in);

			for (SpriteTemplate template : templates) {
				String pathSlicedTo = pathTo + template.suffix + ".png";
				BufferedImage dest = template.getFrom(source);
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				ImageIO.write(dest, "PNG", out);
				out.flush();
				fs.put(pathSlicedTo, out.toByteArray());
			}

			fs.markDelete.add(pathFrom);
		}
	}
}
