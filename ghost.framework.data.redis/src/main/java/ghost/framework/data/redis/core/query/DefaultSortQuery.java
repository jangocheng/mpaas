/*
 * Copyright 2011-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ghost.framework.data.redis.core.query;

import ghost.framework.beans.annotation.constraints.Nullable;
import ghost.framework.data.redis.connection.SortParameters;

import java.util.List;

/**
 * Default SortQuery implementation.
 *
 * @author Costin Leau
 * @author Mark Paluch
 */
class DefaultSortQuery<K> implements SortQuery<K> {

	private final K key;
	private final @Nullable
	SortParameters.Order order;
	private final @Nullable Boolean alpha;
	private final @Nullable
	SortParameters.Range limit;
	private final @Nullable String by;
	private final List<String> getPattern;

	DefaultSortQuery(K key, SortParameters.Order order, Boolean alpha, SortParameters.Range limit, String by, List<String> getPattern) {
		this.key = key;
		this.order = order;
		this.alpha = alpha;
		this.limit = limit;
		this.by = by;
		this.getPattern = getPattern;
	}

	@Override
	public Boolean isAlphabetic() {
		return alpha;
	}

	@Override
	public K getKey() {
		return this.key;
	}

	@Nullable
	@Override
	public SortParameters.Order getOrder() {
		return this.order;
	}

	@Nullable
	public Boolean getAlpha() {
		return this.alpha;
	}

	@Nullable
	@Override
	public SortParameters.Range getLimit() {
		return this.limit;
	}

	@Nullable
	@Override
	public String getBy() {
		return this.by;
	}

	@Override
	public List<String> getGetPattern() {
		return this.getPattern;
	}

	@Override
	public String toString() {
		return "DefaultSortQuery [alpha=" + alpha + ", by=" + by + ", gets=" + getPattern + ", key=" + key + ", limit="
				+ limit + ", order=" + order + "]";
	}
}
