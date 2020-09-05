/*
 * Copyright 2018-2020 the original author or authors.
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
package ghost.framework.data.mongodb.core.messaging;

import com.mongodb.CursorType;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Collation;
import org.bson.Document;
import ghost.framework.data.mongodb.core.MongoTemplate;
import ghost.framework.data.mongodb.core.convert.QueryMapper;
import ghost.framework.data.mongodb.core.messaging.SubscriptionRequest.RequestOptions;
import ghost.framework.data.mongodb.core.messaging.TailableCursorRequest.TailableCursorRequestOptions;
import ghost.framework.data.mongodb.core.query.Query;
import ghost.framework.util.ErrorHandler;

import java.util.concurrent.TimeUnit;

/**
 * @author Christoph Strobl
 * @since 2.1
 */
class TailableCursorTask extends CursorReadingTask<Document, Object> {

	private QueryMapper queryMapper;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public TailableCursorTask(MongoTemplate template, TailableCursorRequest<?> request, Class<?> targetType,
			ErrorHandler errorHandler) {
		super(template, (TailableCursorRequest) request, (Class) targetType, errorHandler);
		queryMapper = new QueryMapper(template.getConverter());
	}

	/* 
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.core.messaging.CursorReadingTask#initCursor(ghost.framework.data.mongodb.core.MongoTemplate, ghost.framework.data.mongodb.core.messaging.SubscriptionRequest.RequestOptions, java.lang.Class)
	 */
	@Override
	protected MongoCursor<Document> initCursor(MongoTemplate template, RequestOptions options, Class<?> targetType) {

		Document filter = new Document();
		Collation collation = null;

		if (options instanceof TailableCursorRequest.TailableCursorRequestOptions) {

			TailableCursorRequestOptions requestOptions = (TailableCursorRequestOptions) options;
			if (requestOptions.getQuery().isPresent()) {

				Query query = requestOptions.getQuery().get();

				filter.putAll(queryMapper.getMappedObject(query.getQueryObject(), template.getConverter().getMappingContext()
						.getPersistentEntity(targetType.equals(Document.class) ? Object.class : targetType)));

				collation = query.getCollation().map(ghost.framework.data.mongodb.core.query.Collation::toMongoCollation)
						.orElse(null);
			}
		}

		FindIterable<Document> iterable = template.getCollection(options.getCollectionName()).find(filter)
				.cursorType(CursorType.TailableAwait).noCursorTimeout(true);

		if (collation != null) {
			iterable = iterable.collation(collation);
		}

		if (!options.maxAwaitTime().isZero()) {
			iterable = iterable.maxAwaitTime(options.maxAwaitTime().toMillis(), TimeUnit.MILLISECONDS);
		}

		return iterable.iterator();
	}
}
