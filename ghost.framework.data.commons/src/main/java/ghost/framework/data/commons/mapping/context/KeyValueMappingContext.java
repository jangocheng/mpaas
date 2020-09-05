/*
 * Copyright 2014-2020 the original author or authors.
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
package ghost.framework.data.commons.mapping.context;

import ghost.framework.beans.annotation.constraints.Nullable;
import ghost.framework.beans.application.event.AbstractApplicationEvent;
import ghost.framework.data.commons.mapping.KeySpaceResolver;
import ghost.framework.data.commons.mapping.KeyValuePersistentEntity;
import ghost.framework.data.commons.mapping.KeyValuePersistentProperty;
import ghost.framework.data.commons.mapping.model.Property;
import ghost.framework.data.commons.mapping.model.SimpleTypeHolder;
import ghost.framework.data.commons.util.TypeInformation;

/**
 * Default implementation of a {@link MappingContext} using {@link KeyValuePersistentEntity} and
 * {@link KeyValuePersistentProperty} as primary abstractions.
 *
 * @author Christoph Strobl
 * @author Oliver Gierke
 * @author Mark Paluch
 */
public class KeyValueMappingContext<E extends KeyValuePersistentEntity<?, P>, P extends KeyValuePersistentProperty<P>>
		extends AbstractMappingContext<E, P> {

	private @Nullable
	KeySpaceResolver fallbackKeySpaceResolver;

	/**
	 * Configures the {@link KeySpaceResolver} to be used if not explicit key space is annotated to the domain type.
	 *
	 * @param fallbackKeySpaceResolver can be {@literal null}.
	 */
	public void setFallbackKeySpaceResolver(KeySpaceResolver fallbackKeySpaceResolver) {
		this.fallbackKeySpaceResolver = fallbackKeySpaceResolver;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected <T> E createPersistentEntity(TypeInformation<T> typeInformation) {
		return (E) new BasicKeyValuePersistentEntity<T, P>(typeInformation, fallbackKeySpaceResolver);
	}
	@Override
	@SuppressWarnings("unchecked")
	protected P createPersistentProperty(Property property, E owner, SimpleTypeHolder simpleTypeHolder) {
		return (P) new KeyValuePersistentProperty<>(property, owner, simpleTypeHolder);
	}

	@Override
	public void publishEvent(AbstractApplicationEvent event) {

	}
}
