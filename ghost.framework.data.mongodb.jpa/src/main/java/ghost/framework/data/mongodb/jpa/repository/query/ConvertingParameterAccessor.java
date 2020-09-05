/*
 * Copyright 2011-2020 the original author or authors.
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
package ghost.framework.data.mongodb.jpa.repository.query;

import com.mongodb.DBRef;
import ghost.framework.beans.annotation.constraints.Nullable;
import ghost.framework.data.domain.Pageable;
import ghost.framework.data.domain.Range;
import ghost.framework.data.domain.Sort;
import ghost.framework.data.geo.Distance;
import ghost.framework.data.geo.Point;
import ghost.framework.data.mongodb.core.convert.MongoWriter;
import ghost.framework.data.mongodb.core.mapping.MongoPersistentProperty;
import ghost.framework.data.mongodb.core.query.Collation;
import ghost.framework.data.mongodb.core.query.TextCriteria;
import ghost.framework.data.repository.query.ParameterAccessor;
import ghost.framework.data.util.TypeInformation;
import ghost.framework.util.Assert;
import ghost.framework.util.CollectionUtils;

import java.util.*;

/**
 * Custom {@link ParameterAccessor} that uses a {@link MongoWriter} to serialize parameters into Mongo format.
 *
 * @author Oliver Gierke
 * @author Christoph Strobl
 * @author Thomas Darimont
 * @author Mark Paluch
 */
public class ConvertingParameterAccessor implements MongoParameterAccessor {

	private final MongoWriter<?> writer;
	private final MongoParameterAccessor delegate;

	/**
	 * Creates a new {@link ConvertingParameterAccessor} with the given {@link MongoWriter} and delegate.
	 *
	 * @param writer must not be {@literal null}.
	 * @param delegate must not be {@literal null}.
	 */
	public ConvertingParameterAccessor(MongoWriter<?> writer, MongoParameterAccessor delegate) {

		Assert.notNull(writer, "MongoWriter must not be null!");
		Assert.notNull(delegate, "MongoParameterAccessor must not be null!");

		this.writer = writer;
		this.delegate = delegate;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Iterable#iterator()
	 */
	public PotentiallyConvertingIterator iterator() {
		return new ConvertingIterator(delegate.iterator());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see ghost.framework.data.repository.query.ParameterAccessor#getPageable()
	 */
	public Pageable getPageable() {
		return delegate.getPageable();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see ghost.framework.data.repository.query.ParameterAccessor#getSort()
	 */
	public Sort getSort() {
		return delegate.getSort();
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.query.ParameterAccessor#getDynamicProjection()
	 */
	@Override
	public Optional<Class<?>> getDynamicProjection() {
		return delegate.getDynamicProjection();
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.query.ParameterAccessor#findDynamicProjection()
	 */
	@Override
	public Class<?> findDynamicProjection() {
		return delegate.findDynamicProjection();
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.query.ParameterAccessor#getBindableValue(int)
	 */
	public Object getBindableValue(int index) {
		return getConvertedValue(delegate.getBindableValue(index), null);
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.jpa.repository.query.MongoParameterAccessor#getDistanceRange()
	 */
	@Override
	public Range<Distance> getDistanceRange() {
		return delegate.getDistanceRange();
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.jpa.repository.MongoParameterAccessor#getGeoNearLocation()
	 */
	public Point getGeoNearLocation() {
		return delegate.getGeoNearLocation();
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.jpa.repository.query.MongoParameterAccessor#getFullText()
	 */
	public TextCriteria getFullText() {
		return delegate.getFullText();
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.jpa.repository.query.MongoParameterAccessor#getCollation()
	 */
	@Override
	public Collation getCollation() {
		return delegate.getCollation();
	}

	/**
	 * Converts the given value with the underlying {@link MongoWriter}.
	 *
	 * @param value can be {@literal null}.
	 * @param typeInformation can be {@literal null}.
	 * @return can be {@literal null}.
	 */
	@Nullable
	private Object getConvertedValue(Object value, @Nullable TypeInformation<?> typeInformation) {
		return writer.convertToMongoType(value, typeInformation == null ? null : typeInformation.getActualType());
	}
	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.repository.query.ParameterAccessor#hasBindableNullValue()
	 */
	public boolean hasBindableNullValue() {
		return delegate.hasBindableNullValue();
	}

	/**
	 * Custom {@link Iterator} to convert items before returning them.
	 *
	 * @author Oliver Gierke
	 */
	private class ConvertingIterator implements PotentiallyConvertingIterator {

		private final Iterator<Object> delegate;

		/**
		 * Creates a new {@link ConvertingIterator} for the given delegate.
		 *
		 * @param delegate
		 */
		public ConvertingIterator(Iterator<Object> delegate) {
			this.delegate = delegate;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			return delegate.hasNext();
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		public Object next() {
			return delegate.next();
		}

		/*
		 * (non-Javadoc)
		 * @see ghost.framework.data.mongodb.jpa.repository.ConvertingParameterAccessor.PotentiallConvertingIterator#nextConverted()
		 */
		public Object nextConverted(MongoPersistentProperty property) {

			Object next = next();

			if (next == null) {
				return null;
			}

			if (property.isAssociation()) {
				if (next.getClass().isArray() || next instanceof Iterable) {

					List<DBRef> dbRefs = new ArrayList<DBRef>();
					for (Object element : asCollection(next)) {
						dbRefs.add(writer.toDBRef(element, property));
					}

					return dbRefs;
				} else {
					return writer.toDBRef(next, property);
				}
			}

			return getConvertedValue(next, property.getTypeInformation());
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
			delegate.remove();
		}
	}

	/**
	 * Returns the given object as {@link Collection}. Will do a copy of it if it implements {@link Iterable} or is an
	 * array. Will return an empty {@link Collection} in case {@literal null} is given. Will wrap all other types into a
	 * single-element collection.
	 *
	 * @param source can be {@literal null}, returns an empty {@link List} in that case.
	 * @return never {@literal null}.
	 */
	private static Collection<?> asCollection(@Nullable Object source) {

		if (source instanceof Iterable) {

			List<Object> result = new ArrayList<Object>();
			for (Object element : (Iterable<?>) source) {
				result.add(element);
			}

			return result;
		}

		if (source == null) {
			return Collections.emptySet();
		}

		return source.getClass().isArray() ? CollectionUtils.arrayToList(source) : Collections.singleton(source);
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.jpa.repository.query.MongoParameterAccessor#getValues()
	 */
	@Override
	public Object[] getValues() {
		return delegate.getValues();
	}

	/**
	 * Custom {@link Iterator} that adds a method to access elements in a converted manner.
	 *
	 * @author Oliver Gierke
	 */
	public interface PotentiallyConvertingIterator extends Iterator<Object> {

		/**
		 * Returns the next element which has already been converted.
		 *
		 * @return
		 */
		Object nextConverted(MongoPersistentProperty property);
	}

}
