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
package multipacks.transforms;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TFSListing {
	public final String path;
	public final File actualFile;
	public final byte[] transformed;

	public TFSListing(String path, File actualFile) {
		this.path = path;
		this.actualFile = actualFile;
		this.transformed = null;
	}

	public TFSListing(String path, byte[] val) {
		this.path = path;
		this.actualFile = null;
		this.transformed = val;
	}

	public byte[] getAsBytes() throws IOException {
		if (transformed != null) return transformed;

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		InputStream in = new FileInputStream(actualFile);
		in.transferTo(out);
		in.close();
		return out.toByteArray();
	}
}
