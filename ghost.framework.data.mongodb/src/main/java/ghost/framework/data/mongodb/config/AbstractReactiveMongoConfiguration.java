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
package ghost.framework.data.mongodb.config;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoClientSettings.Builder;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import ghost.framework.beans.annotation.bean.Bean;
import ghost.framework.beans.annotation.stereotype.Configuration;
import ghost.framework.data.mongodb.ReactiveMongoDatabaseFactory;
import ghost.framework.data.mongodb.SpringDataMongoDB;
import ghost.framework.data.mongodb.core.ReactiveMongoOperations;
import ghost.framework.data.mongodb.core.ReactiveMongoTemplate;
import ghost.framework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory;
import ghost.framework.data.mongodb.core.convert.MappingMongoConverter;
import ghost.framework.data.mongodb.core.convert.MongoCustomConversions;
import ghost.framework.data.mongodb.core.convert.NoOpDbRefResolver;
import ghost.framework.data.mongodb.core.mapping.MongoMappingContext;

/**
 * Base class for reactive Spring Data MongoDB configuration using JavaConfig.
 *
 * @author Mark Paluch
 * @author Christoph Strobl
 * @since 2.0
 * @see MongoConfigurationSupport
 */
@Configuration(proxy = false)
public abstract class AbstractReactiveMongoConfiguration extends MongoConfigurationSupport {

	/**
	 * Return the Reactive Streams {@link MongoClient} instance to connect to. Annotate with {@link Bean} in case you want
	 * to expose a {@link MongoClient} instance to the {@link ghost.framework.context.ApplicationContext}. <br />
	 * Override {@link #mongoClientSettings()} to configure connection details.
	 *
	 * @return never {@literal null}.
	 * @see #mongoClientSettings()
	 * @see #configureClientSettings(Builder)
	 */
	public MongoClient reactiveMongoClient() {
		return createReactiveMongoClient(mongoClientSettings());
	}

	/**
	 * Creates {@link ReactiveMongoOperations}.
	 *
	 * @see #reactiveMongoDbFactory()
	 * @see #mappingMongoConverter(ReactiveMongoDatabaseFactory, MongoCustomConversions, MongoMappingContext)
	 * @return never {@literal null}.
	 */
	@Bean
	public ReactiveMongoTemplate reactiveMongoTemplate(ReactiveMongoDatabaseFactory databaseFactory,
			MappingMongoConverter mongoConverter) {
		return new ReactiveMongoTemplate(databaseFactory, mongoConverter);
	}

	/**
	 * Creates a {@link ReactiveMongoDatabaseFactory} to be used by the {@link ReactiveMongoOperations}. Will use the
	 * {@link MongoClient} instance configured in {@link #reactiveMongoClient()}.
	 *
	 * @see #reactiveMongoClient()
	 * @see #reactiveMongoTemplate(ReactiveMongoDatabaseFactory, MappingMongoConverter)
	 * @return never {@literal null}.
	 */
	@Bean
	public ReactiveMongoDatabaseFactory reactiveMongoDbFactory() {
		return new SimpleReactiveMongoDatabaseFactory(reactiveMongoClient(), getDatabaseName());
	}

	/**
	 * Creates a {@link MappingMongoConverter} using the configured {@link #reactiveMongoDbFactory()} and
	 * {@link #mongoMappingContext(MongoCustomConversions)}. Will get {@link #customConversions()} applied.
	 *
	 * @see #customConversions()
	 * @see #mongoMappingContext(MongoCustomConversions)
	 * @see #reactiveMongoDbFactory()
	 * @return never {@literal null}.
	 */
	@Bean
	public MappingMongoConverter mappingMongoConverter(ReactiveMongoDatabaseFactory databaseFactory,
			MongoCustomConversions customConversions, MongoMappingContext mappingContext) {

		MappingMongoConverter converter = new MappingMongoConverter(NoOpDbRefResolver.INSTANCE, mappingContext);
		converter.setCustomConversions(customConversions);
		converter.setCodecRegistryProvider(databaseFactory);

		return converter;
	}

	/**
	 * Create the Reactive Streams {@link MongoClient} instance with given {@link MongoClientSettings}.
	 *
	 * @return never {@literal null}.
	 * @since 3.0
	 */
	protected MongoClient createReactiveMongoClient(MongoClientSettings settings) {
		return MongoClients.create(settings, SpringDataMongoDB.driverInformation());
	}
}
