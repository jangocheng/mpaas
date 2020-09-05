/*
 * Copyright 2019 the original author or authors.
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
package ghost.framework.data.commons.mapping.model;

import ghost.framework.beans.annotation.constraints.Nullable;
import ghost.framework.data.commons.mapping.PersistentEntity;
import ghost.framework.data.commons.mapping.PersistentProperty;
import ghost.framework.data.commons.mapping.PersistentPropertyAccessor;
import ghost.framework.data.commons.mapping.PreferredConstructor;
import ghost.framework.util.Assert;

/**
 * A {@link PersistentPropertyAccessor} that will use an entity's {@link PersistenceConstructor} to create a new
 * instance of it to apply a new value for a given {@link PersistentProperty}. Will only be used if the
 * {@link PersistentProperty} is to be applied on a completely immutable entity type exposing a persistence constructor.
 *
 * @author Oliver Drotbohm
 */
public class InstantiationAwarePropertyAccessor<T> implements PersistentPropertyAccessor<T> {

	private static final String NO_SETTER_OR_CONSTRUCTOR = "Cannot set property %s because no setter, wither or copy constructor exists for %s!";
	private static final String NO_CONSTRUCTOR_PARAMETER = "Cannot set property %s because no setter, no wither and it's not part of the persistence constructor %s!";

	private final PersistentPropertyAccessor<T> delegate;
	private final EntityInstantiators instantiators;

	private T bean;

	/**
	 * Creates an {@link InstantiationAwarePropertyAccessor} using the given delegate {@link PersistentPropertyAccessor}
	 * and {@link EntityInstantiators}.
	 *
	 * @param delegate must not be {@literal null}.
	 * @param instantiators must not be {@literal null}.
	 */
	public InstantiationAwarePropertyAccessor(PersistentPropertyAccessor<T> delegate, EntityInstantiators instantiators) {

		Assert.notNull(delegate, "Delegate PersistenPropertyAccessor must not be null!");
		Assert.notNull(instantiators, "EntityInstantiators must not be null!");

		this.delegate = delegate;
		this.instantiators = instantiators;
		this.bean = delegate.getBean();
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mapping.PersistentPropertyAccessor#setProperty(ghost.framework.data.mapping.PersistentProperty, java.lang.Object)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void setProperty(PersistentProperty<?> property, @Nullable Object value) {

		PersistentEntity<?, ?> owner = property.getOwner();

		if (!property.isImmutable() || property.getWither() != null) {

			delegate.setProperty(property, value);
			this.bean = delegate.getBean();

			return;
		}

		PreferredConstructor<?, ?> constructor = owner.getPersistenceConstructor();

		if (constructor == null) {
			throw new IllegalStateException(String.format(NO_SETTER_OR_CONSTRUCTOR, property.getName(), owner.getType()));
		}

		if (!constructor.isConstructorParameter(property)) {
			throw new IllegalStateException(
					String.format(NO_CONSTRUCTOR_PARAMETER, property.getName(), constructor.getConstructor()));
		}

		constructor.getParameters().stream().forEach(it -> {

			if (it.getName() == null) {
				throw new IllegalStateException(
						String.format("Cannot detect parameter names of copy constructor of %s!", owner.getType()));
			}
		});

		EntityInstantiator instantiator = instantiators.getInstantiatorFor(owner);

		this.bean = (T) instantiator.createInstance(owner, new ParameterValueProvider() {

			/*
			 * (non-Javadoc)
			 * @see ghost.framework.data.mapping.entity.ParameterValueProvider#getParameterValue(ghost.framework.data.mapping.PreferredConstructor.Parameter)
			 */
			@Override
			@Nullable
			@SuppressWarnings("null")
			public Object getParameterValue(PreferredConstructor.Parameter parameter) {

				return property.getName().equals(parameter.getName()) //
						? value
						: delegate.getProperty(owner.getRequiredPersistentProperty(parameter.getName()));
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mapping.PersistentPropertyAccessor#getProperty(ghost.framework.data.mapping.PersistentProperty)
	 */
	@Nullable
	@Override
	public Object getProperty(PersistentProperty<?> property) {
		return delegate.getProperty(property);
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mapping.PersistentPropertyAccessor#getBean()
	 */
	@Override
	public T getBean() {
		return this.bean;
	}
}
