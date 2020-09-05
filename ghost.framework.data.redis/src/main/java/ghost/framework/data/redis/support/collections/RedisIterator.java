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
package ghost.framework.data.redis.support.collections;


import ghost.framework.beans.annotation.constraints.Nullable;

import java.util.Iterator;

/**
 * Iterator extension for Redis collection removal.
 *
 * @author Costin Leau
 */
abstract class RedisIterator<E> implements Iterator<E> {

	private final Iterator<E> delegate;

	private @Nullable
	E item;

	/**
	 * Constructs a new <code>RedisIterator</code> instance.
	 *
	 * @param delegate
	 */
	RedisIterator(Iterator<E> delegate) {
		this.delegate = delegate;
	}

	/**
	 * @return
	 * @see Iterator#hasNext()
	 */
	public boolean hasNext() {
		return delegate.hasNext();
	}

	/**
	 * @return
	 * @see Iterator#next()
	 */
	public E next() {
		item = delegate.next();
		return item;
	}

	/**
	 * @see Iterator#remove()
	 */
	public void remove() {
		delegate.remove();
		removeFromRedisStorage(item);
		item = null;
	}

	protected abstract void removeFromRedisStorage(E item);
}
