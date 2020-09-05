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
package ghost.framework.data.mongodb.core;

import ghost.framework.data.mongodb.core.aggregation.Aggregation;
import ghost.framework.data.mongodb.core.aggregation.TypedAggregation;
import ghost.framework.util.Assert;
import ghost.framework.util.StringUtils;
import reactor.core.publisher.Flux;

/**
 * Implementation of {@link ReactiveAggregationOperation} operating directly on {@link ReactiveMongoTemplate}.
 *
 * @author Mark Paluch
 * @autor Christoph Strobl
 * @since 2.0
 */
class ReactiveAggregationOperationSupport implements ReactiveAggregationOperation {

	private final ReactiveMongoTemplate template;

	/**
	 * Create new instance of {@link ReactiveAggregationOperationSupport}.
	 *
	 * @param template must not be {@literal null}.
	 * @throws IllegalArgumentException if template is {@literal null}.
	 */
	ReactiveAggregationOperationSupport(ReactiveMongoTemplate template) {

		Assert.notNull(template, "Template must not be null!");

		this.template = template;
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.core.ReactiveAggregationOperation#aggregateAndReturn(java.lang.Class)
	 */
	@Override
	public <T> ReactiveAggregation<T> aggregateAndReturn(Class<T> domainType) {

		Assert.notNull(domainType, "DomainType must not be null!");

		return new ReactiveAggregationSupport<>(template, domainType, null, null);
	}

	static class ReactiveAggregationSupport<T>
			implements AggregationOperationWithAggregation<T>, ReactiveAggregation<T>, TerminatingAggregationOperation<T> {

		private final ReactiveMongoTemplate template;
		private final Class<T> domainType;
		private final Aggregation aggregation;
		private final String collection;

		ReactiveAggregationSupport(ReactiveMongoTemplate template, Class<T> domainType, Aggregation aggregation,
				String collection) {

			this.template = template;
			this.domainType = domainType;
			this.aggregation = aggregation;
			this.collection = collection;
		}

		/*
		 * (non-Javadoc)
		 * @see ghost.framework.data.mongodb.core.ReactiveAggregationOperation.AggregationOperationWithCollection#inCollection(java.lang.String)
		 */
		@Override
		public AggregationOperationWithAggregation<T> inCollection(String collection) {

			Assert.hasText(collection, "Collection must not be null nor empty!");

			return new ReactiveAggregationSupport<>(template, domainType, aggregation, collection);
		}

		/*
		 * (non-Javadoc)
		 * @see ghost.framework.data.mongodb.core.ReactiveAggregationOperation.AggregationOperationWithAggregation#by(ghost.framework.data.mongodb.core.Aggregation)
		 */
		@Override
		public TerminatingAggregationOperation<T> by(Aggregation aggregation) {

			Assert.notNull(aggregation, "Aggregation must not be null!");

			return new ReactiveAggregationSupport<>(template, domainType, aggregation, collection);
		}

		/*
		 * (non-Javadoc)
		 * @see ghost.framework.data.mongodb.core.ReactiveAggregationOperation.TerminatingAggregationOperation#all()
		 */
		@Override
		public Flux<T> all() {
			return template.aggregate(aggregation, getCollectionName(aggregation), domainType);
		}

		private String getCollectionName(Aggregation aggregation) {

			if (StringUtils.hasText(collection)) {
				return collection;
			}

			if (aggregation instanceof TypedAggregation) {

				TypedAggregation<?> typedAggregation = (TypedAggregation<?>) aggregation;

				if (typedAggregation.getInputType() != null) {
					return template.getCollectionName(typedAggregation.getInputType());
				}
			}

			return template.getCollectionName(domainType);
		}
	}
}
