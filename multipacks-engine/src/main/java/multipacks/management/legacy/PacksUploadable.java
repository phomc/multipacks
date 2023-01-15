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
package multipacks.management.legacy;

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
