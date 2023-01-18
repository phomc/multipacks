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

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

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
	 */
	CompletableFuture<Collection<PackIdentifier>> search(PackQuery query);

	/**
	 * Stream pack contents from this repository to a new pack object. It is {@link #download(PackIdentifier)}, but
	 * instead of saving to user's storage, it will download the pack and stores it in memory. Some repository interface
	 * implementations might choose to cache as {@link LocalPack}. In this case, those packs will be loaded so you don't
	 * have to use {@link LocalPack#loadFromStorage()}. 
	 * @param id Pack id to obtain from repository.
	 * @return Pack from repository.
	 */
	CompletableFuture<Pack> obtain(PackIdentifier id);

	CompletableFuture<LocalPack> download(PackIdentifier id);

	CompletableFuture<AuthorizedRepository> login(String username, byte[] secret);
}
