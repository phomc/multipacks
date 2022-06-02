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

/**
 * A virtual file system for transforming resources on the fly. May eats a lot of free
 * memory for big projects...
 * @author nahkd
 *
 */
public class TransformativeFileSystem {
	public final File sourceRoot;
	public final HashMap<String, Object> transformed = new HashMap<>();
	public final HashSet<String> markDelete = new HashSet<>();

	public TransformativeFileSystem(File source) {
		sourceRoot = source;
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
		return true;
	}

	public byte[] getTransformed(String path) {
		while (path.startsWith("/")) path = path.substring(1);
		while (path.endsWith("/")) path = path.substring(0, path.length() - 1);
		String[] parts = path.split("/");
		HashMap<String, Object> dir = parentDirOf(parts);
		Object file = dir.get(parts[parts.length - 1]);

		if (file instanceof byte[] bs) return bs;
		return null;
	}

	public byte[] get(String path) throws IOException {
		byte[] bs = getTransformed(path);
		if (bs != null) return bs;
		if (sourceRoot == null) return null;
		File realFile = new File(sourceRoot, path.replace('/', File.separatorChar));

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
		while (path.startsWith("/")) path = path.substring(1);
		while (path.endsWith("/")) path = path.substring(0, path.length() - 1);
		String[] parts = path.split("/");
		HashMap<String, Object> dir = parentDirOf(parts);
		Object file = dir.get(parts[parts.length - 1]);

		if (file == null || !(file instanceof byte[] bs)) {
			if (sourceRoot == null) return null;

			File realFile = new File(sourceRoot, path.replace('/', File.separatorChar));
			if (!realFile.exists()) return null;

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
			Object obj = entry.getValue();

			if (obj instanceof byte[] bs) callback.accept(new TFSListing(path, bs));
			if (obj instanceof HashMap<?, ?> childDir) forEachTransformed(path, (HashMap<String, Object>) childDir, callback);
		}
	}

	private void forEachOrignal(String parentDir, File dir, Consumer<TFSListing> callback) {
		for (File child : dir.listFiles()) {
			String path = parentDir != null? parentDir + "/" + child.getName() : child.getName();
			
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
		if (dir == null) return new String[0];
		return dir.keySet().toArray(String[]::new);
	}

	public String[] ls(String path) {
		if (sourceRoot == null) return lsTransformed(path);

		List<String> ls = new ArrayList<>();
		ls.addAll(Arrays.asList(lsTransformed(path)));

		File realDir = new File(sourceRoot, path.replace('/', File.separatorChar));
		if (!realDir.exists() || !realDir.isDirectory()) return new String[0];
		String[] realLs = realDir.list();
		if (realLs == null) return ls.toArray(String[]::new);
		ls.addAll(Arrays.asList(realLs));
		return ls.stream().filter(v -> !markDelete.contains(v)).toArray(String[]::new);
	}
}
