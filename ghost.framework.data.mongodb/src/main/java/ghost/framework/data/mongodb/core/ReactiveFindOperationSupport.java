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

import ghost.framework.beans.annotation.constraints.Nullable;
import ghost.framework.data.dao.IncorrectResultSizeDataAccessException;
import ghost.framework.data.mongodb.core.query.NearQuery;
import ghost.framework.data.mongodb.core.query.Query;
import ghost.framework.data.mongodb.core.query.SerializationUtils;
import ghost.framework.util.Assert;
import ghost.framework.util.StringUtils;
import org.bson.Document;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Implementation of {@link ReactiveFindOperation}.
 *
 * @author Mark Paluch
 * @author Christoph Strobl
 * @since 2.0
 */
class ReactiveFindOperationSupport implements ReactiveFindOperation {

	private static final Query ALL_QUERY = new Query();

	private final ReactiveMongoTemplate template;

	ReactiveFindOperationSupport(ReactiveMongoTemplate template) {
		this.template = template;
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.core.ReactiveFindOperation#query(java.lang.Class)
	 */
	@Override
	public <T> ReactiveFind<T> query(Class<T> domainType) {

		Assert.notNull(domainType, "DomainType must not be null!");

		return new ReactiveFindSupport<>(template, domainType, domainType, null, ALL_QUERY);
	}

	/**
	 * @param <T>
	 * @author Mark Paluch
	 * @author Christoph Strobl
	 * @since 2.0
	 */
	static class ReactiveFindSupport<T>
			implements ReactiveFind<T>, FindWithCollection<T>, FindWithProjection<T>, FindWithQuery<T> {

		private final ReactiveMongoTemplate template;
		private final Class<?> domainType;
		private final Class<T> returnType;
		private final String collection;
		private final Query query;

		ReactiveFindSupport(ReactiveMongoTemplate template, Class<?> domainType, Class<T> returnType,
				String collection, Query query) {

			this.template = template;
			this.domainType = domainType;
			this.returnType = returnType;
			this.collection = collection;
			this.query = query;
		}

		/*
		 * (non-Javadoc)
		 * @see ghost.framework.data.mongodb.core.ReactiveFindOperation.FindWithCollection#inCollection(java.lang.String)
		 */
		@Override
		public FindWithProjection<T> inCollection(String collection) {

			Assert.hasText(collection, "Collection name must not be null nor empty!");

			return new ReactiveFindSupport<>(template, domainType, returnType, collection, query);
		}

		/*
		 * (non-Javadoc)
		 * @see ghost.framework.data.mongodb.core.ReactiveFindOperation.FindWithProjection#as(java.lang.Class)
		 */
		@Override
		public <T1> FindWithQuery<T1> as(Class<T1> returnType) {

			Assert.notNull(returnType, "ReturnType must not be null!");

			return new ReactiveFindSupport<>(template, domainType, returnType, collection, query);
		}

		/*
		 * (non-Javadoc)
		 * @see ghost.framework.data.mongodb.core.ReactiveFindOperation.FindWithQuery#matching(ghost.framework.data.mongodb.core.query.Query)
		 */
		@Override
		public TerminatingFind<T> matching(Query query) {

			Assert.notNull(query, "Query must not be null!");

			return new ReactiveFindSupport<>(template, domainType, returnType, collection, query);
		}

		/*
		 * (non-Javadoc)
		 * @see ghost.framework.data.mongodb.core.ReactiveFindOperation.TerminatingFind#first()
		 */
		@Override
		public Mono<T> first() {

			FindPublisherPreparer preparer = getCursorPreparer(query);
			Flux<T> result = doFind(publisher -> preparer.prepare(publisher).limit(1));

			return result.next();
		}

		/*
		 * (non-Javadoc)
		 * @see ghost.framework.data.mongodb.core.ReactiveFindOperation.TerminatingFind#one()
		 */
		@Override
		public Mono<T> one() {

			FindPublisherPreparer preparer = getCursorPreparer(query);
			Flux<T> result = doFind(publisher -> preparer.prepare(publisher).limit(2));

			return result.collectList().flatMap(it -> {

				if (it.isEmpty()) {
					return Mono.empty();
				}

				if (it.size() > 1) {
					return Mono.error(
							new IncorrectResultSizeDataAccessException("Query " + asString() + " returned non unique result.", 1));
				}

				return Mono.just(it.get(0));
			});
		}

		/*
		 * (non-Javadoc)
		 * @see ghost.framework.data.mongodb.core.ReactiveFindOperation.TerminatingFind#all()
		 */
		@Override
		public Flux<T> all() {
			return doFind(null);
		}

		/*
		 * (non-Javadoc)
		 * @see ghost.framework.data.mongodb.core.ReactiveFindOperation.TerminatingFind#tail()
		 */
		@Override
		public Flux<T> tail() {
			return doFind(template.new TailingQueryFindPublisherPreparer(query, domainType));
		}

		/*
		 * (non-Javadoc)
		 * @see ghost.framework.data.mongodb.core.ReactiveFindOperation.FindWithQuery#near(ghost.framework.data.mongodb.core.query.NearQuery)
		 */
		@Override
		public TerminatingFindNear<T> near(NearQuery nearQuery) {
			return () -> template.geoNear(nearQuery, domainType, getCollectionName(), returnType);
		}

		/*
		 * (non-Javadoc)
		 * @see ghost.framework.data.mongodb.core.ReactiveFindOperation.TerminatingFind#count()
		 */
		@Override
		public Mono<Long> count() {
			return template.count(query, domainType, getCollectionName());
		}

		/*
		 * (non-Javadoc)
		 * @see ghost.framework.data.mongodb.core.ReactiveFindOperation.TerminatingFind#exists()
		 */
		@Override
		public Mono<Boolean> exists() {
			return template.exists(query, domainType, getCollectionName());
		}

		/*
		 * (non-Javadoc)
		 * @see ghost.framework.data.mongodb.core.ReactiveFindOperation.FindDistinct#distinct(java.lang.String)
		 */
		@Override
		public TerminatingDistinct<Object> distinct(String field) {

			Assert.notNull(field, "Field must not be null!");

			return new DistinctOperationSupport<>(this, field);
		}

		private Flux<T> doFind(@Nullable FindPublisherPreparer preparer) {

			Document queryObject = query.getQueryObject();
			Document fieldsObject = query.getFieldsObject();

			return template.doFind(getCollectionName(), queryObject, fieldsObject, domainType, returnType,
					preparer != null ? preparer : getCursorPreparer(query));
		}

		@SuppressWarnings("unchecked")
		private Flux<T> doFindDistinct(String field) {

			return template.findDistinct(query, field, getCollectionName(), domainType,
					returnType == domainType ? (Class<T>) Object.class : returnType);
		}

		private FindPublisherPreparer getCursorPreparer(Query query) {
			return template.new QueryFindPublisherPreparer(query, domainType);
		}

		private String getCollectionName() {
			return StringUtils.hasText(collection) ? collection : template.getCollectionName(domainType);
		}

		private String asString() {
			return SerializationUtils.serializeToJsonSafely(query);
		}

		/**
		 * @author Christoph Strobl
		 * @since 2.1
		 */
		static class DistinctOperationSupport<T> implements TerminatingDistinct<T> {

			private final String field;
			private final ReactiveFindSupport delegate;

			public DistinctOperationSupport(ReactiveFindSupport delegate, String field) {

				this.delegate = delegate;
				this.field = field;
			}

			/*
			 * (non-Javadoc)
			 * @see ghost.framework.data.mongodb.core.ReactiveFindOperation.DistinctWithProjection#as(java.lang.Class)
			 */
			@Override
			public <R> TerminatingDistinct<R> as(Class<R> resultType) {

				Assert.notNull(resultType, "ResultType must not be null!");

				return new DistinctOperationSupport<>((ReactiveFindSupport) delegate.as(resultType), field);
			}

			/*
			 * (non-Javadoc)
			 * @see ghost.framework.data.mongodb.core.ReactiveFindOperation.DistinctWithQuery#matching(ghost.framework.data.mongodb.core.query.Query)
			 */
			@Override
			@SuppressWarnings("unchecked")
			public TerminatingDistinct<T> matching(Query query) {

				Assert.notNull(query, "Query must not be null!");

				return new DistinctOperationSupport<>((ReactiveFindSupport<T>) delegate.matching(query), field);
			}

			/*
			 * (non-Javadoc)
			 * @see ghost.framework.data.mongodb.core..ReactiveFindOperation.TerminatingDistinct#all()
			 */
			@Override
			public Flux<T> all() {
				return delegate.doFindDistinct(field);
			}
		}
	}
}
