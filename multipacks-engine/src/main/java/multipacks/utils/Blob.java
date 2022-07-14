/*
 * Copyright (c) 2020-2022 MangoPlex
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package multipacks.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.imageio.ImageIO;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Blob stores binary data, along with its MIME type. Quite similar to Web API Blob.
 * @author nahkd
 *
 */
public class Blob {
	public final String mimeType;
	public final byte[] data;

	public Blob(String mimeType, byte[] data) {
		this.mimeType = mimeType;
		this.data = data;
	}

	@Override
	public String toString() {
		return "Blob(" + mimeType + ", size = " + data.length + ")";
	}

	public static Blob fromDataURL(String url) {
		// data:image/png;base64,iVBORw0KGgoAAA
		// data:text/javascript,alert(1)
		if (!url.startsWith("data:")) return null;
		String[] split = url.split(",", 2);
		String mime = split[0].substring("data:".length());
		String dataText = split[1];

		byte[] data;
		if (mime.contains(";")) {
			String encodingType = mime.split(";")[1];
			data = switch (encodingType.toLowerCase()) {
			case "base64" -> Base64.getDecoder().decode(dataText);
			default -> null;
			};
			if (data == null) return null;
			mime = mime.substring(0, mime.length() - encodingType.length() - 1);
		} else data = dataText.getBytes(StandardCharsets.UTF_8);

		return new Blob(mime, data);
	}

	public String toDataURL() {
		if (mimeType.startsWith("text/")) return "data:" + mimeType + "," + new String(data, StandardCharsets.UTF_8);
		return "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(data);
	}

	public static Blob fromImage(BufferedImage image) throws IOException {
		ByteArrayOutputStream sOut = new ByteArrayOutputStream();
		ImageIO.write(image, "PNG", sOut);
		return new Blob("image/png", sOut.toByteArray());
	}

	public BufferedImage toImage() throws IOException {
		ByteArrayInputStream sIn = new ByteArrayInputStream(data);
		return ImageIO.read(sIn);
	}

	public static Blob fromText(String mime, String text) {
		return new Blob(mime, text.getBytes(StandardCharsets.UTF_8));
	}

	public static Blob fromText(String text) {
		return fromText("text/plain", text);
	}

	public String toText() {
		return new String(data, StandardCharsets.UTF_8);
	}

	public static Blob fromJson(JsonElement json) {
		return fromText("application/json", new Gson().toJson(json));
	}

	public JsonElement toJson() {
		return new JsonParser().parse(toText());
	}

	/**
	 * Find MIME type from given file name. This method is small enough to only support types that is commonly used
	 * in resources pack and data pack. Unknown file type will returns "application/octet-stream".
	 */
	public static String findMimeType(String fileName) {
		String[] split = fileName.split(".");
		if (split.length == 1) return "application/octet-stream";

		return switch (split[split.length - 1]) {
		case ".json" -> "application/json";
		case ".png" -> "image/png";
		case ".yml" -> "text/yaml";
		default -> "application/octet-stream";
		};
	}

	public Blob join(Blob... blobs) {
		int bytesSum = this.data.length;
		for (Blob b : blobs) bytesSum += b.data.length;
		byte[] bs = new byte[bytesSum];
		System.arraycopy(this.data, 0, bs, 0, this.data.length);
		int ptr = this.data.length;

		for (Blob b : blobs) {
			System.arraycopy(b.data, 0, bs, ptr, b.data.length);
			ptr += b.data.length;
		}

		return new Blob(mimeType, bs);
	}
}
