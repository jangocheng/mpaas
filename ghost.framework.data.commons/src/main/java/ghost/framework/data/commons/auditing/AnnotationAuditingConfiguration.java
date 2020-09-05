/*
 * Copyright 2013-2020 the original author or authors.
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
package ghost.framework.data.commons.auditing;
//
//import ghost.framework.core.annotation.AnnotationAttributes;
//import ghost.framework.core.type.AnnotationMetadata;
//import ghost.framework.util.Assert;

import ghost.framework.context.core.annotation.AnnotationAttributes;
import ghost.framework.context.core.type.AnnotationMetadata;
import ghost.framework.util.Assert;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * Default implementation for {@link AuditingConfiguration}.
 *
 * @author Ranie Jade Ramiso
 * @author Thomas Darimont
 * @author Oliver Gierke
 */
public class AnnotationAuditingConfiguration implements AuditingConfiguration {

	private static final String MISSING_ANNOTATION_ATTRIBUTES = "Couldn't find annotation attributes for %s in %s!";

	private final AnnotationAttributes attributes;

	/**
	 * Creates a new instance of {@link AnnotationAuditingConfiguration} for the given {@link AnnotationMetadata} and
	 * annotation type.
	 *
	 * @param metadata must not be {@literal null}.
	 * @param annotation must not be {@literal null}.
	 */
	public AnnotationAuditingConfiguration(AnnotationMetadata metadata, Class<? extends Annotation> annotation) {

		Assert.notNull(metadata, "AnnotationMetadata must not be null!");
		Assert.notNull(annotation, "Annotation must not be null!");

		Map<String, Object> attributesSource = metadata.getAnnotationAttributes(annotation.getName());

		if (attributesSource == null) {
			throw new IllegalArgumentException(String.format(MISSING_ANNOTATION_ATTRIBUTES, annotation, metadata));
		}

		this.attributes = new AnnotationAttributes(attributesSource);
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.auditing.config.AuditingConfiguration#getAuditorAwareRef()
	 */
	@Override
	public String getAuditorAwareRef() {
		return attributes.getString("auditorAwareRef");
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.auditing.config.AuditingConfiguration#isSetDates()
	 */
	@Override
	public boolean isSetDates() {
		return attributes.getBoolean("setDates");
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.auditing.config.AuditingConfiguration#getDateTimeProviderRef()
	 */
	@Override
	public String getDateTimeProviderRef() {
		return attributes.getString("dateTimeProviderRef");
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.auditing.config.AuditingConfiguration#isModifyOnCreate()
	 */
	@Override
	public boolean isModifyOnCreate() {
		return attributes.getBoolean("modifyOnCreate");
	}
}
