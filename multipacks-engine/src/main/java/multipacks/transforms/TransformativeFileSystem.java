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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A virtual file system for transforming resources on the fly. May eats a lot of free
 * memory for big projects...
 * @author nahkd
 *
 */
public class TransformativeFileSystem {
	public final File sourceRoot;
	public final HashMap<String, Object> transformed = new HashMap<>();
	private final HashSet<String> markDelete = new HashSet<>();

	public TransformativeFileSystem(File source) {
		sourceRoot = source;
	}

	private boolean canAccess(File file) {
		try {
			return file.getCanonicalPath().startsWith(sourceRoot.getCanonicalPath());
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	private HashMap<String, Object> parentDirOf(String[] parts) {
		HashMap<String, Object> dir = transformed;

		for (int i = 0; i < parts.length - 1; i++) {
			String part = parts[i];
			Object obj = dir.get(part);

			if (obj == null) {
				HashMap<String, Object> newDir = new HashMap<>();
				dir.put(part, newDir);
				dir = newDir;
			} else if (obj instanceof HashMap<?, ?> child) {
				dir = (HashMap<String, Object>) child;
			} else {
				return null;
			}
		}

		return dir;
	}

	@SuppressWarnings("unchecked")
	private HashMap<String, Object> dirOf(String[] parts) {
		HashMap<String, Object> parent = parentDirOf(parts);
		Object obj = parent.get(parts[parts.length - 1]);
		if (obj == null || !(obj instanceof HashMap<?, ?> hm)) return null;
		return (HashMap<String, Object>) hm;
	}

	/**
	 * Put processed data to this virtual file system.
	 */
	public boolean put(String path, byte[] data) {
		while (path.startsWith("/")) path = path.substring(1);
		while (path.endsWith("/")) path = path.substring(0, path.length() - 1);
		String[] parts = path.split("/");
		HashMap<String, Object> dir = parentDirOf(parts);
		dir.put(parts[parts.length - 1], data);

		markDelete.remove(path);
		String unmarkParent = null;

		for (int i = 0; i < parts.length - 1; i++) {
			unmarkParent = unmarkParent == null? parts[i] : unmarkParent + "/" + parts[i];
			markDelete.remove(unmarkParent);
		}

		return true;
	}

	public byte[] getTransformed(String path) {
		if (isDeleted(path)) return null;

		while (path.startsWith("/")) path = path.substring(1);
		while (path.endsWith("/")) path = path.substring(0, path.length() - 1);
		String[] parts = path.split("/");
		HashMap<String, Object> dir = parentDirOf(parts);
		Object file = dir.get(parts[parts.length - 1]);

		if (file instanceof byte[] bs) return bs;
		return null;
	}

	public byte[] get(String path) throws IOException {
		if (isDeleted(path)) return null;

		byte[] bs = getTransformed(path);
		if (bs != null) return bs;
		if (sourceRoot == null) return null;
		File realFile = new File(sourceRoot, path.replace('/', File.separatorChar));
		if (!canAccess(realFile)) return null;

		InputStream in = new FileInputStream(realFile);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		in.transferTo(out);
		in.close();

		return out.toByteArray();
	}

	/**
	 * Open a new stream to read.
	 * @param path Path to file.
	 * @return null if the file doesn't exists in both virtual FS and actual FS, or a stream from
	 * either VFS or actual FS. The output stream should always be closed once you've done with
	 * it.
	 */
	public InputStream openRead(String path) {
		if (isDeleted(path)) return null;

		while (path.startsWith("/")) path = path.substring(1);
		while (path.endsWith("/")) path = path.substring(0, path.length() - 1);
		String[] parts = path.split("/");
		HashMap<String, Object> dir = parentDirOf(parts);
		Object file = dir.get(parts[parts.length - 1]);

		if (file == null || !(file instanceof byte[] bs)) {
			if (sourceRoot == null) return null;

			File realFile = new File(sourceRoot, path.replace('/', File.separatorChar));
			if (!realFile.exists()) return null;
			if (!canAccess(realFile)) return null;

			try {
				InputStream in = new FileInputStream(realFile);
				return in;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return null;
			}
		} else {
			ByteArrayInputStream in = new ByteArrayInputStream(bs);
			return in;
		}
	}

	@SuppressWarnings("unchecked")
	private void forEachTransformed(String parentDir, HashMap<String, Object> dir, Consumer<TFSListing> callback) {
		for (Entry<String, Object> entry : dir.entrySet()) {
			String path = parentDir != null? parentDir + "/" + entry.getKey() : entry.getKey();
			if (isDeleted(path)) continue;

			Object obj = entry.getValue();

			if (obj instanceof byte[] bs) callback.accept(new TFSListing(path, bs));
			if (obj instanceof HashMap<?, ?> childDir) forEachTransformed(path, (HashMap<String, Object>) childDir, callback);
		}
	}

	private void forEachOrignal(String parentDir, File dir, Consumer<TFSListing> callback) {
		for (File child : dir.listFiles()) {
			if (!canAccess(child)) return;

			String path = parentDir != null? parentDir + "/" + child.getName() : child.getName();
			if (isDeleted(path)) continue;
			
			if (child.isDirectory()) forEachOrignal(path, child, callback);
			else callback.accept(new TFSListing(path, child));
		}
	}

	public void forEach(Consumer<TFSListing> callback) {
		HashSet<String> found = new HashSet<>();

		forEachTransformed(null, transformed, t -> {
			found.add(t.path);
			callback.accept(t);
		});

		if (sourceRoot != null) forEachOrignal(null, sourceRoot, t -> {
			if (found.contains(t.path)) return;
			callback.accept(t);
		});
	}

	public String[] lsTransformed(String path) {
		while (path.startsWith("/")) path = path.substring(1);
		while (path.endsWith("/")) path = path.substring(0, path.length() - 1);
		String[] parts = path.split("/");
		HashMap<String, Object> dir = dirOf(parts);

		if (dir == null) {
			HashMap<String, Object> parent = parentDirOf(parts);
			if (parent.containsKey(parts[parts.length - 1])) return new String[] { "" };
			return new String[0];
		}

		return dir.keySet().toArray(String[]::new);
	}

	/**
	 * Get all file names inside a directory. If the file at given path is not a directory, this method
	 * will returns an array with a single empty string.
	 */
	public String[] ls(String path) {
		if (sourceRoot == null) return lsTransformed(path);

		List<String> ls = new ArrayList<>();
		ls.addAll(Arrays.asList(lsTransformed(path)));

		File realDir = new File(sourceRoot, path.replace('/', File.separatorChar));
		if (!realDir.exists()) return new String[0];
		if (!canAccess(realDir)) return new String[0];
		if (!realDir.isDirectory()) return new String[] { "" };

		String[] realLs = realDir.list();
		if (realLs == null) return ls.toArray(String[]::new);
		ls.addAll(Arrays.asList(realLs));
		return ls.stream().filter(v -> !markDelete.contains(v)).toArray(String[]::new);
	}

	public String[] lsFullPath(String path) {
		return Stream.of(ls(path)).map(v -> {
			if (v.length() == 0) return path;
			return path + "/" + v;
		}).toArray(String[]::new);
	}

	/**
	 * Mark the file as deleted. This doesn't affect to user's pack data.
	 */
	public void delete(String path) {
		while (path.startsWith("/")) path = path.substring(1);
		while (path.endsWith("/")) path = path.substring(0, path.length() - 1);
		for (String p : lsFullPath(path)) markDelete.add(p);
	}

	public boolean isDeleted(String path) {
		while (path.startsWith("/")) path = path.substring(1);
		while (path.endsWith("/")) path = path.substring(0, path.length() - 1);
		return markDelete.contains(path);
	}
}
