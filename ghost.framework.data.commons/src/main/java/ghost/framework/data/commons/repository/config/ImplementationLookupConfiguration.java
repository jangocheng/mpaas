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
package ghost.framework.data.commons.repository.config;


import ghost.framework.context.bean.IBeanDefinition;

/**
 * Configuration that's used to lookup an implementation type for a repository or fragment interface.
 * 
 * @author Oliver Gierke
 * @since 2.1
 */
public interface ImplementationLookupConfiguration extends ImplementationDetectionConfiguration {

	/**
	 * Returns the bean name of the implementation to be looked up.
	 *
	 * @return must not be {@literal null}.
	 */
	String getImplementationBeanName();

	/**
	 * Returns the simple name of the class to be looked up.
	 *
	 * @return must not be {@literal null}.
	 */
	String getImplementationClassName();

	/**
	 * Return whether the given {@link IBeanDefinition} matches the lookup configuration.
	 * 
	 * @param definition must not be {@literal null}.
	 * @return
	 */
	boolean matches(IBeanDefinition definition);

	/**
	 * Returns whether the bean name created for the given bean definition results in the one required. Will be used to
	 * disambiguate between multiple {@link IBeanDefinition}s matching in general.
	 *
	 * @param definition must not be {@literal null}.
	 * @return
	 * @see #matches(IBeanDefinition)
	 */
	boolean hasMatchingBeanName(IBeanDefinition definition);
}
