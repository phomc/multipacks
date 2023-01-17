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

import java.io.File;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

import multipacks.packs.LocalPack;
import multipacks.packs.Pack;
import multipacks.packs.meta.PackIdentifier;
import multipacks.repository.query.PackQuery;

/**
 * @author nahkd
 *
 */
public class LocalRepository implements AuthorizedRepository {
	public final File repositoryRoot;

	public LocalRepository(File repositoryRoot) {
		this.repositoryRoot = repositoryRoot;
	}

	@Override
	public CompletableFuture<Iterator<PackIdentifier>> search(PackQuery query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Pack> obtain(PackIdentifier id) {
		return download(id).thenApply(p -> p);
	}

	@Override
	public CompletableFuture<LocalPack> download(PackIdentifier id) {
		return null;
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
