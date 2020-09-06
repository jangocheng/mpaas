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
package ghost.framework.data.mongodb.core;

import ghost.framework.data.commons.util.StreamUtils;
import ghost.framework.data.mongodb.core.query.Update;
import ghost.framework.data.mongodb.core.query.UpdateDefinition;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Collection;
import java.util.List;

/**
 * A MongoDB document in its mapped state. I.e. after a source document has been mapped using mapping information of the
 * entity the source document was supposed to represent.
 *
 * @author Oliver Gierke
 * @since 2.1
 */
public class MappedDocument {

	private static final String ID_FIELD = "_id";
	private static final Document ID_ONLY_PROJECTION = new Document(ID_FIELD, 1);

	private final Document document;

	private MappedDocument(Document document) {
		this.document = document;
	}

	public static MappedDocument of(Document document) {
		return new MappedDocument(document);
	}

	public static Document getIdOnlyProjection() {
		return ID_ONLY_PROJECTION;
	}

	public static Document getIdIn(Collection<?> ids) {
		return new Document(ID_FIELD, new Document("$in", ids));
	}

	public static List<Object> toIds(Collection<Document> documents) {

		return documents.stream()//
				.map(it -> it.get(ID_FIELD))//
				.collect(StreamUtils.toUnmodifiableList());
	}

	public boolean hasId() {
		return document.containsKey(ID_FIELD);
	}

	public boolean hasNonNullId() {
		return hasId() && document.get(ID_FIELD) != null;
	}

	public Object getId() {
		return document.get(ID_FIELD);
	}

	public <T> T getId(Class<T> type) {
		return document.get(ID_FIELD, type);
	}

	public boolean isIdPresent(Class<?> type) {
		return type.isInstance(getId());
	}

	public Bson getIdFilter() {
		return new Document(ID_FIELD, document.get(ID_FIELD));
	}

	public Object get(String key) {
		return document.get(key);
	}

	public UpdateDefinition updateWithoutId() {
		return new MappedUpdate(Update.fromDocument(document, ID_FIELD));
	}

	public Document getDocument() {
		return this.document;
	}

	/**
	 * An {@link UpdateDefinition} that indicates that the {@link #getUpdateObject() update object} has already been
	 * mapped to the specific domain type.
	 *
	 * @author Christoph Strobl
	 * @since 2.2
	 */
	class MappedUpdate implements UpdateDefinition {

		private final Update delegate;

		MappedUpdate(Update delegate) {
			this.delegate = delegate;
		}

		/*
		 * (non-Javadoc)
		 * @see ghost.framework.data.mongodb.core.query.UpdateDefinition#getUpdateObject()
		 */
		@Override
		public Document getUpdateObject() {
			return delegate.getUpdateObject();
		}

		/*
		 * (non-Javadoc)
		 * @see ghost.framework.data.mongodb.core.query.UpdateDefinition#modifies(java.lang.String)
		 */
		@Override
		public boolean modifies(String key) {
			return delegate.modifies(key);
		}

		/*
		 * (non-Javadoc)
		 * @see ghost.framework.data.mongodb.core.query.UpdateDefinition#inc(java.lang.String)
		 */
		@Override
		public void inc(String version) {
			delegate.inc(version);
		}

		/*
		 * (non-Javadoc)
		 * @see ghost.framework.data.mongodb.core.query.UpdateDefinition#isIsolated()
		 */
		@Override
		public Boolean isIsolated() {
			return delegate.isIsolated();
		}

		/*
		 * (non-Javadoc)
		 * @see ghost.framework.data.mongodb.core.query.UpdateDefinition#getArrayFilters()
		 */
		@Override
		public List<ArrayFilter> getArrayFilters() {
			return delegate.getArrayFilters();
		}
	}
}