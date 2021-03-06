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
package ghost.framework.data.redis.core;

import ghost.framework.data.redis.connection.ReactiveHyperLogLogCommands;
import ghost.framework.data.redis.serializer.RedisSerializationContext;
import ghost.framework.util.Assert;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.function.Function;

/**
 * Default implementation of {@link ReactiveHyperLogLogOperations}.
 *
 * @author Mark Paluch
 * @author Christoph Strobl
 * @since 2.0
 */
class DefaultReactiveHyperLogLogOperations<K, V> implements ReactiveHyperLogLogOperations<K, V> {

	private final ReactiveRedisTemplate<?, ?> template;
	private final RedisSerializationContext<K, V> serializationContext;

	DefaultReactiveHyperLogLogOperations(ReactiveRedisTemplate<?, ?> template,
			RedisSerializationContext<K, V> serializationContext) {

		this.template = template;
		this.serializationContext = serializationContext;
	}

	/* (non-Javadoc)
	 * @see ghost.framework.data.redis.core.ReactiveHyperLogLogOperations#add(java.lang.Object, java.lang.Object[])
	 */
	@Override
	@SafeVarargs
	public final Mono<Long> add(K key, V... values) {

		Assert.notNull(key, "Key must not be null!");
		Assert.notEmpty(values, "Values must not be null or empty!");
		Assert.noNullElements(values, "Values must not contain null elements!");

		return createMono(connection -> Flux.fromArray(values) //
				.map(this::rawValue) //
				.collectList() //
				.flatMap(serializedValues -> connection.pfAdd(rawKey(key), serializedValues)));
	}

	/* (non-Javadoc)
	 * @see ghost.framework.data.redis.core.ReactiveHyperLogLogOperations#size(java.lang.Object[])
	 */
	@Override
	@SafeVarargs
	public final Mono<Long> size(K... keys) {

		Assert.notEmpty(keys, "Keys must not be null or empty!");
		Assert.noNullElements(keys, "Keys must not contain null elements!");

		return createMono(connection -> Flux.fromArray(keys) //
				.map(this::rawKey) //
				.collectList() //
				.flatMap(connection::pfCount));
	}

	/* (non-Javadoc)
	 * @see ghost.framework.data.redis.core.ReactiveHyperLogLogOperations#union(java.lang.Object, java.lang.Object[])
	 */
	@Override
	@SafeVarargs
	public final Mono<Boolean> union(K destination, K... sourceKeys) {

		Assert.notNull(destination, "Destination key must not be null!");
		Assert.notEmpty(sourceKeys, "Source keys must not be null or empty!");
		Assert.noNullElements(sourceKeys, "Source keys must not contain null elements!");

		return createMono(connection -> Flux.fromArray(sourceKeys) //
				.map(this::rawKey) //
				.collectList() //
				.flatMap(serialized -> connection.pfMerge(rawKey(destination), serialized)));
	}

	/* (non-Javadoc)
	 * @see ghost.framework.data.redis.core.ReactiveHyperLogLogOperations#delete(java.lang.Object)
	 */
	@Override
	public Mono<Boolean> delete(K key) {

		Assert.notNull(key, "Key must not be null!");

		return template.createMono(connection -> connection.keyCommands().del(rawKey(key))).map(l -> l != 0);
	}

	private <T> Mono<T> createMono(Function<ReactiveHyperLogLogCommands, Publisher<T>> function) {
		Assert.notNull(function, "Function must not be null!");

		return template.createMono(connection -> function.apply(connection.hyperLogLogCommands()));
	}

	private ByteBuffer rawKey(K key) {
		return serializationContext.getKeySerializationPair().write(key);
	}

	private ByteBuffer rawValue(V value) {
		return serializationContext.getValueSerializationPair().write(value);
	}
}
