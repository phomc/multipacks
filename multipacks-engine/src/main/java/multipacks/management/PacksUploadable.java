package multipacks.management;

import multipacks.packs.Pack;

public interface PacksUploadable {
	/**
	 * Put pack to this repository. Some repositories requires you to authenticate in order to upload your
	 * pack.
	 * @param pack The pack.
	 * @param force Force upload the pack. This may allows you to upload older version but it might reset
	 * your pack info.
	 * @return true if the pack is uploaded successfully. In some cases, you may receive false if you try
	 * to upload a pack with version older than the one in repository.
	 * @throws IllegalAccessException if this repository requires you to authenticate first, or the given
	 * credential is invalid.
	 */
	boolean putPack(Pack pack, boolean force) throws IllegalAccessException;

	/**
	 * Put pack to this repository. Some repositories requires you to authenticate in order to upload your
	 * pack.
	 * @param pack The pack.
	 * @return true if the pack is uploaded successfully. In some cases, you may receive false if you try
	 * to upload a pack with version older than the one in repository.
	 * @throws IllegalAccessException if this repository requires you to authenticate first, or the given
	 * credential is invalid.
	 */
	default boolean putPack(Pack pack) throws IllegalAccessException {
		return putPack(pack, false);
	}
}
