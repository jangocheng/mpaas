/*
 * Copyright 2002-2017 the original author or authors.
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

package ghost.framework.context.converter;

import ghost.framework.beans.annotation.constraints.Nullable;
import ghost.framework.beans.annotation.injection.Autowired;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//import org.springframework.core.convert.ConversionService;
//import org.springframework.lang.Nullable;

/**
 * Converts a {@link Stream} to and from a collection or array, converting the
 * element type if necessary.
 *
 * @author Stephane Nicoll
 * @since 4.2
 */
public class StreamTypeDescriptorConverter implements ConditionalGenericTypeDescriptorConverter {

	private static final TypeDescriptor STREAM_TYPE = TypeDescriptor.valueOf(Stream.class);

	private static final Set<ConvertiblePair> CONVERTIBLE_TYPES = createConvertibleTypes();

	private final ConverterContainer converterContainer;


	public StreamTypeDescriptorConverter(@Autowired ConverterContainer converterContainer) {
		this.converterContainer = converterContainer;
	}


	@Override
	public Set<ConvertiblePair> getConvertibleTypes() {
		return CONVERTIBLE_TYPES;
	}

	@Override
	public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
		if (sourceType.isAssignableTo(STREAM_TYPE)) {
			return matchesFromStream(sourceType.getElementTypeDescriptor(), targetType);
		}
		if (targetType.isAssignableTo(STREAM_TYPE)) {
			return matchesToStream(targetType.getElementTypeDescriptor(), sourceType);
		}
		return false;
	}

	/**
	 * Validate that a {@link Collection} of the elements held within the stream can be
	 * converted to the specified {@code targetType}.
	 * @param elementType the type of the stream elements
	 * @param targetType the type to convert to
	 */
	public boolean matchesFromStream(@Nullable TypeDescriptor elementType, TypeDescriptor targetType) {
		TypeDescriptor collectionOfElement = TypeDescriptor.collection(Collection.class, elementType);
		return this.converterContainer.canConvert(collectionOfElement, targetType);
	}

	/**
	 * Validate that the specified {@code sourceType} can be converted to a {@link Collection} of
	 * the type of the stream elements.
	 * @param elementType the type of the stream elements
	 * @param sourceType the type to convert from
	 */
	public boolean matchesToStream(@Nullable TypeDescriptor elementType, TypeDescriptor sourceType) {
		TypeDescriptor collectionOfElement = TypeDescriptor.collection(Collection.class, elementType);
		return this.converterContainer.canConvert(sourceType, collectionOfElement);
	}

	@Override
	@Nullable
	public Object convert(@Nullable Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
		if (sourceType.isAssignableTo(STREAM_TYPE)) {
			return convertFromStream((Stream<?>) source, sourceType, targetType);
		}
		if (targetType.isAssignableTo(STREAM_TYPE)) {
			return convertToStream(source, sourceType, targetType);
		}
		// Should not happen
		throw new IllegalStateException("Unexpected source/target types");
	}

	@Nullable
	private Object convertFromStream(@Nullable Stream<?> source, TypeDescriptor streamType, TypeDescriptor targetType) {
		List<Object> content = (source != null ? source.collect(Collectors.<Object>toList()) : Collections.emptyList());
		TypeDescriptor listType = TypeDescriptor.collection(List.class, streamType.getElementTypeDescriptor());
		return this.converterContainer.convert(content, listType, targetType);
	}

	private Object convertToStream(@Nullable Object source, TypeDescriptor sourceType, TypeDescriptor streamType) {
		TypeDescriptor targetCollection = TypeDescriptor.collection(List.class, streamType.getElementTypeDescriptor());
		List<?> target = (List<?>) this.converterContainer.convert(source, sourceType, targetCollection);
		if (target == null) {
			target = Collections.emptyList();
		}
		return target.stream();
	}


	private static Set<ConvertiblePair> createConvertibleTypes() {
		Set<ConvertiblePair> convertiblePairs = new HashSet<>();
		convertiblePairs.add(new ConvertiblePair(Stream.class, Collection.class));
		convertiblePairs.add(new ConvertiblePair(Stream.class, Object[].class));
		convertiblePairs.add(new ConvertiblePair(Collection.class, Stream.class));
		convertiblePairs.add(new ConvertiblePair(Object[].class, Stream.class));
		return convertiblePairs;
	}

}
