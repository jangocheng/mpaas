/*
 * Copyright 2016-2020 the original author or authors.
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
package ghost.framework.data.lettuce;

import ghost.framework.data.dao.InvalidDataAccessApiUsageException;
import ghost.framework.data.redis.connection.ClusterSlotHashUtil;
import ghost.framework.data.redis.connection.ReactiveClusterListCommands;
import ghost.framework.data.redis.connection.ReactiveRedisConnection;
import ghost.framework.util.Assert;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

/**
 * @author Christoph Strobl
 * @author Mark Paluch
 * @since 2.0
 */
class LettuceReactiveClusterListCommands extends LettuceReactiveListCommands implements ReactiveClusterListCommands {

	/**
	 * Create new {@link LettuceReactiveClusterListCommands}.
	 *
	 * @param connection must not be {@literal null}.
	 */
	LettuceReactiveClusterListCommands(LettuceReactiveRedisConnection connection) {
		super(connection);
	}

	/* (non-Javadoc)
	 * @see ghost.framework.data.redis.connection.lettuce.LettuceReactiveListCommands#bPop(org.reactivestreams.Publisher)
	 */
	@Override
	public Flux<PopResponse> bPop(Publisher<BPopCommand> commands) {

		return getConnection().execute(cmd -> Flux.from(commands).concatMap(command -> {

			Assert.notNull(command.getKeys(), "Keys must not be null!");
			Assert.notNull(command.getDirection(), "Direction must not be null!");

			if (ClusterSlotHashUtil.isSameSlotForAllKeys(command.getKeys())) {
				return super.bPop(Mono.just(command));
			}

			return Mono.error(new InvalidDataAccessApiUsageException("All keys must map to the same slot for BPOP command."));
		}));
	}

	/* (non-Javadoc)
	 * @see ghost.framework.data.redis.connection.lettuce.LettuceReactiveListCommands#rPopLPush(org.reactivestreams.Publisher)
	 */
	@Override
	public Flux<ReactiveRedisConnection.ByteBufferResponse<RPopLPushCommand>> rPopLPush(Publisher<RPopLPushCommand> commands) {

		return getConnection().execute(cmd -> Flux.from(commands).concatMap(command -> {

			Assert.notNull(command.getKey(), "Key must not be null!");
			Assert.notNull(command.getDestination(), "Destination key must not be null!");

			if (ClusterSlotHashUtil.isSameSlotForAllKeys(command.getKey(), command.getDestination())) {
				return super.rPopLPush(Mono.just(command));
			}

			Mono<ByteBuffer> result = cmd.rpop(command.getKey())
					.flatMap(value -> cmd.lpush(command.getDestination(), value).map(x -> value));

			return result.map(value -> new ReactiveRedisConnection.ByteBufferResponse<>(command, value));
		}));
	}
}
