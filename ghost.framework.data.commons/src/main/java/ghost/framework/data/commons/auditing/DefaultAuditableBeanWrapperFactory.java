/*
 * Copyright 2012-2020 the original author or authors.
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
package ghost.framework.data.commons.auditing;


import ghost.framework.beans.annotation.constraints.Nullable;
import ghost.framework.context.ResolvableType;
import ghost.framework.context.converter.ConverterContainer;
import ghost.framework.util.Assert;
import ghost.framework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A factory class to {@link AuditableBeanWrapper} instances.
 *
 * @author Oliver Gierke
 * @author Christoph Strobl
 * @author Jens Schauder
 * @author Pavel Horal
 * @since 1.5
 */
class DefaultAuditableBeanWrapperFactory implements AuditableBeanWrapperFactory {

	private ConverterContainer converterContainer;

	public DefaultAuditableBeanWrapperFactory() {

//		DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
//
//		JodaTimeConverters.getConvertersToRegister().forEach(conversionService::addConverter);
//		Jsr310Converters.getConvertersToRegister().forEach(conversionService::addConverter);
//		ThreeTenBackPortConverters.getConvertersToRegister().forEach(conversionService::addConverter);
//
//		this.converterContainer = conversionService;
	}

	ConverterContainer getConverterContainer() {
		return converterContainer;
	}

	/**
	 * Returns an {@link AuditableBeanWrapper} if the given object is capable of being equipped with auditing information.
	 *
	 * @param source the auditing candidate.
	 * @return
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> Optional<AuditableBeanWrapper<T>> getBeanWrapperFor(T source) {

		Assert.notNull(source, "Source must not be null!");

		return Optional.of(source).map(it -> {
			if (it instanceof Auditable) {
				return (AuditableBeanWrapper<T>) new AuditableInterfaceBeanWrapper(converterContainer, (Auditable<Object, ?, TemporalAccessor>) it);
			}

			AnnotationAuditingMetadata metadata = AnnotationAuditingMetadata.getMetadata(it.getClass());

			if (metadata.isAuditable()) {
				return new ReflectionAuditingBeanWrapper<T>(converterContainer, it);
			}

			return null;
		});
	}

	/**
	 * An {@link AuditableBeanWrapper} that works with objects implementing
	 *
	 * @author Oliver Gierke
	 */
	public static class AuditableInterfaceBeanWrapper
			extends DateConvertingAuditableBeanWrapper<Auditable<Object, ?, TemporalAccessor>> {

		private final Auditable<Object, ?, TemporalAccessor> auditable;
		private final Class<? extends TemporalAccessor> type;

		@SuppressWarnings("unchecked")
		public AuditableInterfaceBeanWrapper(ConverterContainer conversionService, Auditable<Object, ?, TemporalAccessor> auditable) {

			super(conversionService);

			this.auditable = auditable;
			this.type = (Class<? extends TemporalAccessor>) ResolvableType.forClass(Auditable.class, auditable.getClass())
					.getGeneric(2).resolve(TemporalAccessor.class);
		}

		/*
		 * (non-Javadoc)
		 * @see ghost.framework.data.auditing.AuditableBeanWrapper#setCreatedBy(java.util.Optional)
		 */
		@Override
		public Object setCreatedBy(Object value) {

			auditable.setCreatedBy(value);
			return value;
		}

		/*
		 * (non-Javadoc)
		 * @see ghost.framework.data.auditing.AuditableBeanWrapper#setCreatedDate(java.util.Optional)
		 */
		@Override
		public TemporalAccessor setCreatedDate(TemporalAccessor value) {

			auditable.setCreatedDate(getAsTemporalAccessor(Optional.of(value), type).orElseThrow(IllegalStateException::new));

			return value;
		}

		/*
		 * (non-Javadoc)
		 * @see ghost.framework.data.auditing.DefaultAuditableBeanWrapperFactory.AuditableInterfaceBeanWrapper#setLastModifiedBy(java.util.Optional)
		 */
		@Override
		public Object setLastModifiedBy(Object value) {

			auditable.setLastModifiedBy(value);

			return value;
		}

		/*
		 * (non-Javadoc)
		 * @see ghost.framework.data.auditing.AuditableBeanWrapper#getLastModifiedDate()
		 */
		@Override
		public Optional<TemporalAccessor> getLastModifiedDate() {
			return getAsTemporalAccessor(auditable.getLastModifiedDate(), TemporalAccessor.class);
		}

		/*
		 * (non-Javadoc)
		 * @see ghost.framework.data.auditing.AuditableBeanWrapper#setLastModifiedDate(java.util.Optional)
		 */
		@Override
		public TemporalAccessor setLastModifiedDate(TemporalAccessor value) {

			auditable
					.setLastModifiedDate(getAsTemporalAccessor(Optional.of(value), type).orElseThrow(IllegalStateException::new));

			return value;
		}

		/*
		 * (non-Javadoc)
		 * @see ghost.framework.data.auditing.AuditableBeanWrapper#getBean()
		 */
		@Override
		public Auditable<Object, ?, TemporalAccessor> getBean() {
			return auditable;
		}
	}

	/**
	 * Base class for {@link AuditableBeanWrapper} implementations that might need to convert {@link TemporalAccessor} values into
	 * compatible types when setting date/time information.
	 *
	 * @author Oliver Gierke
	 * @since 1.8
	 */
	abstract static class DateConvertingAuditableBeanWrapper<T> implements AuditableBeanWrapper<T> {

		private final ConverterContainer conversionService;

		DateConvertingAuditableBeanWrapper(ConverterContainer conversionService) {
			this.conversionService = conversionService;
		}

		/**
		 * Returns the {@link TemporalAccessor} in a type, compatible to the given field.
		 *
		 * @param value can be {@literal null}.
		 * @param targetType must not be {@literal null}.
		 * @param source must not be {@literal null}.
		 * @return
		 */
		@Nullable
		protected Object getDateValueToSet(TemporalAccessor value, Class<?> targetType, Object source) {

			if (TemporalAccessor.class.equals(targetType)) {
				return value;
			}

			if (conversionService.canConvert(value.getClass(), targetType)) {
				return conversionService.convert(value, targetType);
			}

			if (conversionService.canConvert(Date.class, targetType)) {

				if (!conversionService.canConvert(value.getClass(), Date.class)) {
					throw new IllegalArgumentException(
							String.format("Cannot convert date type for member %s! From %s to java.util.Date to %s.", source,
									value.getClass(), targetType));
				}

				Date date = (Date)conversionService.convert(value, Date.class);
				return conversionService.convert(date, targetType);
			}

			throw rejectUnsupportedType(source);
		}

		/**
		 * Returns the given object as {@link TemporalAccessor}.
		 *
		 * @param source can be {@literal null}.
		 * @param target must not be {@literal null}.
		 * @return
		 */
		@SuppressWarnings("unchecked")
		protected <S extends TemporalAccessor> Optional<S> getAsTemporalAccessor(Optional<?> source,
				Class<? extends S> target) {

			return source.map(it -> {

				if (target.isInstance(it)) {
					return (S) it;
				}

				Class<?> typeToConvertTo = Stream.of(target, Instant.class)//
						.filter(type -> target.isAssignableFrom(type))//
						.filter(type -> conversionService.canConvert(it.getClass(), type))//
						.findFirst() //
						.orElseThrow(() -> rejectUnsupportedType(source.map(Object.class::cast).orElseGet(() -> source)));

				return (S) conversionService.convert(it, typeToConvertTo);
			});
		}
	}

	private static IllegalArgumentException rejectUnsupportedType(Object source) {
		return new IllegalArgumentException(String.format("Invalid date type %s for member %s! Supported types are %s.",
				source.getClass(), source, AnnotationAuditingMetadata.SUPPORTED_DATE_TYPES));
	}

	/**
	 * An {@link AuditableBeanWrapper} implementation that sets values on the target object using reflection.
	 *
	 * @author Oliver Gierke
	 */
	static class ReflectionAuditingBeanWrapper<T> extends DateConvertingAuditableBeanWrapper<T> {

		private final AnnotationAuditingMetadata metadata;
		private final T target;

		/**
		 * Creates a new {@link ReflectionAuditingBeanWrapper} to set auditing data on the given target object.
		 *
		 * @param conversionService conversion service for date value type conversions
		 * @param target must not be {@literal null}.
		 */
		public ReflectionAuditingBeanWrapper(ConverterContainer conversionService, T target) {
			super(conversionService);

			Assert.notNull(target, "Target object must not be null!");

			this.metadata = AnnotationAuditingMetadata.getMetadata(target.getClass());
			this.target = target;
		}

		/*
		 * (non-Javadoc)
		 * @see ghost.framework.data.auditing.AuditableBeanWrapper#setCreatedBy(java.util.Optional)
		 */
		@Override
		public Object setCreatedBy(Object value) {
			return setField(metadata.getCreatedByField(), value);
		}

		/*
		 * (non-Javadoc)
		 * @see ghost.framework.data.auditing.AuditableBeanWrapper#setCreatedDate(java.util.Optional)
		 */
		@Override
		public TemporalAccessor setCreatedDate(TemporalAccessor value) {

			return setDateField(metadata.getCreatedDateField(), value);
		}

		/*
		 * (non-Javadoc)
		 * @see ghost.framework.data.auditing.AuditableBeanWrapper#setLastModifiedBy(java.util.Optional)
		 */
		@Override
		public Object setLastModifiedBy(Object value) {
			return setField(metadata.getLastModifiedByField(), value);
		}

		/*
		 * (non-Javadoc)
		 * @see ghost.framework.data.auditing.AuditableBeanWrapper#getLastModifiedDate()
		 */
		@Override
		public Optional<TemporalAccessor> getLastModifiedDate() {

			return getAsTemporalAccessor(metadata.getLastModifiedDateField().map(field -> {

				Object value = ReflectionUtils.getField(field, target);
				return value instanceof Optional ? ((Optional<?>) value).orElse(null) : value;

			}), TemporalAccessor.class);
		}

		/*
		 * (non-Javadoc)
		 * @see ghost.framework.data.auditing.AuditableBeanWrapper#setLastModifiedDate(java.util.Optional)
		 */
		@Override
		public TemporalAccessor setLastModifiedDate(TemporalAccessor value) {
			return setDateField(metadata.getLastModifiedDateField(), value);
		}

		/*
		 * (non-Javadoc)
		 * @see ghost.framework.data.auditing.AuditableBeanWrapper#getBean()
		 */
		@Override
		public T getBean() {
			return target;
		}

		/**
		 * Sets the given field to the given value if present.
		 *
		 * @param field
		 * @param value
		 */
		private <S> S setField(Optional<Field> field, S value) {

			field.ifPresent(it -> ReflectionUtils.setField(it, target, value));

			return value;
		}

		/**
		 * Sets the given field to the given value if the field is not {@literal null}.
		 *
		 * @param field
		 * @param value
		 */
		private TemporalAccessor setDateField(Optional<Field> field, TemporalAccessor value) {

			field.ifPresent(it -> ReflectionUtils.setField(it, target, getDateValueToSet(value, it.getType(), it)));

			return value;
		}
	}
}
