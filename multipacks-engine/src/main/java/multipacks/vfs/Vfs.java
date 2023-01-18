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
package multipacks.vfs;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import multipacks.utils.Messages;

/**
 * @author nahkd
 *
 */
public class Vfs {
	private Vfs parent;
	private String name;
	protected java.nio.file.Path nativePath;
	protected byte[] content;
	private HashMap<String, Vfs> directoryContent;
	private HashSet<String> removedChildren;

	protected Vfs(Vfs parent, String name) {
		this.parent = parent;
		this.name = name;
	}

	public static Vfs createRoot(java.nio.file.Path root) {
		if (!Files.isDirectory(root)) throw new IllegalArgumentException("Native path is not a directory");

		Vfs vfs = new Vfs(null, null);
		vfs.nativePath = root;
		return vfs.initAsDir();
	}

	public static Vfs createRoot(File root) {
		return createRoot(root.toPath());
	}

	public static Vfs createVirtualRoot() {
		return new Vfs(null, null).initAsDir();
	}

	private Vfs initAsDir() {
		directoryContent = new HashMap<>();
		removedChildren = new HashSet<>();
		return this;
	}

	public Vfs getRoot() {
		if (parent == null) return this;
		return parent.getRoot();
	}

	public Vfs[] listFiles() {
		if (directoryContent == null) throw new IllegalArgumentException(Messages.FILE_ISNOTDIR);
		List<Vfs> files = new ArrayList<>();

		if (nativePath != null) {
			try {
				for (java.nio.file.Path child : Files.list(nativePath).toList()) {
					String childName = child.getFileName().toString();
					if (removedChildren.contains(childName)) continue;
					if (directoryContent.containsKey(childName)) continue;
					files.add(initChildFile(child));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		for (Vfs child : directoryContent.values()) {
			if (files.contains(child)) continue;
			files.add(child);
		}

		return files.toArray(Vfs[]::new);
	}

	private Vfs initChildFile(java.nio.file.Path child) {
		Vfs f = new Vfs(this, child.getFileName().toString());
		f.nativePath = child;
		if (Files.isDirectory(child)) f.initAsDir();
		directoryContent.put(child.getFileName().toString(), f);
		return f;
	}

	public Vfs get(String name) {
		if (name.equals(".")) return this;
		if (name.equals("..")) return parent != null? parent : this;

		if (directoryContent == null) throw new IllegalArgumentException(Messages.FILE_ISNOTDIR);

		if (removedChildren.contains(name)) return null;
		Vfs child = directoryContent.get(name);
		if (child != null) return child;
		if (nativePath == null) return null;

		// File physicalChild = new File(attachedFile, name);
		java.nio.file.Path physicalChild = nativePath.resolve(name);
		if (Files.exists(physicalChild)) return initChildFile(physicalChild);
		return null;
	}

	public Vfs get(Path path) {
		Vfs stack = this;

		for (String s : path.getSegments()) {
			stack = stack.get(s);
			if (stack == null) return null;
		}

		return stack;
	}

	public String getName() {
		return name;
	}

	public boolean isDir() {
		return directoryContent != null;
	}

	public Path getPathFromRoot() {
		if (this.parent == null) return Path.ROOT;
		return this.parent.getPathFromRoot().join(name);
	}

	public Vfs getParent() {
		return parent;
	}

	public boolean delete(String name) {
		if (removedChildren.contains(name)) return false;
		directoryContent.remove(name);
		removedChildren.add(name);
		return true;
	}

	public Vfs mkdir(String name) {
		if (directoryContent == null) throw new IllegalArgumentException(Messages.FILE_ISNOTDIR);

		Vfs file = get(name);
		if (file != null && file.isDir()) return file;

		file = new Vfs(this, name).initAsDir();
		directoryContent.put(name, file);
		removedChildren.remove(name);
		return file;
	}

	public Vfs touch(String name) {
		if (directoryContent == null) throw new IllegalArgumentException(Messages.FILE_ISNOTDIR);

		Vfs file = get(name);
		if (file != null && !file.isDir()) return file;

		file = new Vfs(this, name);
		file.content = new byte[0];
		directoryContent.put(name, file);
		removedChildren.remove(name);
		return file;
	}

	public InputStream getInputStream() {
		if (directoryContent != null) throw new IllegalArgumentException(Messages.FILE_ISDIR);
		if (nativePath != null) {
			try {
				return Files.newInputStream(nativePath, StandardOpenOption.READ);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return new ByteArrayInputStream(content);
	}

	public OutputStream getOutputStream() {
		if (directoryContent != null) throw new IllegalArgumentException(Messages.FILE_ISDIR);
		return new VfsOutputStream(this);
	}

	public byte[] getContent() {
		if (content != null) return content;

		try (InputStream input = getInputStream()) {
			return input.readAllBytes();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString() {
		return "vfs:/" + getPathFromRoot() + (nativePath != null? " (physical)" : "");
	}

	public static void copyRecursive(Vfs from, Vfs to, boolean override) {
		if (!to.isDir()) throw new IllegalArgumentException(Messages.FILE_ISNOTDIR);

		for (Vfs fromFile : from.listFiles()) {
			Vfs toFile = to.get(fromFile.name);

			if (fromFile.isDir()) {
				if (!toFile.isDir()) {
					to.delete(fromFile.name);
					toFile = to.mkdir(fromFile.name);
				}

				copyRecursive(fromFile, toFile, override);
			} else {
				toFile = to.touch(fromFile.name);
				toFile.content = fromFile.getContent();
			}
		}
	}
}
