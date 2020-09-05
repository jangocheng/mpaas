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
package ghost.framework.data.mongodb.jpa.repository;

import ghost.framework.data.annotation.QueryAnnotation;

import java.lang.annotation.*;

/**
 * Annotation to declare finder queries directly on repository methods. Both attributes allow using a placeholder
 * notation of {@code ?0}, {@code ?1} and so on.
 *
 * @author Oliver Gierke
 * @author Thomas Darimont
 * @author Christoph Strobl
 * @author Mark Paluch
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Documented
@QueryAnnotation
public @interface Query {

	/**
	 * Takes a MongoDB JSON string to define the actual query to be executed. This one will take precedence over the
	 * method name then.
	 *
	 * @return empty {@link String}	by default.
	 */
	String value() default "";

	/**
	 * Defines the fields that should be returned for the given query. Note that only these fields will make it into the
	 * domain object returned.
	 *
	 * @return empty {@link String}	by default.
	 */
	String fields() default "";

	/**
	 * Returns whether the query defined should be executed as count projection.
	 *
	 * @since 1.3
	 * @return {@literal false} by default.
	 */
	boolean count() default false;

	/**
	 * Returns whether the query defined should be executed as exists projection.
	 *
	 * @since 1.10
	 * @return {@literal false} by default.
	 */
	boolean exists() default false;

	/**
	 * Returns whether the query should delete matching documents.
	 *
	 * @since 1.5
	 * @return {@literal false} by default.
	 */
	boolean delete() default false;

	/**
	 * Defines a default sort order for the given query.<br />
	 * <strong>NOTE:</strong> The so set defaults can be altered / overwritten using an explicit
	 * {@link ghost.framework.data.domain.Sort} argument of the query method.
	 *
	 * <pre>
	 * <code>
	 *
	 * 		&#64;Query(sort = "{ age : -1 }") // order by age descending
	 * 		List<Person> findByFirstname(String firstname);
	 * </code>
	 * </pre>
	 *
	 * @return empty {@link String} by default.
	 * @since 2.1
	 */
	String sort() default "";

	/**
	 * Defines the collation to apply when executing the query.
	 *
	 * <pre class="code">
	 * // Fixed value
	 * &#64;Query(collation = "en_US")
	 * List<Entry> findAllByFixedCollation();
	 *
	 * // Fixed value as Document
	 * &#64;Query(collation = "{ 'locale' :  'en_US' }")
	 * List<Entry> findAllByFixedJsonCollation();
	 *
	 * // Dynamic value as String
	 * &#64;Query(collation = "?0")
	 * List<Entry> findAllByDynamicCollation(String collation);
	 *
	 * // Dynamic value as Document
	 * &#64;Query(collation = "{ 'locale' :  ?0 }")
	 * List<Entry> findAllByDynamicJsonCollation(String collation);
	 *
	 * // SpEL expression
	 * &#64;Query(collation = "?#{[0]}")
	 * List<Entry> findAllByDynamicSpElCollation(String collation);
	 * </pre>
	 *
	 * @return an empty {@link String} by default.
	 * @since 2.2
	 */
	String collation() default "";
}
