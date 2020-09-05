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
package ghost.framework.data.mongodb.jpa.repository.support;

import com.mongodb.client.result.DeleteResult;
import org.reactivestreams.Publisher;
import ghost.framework.dao.IncorrectResultSizeDataAccessException;
import ghost.framework.dao.OptimisticLockingFailureException;
import ghost.framework.data.domain.Example;
import ghost.framework.data.domain.Sort;
import ghost.framework.data.mongodb.core.ReactiveMongoOperations;
import ghost.framework.data.mongodb.core.query.Criteria;
import ghost.framework.data.mongodb.core.query.Query;
import ghost.framework.data.mongodb.jpa.repository.ReactiveMongoRepository;
import ghost.framework.data.mongodb.jpa.repository.query.MongoEntityInformation;
import ghost.framework.data.util.StreamUtils;
import ghost.framework.data.util.Streamable;
import ghost.framework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import static ghost.framework.data.mongodb.core.query.Criteria.*;
import static ghost.framework.data.mongodb.core.query.Criteria.where;

/**
 * Reactive repository base implementation for Mongo.
 *
 * @author Mark Paluch
 * @author Oliver Gierke
 * @author Christoph Strobl
 * @author Ruben J Garcia
 * @since 2.0
 */
public class SimpleReactiveMongoRepository<T, ID extends Serializable> implements ReactiveMongoRepository<T, ID> {

	private final MongoEntityInformation<T, ID> entityInformation;
	private final ReactiveMongoOperations mongoOperations;

	public SimpleReactiveMongoRepository(MongoEntityInformation<T, ID> entityInformation,
			ReactiveMongoOperations mongoOperations) {

		Assert.notNull(entityInformation, "EntityInformation must not be null!");
		Assert.notNull(mongoOperations, "MongoOperations must not be null!");

		this.entityInformation = entityInformation;
		this.mongoOperations = mongoOperations;
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.reactive.ReactiveCrudRepository#findById(java.lang.Object)
	 */
	@Override
	public Mono<T> findById(ID id) {

		Assert.notNull(id, "The given id must not be null!");

		return mongoOperations.findById(id, entityInformation.getJavaType(), entityInformation.getCollectionName());
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.reactive.ReactiveCrudRepository#findById(org.reactivestreams.Publisher)
	 */
	@Override
	public Mono<T> findById(Publisher<ID> publisher) {

		Assert.notNull(publisher, "The given id must not be null!");

		return Mono.from(publisher).flatMap(
				id -> mongoOperations.findById(id, entityInformation.getJavaType(), entityInformation.getCollectionName()));
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.query.ReactiveQueryByExampleExecutor#findOne(ghost.framework.data.domain.Example)
	 */
	@Override
	public <S extends T> Mono<S> findOne(Example<S> example) {

		Assert.notNull(example, "Sample must not be null!");

		Query query = new Query(new Criteria().alike(example)) //
				.collation(entityInformation.getCollation()) //
				.limit(2);

		return mongoOperations.find(query, example.getProbeType(), entityInformation.getCollectionName()).buffer(2)
				.map(vals -> {

					if (vals.size() > 1) {
						throw new IncorrectResultSizeDataAccessException(1);
					}
					return vals.iterator().next();
				}).next();
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.reactive.ReactiveCrudRepository#existsById(java.lang.Object)
	 */
	@Override
	public Mono<Boolean> existsById(ID id) {

		Assert.notNull(id, "The given id must not be null!");

		return mongoOperations.exists(getIdQuery(id), entityInformation.getJavaType(),
				entityInformation.getCollectionName());
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.reactive.ReactiveCrudRepository#existsById(org.reactivestreams.Publisher)
	 */
	@Override
	public Mono<Boolean> existsById(Publisher<ID> publisher) {

		Assert.notNull(publisher, "The given id must not be null!");

		return Mono.from(publisher).flatMap(id -> mongoOperations.exists(getIdQuery(id), entityInformation.getJavaType(),
				entityInformation.getCollectionName()));

	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.query.ReactiveQueryByExampleExecutor#exists(ghost.framework.data.domain.Example)
	 */
	@Override
	public <S extends T> Mono<Boolean> exists(Example<S> example) {

		Assert.notNull(example, "Sample must not be null!");

		Query query = new Query(new Criteria().alike(example)) //
				.collation(entityInformation.getCollation());

		return mongoOperations.exists(query, example.getProbeType(), entityInformation.getCollectionName());
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.reactive.ReactiveSortingRepository#findAll()
	 */
	@Override
	public Flux<T> findAll() {
		return findAll(new Query());
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.reactive.ReactiveCrudRepository#findAllById(java.lang.Iterable)
	 */
	@Override
	public Flux<T> findAllById(Iterable<ID> ids) {

		Assert.notNull(ids, "The given Iterable of Id's must not be null!");

		return findAll(new Query(new Criteria(entityInformation.getIdAttribute())
				.in(Streamable.of(ids).stream().collect(StreamUtils.toUnmodifiableList()))));
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.reactive.ReactiveCrudRepository#findAllById(org.reactivestreams.Publisher)
	 */
	@Override
	public Flux<T> findAllById(Publisher<ID> ids) {

		Assert.notNull(ids, "The given Publisher of Id's must not be null!");

		return Flux.from(ids).buffer().flatMap(this::findAllById);
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.reactive.ReactiveSortingRepository#findAll(ghost.framework.data.domain.Sort)
	 */
	@Override
	public Flux<T> findAll(Sort sort) {

		Assert.notNull(sort, "Sort must not be null!");

		return findAll(new Query().with(sort));
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.jpa.repository.ReactiveMongoRepository#findAll(ghost.framework.data.domain.Example, ghost.framework.data.domain.Sort)
	 */
	@Override
	public <S extends T> Flux<S> findAll(Example<S> example, Sort sort) {

		Assert.notNull(example, "Sample must not be null!");
		Assert.notNull(sort, "Sort must not be null!");

		Query query = new Query(new Criteria().alike(example)) //
				.collation(entityInformation.getCollation()) //
				.with(sort);

		return mongoOperations.find(query, example.getProbeType(), entityInformation.getCollectionName());
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.jpa.repository.ReactiveMongoRepository#findAll(ghost.framework.data.domain.Example)
	 */
	@Override
	public <S extends T> Flux<S> findAll(Example<S> example) {

		Assert.notNull(example, "Example must not be null!");

		return findAll(example, Sort.unsorted());
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.reactive.ReactiveCrudRepository#count()
	 */
	@Override
	public Mono<Long> count() {
		return mongoOperations.count(new Query(), entityInformation.getCollectionName());
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.query.ReactiveQueryByExampleExecutor#count(ghost.framework.data.domain.Example)
	 */
	@Override
	public <S extends T> Mono<Long> count(Example<S> example) {

		Assert.notNull(example, "Sample must not be null!");

		Query query = new Query(new Criteria().alike(example)) //
				.collation(entityInformation.getCollation());

		return mongoOperations.count(query, example.getProbeType(), entityInformation.getCollectionName());
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.jpa.repository.ReactiveMongoRepository#insert(java.lang.Object)
	 */
	@Override
	public <S extends T> Mono<S> insert(S entity) {

		Assert.notNull(entity, "Entity must not be null!");

		return mongoOperations.insert(entity, entityInformation.getCollectionName());
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.jpa.repository.ReactiveMongoRepository#insert(java.lang.Iterable)
	 */
	@Override
	public <S extends T> Flux<S> insert(Iterable<S> entities) {

		Assert.notNull(entities, "The given Iterable of entities must not be null!");

		List<S> source = Streamable.of(entities).stream().collect(StreamUtils.toUnmodifiableList());

		return source.isEmpty() ? Flux.empty() : Flux.from(mongoOperations.insertAll(source));
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.jpa.repository.ReactiveMongoRepository#insert(org.reactivestreams.Publisher)
	 */
	@Override
	public <S extends T> Flux<S> insert(Publisher<S> entities) {

		Assert.notNull(entities, "The given Publisher of entities must not be null!");

		return Flux.from(entities).flatMap(entity -> mongoOperations.insert(entity, entityInformation.getCollectionName()));
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.reactive.ReactiveCrudRepository#save(java.lang.Object)
	 */
	@Override
	public <S extends T> Mono<S> save(S entity) {

		Assert.notNull(entity, "Entity must not be null!");

		if (entityInformation.isNew(entity)) {
			return mongoOperations.insert(entity, entityInformation.getCollectionName());
		}

		return mongoOperations.save(entity, entityInformation.getCollectionName());
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.reactive.ReactiveCrudRepository#saveAll(java.lang.Iterable)
	 */
	@Override
	public <S extends T> Flux<S> saveAll(Iterable<S> entities) {

		Assert.notNull(entities, "The given Iterable of entities must not be null!");

		Streamable<S> source = Streamable.of(entities);

		return source.stream().allMatch(entityInformation::isNew) ? //
				mongoOperations.insert(source.stream().collect(Collectors.toList()), entityInformation.getCollectionName()) : //
				Flux.fromIterable(entities).flatMap(this::save);
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.reactive.ReactiveCrudRepository#saveAll(org.reactivestreams.Publisher)
	 */
	@Override
	public <S extends T> Flux<S> saveAll(Publisher<S> entityStream) {

		Assert.notNull(entityStream, "The given Publisher of entities must not be null!");

		return Flux.from(entityStream).flatMap(entity -> entityInformation.isNew(entity) ? //
				mongoOperations.insert(entity, entityInformation.getCollectionName()).then(Mono.just(entity)) : //
				mongoOperations.save(entity, entityInformation.getCollectionName()).then(Mono.just(entity)));
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.reactive.ReactiveCrudRepository#deleteById(java.lang.Object)
	 */
	@Override
	public Mono<Void> deleteById(ID id) {

		Assert.notNull(id, "The given id must not be null!");

		return mongoOperations
				.remove(getIdQuery(id), entityInformation.getJavaType(), entityInformation.getCollectionName()).then();
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.reactive.ReactiveCrudRepository#deleteById(org.reactivestreams.Publisher)
	 */
	@Override
	public Mono<Void> deleteById(Publisher<ID> publisher) {

		Assert.notNull(publisher, "Id must not be null!");

		return Mono.from(publisher).flatMap(id -> mongoOperations.remove(getIdQuery(id), entityInformation.getJavaType(),
				entityInformation.getCollectionName())).then();
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.reactive.ReactiveCrudRepository#delete(java.lang.Object)
	 */
	@Override
	public Mono<Void> delete(T entity) {

		Assert.notNull(entity, "The given entity must not be null!");

		Mono<DeleteResult> remove = mongoOperations.remove(entity, entityInformation.getCollectionName());

		if (entityInformation.isVersioned()) {

			remove = remove.handle((deleteResult, sink) -> {

				if (deleteResult.wasAcknowledged() && deleteResult.getDeletedCount() == 0) {
					sink.error(new OptimisticLockingFailureException(String.format(
							"The entity with id %s with version %s in %s cannot be deleted! Was it modified or deleted in the meantime?",
							entityInformation.getId(entity), entityInformation.getVersion(entity),
							entityInformation.getCollectionName())));
				} else {
					sink.next(deleteResult);
				}
			});
		}

		return remove.then();
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.reactive.ReactiveCrudRepository#deleteAll(java.lang.Iterable)
	 */
	@Override
	public Mono<Void> deleteAll(Iterable<? extends T> entities) {

		Assert.notNull(entities, "The given Iterable of entities must not be null!");

		return Flux.fromIterable(entities).flatMap(this::delete).then();
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.reactive.ReactiveCrudRepository#deleteAll(org.reactivestreams.Publisher)
	 */
	@Override
	public Mono<Void> deleteAll(Publisher<? extends T> entityStream) {

		Assert.notNull(entityStream, "The given Publisher of entities must not be null!");

		return Flux.from(entityStream)//
				.map(entityInformation::getRequiredId)//
				.flatMap(this::deleteById)//
				.then();
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.reactive.ReactiveCrudRepository#deleteAll()
	 */
	@Override
	public Mono<Void> deleteAll() {
		return mongoOperations.remove(new Query(), entityInformation.getCollectionName()).then(Mono.empty());
	}

	private Query getIdQuery(Object id) {
		return new Query(getIdCriteria(id));
	}

	private Criteria getIdCriteria(Object id) {
		return where(entityInformation.getIdAttribute()).is(id);
	}

	private Flux<T> findAll(Query query) {

		return mongoOperations.find(query, entityInformation.getJavaType(), entityInformation.getCollectionName());
	}
}
