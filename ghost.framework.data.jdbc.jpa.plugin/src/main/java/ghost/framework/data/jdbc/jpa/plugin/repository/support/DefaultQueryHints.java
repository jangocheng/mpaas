/*
 * Copyright 2017-2020 the original author or authors.
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
package ghost.framework.data.jdbc.jpa.plugin.repository.support;

//import ghost.framework.data.jpa.repository.EntityGraph;
//import ghost.framework.data.jpa.repository.query.Jpa21Utils;
//import ghost.framework.data.jpa.repository.query.JpaEntityGraph;
//import ghost.framework.data.util.Optionals;

import ghost.framework.data.commons.util.Optionals;
import ghost.framework.data.jdbc.jpa.plugin.repository.EntityGraph;
import ghost.framework.data.jdbc.jpa.plugin.repository.query.Jpa21Utils;
import ghost.framework.data.jdbc.jpa.plugin.repository.query.JpaEntityGraph;
import ghost.framework.util.Assert;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.Map.Entry;

/**
 * Default implementation of {@link QueryHints}.
 *
 * @author Christoph Strobl
 * @author Oliver Gierke
 * @author Jens Schauder
 * @since 2.0
 */
class DefaultQueryHints implements QueryHints {

	private final JpaEntityInformation<?, ?> information;
	private final CrudMethodMetadata metadata;
	private final Optional<EntityManager> entityManager;
	private final boolean forCounts;

	/**
	 * Creates a new {@link DefaultQueryHints} instance for the given {@link JpaEntityInformation},
	 * {@link CrudMethodMetadata}, {@link EntityManager} and whether to include fetch graphs.
	 *
	 * @param information must not be {@literal null}.
	 * @param metadata must not be {@literal null}.
	 * @param entityManager must not be {@literal null}.
	 * @param forCounts
	 */
	private DefaultQueryHints(JpaEntityInformation<?, ?> information, CrudMethodMetadata metadata,
			Optional<EntityManager> entityManager, boolean forCounts) {

		this.information = information;
		this.metadata = metadata;
		this.entityManager = entityManager;
		this.forCounts = forCounts;
	}

	/**
	 * Creates a new {@link QueryHints} instance for the given {@link JpaEntityInformation}, {@link CrudMethodMetadata}
	 * and {@link EntityManager}.
	 *
	 * @param information must not be {@literal null}.
	 * @param metadata must not be {@literal null}.
	 * @return
	 */
	public static QueryHints of(JpaEntityInformation<?, ?> information, CrudMethodMetadata metadata) {

		Assert.notNull(information, "JpaEntityInformation must not be null!");
		Assert.notNull(metadata, "CrudMethodMetadata must not be null!");

		return new DefaultQueryHints(information, metadata, Optional.empty(), false);
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.jpa.repository.support.QueryHints#withFetchGraphs()
	 */
	@Override
	public QueryHints withFetchGraphs(EntityManager em) {
		return new DefaultQueryHints(this.information, this.metadata, Optional.of(em), this.forCounts);
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.jpa.repository.support.QueryHints#forCounts()
	 */
	@Override
	public QueryHints forCounts() {
		return new DefaultQueryHints(this.information, this.metadata, this.entityManager, true);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Entry<String, Object>> iterator() {
		return asMap().entrySet().iterator();
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.jpa.repository.support.QueryHints#asMap()
	 */
	@Override
	public Map<String, Object> asMap() {

		Map<String, Object> hints = new HashMap<>();

		if (forCounts) {
			hints.putAll(metadata.getQueryHintsForCount());
		} else {
			hints.putAll(metadata.getQueryHints());
		}

		hints.putAll(getFetchGraphs());

		return hints;
	}

	private Map<String, Object> getFetchGraphs() {

		return Optionals
				.mapIfAllPresent(entityManager, metadata.getEntityGraph(),
						(em, graph) -> Jpa21Utils.tryGetFetchGraphHints(em, getEntityGraph(graph), information.getJavaType()))
				.orElse(Collections.emptyMap());
	}

	private JpaEntityGraph getEntityGraph(EntityGraph entityGraph) {

		String fallbackName = information.getEntityName() + "." + metadata.getMethod().getName();
		return new JpaEntityGraph(entityGraph, fallbackName);
	}
}
