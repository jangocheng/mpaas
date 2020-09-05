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
package ghost.framework.data.mongodb.core.schema;

import org.bson.Document;
import ghost.framework.util.Assert;

/**
 * Value object representing a MongoDB-specific JSON schema which is the default {@link MongoJsonSchema} implementation.
 *
 * @author Christoph Strobl
 * @author Mark Paluch
 * @since 2.1
 */
class DefaultMongoJsonSchema implements MongoJsonSchema {

	private final JsonSchemaObject root;

	DefaultMongoJsonSchema(JsonSchemaObject root) {
		
		Assert.notNull(root, "Root must not be null!");
		this.root = root;
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.core.schema.MongoJsonSchema#toDocument()
	 */
	@Override
	public Document toDocument() {
		return new Document("$jsonSchema", root.toDocument());
	}
}
