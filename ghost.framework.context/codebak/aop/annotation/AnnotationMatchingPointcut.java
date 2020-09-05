/*
 * Copyright 2002-2019 the original author or authors.
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

package ghost.framework.context.proxy.aop.annotation;

//import ghost.framework.aop.ClassFilter;
//import ghost.framework.aop.MethodMatcher;
//import ghost.framework.aop.Pointcut;
//import ghost.framework.core.annotation.AnnotationUtils;
//import ghost.framework.lang.Nullable;
//import ghost.framework.util.Assert;

import ghost.framework.beans.annotation.constraints.Nullable;
import ghost.framework.context.annotation.AnnotationUtils;
import ghost.framework.context.proxy.aop.ClassFilter;
import ghost.framework.context.proxy.aop.MethodMatcher;
import ghost.framework.context.proxy.aop.Pointcut;
import ghost.framework.util.Assert;

import java.lang.annotation.Annotation;

/**
 * Simple Pointcut that looks for a specific Java 5 annotation
 * being present on a {@link #forClassAnnotation class} or
 * {@link #forMethodAnnotation method}.
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 2.0
 * @see AnnotationClassFilter
 * @see AnnotationMethodMatcher
 */
public class AnnotationMatchingPointcut implements Pointcut {

	private final ClassFilter classFilter;

	private final MethodMatcher methodMatcher;


	/**
	 * Create a new AnnotationMatchingPointcut for the given annotation type.
	 * @param classAnnotationType the annotation type to look for at the class level
	 */
	public AnnotationMatchingPointcut(Class<? extends Annotation> classAnnotationType) {
		this(classAnnotationType, false);
	}

	/**
	 * Create a new AnnotationMatchingPointcut for the given annotation type.
	 * @param classAnnotationType the annotation type to look for at the class level
	 * @param checkInherited whether to also check the superclasses and interfaces
	 * as well as meta-annotations for the annotation type
	 * @see AnnotationClassFilter#AnnotationClassFilter(Class, boolean)
	 */
	public AnnotationMatchingPointcut(Class<? extends Annotation> classAnnotationType, boolean checkInherited) {
		this.classFilter = new AnnotationClassFilter(classAnnotationType, checkInherited);
		this.methodMatcher = MethodMatcher.TRUE;
	}

	/**
	 * Create a new AnnotationMatchingPointcut for the given annotation types.
	 * @param classAnnotationType the annotation type to look for at the class level
	 * (can be {@code null})
	 * @param methodAnnotationType the annotation type to look for at the method level
	 * (can be {@code null})
	 */
	public AnnotationMatchingPointcut(@Nullable Class<? extends Annotation> classAnnotationType,
			@Nullable Class<? extends Annotation> methodAnnotationType) {

		this(classAnnotationType, methodAnnotationType, false);
	}

	/**
	 * Create a new AnnotationMatchingPointcut for the given annotation types.
	 * @param classAnnotationType the annotation type to look for at the class level
	 * (can be {@code null})
	 * @param methodAnnotationType the annotation type to look for at the method level
	 * (can be {@code null})
	 * @param checkInherited whether to also check the superclasses and interfaces
	 * as well as meta-annotations for the annotation type
	 * @since 5.0
	 * @see AnnotationClassFilter#AnnotationClassFilter(Class, boolean)
	 * @see AnnotationMethodMatcher#AnnotationMethodMatcher(Class, boolean)
	 */
	public AnnotationMatchingPointcut(@Nullable Class<? extends Annotation> classAnnotationType,
			@Nullable Class<? extends Annotation> methodAnnotationType, boolean checkInherited) {

		Assert.isTrue((classAnnotationType != null || methodAnnotationType != null),
				"Either Class annotation type or Method annotation type needs to be specified (or both)");

		if (classAnnotationType != null) {
			this.classFilter = new AnnotationClassFilter(classAnnotationType, checkInherited);
		}
		else {
			this.classFilter = new AnnotationCandidateClassFilter(methodAnnotationType);
		}

		if (methodAnnotationType != null) {
			this.methodMatcher = new AnnotationMethodMatcher(methodAnnotationType, checkInherited);
		}
		else {
			this.methodMatcher = MethodMatcher.TRUE;
		}
	}


	@Override
	public ClassFilter getClassFilter() {
		return this.classFilter;
	}

	@Override
	public MethodMatcher getMethodMatcher() {
		return this.methodMatcher;
	}

	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof AnnotationMatchingPointcut)) {
			return false;
		}
		AnnotationMatchingPointcut otherPointcut = (AnnotationMatchingPointcut) other;
		return (this.classFilter.equals(otherPointcut.classFilter) &&
				this.methodMatcher.equals(otherPointcut.methodMatcher));
	}

	@Override
	public int hashCode() {
		return this.classFilter.hashCode() * 37 + this.methodMatcher.hashCode();
	}

	@Override
	public String toString() {
		return "AnnotationMatchingPointcut: " + this.classFilter + ", " + this.methodMatcher;
	}

	/**
	 * Factory method for an AnnotationMatchingPointcut that matches
	 * for the specified annotation at the class level.
	 * @param annotationType the annotation type to look for at the class level
	 * @return the corresponding AnnotationMatchingPointcut
	 */
	public static AnnotationMatchingPointcut forClassAnnotation(Class<? extends Annotation> annotationType) {
		Assert.notNull(annotationType, "Annotation type must not be null");
		return new AnnotationMatchingPointcut(annotationType);
	}

	/**
	 * Factory method for an AnnotationMatchingPointcut that matches
	 * for the specified annotation at the method level.
	 * @param annotationType the annotation type to look for at the method level
	 * @return the corresponding AnnotationMatchingPointcut
	 */
	public static AnnotationMatchingPointcut forMethodAnnotation(Class<? extends Annotation> annotationType) {
		Assert.notNull(annotationType, "Annotation type must not be null");
		return new AnnotationMatchingPointcut(null, annotationType);
	}


	/**
	 * {@link ClassFilter} that delegates to {@link AnnotationUtils#isCandidateClass}
	 * for filtering classes whose methods are not worth searching to begin with.
	 * @since 5.2
	 */
	private static class AnnotationCandidateClassFilter implements ClassFilter {

		private final Class<? extends Annotation> annotationType;

		AnnotationCandidateClassFilter(Class<? extends Annotation> annotationType) {
			this.annotationType = annotationType;
		}

		@Override
		public boolean matches(Class<?> clazz) {
			return AnnotationUtils.isCandidateClass(clazz, this.annotationType);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof AnnotationCandidateClassFilter)) {
				return false;
			}
			AnnotationCandidateClassFilter that = (AnnotationCandidateClassFilter) obj;
			return this.annotationType.equals(that.annotationType);
		}

		@Override
		public int hashCode() {
			return this.annotationType.hashCode();
		}

		@Override
		public String toString() {
			return getClass().getName() + ": " + this.annotationType;
		}

	}

}
