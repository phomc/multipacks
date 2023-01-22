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
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
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
			return new LocalRepository(Path.of(clsLoader.getResource(pathToRepo).toURI()));
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
		if (!Files.exists(packRoot)) return CompletableFuture.failedFuture(new IllegalArgumentException(Messages.packNotFoundRepo(id)));
		return CompletableFuture.completedFuture(new LocalPack(packRoot));
	}

	@Override
	public CompletableFuture<PackIdentifier> upload(LocalPack pack) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Void> delete(PackIdentifier id) {
		// TODO Auto-generated method stub
		return null;
	}
}
