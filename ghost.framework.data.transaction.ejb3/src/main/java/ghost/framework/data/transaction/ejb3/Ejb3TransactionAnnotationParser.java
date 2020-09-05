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

package ghost.framework.data.transaction.ejb3;

//import ghost.framework.core.annotation.AnnotationUtils;
//import ghost.framework.lang.Nullable;
//import ghost.framework.transaction.interceptor.TransactionAttribute;

import ghost.framework.beans.annotation.constraints.Nullable;
import ghost.framework.context.annotation.AnnotationUtils;
import ghost.framework.transaction.TransactionAnnotationParser;
import ghost.framework.transaction.interceptor.TransactionAttribute;
import ghost.framework.transaction.interceptor.DefaultTransactionAttribute;
import ghost.framework.transaction.support.DefaultTransactionDefinition;

import javax.ejb.ApplicationException;
import javax.ejb.TransactionAttributeType;
import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;

/**
 * Strategy implementation for parsing EJB3's {@link javax.ejb.TransactionAttribute}
 * annotation.
 *
 * @author Juergen Hoeller
 * @since 2.5
 */
@SuppressWarnings("serial")
public class Ejb3TransactionAnnotationParser implements TransactionAnnotationParser, Serializable {

	@Override
	public boolean isCandidateClass(Class<?> targetClass) {
		return AnnotationUtils.isCandidateClass(targetClass, javax.ejb.TransactionAttribute.class);
	}

	@Override
	@Nullable
	public TransactionAttribute parseTransactionAnnotation(AnnotatedElement element) {
		javax.ejb.TransactionAttribute ann = element.getAnnotation(javax.ejb.TransactionAttribute.class);
		if (ann != null) {
			return parseTransactionAnnotation(ann);
		}
		else {
			return null;
		}
	}

	public TransactionAttribute parseTransactionAnnotation(javax.ejb.TransactionAttribute ann) {
		return new Ejb3TransactionAttribute(ann.value());
	}


	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || other instanceof Ejb3TransactionAnnotationParser);
	}

	@Override
	public int hashCode() {
		return Ejb3TransactionAnnotationParser.class.hashCode();
	}


	/**
	 * EJB3-specific TransactionAttribute, implementing EJB3's rollback rules
	 * which are based on annotated exceptions.
	 */
	private static class Ejb3TransactionAttribute extends DefaultTransactionAttribute {
		public Ejb3TransactionAttribute(TransactionAttributeType type) {
			setPropagationBehaviorName(DefaultTransactionDefinition.PREFIX_PROPAGATION + type.name());
		}
		@Override
		public boolean rollbackOn(Throwable ex) {
			ApplicationException ann = ex.getClass().getAnnotation(ApplicationException.class);
			return (ann != null ? ann.rollback() : super.rollbackOn(ex));
		}
	}

}
