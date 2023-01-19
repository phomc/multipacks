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
package multipacks.tests.packs;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.api.Test;

import multipacks.bundling.Bundler;
import multipacks.packs.Pack;
import multipacks.packs.meta.PackIdentifier;
import multipacks.repository.LocalRepository;
import multipacks.repository.RepositoriesAccess;
import multipacks.repository.Repository;
import multipacks.repository.query.PackQuery;
import multipacks.versioning.Version;
import multipacks.vfs.Vfs;

/**
 * @author nahkd
 *
 */
class RepositoriesTest {
	@Test
	void testQuery() throws Exception {
		Path rootPath = Path.of(this.getClass().getClassLoader().getResource("testRepo").toURI());
		LocalRepository repo = new LocalRepository(rootPath);

		// Repository structure:
		// packA/
		//   1.0.0/
		//   1.0.1/
		// packB/
		//   1.0.0/

		assertEquals(4, repo.search(null).get().size());
		assertEquals(4, repo.search(PackQuery.parse("version >= 1.0.0")).get().size());
		assertEquals(1, repo.search(PackQuery.parse("version > 1.0.0")).get().size());
		assertEquals(1, repo.search(PackQuery.parse("version >= 1.0.1")).get().size());
		assertEquals(2, repo.search(PackQuery.parse("name 'packA'")).get().size());
		assertEquals(1, repo.search(PackQuery.parse("name 'packB'")).get().size());
		assertEquals(1, repo.search(PackQuery.parse("name 'packA'; version > 1.0.0")).get().size());
	}

	@Test
	void testObtain() throws Exception {
		Path rootPath = Path.of(this.getClass().getClassLoader().getResource("testRepo").toURI());
		LocalRepository repo = new LocalRepository(rootPath);

		Pack pack = repo.obtain(new PackIdentifier("packA", new Version("1.0.0"))).get();
		assertEquals("packA", pack.getIndex().name);

		try {
			repo.obtain(new PackIdentifier("packA", new Version("1.0.2"))).get();
			fail("Not thrown when getting non-existent pack");
		} catch (Exception e) {
			// Must throw exception here.
		}
	}

	@Test
	void testPacksBundler() throws Exception {
		// TODO: Should we move this to PacksTest?
		Path rootPath = Path.of(this.getClass().getClassLoader().getResource("testRepo").toURI());
		LocalRepository repo = new LocalRepository(rootPath);

		Pack master = repo.obtain(new PackIdentifier("master", new Version("1.0.0"))).get();
		Bundler bundler = new Bundler().setRepositoriesAccess(new RepositoriesAccess() {
			@Override
			public Collection<Repository> getRepositories() {
				return Arrays.asList(repo);
			}
		});

		Vfs content = bundler.bundle(master, master.getIndex().sourceGameVersion);
		assertNotNull(content.get(new multipacks.vfs.Path("assets/a.txt")));
		assertNotNull(content.get(new multipacks.vfs.Path("assets/b.txt")));
		assertNotNull(content.get(new multipacks.vfs.Path("data/b.txt")));
		assertNotNull(content.get(new multipacks.vfs.Path("license-packA")));
		assertNotNull(content.get(new multipacks.vfs.Path("license-packB")));
		assertNotNull(content.get(new multipacks.vfs.Path("license-master")));
		assertNotNull(content.get(new multipacks.vfs.Path("pack.png")));
	}
}
