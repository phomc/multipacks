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
package multipacks.bundling;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.FileTime;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import multipacks.packs.Pack;
import multipacks.vfs.Vfs;

/**
 * @author nahkd
 *
 */
public class Bundler {
	public Vfs bundle(Pack pack) {
		Vfs content = pack.createVfs(true);
		// TODO: apply dependencies
		return content;
	}

	public void bundleToStream(Pack pack, OutputStream stream) throws IOException {
		ZipOutputStream zip = new ZipOutputStream(stream, StandardCharsets.UTF_8);
		FileTime bundleTime = FileTime.fromMillis(System.currentTimeMillis());
		Vfs content = bundle(pack);
		vfsAddZipEntry(content, bundleTime, zip);
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
