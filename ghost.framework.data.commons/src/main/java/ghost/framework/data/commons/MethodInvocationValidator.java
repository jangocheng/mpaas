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
package ghost.framework.data.commons;

import ghost.framework.beans.annotation.constraints.Nullable;
import ghost.framework.context.DefaultParameterNameDiscoverer;
import ghost.framework.context.ParameterNameDiscoverer;
import ghost.framework.context.core.MethodParameter;
import ghost.framework.data.commons.util.NullableUtils;
import ghost.framework.data.dao.EmptyResultDataAccessException;
import ghost.framework.util.ClassUtils;
import ghost.framework.util.ConcurrentReferenceHashMap;
import ghost.framework.util.ObjectUtils;
import ghost.framework.util.ReflectionUtils;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.annotation.ElementType;
import java.lang.reflect.Method;
import java.util.Map;

//import ghost.framework.core.DefaultParameterNameDiscoverer;
//import ghost.framework.core.MethodParameter;
//import ghost.framework.core.ParameterNameDiscoverer;
//import ghost.framework.dao.EmptyResultDataAccessException;
//import ghost.framework.data.util.NullableUtils;
//import ghost.framework.data.util.ReflectionUtils;
//import ghost.framework.lang.Nullable;
//import ghost.framework.util.ClassUtils;
//import ghost.framework.util.ConcurrentReferenceHashMap;
//import ghost.framework.util.ConcurrentReferenceHashMap.ReferenceType;
//import ghost.framework.util.ObjectUtils;

/**
 * Interceptor enforcing required return value and method parameter constraints declared on repository query methods.
 * Supports Kotlin nullability markers and JSR-305 Non-null annotations.
 *
 * @author Mark Paluch
 * @since 2.0
 * @see javax.annotation.Nonnull
 * @see ReflectionUtils#isNullable(MethodParameter)
 * @see NullableUtils
 */
public class MethodInvocationValidator implements MethodInterceptor {

	private final ParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();
	private final Map<Method, Nullability> nullabilityCache = new ConcurrentReferenceHashMap<>(16, ConcurrentReferenceHashMap.ReferenceType.WEAK);

	/**
	 * Returns {@literal true} if the {@code repositoryInterface} is supported by this interceptor.
	 *
	 * @param repositoryInterface the interface class.
	 * @return {@literal true} if the {@code repositoryInterface} is supported by this interceptor.
	 */
	public static boolean supports(Class<?> repositoryInterface) {

		return /*ReflectionUtils.isSupportedKotlinClass(repositoryInterface)*/
				 NullableUtils.isNonNull(repositoryInterface, ElementType.METHOD)
				|| NullableUtils.isNonNull(repositoryInterface, ElementType.PARAMETER);
	}

	/*
	 * (non-Javadoc)
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
	 */
	@Nullable
	@Override
	public Object invoke(@SuppressWarnings("null") MethodInvocation invocation) throws Throwable {

		Method method = invocation.getMethod();
		Nullability nullability = nullabilityCache.get(method);

		if (nullability == null) {

			nullability = Nullability.of(method, discoverer);
			nullabilityCache.put(method, nullability);
		}

		Object[] arguments = invocation.getArguments();

		for (int i = 0; i < method.getParameterCount(); i++) {

			if (nullability.isNullableParameter(i)) {
				continue;
			}

			if (arguments.length < i || arguments[i] == null) {
				throw new IllegalArgumentException(
						String.format("Parameter %s in %s.%s must not be null!", nullability.getMethodParameterName(i),
								ClassUtils.getShortName(method.getDeclaringClass()), method.getName()));
			}
		}

		Object result = invocation.proceed();

		if (result == null && !nullability.isNullableReturn()) {
			throw new EmptyResultDataAccessException("Result must not be null!", 1);
		}
		return result;
	}

	static final class Nullability {

		private final boolean nullableReturn;
		private final boolean[] nullableParameters;
		private final MethodParameter[] methodParameters;

		private Nullability(boolean nullableReturn, boolean[] nullableParameters, MethodParameter[] methodParameters) {
			this.nullableReturn = nullableReturn;
			this.nullableParameters = nullableParameters;
			this.methodParameters = methodParameters;
		}

		static Nullability of(Method method, ParameterNameDiscoverer discoverer) {

			boolean nullableReturn = isNullableParameter(new MethodParameter(method, -1));
			boolean[] nullableParameters = new boolean[method.getParameterCount()];
			MethodParameter[] methodParameters = new MethodParameter[method.getParameterCount()];

			for (int i = 0; i < method.getParameterCount(); i++) {

				MethodParameter parameter = new MethodParameter(method, i);
				parameter.initParameterNameDiscovery(discoverer);
				nullableParameters[i] = isNullableParameter(parameter);
				methodParameters[i] = parameter;
			}

			return new Nullability(nullableReturn, nullableParameters, methodParameters);
		}

		String getMethodParameterName(int index) {

			String parameterName = methodParameters[index].getParameterName();

			if (parameterName == null) {
				parameterName = String.format("of type %s at index %d",
						ClassUtils.getShortName(methodParameters[index].getParameterType()), index);
			}

			return parameterName;
		}

		boolean isNullableReturn() {
			return nullableReturn;
		}

		boolean isNullableParameter(int index) {
			return nullableParameters[index];
		}

		private static boolean isNullableParameter(MethodParameter parameter) {

			return requiresNoValue(parameter) || NullableUtils.isExplicitNullable(parameter);
					/*|| (ReflectionUtils.isSupportedKotlinClass(parameter.getDeclaringClass())
							&& ReflectionUtils.isNullable(parameter));*/
		}

		private static boolean requiresNoValue(MethodParameter parameter) {
			return parameter.getParameterType().equals(Void.class) || parameter.getParameterType().equals(Void.TYPE);
		}

		public boolean[] getNullableParameters() {
			return this.nullableParameters;
		}

		public MethodParameter[] getMethodParameters() {
			return this.methodParameters;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object o) {

			if (this == o) {
				return true;
			}

			if (!(o instanceof Nullability)) {
				return false;
			}

			Nullability that = (Nullability) o;

			if (nullableReturn != that.nullableReturn) {
				return false;
			}

			if (!ObjectUtils.nullSafeEquals(nullableParameters, that.nullableParameters)) {
				return false;
			}

			return ObjectUtils.nullSafeEquals(methodParameters, that.methodParameters);
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			int result = (nullableReturn ? 1 : 0);
			result = 31 * result + ObjectUtils.nullSafeHashCode(nullableParameters);
			result = 31 * result + ObjectUtils.nullSafeHashCode(methodParameters);
			return result;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "MethodInvocationValidator.Nullability(nullableReturn=" + this.isNullableReturn() + ", nullableParameters="
					+ java.util.Arrays.toString(this.getNullableParameters()) + ", methodParameters="
					+ java.util.Arrays.deepToString(this.getMethodParameters()) + ")";
		}
	}
}
