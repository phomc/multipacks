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
package multipacks.modifier.builtin.models.overrides;

import com.google.gson.JsonObject;

import multipacks.modifier.builtin.models.BaseItemModel;
import multipacks.utils.ResourcePath;

/**
 * @author nahkd
 *
 */
public class TrimModelOverride extends ModelOverride {
	public static final String PREDICATE_TYPE = "trim_type";

	public final double trimType;

	public TrimModelOverride(BaseItemModel base, ResourcePath modelPath, double trimType) {
		super(base, modelPath);
		this.trimType = trimType;
	}

	@Override
	public JsonObject toPredicateJson() {
		JsonObject predicate = new JsonObject();
		predicate.addProperty(PREDICATE_TYPE, trimType);
		return predicate;
	}
}
