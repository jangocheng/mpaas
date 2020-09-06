/*
 * Copyright 2018-2020 the original author or authors.
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
package ghost.framework.data.redis.support.atomic;

import ghost.framework.dao.DataAccessException;
import ghost.framework.data.redis.core.RedisOperations;
import ghost.framework.data.redis.core.SessionCallback;
import ghost.framework.util.CollectionUtils;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Compare-and-set (CAS) operation using Redis Transactions ({@literal WATCH} and {@literal MULTI}) to atomically update
 * the value at {@code key}.
 * <p>
 * The CAS block registers a {@literal WATCH} on the key holding the expected value which guarantees that changes after
 * watching and comparing the key will rollback the transaction. The {@literal WATCH} is reset if the comparison fails.
 *
 * @author Mark Paluch
 * @since 2.0.8
 * @see RedisAtomicDouble
 * @see RedisAtomicInteger
 * @see RedisAtomicLong
 */
class CompareAndSet<T> implements SessionCallback<Boolean> {

	private final Supplier<T> getter;
	private final Consumer<T> setter;
	private final Object key;
	private final T expect;
	private final T update;

	CompareAndSet(Supplier<T> getter, Consumer<T> setter, Object key, T expect, T update) {

		this.getter = getter;
		this.setter = setter;
		this.key = key;
		this.expect = expect;
		this.update = update;
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.redis.core.SessionCallback#execute(ghost.framework.data.redis.core.RedisOperations)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <K, V> Boolean execute(RedisOperations<K, V> operations) throws DataAccessException {

		operations.watch((K) key);

		if (expect.equals(getter.get())) {

			operations.multi();
			setter.accept(update);

			if (updateSuccessful(operations.exec())) {
				return true;
			}
		}

		operations.unwatch();
		return false;
	}

	private static boolean updateSuccessful(Collection<?> exec) {
		return !CollectionUtils.isEmpty(exec);
	}
}