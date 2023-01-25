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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import multipacks.packs.LocalPack;
import multipacks.packs.Pack;
import multipacks.packs.meta.PackIdentifier;
import multipacks.repository.query.PackQuery;

/**
 * A simple repository implementation. This implementation lacks some features, such as 'logging in' (you can't upload
 * or delete packs from this repository) or 'download' packs that's not {@link LocalPack} (let's just say that we haven't
 * implemented it yet, or we wanted to, but we need a destination directory to store all downloaded packs).
 * @author nahkd
 *
 */
public class SimpleRepository implements Repository {
	private List<Pack> packs = new ArrayList<>();
	private String displayName;

	public SimpleRepository(Pack... packs) {
		this.packs = new ArrayList<>();
		this.packs.addAll(Arrays.asList(packs));
	}

	public SimpleRepository setDisplayName(String displayName) {
		this.displayName = displayName;
		return this;
	}

	@Override
	public CompletableFuture<Collection<PackIdentifier>> search(PackQuery query) {
		return CompletableFuture.completedFuture(packs.stream()
				.filter(p -> query.matches(p.getIndex()))
				.map(p -> new PackIdentifier(p.getIndex().name, p.getIndex().packVersion))
				.toList());
	}

	private Pack obtainSync(PackIdentifier id) {
		Optional<Pack> packOpt = packs.stream().filter(p -> id.name.equals(p.getIndex().name) && id.packVersion.compareTo(p.getIndex().packVersion) == 0).findAny();
		if (packOpt.isPresent()) return packOpt.get();
		else return null;
	}

	@Override
	public CompletableFuture<Pack> obtain(PackIdentifier id) {
		return CompletableFuture.completedFuture(obtainSync(id));
	}

	@Override
	public CompletableFuture<LocalPack> download(PackIdentifier id) {
		Pack pack = obtainSync(id);
		if (pack == null) return CompletableFuture.completedFuture(null);
		if (pack instanceof LocalPack local) return CompletableFuture.completedFuture(local);
		return CompletableFuture.failedFuture(new RuntimeException("Failed to download " + id + ": SimpleRepository does not download the pack to user's machine, unlike other repository types. Only LocalPack can be 'downloaded' from SimpleRepository"));
	}

	@Override
	public CompletableFuture<AuthorizedRepository> login(String username, byte[] secret) {
		return CompletableFuture.failedFuture(new RuntimeException("SimpleRepository does not allows logging in; You can't upload or delete packs from this repository."));
	}

	@Override
	public String toString() {
		return "external repository " + displayName;
	}
}
