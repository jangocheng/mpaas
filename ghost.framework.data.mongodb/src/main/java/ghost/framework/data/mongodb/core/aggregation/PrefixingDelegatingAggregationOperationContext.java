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
package ghost.framework.data.mongodb.core.aggregation;

import org.bson.Document;
import ghost.framework.data.mongodb.core.aggregation.ExposedFields.FieldReference;
import ghost.framework.beans.annotation.constraints.Nullable;

import java.util.*;

/**
 * {@link AggregationOperationContext} implementation prefixing non-command keys on root level with the given prefix.
 * Useful when mapping fields to domain specific types while having to prefix keys for query purpose.
 * <p />
 * Fields to be excluded from prefixing my be added to a {@literal denylist}.
 *
 * @author Christoph Strobl
 * @author Mark Paluch
 * @since 2.1
 */
public class PrefixingDelegatingAggregationOperationContext implements AggregationOperationContext {

	private final AggregationOperationContext delegate;
	private final String prefix;
	private final Set<String> denylist;

	public PrefixingDelegatingAggregationOperationContext(AggregationOperationContext delegate, String prefix) {
		this(delegate, prefix, Collections.emptySet());
	}

	public PrefixingDelegatingAggregationOperationContext(AggregationOperationContext delegate, String prefix,
			Collection<String> denylist) {

		this.delegate = delegate;
		this.prefix = prefix;
		this.denylist = new HashSet<>(denylist);
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.core.aggregation.AggregationOperationContext#getMappedObject(org.bson.Document)
	 */
	@Override
	public Document getMappedObject(Document document) {
		return doPrefix(delegate.getMappedObject(document));
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.core.aggregation.AggregationOperationContext#getMappedObject(org.bson.Document, java.lang.Class)
	 */
	@Override
	public Document getMappedObject(Document document, @Nullable Class<?> type) {
		return doPrefix(delegate.getMappedObject(document, type));
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.core.aggregation.AggregationOperationContext#getReference(ghost.framework.data.mongodb.core.aggregation.Field)
	 */
	@Override
	public FieldReference getReference(Field field) {
		return delegate.getReference(field);
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.core.aggregation.AggregationOperationContext#getReference(java.lang.String)
	 */
	@Override
	public FieldReference getReference(String name) {
		return delegate.getReference(name);
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.core.aggregation.AggregationOperationContext#getFields(java.lang.Class)
	 */
	@Override
	public Fields getFields(Class<?> type) {
		return delegate.getFields(type);
	}

	@SuppressWarnings("unchecked")
	private Document doPrefix(Document source) {

		Document result = new Document();
		for (Map.Entry<String, Object> entry : source.entrySet()) {

			String key = prefixKey(entry.getKey());
			Object value = entry.getValue();

			if (entry.getValue() instanceof Collection) {

				Collection<Object> sourceCollection = (Collection<Object>) entry.getValue();
				value = prefixCollection(sourceCollection);
			}

			result.append(key, value);
		}
		return result;
	}

	private String prefixKey(String key) {
		return (key.startsWith("$") || isDenied(key)) ? key : (prefix + "." + key);
	}

	private Object prefixCollection(Collection<Object> sourceCollection) {

		List<Object> prefixed = new ArrayList<>(sourceCollection.size());

		for (Object o : sourceCollection) {
			if (o instanceof Document) {
				prefixed.add(doPrefix((Document) o));
			} else {
				prefixed.add(o);
			}
		}

		return prefixed;
	}

	private boolean isDenied(String key) {

		if (denylist.contains(key)) {
			return true;
		}

		if (!key.contains(".")) {
			return false;
		}

		for (String denied : denylist) {
			if (key.startsWith(denied + ".")) {
				return true;
			}
		}

		return false;
	}
}
