package multipacks.management;

import java.io.File;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

import multipacks.packs.Pack;
import multipacks.packs.PackIdentifier;
import multipacks.packs.PackIndex;

public abstract class PacksRepository {
	/**
	 * Query packs inside this repository.
	 * @param query Packs query filter, pass null to get all. In some repositories, this method may
	 * returns an empty iterator when you pass null to this argument (usually happens to online
	 * repositories).
	 * @return The iterator which can be used for further processing.
	 * @see #getPack(PackIndex)
	 * @see #getPackAsync(PackIndex)
	 */
	public abstract Iterator<PackIndex> queryPacks(PackIdentifier query);

	/**
	 * Get the pack from pack index (which could have been obtained from {@link #queryPacks(PackIdentifier)}).
	 * In some repositories, this may blocks the current thread to downloads the pack and store it inside
	 * temporary directory.
	 * @param index The pack index.
	 * @return The pack. May returns null if this repository can't find your pack.
	 * @see #queryPacks(PackIdentifier)
	 * @see #getPackAsync(PackIndex)
	 */
	public abstract Pack getPack(PackIndex index);

	/**
	 * Get pack from pack index asynchronously.
	 * @param index The pack index.
	 * @see #queryPacks(PackIdentifier)
	 */
	public CompletableFuture<Pack> getPackAsync(PackIndex index) {
		return CompletableFuture.supplyAsync(() -> {
			return getPack(index);
		});
	}

	/**
	 * Parse repository string.
	 */
	public static PacksRepository parseRepository(File root, String str) {
		if (str.startsWith("file:")) return new LocalRepository(new File(root, str.substring(5).replace('/', File.separatorChar)));
		return null;
	}
}
