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
package multipacks.repository;

import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import multipacks.packs.LocalPack;
import multipacks.packs.Pack;
import multipacks.packs.meta.PackIdentifier;
import multipacks.repository.query.PackQuery;

/**
 * Represent the packs repository that you can query, download packs or even upload your packs.
 * @author nahkd
 *
 */
public interface Repository {
	/**
	 * Query (a.k.a search) packs from this repository.
	 * @param query Pack query info. Can be {@code null} to query all packs.
	 * @return A collection of {@link PackIdentifier}.
	 * @throws CompletionException wrapped {@link RuntimeException}; if something went wrong.
	 */
	CompletableFuture<Collection<PackIdentifier>> search(PackQuery query);

	/**
	 * Stream pack contents from this repository to a new pack object. It is {@link #download(PackIdentifier)}, but
	 * instead of saving to user's storage, it will download the pack and stores it in memory. Some repository interface
	 * implementations might choose to cache as {@link LocalPack}. In this case, those packs will be loaded so you don't
	 * have to use {@link LocalPack#loadFromStorage()}. 
	 * @param id Pack id to obtain from repository.
	 * @return Pack from repository, or {@code null} if the pack couldn't be found.
	 * @throws CompletionException wrapped {@link RuntimeException}; if something went wrong.
	 */
	CompletableFuture<Pack> obtain(PackIdentifier id);

	/**
	 * Download the pack from this repository to user's machine. The download destination should be configured.
	 * @param id Pack id to download from repository.
	 * @return Downloaded pack contents in a form of {@link LocalPack}, or {@code null} if the pack couldn't be found.
	 * @throws CompletionException wrapped {@link RuntimeException}; if something went wrong.
	 */
	CompletableFuture<LocalPack> download(PackIdentifier id);

	/**
	 * Login to this repository to obtain {@link AuthorizedRepository}. Authorized repositories can uploads and
	 * deletes pack, in addition to querying and downloading.
	 * @param username Username to login.
	 * @param secret User's secret to login.
	 * @return Authorized repository.
	 * @throws CompletionException wrapped {@link IllegalArgumentException}; if the login attempt failed.
	 * @throws CompletionException wrapped {@link RuntimeException}; if something went wrong.
	 */
	CompletableFuture<AuthorizedRepository> login(String username, byte[] secret);

	static Repository fromConnectionString(String str, Path cwd) {
		String[] split = str.split(" ", 2);
		String type = split[0];
		String connectTo = split[1];

		switch (type) {
		case "local":
			if (cwd == null) throw new NullPointerException("CWD is not supplied to this method");
			return new LocalRepository(cwd.resolve(connectTo));
		default: throw new IllegalArgumentException("Unknown repository type: " + type);
		}
	}
}
