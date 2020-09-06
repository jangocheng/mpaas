/*
 * Copyright 2017-2020 the original author or authors.
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
package ghost.framework.data.redis.serializer;

import ghost.framework.beans.annotation.constraints.Nullable;

import java.nio.ByteBuffer;

/**
 * Default implementation of {@link RedisElementWriter}.
 *
 * @author Mark Paluch
 * @author Christoph Strobl
 * @since 2.0
 */
class DefaultRedisElementWriter<T> implements RedisElementWriter<T> {

	private final @Nullable
	RedisSerializer<T> serializer;

	DefaultRedisElementWriter(RedisSerializer<T> serializer) {
		this.serializer = serializer;
	}

	/* (non-Javadoc)
	 * @see ghost.framework.data.redis.serializer.RedisElementWriter#write(java.lang.Object)
	 */
	@Override
	public ByteBuffer write(T value) {

		if (serializer != null && (value == null || serializer.canSerialize(value.getClass()))) {
			return ByteBuffer.wrap(serializer.serialize(value));
		}

		if (value instanceof byte[]) {
			return ByteBuffer.wrap((byte[]) value);
		}

		if (value instanceof ByteBuffer) {
			return (ByteBuffer) value;
		}

		throw new IllegalStateException(
				String.format("Cannot serialize value of type %s without a serializer", value.getClass()));

	}
}