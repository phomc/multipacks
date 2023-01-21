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
package multipacks.utils.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;

import multipacks.vfs.Vfs;

public class IOUtils {
	public static JsonElement jsonFromStream(InputStream stream) throws IOException {
		JsonParser parser = new JsonParser();
		JsonElement json = parser.parse(new InputStreamReader(stream));
		return json;
	}

	public static JsonElement jsonFromFile(File file) throws IOException {
		try (FileInputStream in = new FileInputStream(file)) {
			return jsonFromStream(in);
		}
	}

	public static JsonElement jsonFromPath(Path path) throws IOException {
		try (InputStream in = Files.newInputStream(path, StandardOpenOption.READ)) {
			return jsonFromStream(in);
		}
	}

	public static JsonElement jsonFromVfs(Vfs file) throws IOException {
		try (InputStream in = file.getInputStream()) {
			return jsonFromStream(in);
		}
	}

	public static void jsonToStream(JsonElement json, OutputStream stream) throws IOException {
		OutputStreamWriter sw = new OutputStreamWriter(stream);
		JsonWriter writer = new JsonWriter(sw);
		writer.setIndent("    ");
		new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(json, writer);
		writer.flush();
	}

	public static void jsonToFile(JsonElement json, File file) throws IOException {
		try (FileOutputStream out = new FileOutputStream(file)) {
			jsonToStream(json, out);
		}
	}

	public static void jsonToVfs(JsonElement json, Vfs file) throws IOException {
		try (OutputStream out = file.getOutputStream()) {
			jsonToStream(json, out);
		}
	}

	public static void copyRecursive(File from, File to, CopyOption... options) throws IOException {
		if (from.isDirectory()) {
			to.mkdirs();

			for (String childName : from.list()) {
				File fromFile = new File(from, childName);
				File toFile = new File(to, childName);
				copyRecursive(fromFile, toFile);
			}
		} else {
			Files.copy(from.toPath(), to.toPath(), options);
		}
	}
}
