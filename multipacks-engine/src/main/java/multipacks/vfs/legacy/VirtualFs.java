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
package multipacks.vfs.legacy;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import multipacks.utils.Blob;
import multipacks.vfs.Path;

/**
 * Multipacks virtual file system, primarily used for performing data transformation without tampering the underlying
 * file, or used for dynamically generating a temporary pack.
 * @author nahkd
 * @deprecated Legacy virtual file system implementation.
 *
 */
@Deprecated
public class VirtualFs {
	public final File root;
	public final HashMap<Path, byte[]> emulatedFs = new HashMap<>();
	public final HashSet<Path> emulatedDeletes = new HashSet<>();

	/**
	 * Construct a new virtual file system.
	 * @param root Root directory. If this value is null, VFS will not attempt to read underlying file system.
	 */
	public VirtualFs(File root) {
		this.root = root;
	}

	public boolean isExists(Path file) {
		if (file.isAccessingParent()) return false;
		if (emulatedFs.containsKey(file)) return true;
		if (emulatedDeletes.contains(file)) return false;
		if (root != null && file.joinWith(root).exists()) return true;
		return false;
	}

	/**
	 * Perform "soft delete". Soft delete operations does not actually deletes the underlying file, but mark it
	 * as deleted inside virtual file system. The only thing that this removes is the emulated FS, which is the
	 * key <=> value pair inside {@link #emulatedFs} map.
	 */
	public boolean delete(Path file) {
		if (file.isAccessingParent()) return false;
		if (emulatedFs.containsKey(file)) emulatedFs.remove(file);
		if (root != null && !emulatedDeletes.contains(file)) { emulatedDeletes.add(file); return true; }
		return false;
	}

	/**
	 * Get stream for given file inside this virtual file system. Always close the stream when you are done
	 * reading it.
	 */
	public InputStream getStream(Path file) throws IOException {
		if (file.isAccessingParent()) return null;
		if (emulatedFs.containsKey(file)) return new ByteArrayInputStream(emulatedFs.get(file));
		if (root == null) return null;
		if (emulatedDeletes.contains(file)) return null;
		if (!file.joinWith(root).exists()) return null;
		return new FileInputStream(file.joinWith(root));
	}

	public byte[] read(Path file) throws IOException {
		if (file.isAccessingParent()) return null;
		if (emulatedFs.containsKey(file)) {
			byte[] from = emulatedFs.get(file);
			byte[] to = new byte[from.length];
			System.arraycopy(from, 0, to, 0, to.length);
			return to;
		}

		InputStream s = getStream(file);
		if (s == null) return null;
		byte[] bs = s.readAllBytes();
		s.close();
		return bs;
	}

	public Blob readBlob(Path file) throws IOException { return new Blob(Blob.findMimeType(file.fileName()), read(file)); }
	public String readText(Path file) throws IOException { return new String(read(file), StandardCharsets.UTF_8); }
	public JsonElement readJson(Path file) throws IOException { return new JsonParser().parse(readText(file)); }

	/**
	 * Write a file to this virtual file system. This does not overwrite to underlying FS.
	 */
	public void write(Path file, byte[] bs) {
		if (file.isAccessingParent()) throw new IllegalAccessError("Attempting to access parent directory: " + file);
		if (root != null && emulatedDeletes.contains(file)) emulatedDeletes.remove(file);
		emulatedFs.put(file, bs);
	}

	public void write(Path file, Blob blob) { write(file, blob.data); }
	public void writeText(Path file, String text) { write(file, text.getBytes(StandardCharsets.UTF_8)); }
	public void writeJson(Path file, JsonElement json) { writeText(file, new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(json)); }

	/**
	 * List all paths from both virtual file system and underlying file system (if the path is not marked as deleted).
	 */
	public Path[] ls(Path dir) {
		List<Path> l = new ArrayList<>();
		for (Path p : emulatedFs.keySet()) if (dir.isParentOf(p)) l.add(p);
		if (emulatedFs.containsKey(dir)) {
			// Not a directory!
			return new Path[] { dir };
		}

		if (root != null) {
			File underlyingDir = dir.joinWith(root);
			if (underlyingDir.exists()) lsExpand(underlyingDir, dir, l);
		}

		return l.toArray(Path[]::new);
	}

	private void lsExpand(File f, Path current, List<Path> ps) {
		if (emulatedDeletes.contains(current)) return;
		if (f.isDirectory()) for (String name : f.list()) {
			Path pTo = Path.join(current, new Path(name));
			if (!ps.contains(pTo)) lsExpand(new File(f, name), pTo, ps);
		}
		else ps.add(current);
	}
}
