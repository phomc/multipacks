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
package multipacks.repository;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Stream;

import multipacks.packs.LocalPack;
import multipacks.packs.Pack;
import multipacks.packs.meta.PackIdentifier;
import multipacks.packs.meta.PackInfo;
import multipacks.repository.query.PackQuery;
import multipacks.utils.Messages;
import multipacks.versioning.Version;

/**
 * The local packs repository for Multipacks. Multipacks uses repositories system to build packs with dependencies.
 * You can declare dependencies by adding pack query string to {@code dependencies} field inside {@code multipacks.index.json}.
 * @author nahkd
 *
 */
public class LocalRepository implements AuthorizedRepository {
	public final Path repositoryRoot;

	public LocalRepository(Path repositoryRoot) {
		this.repositoryRoot = repositoryRoot;
	}

	public static LocalRepository fromClassLoader(ClassLoader clsLoader, String pathToRepo) {
		try {
			URI uri = clsLoader.getResource(pathToRepo).toURI();
			if (uri.getScheme().equalsIgnoreCase("jar")) {
				// We are loading from JAR
				FileSystem fs;
				for (FileSystemProvider provider : FileSystemProvider.installedProviders()) {
					if (provider.getScheme().equalsIgnoreCase("jar")) {
						try {
							fs = provider.getFileSystem(uri);
						} catch (FileSystemNotFoundException e) {
							// File system not found, create a new one
							try {
								fs = provider.newFileSystem(uri, Collections.emptyMap());
							} catch (IOException e1) {
								throw new RuntimeException("An error occured", e1);
							}
						}

						return new LocalRepository(fs.getPath(pathToRepo));
					}
				}
			}

			return new LocalRepository(Path.of(uri));
		} catch (URISyntaxException e) {
			// It SHOULD NOT throw URISyntaxException
			throw new RuntimeException(Messages.INTERNAL_ERROR, e);
		}
	}

	@Override
	public CompletableFuture<Collection<PackIdentifier>> search(PackQuery query) {
		try {
			return CompletableFuture.completedFuture(Files.list(repositoryRoot).flatMap(packPath -> {
				String name = packPath.getFileName().toString();
				try {
					return Files.list(packPath).filter(versionPath -> {
						if (query == null) return true;

						Version version = new Version(versionPath.getFileName().toString());
						return query.matches(new PackInfo(name, version));
					}).map(v -> new PackIdentifier(name, new Version(v.getFileName().toString())));
				} catch (IOException e) {
					e.printStackTrace();
					return Stream.empty();
				}
			}).toList());
		} catch (IOException e) {
			e.printStackTrace();
			return CompletableFuture.completedFuture(Collections.emptyList());
		}
	}

	@Override
	public CompletableFuture<Pack> obtain(PackIdentifier id) {
		return download(id).thenApply(p -> {
			if (p == null) return null;

			try {
				p.loadFromStorage();
				return p;
			} catch (IOException e) {
				throw new CompletionException(e);
			}
		});
	}

	@Override
	public CompletableFuture<LocalPack> download(PackIdentifier id) {
		Path packRoot = repositoryRoot.resolve(id.name).resolve(id.packVersion.toStringNoPrefix());
		if (!Files.exists(packRoot)) return CompletableFuture.completedFuture(null);
		return CompletableFuture.completedFuture(new LocalPack(packRoot));
	}

	@Override
	public CompletableFuture<PackIdentifier> upload(LocalPack pack) {
		try {
			Path packDestDir = repositoryRoot.resolve(pack.getIndex().name + "/" + pack.getIndex().packVersion.toString());

			if (Files.notExists(packDestDir)) {
				Files.createDirectories(packDestDir);
			} else if (Files.exists(packDestDir.resolve("multipacks.index.json"))) {
				return CompletableFuture.failedFuture(new RuntimeException(Messages.packFoundRepo(pack.getIndex())));
			}

			Files.walkFileTree(pack.packRoot, new SimpleFileVisitor<Path>() {
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					Files.createDirectories(packDestDir.resolve(pack.packRoot.relativize(dir).toString()));
					return FileVisitResult.CONTINUE;
				};

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.copy(file, packDestDir.resolve(pack.packRoot.relativize(file).toString()), StandardCopyOption.REPLACE_EXISTING);
					return FileVisitResult.CONTINUE;
				}
			});

			return CompletableFuture.completedFuture(new PackIdentifier(pack.getIndex().name, pack.getIndex().packVersion));
		} catch (IOException e) {
			return CompletableFuture.failedFuture(e);
		}
	}

	@Override
	public CompletableFuture<Void> delete(PackIdentifier id) {
		try {
			Path packDestDir = repositoryRoot.resolve(id.name + "/" + id.packVersion.toString());
			if (Files.notExists(packDestDir)) return CompletableFuture.failedFuture(new IllegalArgumentException(Messages.packNotFoundRepo(id)));
			deleteRecursively(packDestDir);
			return CompletableFuture.completedFuture(null);
		} catch (IOException e) {
			return CompletableFuture.failedFuture(e);
		}
	}

	private void deleteRecursively(Path p) throws IOException {
		if (Files.isDirectory(p)) {
			for (Path child : Files.list(p).toList()) deleteRecursively(child);
		}

		Files.delete(p);
	}

	@Override
	public String toString() {
		return "local " + repositoryRoot;
	}
}
