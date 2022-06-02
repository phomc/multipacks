package multipacks.management;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import multipacks.packs.Pack;
import multipacks.packs.PackIdentifier;
import multipacks.packs.PackIndex;
import multipacks.utils.IOUtils;
import multipacks.utils.iter.EmptyIterator;
import multipacks.versioning.Version;

public class LocalRepository extends PacksRepository implements PacksUploadable {
	public final File repoDir;

	public LocalRepository(File repoDir) {
		this.repoDir = repoDir;
		if (!repoDir.exists()) repoDir.mkdirs();
	}

	@Override
	public String toString() {
		return "LocalRepository(file:" + repoDir.toString() + ")";
	}

	private File getPackDir(PackIdentifier identifier) {
		File packPath = new File(repoDir, identifier.id);
		File packVersionPath = new File(packPath, "v" + identifier.version.toStringNoPrefix());
		return packVersionPath;
	}

	private Version getPackVersionFromPackDir(File packDir) {
		return new Version(packDir.getName().substring(1));
	}

	@Override
	public boolean putPack(Pack pack, boolean force) throws IllegalAccessException {
		File packDir = getPackDir(pack.getIdentifier());
		if (packDir.exists()) return false;

		try {
			IOUtils.copyRecursive(pack.getRoot(), packDir, StandardCopyOption.REPLACE_EXISTING);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public Iterator<PackIndex> queryPacks(PackIdentifier query) {
		if (query == null) {
			File[] packs = repoDir.listFiles();

			return new Iterator<PackIndex>() {
				int packPointer = 0;
				File[] versions;
				int versionPointer = 0;
				File currentDir;

				@Override
				public PackIndex next() {
					if (currentDir == null) {
						if (!hasNext()) throw new NoSuchElementException();
					}

					File index = new File(currentDir, "multipacks.json");
					currentDir = null;
					if (!index.exists()) return null;

					try {
						PackIndex idx = PackIndex.fromJson(IOUtils.jsonFromFile(index).getAsJsonObject());
						return idx;
					} catch (IOException e) {
						e.printStackTrace();
						return null;
					}
				}

				@Override
				public boolean hasNext() {
					if (currentDir != null) return true;
					if (packPointer >= packs.length) return false;

					if (versions == null || versionPointer >= versions.length) {
						versions = packs[packPointer++].listFiles();
						versionPointer = 0;
					}

					currentDir = versions[versionPointer];
					return true;
				}
			};
		}

		File packPath = new File(repoDir, query.id);
		if (!packPath.exists()) return new EmptyIterator<>();

		File[] matchingVersions = Stream.of(packPath.listFiles()).filter(v -> query.version.check(getPackVersionFromPackDir(v))).toArray(File[]::new);

		return new Iterator<PackIndex>() {
			int pointer = 0;

			@Override
			public PackIndex next() throws NoSuchElementException {
				if (pointer >= matchingVersions.length) throw new NoSuchElementException();
				File packVersionPath = matchingVersions[pointer++];
				File index = new File(packVersionPath, "multipacks.json");
				if (!index.exists()) return null;

				try {
					PackIndex idx = PackIndex.fromJson(IOUtils.jsonFromFile(index).getAsJsonObject());
					return idx;
				} catch (IOException e) {
					e.printStackTrace();
					return null;
				}
			}

			@Override
			public boolean hasNext() {
				return pointer < matchingVersions.length;
			}
		};
	}

	@Override
	public Pack getPack(PackIndex index) {
		File packVersionPath = getPackDir(index.getIdentifier());

		try {
			return new Pack(packVersionPath);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}