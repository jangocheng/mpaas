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
package ghost.framework.data.commons.annotation;

import ghost.framework.data.commons.mapping.PersistentEntity;

import java.lang.annotation.*;

/**
 * Annotation to allow {@link String} based type aliases to be used when writing type information for
 * {@link PersistentEntity}s.
 *
 * @author Oliver Gierke
 */
@Documented
@Inherited
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Persistent
public @interface TypeAlias {

	/**
	 * The type alias to be used when persisting
	 *
	 * @return
	 */
	String value();
}
