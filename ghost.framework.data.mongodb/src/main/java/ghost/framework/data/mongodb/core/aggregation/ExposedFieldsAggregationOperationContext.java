/*
 * Copyright 2013-2020 the original author or authors.
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
import ghost.framework.data.mongodb.core.aggregation.ExposedFields.DirectFieldReference;
import ghost.framework.data.mongodb.core.aggregation.ExposedFields.ExposedField;
import ghost.framework.data.mongodb.core.aggregation.ExposedFields.FieldReference;
import ghost.framework.beans.annotation.constraints.Nullable;
import ghost.framework.util.Assert;

/**
 * {@link AggregationOperationContext} that combines the available field references from a given
 * {@code AggregationOperationContext} and an {@link FieldsExposingAggregationOperation}.
 *
 * @author Thomas Darimont
 * @author Oliver Gierke
 * @author Mark Paluch
 * @author Christoph Strobl
 * @since 1.4
 */
class ExposedFieldsAggregationOperationContext implements AggregationOperationContext {

	private final ExposedFields exposedFields;
	private final AggregationOperationContext rootContext;

	/**
	 * Creates a new {@link ExposedFieldsAggregationOperationContext} from the given {@link ExposedFields}. Uses the given
	 * {@link AggregationOperationContext} to perform a mapping to mongo types if necessary.
	 *
	 * @param exposedFields must not be {@literal null}.
	 * @param rootContext must not be {@literal null}.
	 */
	public ExposedFieldsAggregationOperationContext(ExposedFields exposedFields,
			AggregationOperationContext rootContext) {

		Assert.notNull(exposedFields, "ExposedFields must not be null!");
		Assert.notNull(rootContext, "RootContext must not be null!");

		this.exposedFields = exposedFields;
		this.rootContext = rootContext;
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.core.aggregation.AggregationOperationContext#getMappedObject(org.bson.Document, java.lang.Class)
	 */
	@Override
	public Document getMappedObject(Document document, @Nullable Class<?> type) {
		return rootContext.getMappedObject(document, type);
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.core.aggregation.AggregationOperationContext#getReference(ghost.framework.data.mongodb.core.aggregation.ExposedFields.AvailableField)
	 */
	@Override
	public FieldReference getReference(Field field) {

		if (field.isInternal()) {
			return new DirectFieldReference(new ExposedField(field, true));
		}

		return getReference(field, field.getTarget());
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.core.aggregation.AggregationOperationContext#getReference(java.lang.String)
	 */
	@Override
	public FieldReference getReference(String name) {
		return getReference(null, name);
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.core.aggregation.AggregationOperationContext#getFields(java.lang.Class)
	 */
	@Override
	public Fields getFields(Class<?> type) {
		return rootContext.getFields(type);
	}

	/**
	 * Returns a {@link FieldReference} to the given {@link Field} with the given {@code name}.
	 *
	 * @param field may be {@literal null}.
	 * @param name must not be {@literal null}.
	 * @return
	 */
	private FieldReference getReference(@Nullable Field field, String name) {

		Assert.notNull(name, "Name must not be null!");

		FieldReference exposedField = resolveExposedField(field, name);
		if (exposedField != null) {
			return exposedField;
		}

		throw new IllegalArgumentException(String.format("Invalid reference '%s'!", name));
	}

	/**
	 * Resolves a {@link Field}/{@code name} for a {@link FieldReference} if possible.
	 *
	 * @param field may be {@literal null}.
	 * @param name must not be {@literal null}.
	 * @return the resolved reference or {@literal null}.
	 */
	@Nullable
	protected FieldReference resolveExposedField(@Nullable Field field, String name) {

		ExposedField exposedField = exposedFields.getField(name);

		if (exposedField != null) {

			if (field != null) {
				// we return a FieldReference to the given field directly to make sure that we reference the proper alias here.
				return new DirectFieldReference(new ExposedField(field, exposedField.isSynthetic()));
			}

			return new DirectFieldReference(exposedField);
		}

		if (name.contains(".")) {

			// for nested field references we only check that the root field exists.
			ExposedField rootField = exposedFields.getField(name.split("\\.")[0]);

			if (rootField != null) {

				// We have to synthetic to true, in order to render the field-name as is.
				return new DirectFieldReference(new ExposedField(name, true));
			}
		}
		return null;
	}
}