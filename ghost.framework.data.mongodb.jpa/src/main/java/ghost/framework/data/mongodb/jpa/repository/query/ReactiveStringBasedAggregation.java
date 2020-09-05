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
package ghost.framework.data.mongodb.jpa.repository.query;

import org.bson.Document;
import ghost.framework.data.mongodb.core.ReactiveMongoOperations;
import ghost.framework.data.mongodb.core.aggregation.Aggregation;
import ghost.framework.data.mongodb.core.aggregation.AggregationOperation;
import ghost.framework.data.mongodb.core.aggregation.AggregationOptions;
import ghost.framework.data.mongodb.core.aggregation.TypedAggregation;
import ghost.framework.data.mongodb.core.convert.MongoConverter;
import ghost.framework.data.mongodb.core.mapping.MongoSimpleTypes;
import ghost.framework.data.mongodb.core.query.Query;
import ghost.framework.data.repository.query.QueryMethodEvaluationContextProvider;
import ghost.framework.data.repository.query.ResultProcessor;
import ghost.framework.expression.spel.standard.SpelExpressionParser;
import ghost.framework.util.ClassUtils;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * A reactive {@link ghost.framework.data.repository.query.RepositoryQuery} to use a plain JSON String to create an
 * {@link AggregationOperation aggregation} pipeline to actually execute.
 *
 * @author Christoph Strobl
 * @author Mark Paluch
 * @since 2.2
 */
public class ReactiveStringBasedAggregation extends AbstractReactiveMongoQuery {

	private final SpelExpressionParser expressionParser;
	private final QueryMethodEvaluationContextProvider evaluationContextProvider;
	private final ReactiveMongoOperations reactiveMongoOperations;
	private final MongoConverter mongoConverter;

	/**
	 * @param method must not be {@literal null}.
	 * @param reactiveMongoOperations must not be {@literal null}.
	 * @param expressionParser must not be {@literal null}.
	 * @param evaluationContextProvider must not be {@literal null}.
	 */
	public ReactiveStringBasedAggregation(ReactiveMongoQueryMethod method,
			ReactiveMongoOperations reactiveMongoOperations, SpelExpressionParser expressionParser,
			QueryMethodEvaluationContextProvider evaluationContextProvider) {

		super(method, reactiveMongoOperations, expressionParser, evaluationContextProvider);

		this.reactiveMongoOperations = reactiveMongoOperations;
		this.mongoConverter = reactiveMongoOperations.getConverter();
		this.expressionParser = expressionParser;
		this.evaluationContextProvider = evaluationContextProvider;
	}

	/*
	 * (non-Javascript)
	 * @see ghost.framework.data.mongodb.jpa.repository.query.AbstractReactiveMongoQuery#doExecute(ghost.framework.data.mongodb.jpa.repository.query.ReactiveMongoQueryMethod, ghost.framework.data.repository.query.ResultProcessor, ghost.framework.data.mongodb.jpa.repository.query.ConvertingParameterAccessor, java.lang.Class)
	 */
	@Override
	protected Object doExecute(ReactiveMongoQueryMethod method, ResultProcessor processor,
			ConvertingParameterAccessor accessor, Class<?> typeToRead) {

		Class<?> sourceType = method.getDomainClass();
		Class<?> targetType = typeToRead;

		List<AggregationOperation> pipeline = computePipeline(accessor);
		AggregationUtils.appendSortIfPresent(pipeline, accessor, typeToRead);
		AggregationUtils.appendLimitAndOffsetIfPresent(pipeline, accessor);

		boolean isSimpleReturnType = isSimpleReturnType(typeToRead);
		boolean isRawReturnType = ClassUtils.isAssignable(org.bson.Document.class, typeToRead);

		if (isSimpleReturnType || isRawReturnType) {
			targetType = Document.class;
		}

		AggregationOptions options = computeOptions(method, accessor);
		TypedAggregation<?> aggregation = new TypedAggregation<>(sourceType, pipeline, options);

		Flux<?> flux = reactiveMongoOperations.aggregate(aggregation, targetType);

		if (isSimpleReturnType && !isRawReturnType) {
			flux = flux.handle((it, sink) -> {

				Object result = AggregationUtils.extractSimpleTypeResult((Document) it, typeToRead, mongoConverter);

				if (result != null) {
					sink.next(result);
				}
			});
		}

		if (method.isCollectionQuery()) {
			return flux;
		} else {
			return flux.next();
		}
	}

	private boolean isSimpleReturnType(Class<?> targetType) {
		return MongoSimpleTypes.HOLDER.isSimpleType(targetType);
	}

	List<AggregationOperation> computePipeline(ConvertingParameterAccessor accessor) {
		return AggregationUtils.computePipeline(getQueryMethod(), accessor, expressionParser, evaluationContextProvider);
	}

	private AggregationOptions computeOptions(MongoQueryMethod method, ConvertingParameterAccessor accessor) {

		AggregationOptions.Builder builder = Aggregation.newAggregationOptions();

		AggregationUtils.applyCollation(builder, method.getAnnotatedCollation(), accessor, method.getParameters(),
				expressionParser, evaluationContextProvider);
		AggregationUtils.applyMeta(builder, method);

		return builder.build();
	}

	/*
	 * (non-Javascript)
	 * @see ghost.framework.data.mongodb.jpa.repository.query.AbstractReactiveMongoQuery#createQuery(ghost.framework.data.mongodb.jpa.repository.query.ConvertingParameterAccessor)
	 */
	@Override
	protected Query createQuery(ConvertingParameterAccessor accessor) {
		throw new UnsupportedOperationException("No query support for aggregation");
	}

	/*
	 * (non-Javascript)
	 * @see ghost.framework.data.mongodb.jpa.repository.query.AbstractReactiveMongoQuery#isCountQuery()
	 */
	@Override
	protected boolean isCountQuery() {
		return false;
	}

	/*
	 * (non-Javascript)
	 * @see ghost.framework.data.mongodb.jpa.repository.query.AbstractReactiveMongoQuery#isExistsQuery()
	 */
	@Override
	protected boolean isExistsQuery() {
		return false;
	}

	/*
	 * (non-Javascript)
	 * @see ghost.framework.data.mongodb.jpa.repository.query.AbstractReactiveMongoQuery#isDeleteQuery()
	 */
	@Override
	protected boolean isDeleteQuery() {
		return false;
	}

	/*
	 * (non-Javascript)
	 * @see ghost.framework.data.mongodb.jpa.repository.query.AbstractReactiveMongoQuery#isLimiting()
	 */
	@Override
	protected boolean isLimiting() {
		return false;
	}
}
