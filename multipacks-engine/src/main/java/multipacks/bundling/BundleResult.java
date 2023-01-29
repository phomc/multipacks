/*
 * Copyright (c) 2022-2023 PhoMC
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
package multipacks.bundling;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.FileTime;
import java.util.Collections;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import multipacks.modifier.Modifier;
import multipacks.utils.ResourcePath;
import multipacks.vfs.Vfs;

/**
 * @author nahkd
 *
 */
public class BundleResult {
	public final Vfs contents;
	protected Map<ResourcePath, Modifier<?, ?>> modifiers;

	public BundleResult(Vfs contents) {
		this.contents = contents;
	}

	public Map<ResourcePath, Modifier<?, ?>> getModifiers() {
		return Collections.unmodifiableMap(modifiers);
	}

	public void writeZipData(OutputStream stream) throws IOException {
		ZipOutputStream zip = new ZipOutputStream(stream, StandardCharsets.UTF_8);
		FileTime bundleTime = FileTime.fromMillis(System.currentTimeMillis());
		vfsAddZipEntry(contents, bundleTime, zip);
		zip.finish();
	}

	private void vfsAddZipEntry(Vfs file, FileTime bundleTime, ZipOutputStream zip) throws IOException {
		if (file.isDir()) {
			for (Vfs child : file.listFiles()) vfsAddZipEntry(child, bundleTime, zip);
		} else {
			ZipEntry entry = new ZipEntry(file.getPathFromRoot().toString())
					.setCreationTime(bundleTime)
					.setLastModifiedTime(bundleTime);
			zip.putNextEntry(entry);
			zip.write(file.getContent());
			zip.closeEntry();
		}
	}
}
