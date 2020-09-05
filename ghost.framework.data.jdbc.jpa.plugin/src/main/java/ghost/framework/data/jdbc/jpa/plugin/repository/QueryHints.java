/*
 * Copyright 2008-2020 the original author or authors.
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
package ghost.framework.data.jdbc.jpa.plugin.repository;

import javax.persistence.QueryHint;
import java.lang.annotation.*;

/**
 * Wrapper annotation to allow {@link QueryHint} annotations to be bound to methods. It will be evaluated when using
 * {@link Query} on a query method or if you derive the query from the method name. If you rely on named queries either
 * use the XML or annotation based way to declare {@link QueryHint}s in combination with the actual named query
 * declaration.
 *
 * @author Oliver Gierke
 */
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface QueryHints {

	/**
	 * The {@link QueryHint}s to apply when the query will be executed.
	 *
	 * @return
	 */
	QueryHint[] value() default {};

	/**
	 * Defines whether the configured {@link QueryHint}s shall be applied for count queries during pagination as well.
	 * Defaults to {@literal true}.
	 *
	 * @return
	 */
	boolean forCounting() default true;
}
