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
package ghost.framework.data.mongodb.core.query;

import ghost.framework.beans.annotation.constraints.Nullable;
import ghost.framework.util.Assert;
import org.bson.Document;

import static ghost.framework.util.ObjectUtils.nullSafeEquals;
import static ghost.framework.util.ObjectUtils.nullSafeHashCode;

/**
 * Custom {@link Query} implementation to setup a basic query from some arbitrary JSON query string.
 *
 * @author Thomas Risberg
 * @author Oliver Gierke
 * @author Christoph Strobl
 * @author Thomas Darimont
 * @author John Willemin
 * @author Mark Paluch
 */
public class BasicQuery extends Query {

	private final Document queryObject;

	private Document fieldsObject;
	private Document sortObject;

	/**
	 * Create a new {@link BasicQuery} given a JSON {@code query}.
	 *
	 * @param query may be {@literal null}.
	 */
	public BasicQuery(@Nullable String query) {
		this(query, null);
	}

	/**
	 * Create a new {@link BasicQuery} given a query {@link Document}.
	 *
	 * @param queryObject must not be {@literal null}.
	 */
	public BasicQuery(Document queryObject) {
		this(queryObject, new Document());
	}

	/**
	 * Create a new {@link BasicQuery} given a JSON {@code query} and {@code fields}.
	 *
	 * @param query may be {@literal null}.
	 * @param fields may be {@literal null}.
	 */
	public BasicQuery(@Nullable String query, @Nullable String fields) {

		this(query != null ? Document.parse(query) : new Document(),
				fields != null ? Document.parse(fields) : new Document());
	}

	/**
	 * Create a new {@link BasicQuery} given a query {@link Document} and field specification {@link Document}.
	 *
	 * @param queryObject must not be {@literal null}.
	 * @param fieldsObject must not be {@literal null}.
	 * @throws IllegalArgumentException when {@code sortObject} or {@code fieldsObject} is {@literal null}.
	 */
	public BasicQuery(Document queryObject, Document fieldsObject) {

		Assert.notNull(queryObject, "Query document must not be null");
		Assert.notNull(fieldsObject, "Field document must not be null");

		this.queryObject = queryObject;
		this.fieldsObject = fieldsObject;
		this.sortObject = new Document();
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.core.query.Query#addCriteria(ghost.framework.data.mongodb.core.query.CriteriaDefinition)
	 */
	@Override
	public Query addCriteria(CriteriaDefinition criteria) {

		this.queryObject.putAll(criteria.getCriteriaObject());

		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.core.query.Query#getQueryObject()
	 */
	@Override
	public Document getQueryObject() {
		return this.queryObject;
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.core.query.Query#getFieldsObject()
	 */
	@Override
	public Document getFieldsObject() {

		Document combinedFieldsObject = new Document();
		combinedFieldsObject.putAll(fieldsObject);
		combinedFieldsObject.putAll(super.getFieldsObject());
		return combinedFieldsObject;
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.core.query.Query#getSortObject()
	 */
	@Override
	public Document getSortObject() {

		Document result = new Document();
		result.putAll(sortObject);

		Document overrides = super.getSortObject();
		result.putAll(overrides);

		return result;
	}

	/**
	 * Set the sort {@link Document}.
	 *
	 * @param sortObject must not be {@literal null}.
	 * @throws IllegalArgumentException when {@code sortObject} is {@literal null}.
	 */
	public void setSortObject(Document sortObject) {

		Assert.notNull(sortObject, "Sort document must not be null");

		this.sortObject = sortObject;
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.core.query.Query#isSorted()
	 */
	@Override
	public boolean isSorted() {
		return super.isSorted() || !sortObject.isEmpty();
	}

	/**
	 * Set the fields (projection) {@link Document}.
	 *
	 * @param fieldsObject must not be {@literal null}.
	 * @throws IllegalArgumentException when {@code fieldsObject} is {@literal null}.
	 * @since 1.6
	 */
	protected void setFieldsObject(Document fieldsObject) {

		Assert.notNull(sortObject, "Field document must not be null");

		this.fieldsObject = fieldsObject;
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.core.query.Query#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {

		if (this == o) {
			return true;
		}

		if (!(o instanceof BasicQuery)) {
			return false;
		}

		BasicQuery that = (BasicQuery) o;

		return querySettingsEquals(that) && //
				nullSafeEquals(fieldsObject, that.fieldsObject) && //
				nullSafeEquals(queryObject, that.queryObject) && //
				nullSafeEquals(sortObject, that.sortObject);
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.core.query.Query#hashCode()
	 */
	@Override
	public int hashCode() {

		int result = super.hashCode();
		result = 31 * result + nullSafeHashCode(queryObject);
		result = 31 * result + nullSafeHashCode(fieldsObject);
		result = 31 * result + nullSafeHashCode(sortObject);

		return result;
	}
}
