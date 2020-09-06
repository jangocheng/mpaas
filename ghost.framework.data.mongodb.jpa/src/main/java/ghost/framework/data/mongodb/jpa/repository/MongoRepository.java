/*
 * Copyright 2010-2020 the original author or authors.
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
package ghost.framework.data.mongodb.jpa.repository;

import ghost.framework.data.domain.Example;
import ghost.framework.data.domain.Sort;
import ghost.framework.data.repository.NoRepositoryBean;
import ghost.framework.data.repository.PagingAndSortingRepository;
import ghost.framework.data.repository.query.QueryByExampleExecutor;

import java.util.List;

/**
 * Mongo specific {@link ghost.framework.data.repository.Repository} interface.
 *
 * @author Oliver Gierke
 * @author Christoph Strobl
 * @author Thomas Darimont
 * @author Mark Paluch
 * @author Khaled Baklouti
 */
@NoRepositoryBean
public interface MongoRepository<T, ID> extends PagingAndSortingRepository<T, ID>, QueryByExampleExecutor<T> {

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.CrudRepository#saveAll(java.lang.Iterable)
	 */
	@Override
	<S extends T> List<S> saveAll(Iterable<S> entities);

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.CrudRepository#findAll()
	 */
	@Override
	List<T> findAll();

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.PagingAndSortingRepository#findAll(ghost.framework.data.domain.Sort)
	 */
	@Override
	List<T> findAll(Sort sort);

	/**
	 * Inserts the given entity. Assumes the instance to be new to be able to apply insertion optimizations. Use the
	 * returned instance for further operations as the save operation might have changed the entity instance completely.
	 * Prefer using {@link #save(Object)} instead to avoid the usage of store-specific API.
	 *
	 * @param entity must not be {@literal null}.
	 * @return the saved entity
	 * @since 1.7
	 */
	<S extends T> S insert(S entity);

	/**
	 * Inserts the given entities. Assumes the given entities to have not been persisted yet and thus will optimize the
	 * insert over a call to {@link #saveAll(Iterable)}. Prefer using {@link #saveAll(Iterable)} to avoid the usage of store
	 * specific API.
	 *
	 * @param entities must not be {@literal null}.
	 * @return the saved entities
	 * @since 1.7
	 */
	<S extends T> List<S> insert(Iterable<S> entities);

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.query.QueryByExampleExecutor#findAll(ghost.framework.data.domain.Example)
	 */
	@Override
	<S extends T> List<S> findAll(Example<S> example);

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.query.QueryByExampleExecutor#findAll(ghost.framework.data.domain.Example, ghost.framework.data.domain.Sort)
	 */
	@Override
	<S extends T> List<S> findAll(Example<S> example, Sort sort);
}