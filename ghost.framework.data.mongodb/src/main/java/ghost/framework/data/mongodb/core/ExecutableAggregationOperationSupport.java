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

import ghost.framework.data.commons.util.CloseableIterator;
import ghost.framework.data.mongodb.core.aggregation.Aggregation;
import ghost.framework.data.mongodb.core.aggregation.AggregationResults;
import ghost.framework.data.mongodb.core.aggregation.TypedAggregation;
import ghost.framework.util.Assert;
import ghost.framework.util.StringUtils;

/**
 * Implementation of {@link ExecutableAggregationOperation} operating directly on {@link MongoTemplate}.
 *
 * @author Christoph Strobl
 * @author Mark Paluch
 * @since 2.0
 */
class ExecutableAggregationOperationSupport implements ExecutableAggregationOperation {

	private final MongoTemplate template;

	ExecutableAggregationOperationSupport(MongoTemplate template) {
		this.template = template;
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.core.ExecutableAggregationOperation#aggregateAndReturn(java.lang.Class)
	 */
	@Override
	public <T> ExecutableAggregation<T> aggregateAndReturn(Class<T> domainType) {

		Assert.notNull(domainType, "DomainType must not be null!");

		return new ExecutableAggregationSupport<>(template, domainType, null, null);
	}

	/**
	 * @author Christoph Strobl
	 * @since 2.0
	 */
	static class ExecutableAggregationSupport<T>
			implements AggregationWithAggregation<T>, ExecutableAggregation<T>, TerminatingAggregation<T> {

		private final MongoTemplate template;
		private final Class<T> domainType;
		private final Aggregation aggregation;
		private final String collection;

		public ExecutableAggregationSupport(MongoTemplate template, Class<T> domainType, Aggregation aggregation,
				String collection) {
			this.template = template;
			this.domainType = domainType;
			this.aggregation = aggregation;
			this.collection = collection;
		}

		/*
		 * (non-Javadoc)
		 * @see ghost.framework.data.mongodb.core.ExecutableAggregationOperation.AggregationWithCollection#inCollection(java.lang.String)
		 */
		@Override
		public AggregationWithAggregation<T> inCollection(String collection) {

			Assert.hasText(collection, "Collection must not be null nor empty!");

			return new ExecutableAggregationSupport<>(template, domainType, aggregation, collection);
		}

		/*
		 * (non-Javadoc)
		 * @see ghost.framework.data.mongodb.core.ExecutableAggregationOperation.AggregationWithAggregation#by(ghost.framework.data.mongodb.core.aggregation.Aggregation)
		 */
		@Override
		public TerminatingAggregation<T> by(Aggregation aggregation) {

			Assert.notNull(aggregation, "Aggregation must not be null!");

			return new ExecutableAggregationSupport<>(template, domainType, aggregation, collection);
		}

		/*
		 * (non-Javadoc)
		 * @see ghost.framework.data.mongodb.core.ExecutableAggregationOperation.TerminatingAggregation#all()
		 */
		@Override
		public AggregationResults<T> all() {
			return template.aggregate(aggregation, getCollectionName(aggregation), domainType);
		}

		/*
		 * (non-Javadoc)
		 * @see ghost.framework.data.mongodb.core.ExecutableAggregationOperation.TerminatingAggregation#stream()
		 */
		@Override
		public CloseableIterator<T> stream() {
			return template.aggregateStream(aggregation, getCollectionName(aggregation), domainType);
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
