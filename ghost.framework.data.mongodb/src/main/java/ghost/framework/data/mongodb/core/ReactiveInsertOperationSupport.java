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

import ghost.framework.util.Assert;
import ghost.framework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

/**
 * Implementation of {@link ReactiveInsertOperation}.
 *
 * @author Mark Paluch
 * @author Christoph Strobl
 * @since 2.0
 */
class ReactiveInsertOperationSupport implements ReactiveInsertOperation {

	private final ReactiveMongoTemplate template;

	ReactiveInsertOperationSupport(ReactiveMongoTemplate template) {
		this.template = template;
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.core.ReactiveInsertOperation#insert(java.lang.Class)
	 */
	@Override
	public <T> ReactiveInsert<T> insert(Class<T> domainType) {

		Assert.notNull(domainType, "DomainType must not be null!");

		return new ReactiveInsertSupport<>(template, domainType, null);
	}

	static class ReactiveInsertSupport<T> implements ReactiveInsert<T> {

		private final ReactiveMongoTemplate template;
		private final Class<T> domainType;
		private final String collection;

		ReactiveInsertSupport(ReactiveMongoTemplate template, Class<T> domainType, String collection) {

			this.template = template;
			this.domainType = domainType;
			this.collection = collection;
		}

		/*
		 * (non-Javadoc)
		 * @see ghost.framework.data.mongodb.core.ReactiveInsertOperation.TerminatingInsert#one(java.lang.Object)
		 */
		@Override
		public Mono<T> one(T object) {

			Assert.notNull(object, "Object must not be null!");

			return template.insert(object, getCollectionName());
		}

		/*
		 * (non-Javadoc)
		 * @see ghost.framework.data.mongodb.core.ReactiveInsertOperation.TerminatingInsert#all(java.util.Collection)
		 */
		@Override
		public Flux<T> all(Collection<? extends T> objects) {

			Assert.notNull(objects, "Objects must not be null!");

			return template.insert(objects, getCollectionName());
		}

		/*
		 * (non-Javadoc)
		 * @see ghost.framework.data.mongodb.core.ReactiveInsertOperation.InsertWithCollection#inCollection(java.lang.String)
		 */
		@Override
		public ReactiveInsert<T> inCollection(String collection) {

			Assert.hasText(collection, "Collection must not be null nor empty.");

			return new ReactiveInsertSupport<>(template, domainType, collection);
		}

		private String getCollectionName() {
			return StringUtils.hasText(collection) ? collection : template.getCollectionName(domainType);
		}
	}
}
