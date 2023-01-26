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
package multipacks.authentication;

import java.util.concurrent.CompletableFuture;

/**
 * @author nahkd
 *
 */
public interface CanLogin {
	boolean isLoggedIn();

	/**
	 * Attempt to login to repository.
	 * @param username Username to login.
	 * @param secret Password or secret to login.
	 * @return {@link CompletableFuture} for async login.
	 */
	default CompletableFuture<CanLogin> login(String username, byte[] secret) {
		return CompletableFuture.completedFuture(this);
	}
}
