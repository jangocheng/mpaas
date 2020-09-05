/*
 * Copyright 2019-2020 the original author or authors.
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

import ghost.framework.core.annotation.AliasFor;
import ghost.framework.data.annotation.QueryAnnotation;

import java.lang.annotation.*;

/**
 * The {@link Aggregation} annotation can be used to annotate a {@link ghost.framework.data.repository.Repository}
 * query method so that it runs the {@link Aggregation#pipeline()} on invocation.
 * <p />
 * Pipeline stages are mapped against the {@link ghost.framework.data.repository.Repository} domain type to consider
 * {@link ghost.framework.data.mongodb.core.mapping.Field field} mappings and may contain simple placeholders
 * {@code ?0} as well as {@link ghost.framework.expression.spel.standard.SpelExpression SpelExpressions}.
 * <p />
 * Query method {@link ghost.framework.data.domain.Sort} and {@link ghost.framework.data.domain.Pageable}
 * arguments are applied at the end of the pipeline or can be defined manually as part of it.
 *
 * @author Christoph Strobl
 * @since 2.2
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Documented
@QueryAnnotation
public @interface Aggregation {

	/**
	 * Alias for {@link #pipeline()}. Defines the aggregation pipeline to apply.
	 *
	 * @return an empty array by default.
	 * @see #pipeline()
	 */
	@AliasFor("pipeline")
	String[] value() default {};

	/**
	 * Defines the aggregation pipeline to apply.
	 *
	 * <pre class="code">
	 *
	 * // aggregation resulting in collection with single value
	 * &#64;Aggregation("{ '$project': { '_id' : '$lastname' } }")
	 * List<String> findAllLastnames();
	 *
	 * // aggregation with parameter replacement
	 * &#64;Aggregation("{ '$group': { '_id' : '$lastname', names : { $addToSet : '$?0' } } }")
	 * List<PersonAggregate> groupByLastnameAnd(String property);
	 *
	 * // aggregation with sort in pipeline
	 * &#64;Aggregation(pipeline = {"{ '$group': { '_id' : '$lastname', names : { $addToSet : '$?0' } } }", "{ '$sort' : { 'lastname' : -1 } }"})
	 * List<PersonAggregate> groupByLastnameAnd(String property);
	 *
	 * // Sort parameter is used for sorting results
	 * &#64;Aggregation("{ '$group': { '_id' : '$lastname', names : { $addToSet : '$?0' } } }")
	 * List<PersonAggregate> groupByLastnameAnd(String property, Sort sort);
	 *
	 * // Pageable parameter used for sort, skip and limit
	 * &#64;Aggregation("{ '$group': { '_id' : '$lastname', names : { $addToSet : '$?0' } } }")
	 * List<PersonAggregate> groupByLastnameAnd(String property, Pageable page);
	 *
	 * // Single value result aggregation.
	 * &#64;Aggregation("{ '$group' : { '_id' : null, 'total' : { $sum: '$age' } } }")
	 * Long sumAge();
	 *
	 * // Single value wrapped in container object
	 * &#64;Aggregation("{ '$group' : { '_id' : null, 'total' : { $sum: '$age' } } })
	 * SumAge sumAgeAndReturnAggregationResultWrapperWithConcreteType();
	 *
	 * // Raw aggregation result
	 * &#64;Aggregation("{ '$group' : { '_id' : null, 'total' : { $sum: '$age' } } })
	 * AggregationResults&lt;org.bson.Document>&gt; sumAgeAndReturnAggregationResultWrapper();
	 * </pre>
	 *
	 * @return an empty array by default.
	 */
	@AliasFor("value")
	String[] pipeline() default {};

	/**
	 * Defines the collation to apply when executing the aggregation.
	 *
	 * <pre class="code">
	 * // Fixed value
	 * &#64;Aggregation(pipeline = "...", collation = "en_US")
	 * List<Entry> findAllByFixedCollation();
	 *
	 * // Fixed value as Document
	 * &#64;Aggregation(pipeline = "...", collation = "{ 'locale' :  'en_US' }")
	 * List<Entry> findAllByFixedJsonCollation();
	 *
	 * // Dynamic value as String
	 * &#64;Aggregation(pipeline = "...", collation = "?0")
	 * List<Entry> findAllByDynamicCollation(String collation);
	 *
	 * // Dynamic value as Document
	 * &#64;Aggregation(pipeline = "...", collation = "{ 'locale' :  ?0 }")
	 * List<Entry> findAllByDynamicJsonCollation(String collation);
	 *
	 * // SpEL expression
	 * &#64;Aggregation(pipeline = "...", collation = "?#{[0]}")
	 * List<Entry> findAllByDynamicSpElCollation(String collation);
	 * </pre>
	 *
	 * @return an empty {@link String} by default.
	 */
	String collation() default "";
}
