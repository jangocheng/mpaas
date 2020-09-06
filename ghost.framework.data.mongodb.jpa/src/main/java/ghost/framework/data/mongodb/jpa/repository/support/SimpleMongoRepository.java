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
package ghost.framework.data.mongodb.jpa.repository.support;

import com.mongodb.client.result.DeleteResult;
import ghost.framework.dao.OptimisticLockingFailureException;
import ghost.framework.data.domain.*;
import ghost.framework.data.mongodb.core.MongoOperations;
import ghost.framework.data.mongodb.core.MongoTemplate;
import ghost.framework.data.mongodb.core.query.Criteria;
import ghost.framework.data.mongodb.core.query.Query;
import ghost.framework.data.mongodb.jpa.repository.MongoRepository;
import ghost.framework.data.mongodb.jpa.repository.query.MongoEntityInformation;
import ghost.framework.data.repository.support.PageableExecutionUtils;
import ghost.framework.data.util.StreamUtils;
import ghost.framework.data.util.Streamable;
import ghost.framework.beans.annotation.constraints.Nullable;
import ghost.framework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ghost.framework.data.mongodb.core.query.Criteria.*;
import static ghost.framework.data.mongodb.core.query.Criteria.where;

/**
 * Repository base implementation for Mongo.
 *
 * @author Oliver Gierke
 * @author Christoph Strobl
 * @author Thomas Darimont
 * @author Mark Paluch
 */
public class SimpleMongoRepository<T, ID> implements MongoRepository<T, ID> {

	private final MongoOperations mongoOperations;
	private final MongoEntityInformation<T, ID> entityInformation;

	/**
	 * Creates a new {@link SimpleMongoRepository} for the given {@link MongoEntityInformation} and {@link MongoTemplate}.
	 *
	 * @param metadata must not be {@literal null}.
	 * @param mongoOperations must not be {@literal null}.
	 */
	public SimpleMongoRepository(MongoEntityInformation<T, ID> metadata, MongoOperations mongoOperations) {

		Assert.notNull(metadata, "MongoEntityInformation must not be null!");
		Assert.notNull(mongoOperations, "MongoOperations must not be null!");

		this.entityInformation = metadata;
		this.mongoOperations = mongoOperations;
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.CrudRepository#save(java.lang.Object)
	 */
	@Override
	public <S extends T> S save(S entity) {

		Assert.notNull(entity, "Entity must not be null!");

		if (entityInformation.isNew(entity)) {
			return mongoOperations.insert(entity, entityInformation.getCollectionName());
		}

		return mongoOperations.save(entity, entityInformation.getCollectionName());
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.jpa.repository.MongoRepository#saveAll(java.lang.Iterable)
	 */
	@Override
	public <S extends T> List<S> saveAll(Iterable<S> entities) {

		Assert.notNull(entities, "The given Iterable of entities not be null!");

		Streamable<S> source = Streamable.of(entities);
		boolean allNew = source.stream().allMatch(it -> entityInformation.isNew(it));

		if (allNew) {

			List<S> result = source.stream().collect(Collectors.toList());
			return new ArrayList<>(mongoOperations.insert(result, entityInformation.getCollectionName()));
		}

		return source.stream().map(this::save).collect(Collectors.toList());
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.CrudRepository#findById(java.io.Serializable)
	 */
	@Override
	public Optional<T> findById(ID id) {

		Assert.notNull(id, "The given id must not be null!");

		return Optional.ofNullable(
				mongoOperations.findById(id, entityInformation.getJavaType(), entityInformation.getCollectionName()));
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.CrudRepository#existsById(java.lang.Object)
	 */
	@Override
	public boolean existsById(ID id) {

		Assert.notNull(id, "The given id must not be null!");

		return mongoOperations.exists(getIdQuery(id), entityInformation.getJavaType(),
				entityInformation.getCollectionName());
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.CrudRepository#count()
	 */
	@Override
	public long count() {
		return mongoOperations.count(new Query(), entityInformation.getCollectionName());
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.CrudRepository#deleteById(java.lang.Object)
	 */
	@Override
	public void deleteById(ID id) {

		Assert.notNull(id, "The given id must not be null!");

		mongoOperations.remove(getIdQuery(id), entityInformation.getJavaType(), entityInformation.getCollectionName());
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.CrudRepository#delete(java.lang.Object)
	 */
	@Override
	public void delete(T entity) {

		Assert.notNull(entity, "The given entity must not be null!");

		DeleteResult deleteResult = mongoOperations.remove(entity, entityInformation.getCollectionName());

		if (entityInformation.isVersioned() && deleteResult.wasAcknowledged() && deleteResult.getDeletedCount() == 0) {
			throw new OptimisticLockingFailureException(String.format(
					"The entity with id %s with version %s in %s cannot be deleted! Was it modified or deleted in the meantime?",
					entityInformation.getId(entity), entityInformation.getVersion(entity),
					entityInformation.getCollectionName()));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.CrudRepository#delete(java.lang.Iterable)
	 */
	@Override
	public void deleteAll(Iterable<? extends T> entities) {

		Assert.notNull(entities, "The given Iterable of entities not be null!");

		entities.forEach(this::delete);
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.CrudRepository#deleteAll()
	 */
	@Override
	public void deleteAll() {
		mongoOperations.remove(new Query(), entityInformation.getCollectionName());
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.CrudRepository#findAll()
	 */
	@Override
	public List<T> findAll() {
		return findAll(new Query());
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.CrudRepository#findAllById(java.lang.Iterable)
	 */
	@Override
	public Iterable<T> findAllById(Iterable<ID> ids) {

		return findAll(new Query(new Criteria(entityInformation.getIdAttribute())
				.in(Streamable.of(ids).stream().collect(StreamUtils.toUnmodifiableList()))));
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.PagingAndSortingRepository#findAll(ghost.framework.data.domain.Pageable)
	 */
	@Override
	public Page<T> findAll(Pageable pageable) {

		Assert.notNull(pageable, "Pageable must not be null!");

		Long count = count();
		List<T> list = findAll(new Query().with(pageable));

		return new PageImpl<>(list, pageable, count);
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.PagingAndSortingRepository#findAll(ghost.framework.data.domain.Sort)
	 */
	@Override
	public List<T> findAll(Sort sort) {

		Assert.notNull(sort, "Sort must not be null!");

		return findAll(new Query().with(sort));
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.jpa.repository.MongoRepository#insert(java.lang.Object)
	 */
	@Override
	public <S extends T> S insert(S entity) {

		Assert.notNull(entity, "Entity must not be null!");

		return mongoOperations.insert(entity, entityInformation.getCollectionName());
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.jpa.repository.MongoRepository#insert(java.lang.Iterable)
	 */
	@Override
	public <S extends T> List<S> insert(Iterable<S> entities) {

		Assert.notNull(entities, "The given Iterable of entities not be null!");

		List<S> list = Streamable.of(entities).stream().collect(StreamUtils.toUnmodifiableList());

		if (list.isEmpty()) {
			return list;
		}

		return new ArrayList<>(mongoOperations.insertAll(list));
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.jpa.repository.MongoRepository#findAllByExample(ghost.framework.data.domain.Example, ghost.framework.data.domain.Pageable)
	 */
	@Override
	public <S extends T> Page<S> findAll(final Example<S> example, Pageable pageable) {

		Assert.notNull(example, "Sample must not be null!");
		Assert.notNull(pageable, "Pageable must not be null!");

		Query query = new Query(new Criteria().alike(example)) //
				.collation(entityInformation.getCollation()).with(pageable); //

		List<S> list = mongoOperations.find(query, example.getProbeType(), entityInformation.getCollectionName());

		return PageableExecutionUtils.getPage(list, pageable,
				() -> mongoOperations.count(Query.of(query).limit(-1).skip(-1), example.getProbeType(), entityInformation.getCollectionName()));
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.jpa.repository.MongoRepository#findAllByExample(ghost.framework.data.domain.Example, ghost.framework.data.domain.Sort)
	 */
	@Override
	public <S extends T> List<S> findAll(Example<S> example, Sort sort) {

		Assert.notNull(example, "Sample must not be null!");
		Assert.notNull(sort, "Sort must not be null!");

		Query query = new Query(new Criteria().alike(example)) //
				.collation(entityInformation.getCollation()) //
				.with(sort);

		return mongoOperations.find(query, example.getProbeType(), entityInformation.getCollectionName());
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.jpa.repository.MongoRepository#findAllByExample(ghost.framework.data.domain.Example)
	 */
	@Override
	public <S extends T> List<S> findAll(Example<S> example) {
		return findAll(example, Sort.unsorted());
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.query.QueryByExampleExecutor#findOne(ghost.framework.data.domain.Example)
	 */
	@Override
	public <S extends T> Optional<S> findOne(Example<S> example) {

		Assert.notNull(example, "Sample must not be null!");

		Query query = new Query(new Criteria().alike(example)) //
				.collation(entityInformation.getCollation());

		return Optional
				.ofNullable(mongoOperations.findOne(query, example.getProbeType(), entityInformation.getCollectionName()));
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.query.QueryByExampleExecutor#count(ghost.framework.data.domain.Example)
	 */
	@Override
	public <S extends T> long count(Example<S> example) {

		Assert.notNull(example, "Sample must not be null!");

		Query query = new Query(new Criteria().alike(example)) //
				.collation(entityInformation.getCollation());

		return mongoOperations.count(query, example.getProbeType(), entityInformation.getCollectionName());
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.query.QueryByExampleExecutor#exists(ghost.framework.data.domain.Example)
	 */
	@Override
	public <S extends T> boolean exists(Example<S> example) {

		Assert.notNull(example, "Sample must not be null!");

		Query query = new Query(new Criteria().alike(example)) //
				.collation(entityInformation.getCollation());

		return mongoOperations.exists(query, example.getProbeType(), entityInformation.getCollectionName());
	}

	private Query getIdQuery(Object id) {
		return new Query(getIdCriteria(id));
	}

	private Criteria getIdCriteria(Object id) {
		return where(entityInformation.getIdAttribute()).is(id);
	}

	private List<T> findAll(@Nullable Query query) {

		if (query == null) {
			return Collections.emptyList();
		}

		return mongoOperations.find(query, entityInformation.getJavaType(), entityInformation.getCollectionName());
	}
}