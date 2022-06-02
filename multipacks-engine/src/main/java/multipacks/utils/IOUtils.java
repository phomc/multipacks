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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.CopyOption;
import java.nio.file.Files;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;

public class IOUtils {
	public static JsonElement jsonFromFile(File file) throws IOException {
		FileInputStream in = new FileInputStream(file);
		JsonParser parser = new JsonParser();
		JsonElement json = parser.parse(new InputStreamReader(in));
		in.close();
		return json;
	}

	public static void jsonToFile(JsonElement json, File file) throws IOException {
		FileWriter fw = new FileWriter(file);
		JsonWriter writer = new JsonWriter(fw);
		writer.setIndent("    ");
		new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(json, writer);
		writer.close();
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
