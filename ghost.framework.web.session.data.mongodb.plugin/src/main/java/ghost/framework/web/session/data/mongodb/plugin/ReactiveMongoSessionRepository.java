/*
 * Copyright 2019 the original author or authors.
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
package ghost.framework.web.session.data.mongodb.plugin;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ghost.framework.beans.factory.InitializingBean;
import ghost.framework.context.ApplicationEvent;
import ghost.framework.context.ApplicationEventPublisher;
import ghost.framework.context.ApplicationEventPublisherAware;
import ghost.framework.data.mongodb.core.MongoOperations;
import ghost.framework.data.mongodb.core.ReactiveMongoOperations;
import ghost.framework.data.mongodb.core.index.IndexOperations;
import ghost.framework.session.ReactiveSessionRepository;
import ghost.framework.session.events.SessionCreatedEvent;
import ghost.framework.session.events.SessionDeletedEvent;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static ghost.framework.data.mongodb.core.query.Criteria.*;
import static ghost.framework.data.mongodb.core.query.Criteria.where;
import static ghost.framework.data.mongodb.core.query.Query.*;
import static ghost.framework.data.mongodb.core.query.Query.query;
import static ghost.framework.session.data.mongo.MongoSessionUtils.*;
import static ghost.framework.session.data.mongo.MongoSessionUtils.convertToDBObject;
import static ghost.framework.session.data.mongo.MongoSessionUtils.convertToSession;

/**
 * A {@link ReactiveSessionRepository} implementation that uses Spring Data MongoDB.
 * 
 * @author Greg Turnquist
 * @since 2.2.0
 */
public class ReactiveMongoSessionRepository
		implements ReactiveSessionRepository<MongoSession>, ApplicationEventPublisherAware, InitializingBean {

	/**
	 * The default time period in seconds in which a session will expire.
	 */
	public static final int DEFAULT_INACTIVE_INTERVAL = 1800;

	/**
	 * The default collection name for storing session.
	 */
	public static final String DEFAULT_COLLECTION_NAME = "sessions";

	private static final Logger logger = LoggerFactory.getLogger(ReactiveMongoSessionRepository.class);

	private final ReactiveMongoOperations mongoOperations;

	private Integer maxInactiveIntervalInSeconds = DEFAULT_INACTIVE_INTERVAL;
	private String collectionName = DEFAULT_COLLECTION_NAME;
	private AbstractMongoSessionConverter mongoSessionConverter = new JdkMongoSessionConverter(
			Duration.ofSeconds(this.maxInactiveIntervalInSeconds));
	private MongoOperations blockingMongoOperations;
	private ApplicationEventPublisher eventPublisher;

	public ReactiveMongoSessionRepository(ReactiveMongoOperations mongoOperations) {
		this.mongoOperations = mongoOperations;
	}

	/**
	 * Creates a new {@link MongoSession} that is capable of being persisted by this {@link ReactiveSessionRepository}.
	 * <p>
	 * This allows optimizations and customizations in how the {@link MongoSession} is persisted. For example, the
	 * implementation returned might keep track of the changes ensuring that only the delta needs to be persisted on a
	 * save.
	 * </p>
	 *
	 * @return a new {@link MongoSession} that is capable of being persisted by this {@link ReactiveSessionRepository}
	 */
	@Override
	public Mono<MongoSession> createSession() {

		return Mono.justOrEmpty(this.maxInactiveIntervalInSeconds) //
				.map(MongoSession::new) //
				.doOnNext(mongoSession -> publishEvent(new SessionCreatedEvent(this, mongoSession))) //
				.switchIfEmpty(Mono.just(new MongoSession()));
	}

	@Override
	public Mono<Void> save(MongoSession session) {

		return Mono //
				.justOrEmpty(convertToDBObject(this.mongoSessionConverter, session)) //
				.flatMap(dbObject -> {
					if (session.hasChangedSessionId()) {

						return this.mongoOperations
								.remove(query(where("_id").is(session.getOriginalSessionId())), this.collectionName) //
								.then(this.mongoOperations.save(dbObject, this.collectionName));
					} else {

						return this.mongoOperations.save(dbObject, this.collectionName);
					}
				}) //
				.then();
	}

	@Override
	public Mono<MongoSession> findById(String id) {

		return findSession(id) //
				.map(document -> convertToSession(this.mongoSessionConverter, document)) //
				.filter(mongoSession -> !mongoSession.isExpired()) //
				.switchIfEmpty(Mono.defer(() -> this.deleteById(id).then(Mono.empty())));
	}

	@Override
	public Mono<Void> deleteById(String id) {

		return findSession(id) //
				.flatMap(document -> this.mongoOperations.remove(document, this.collectionName) //
						.then(Mono.just(document))) //
				.map(document -> convertToSession(this.mongoSessionConverter, document)) //
				.doOnNext(mongoSession -> publishEvent(new SessionDeletedEvent(this, mongoSession))) //
				.then();
	}

	/**
	 * Do not use {@link ghost.framework.data.mongodb.core.index.ReactiveIndexOperations} to ensure indexes exist.
	 * Instead, get a blocking {@link IndexOperations} and use that instead, if possible.
	 */
	@Override
	public void afterPropertiesSet() {

		if (this.blockingMongoOperations != null) {
			IndexOperations indexOperations = this.blockingMongoOperations.indexOps(this.collectionName);
			this.mongoSessionConverter.ensureIndexes(indexOperations);
		}
	}

	private Mono<Document> findSession(String id) {
		return this.mongoOperations.findById(id, Document.class, this.collectionName);
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}

	private void publishEvent(ApplicationEvent event) {

		try {
			this.eventPublisher.publishEvent(event);
		} catch (Throwable ex) {
			logger.error("Error publishing " + event + ".", ex);
		}
	}

	public Integer getMaxInactiveIntervalInSeconds() {
		return this.maxInactiveIntervalInSeconds;
	}

	public void setMaxInactiveIntervalInSeconds(final Integer maxInactiveIntervalInSeconds) {
		this.maxInactiveIntervalInSeconds = maxInactiveIntervalInSeconds;
	}

	public String getCollectionName() {
		return this.collectionName;
	}

	public void setCollectionName(final String collectionName) {
		this.collectionName = collectionName;
	}

	public void setMongoSessionConverter(final AbstractMongoSessionConverter mongoSessionConverter) {
		this.mongoSessionConverter = mongoSessionConverter;
	}

	public void setBlockingMongoOperations(final MongoOperations blockingMongoOperations) {
		this.blockingMongoOperations = blockingMongoOperations;
	}
}