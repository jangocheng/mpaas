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

import ghost.framework.beans.annotation.constraints.Nullable;
import ghost.framework.data.mongodb.core.aggregation.ExposedFields.FieldReference;
import ghost.framework.util.Assert;
import org.bson.Document;

/**
 * The context for an {@link AggregationOperation}.
 *
 * @author Oliver Gierke
 * @author Christoph Strobl
 * @since 1.3
 */
public interface AggregationOperationContext {

	/**
	 * Returns the mapped {@link Document}, potentially converting the source considering mapping metadata etc.
	 *
	 * @param document will never be {@literal null}.
	 * @return must not be {@literal null}.
	 */
	default Document getMappedObject(Document document) {
		return getMappedObject(document, null);
	}

	/**
	 * Returns the mapped {@link Document}, potentially converting the source considering mapping metadata for the given
	 * type.
	 *
	 * @param document will never be {@literal null}.
	 * @param type can be {@literal null}.
	 * @return must not be {@literal null}.
	 * @since 2.2
	 */
	Document getMappedObject(Document document, @Nullable Class<?> type);

	/**
	 * Returns a {@link FieldReference} for the given field.
	 *
	 * @param field must not be {@literal null}.
	 * @return the {@link FieldReference} for the given {@link Field}.
	 * @throws IllegalArgumentException if the context does not expose a field with the given name
	 */
	FieldReference getReference(Field field);

	/**
	 * Returns the {@link FieldReference} for the field with the given name.
	 *
	 * @param name must not be {@literal null} or empty.
	 * @return the {@link FieldReference} for the field with given {@literal name}.
	 * @throws IllegalArgumentException if the context does not expose a field with the given name
	 */
	FieldReference getReference(String name);

	/**
	 * Returns the {@link Fields} exposed by the type. May be a {@literal class} or an {@literal interface}. The default
	 * implementation uses {@link BeanUtils#getPropertyDescriptors(Class) property descriptors} discover fields from a
	 * {@link Class}.
	 *
	 * @param type must not be {@literal null}.
	 * @return never {@literal null}.
	 * @since 2.2
	 * @see BeanUtils#getPropertyDescriptor(Class, String)
	 */
	default Fields getFields(Class<?> type) {

		Assert.notNull(type, "Type must not be null!");
/*
		return Fields.fields(Arrays.stream(BeanUtils.getPropertyDescriptors(type)) //
				.filter(it -> { // object and default methods
					Method method = it.getReadMethod();
					if (method == null) {
						return false;
					}
					if (ReflectionUtils.isObjectMethod(method)) {
						return false;
					}
					return !method.isDefault();
				}) //
				.map(PropertyDescriptor::getName) //
				.toArray(String[]::new));*/
return null;
	}

	/**
	 * This toggle allows the {@link AggregationOperationContext context} to use any given field name without checking for
	 * its existence. Typically the {@link AggregationOperationContext} fails when referencing unknown fields, those that
	 * are not present in one of the previous stages or the input source, throughout the pipeline.
	 * 
	 * @return a more relaxed {@link AggregationOperationContext}.
	 * @since 3.0
	 */
	default AggregationOperationContext continueOnMissingFieldReference() {
		return this;
	}
}
