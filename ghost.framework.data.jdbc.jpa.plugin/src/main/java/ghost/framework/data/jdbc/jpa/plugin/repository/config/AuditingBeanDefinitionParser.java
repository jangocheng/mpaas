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
package ghost.framework.data.jdbc.jpa.plugin.repository.config;

//import ghost.framework.beans.factory.config.IBeanDefinition;
//import ghost.framework.beans.factory.parsing.BeanComponentDefinition;
//import ghost.framework.beans.factory.support.AbstractBeanDefinition;
//import ghost.framework.beans.factory.support.BeanDefinitionBuilder;
//import ghost.framework.beans.factory.support.RootBeanDefinition;
//import ghost.framework.beans.factory.xml.BeanDefinitionParser;
//import ghost.framework.beans.factory.xml.ParserContext;
//import ghost.framework.data.auditing.config.AuditingHandlerBeanDefinitionParser;
//import ghost.framework.data.config.ParsingUtils;

import ghost.framework.context.annotation.AbstractBeanDefinition;
import ghost.framework.context.bean.BeanDefinitionBuilder;
import ghost.framework.context.bean.IBeanDefinition;
import ghost.framework.context.bean.RootBeanDefinition;
import ghost.framework.context.factory.parsing.BeanComponentDefinition;
import ghost.framework.context.xml.BeanDefinitionParser;
import ghost.framework.context.xml.ParserContext;
import ghost.framework.data.commons.auditing.AuditingHandlerBeanDefinitionParser;
import ghost.framework.util.ClassUtils;
import org.w3c.dom.Element;

import static ghost.framework.context.bean.BeanDefinitionBuilder.rootBeanDefinition;

//import static ghost.framework.beans.factory.support.BeanDefinitionBuilder.*;
//import static ghost.framework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;

/**
 * {@link BeanDefinitionParser} for the {@code auditing} element.
 *
 * @author Oliver Gierke
 * @author Thomas Darimont
 */
public class AuditingBeanDefinitionParser implements BeanDefinitionParser {
	static final String AUDITING_ENTITY_LISTENER_CLASS_NAME = "ghost.framework.data.jpa.domain.support.AuditingEntityListener";
	private static final String AUDITING_BFPP_CLASS_NAME = "ghost.framework.data.jpa.domain.support.AuditingBeanFactoryPostProcessor";
	private final AuditingHandlerBeanDefinitionParser auditingHandlerParser = new AuditingHandlerBeanDefinitionParser(BeanDefinitionNames.JPA_MAPPING_CONTEXT_BEAN_NAME);
	private final SpringConfiguredBeanDefinitionParser springConfiguredParser = new SpringConfiguredBeanDefinitionParser();
	/*
	 * (non-Javadoc)
	 * @see ghost.framework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element, ghost.framework.beans.factory.xml.ParserContext)
	 */
	@Override
	public IBeanDefinition parse(Element element, ParserContext parser) {
		springConfiguredParser.parse(element, parser);
		auditingHandlerParser.parse(element, parser);
		Object source = parser.getReaderContext().extractSource(element);
		BeanDefinitionBuilder builder = rootBeanDefinition(AUDITING_ENTITY_LISTENER_CLASS_NAME);
//		builder.addPropertyValue("auditingHandler", ParsingUtils.getObjectFactoryBeanDefinition(auditingHandlerParser.getResolvedBeanName(), source));
		builder.setScope("prototype");

		registerInfrastructureBeanWithId(builder.getRawBeanDefinition(), AUDITING_ENTITY_LISTENER_CLASS_NAME, parser,
				element);

		RootBeanDefinition def = new RootBeanDefinition(AUDITING_BFPP_CLASS_NAME);
		registerInfrastructureBeanWithId(def, AUDITING_BFPP_CLASS_NAME, parser, element);

		return null;
	}

	private void registerInfrastructureBeanWithId(AbstractBeanDefinition def, String id, ParserContext context,
												  Element element) {
//		def.setRole(IBeanDefinition.ROLE_INFRASTRUCTURE);
		def.setSource(context.extractSource(element));
		context.registerBeanComponent(new BeanComponentDefinition(def, id));
	}

	/**
	 * Copied code of SpringConfiguredBeanDefinitionParser until this class gets public.
	 *
	 * @see <a href="https://jira.springframework.org/browse/SPR-7340">SPR-7340</a>
	 * @author Juergen Hoeller
	 */
	private static class SpringConfiguredBeanDefinitionParser implements BeanDefinitionParser {

		/**
		 * The bean name of the internally managed bean configurer aspect.
		 */
		private static final String BEAN_CONFIGURER_ASPECT_BEAN_NAME = "ghost.framework.context.config.internalBeanConfigurerAspect";

		private static final String BEAN_CONFIGURER_ASPECT_CLASS_NAME = "ghost.framework.beans.factory.aspectj.AnnotationBeanConfigurerAspect";

		@Override
		public IBeanDefinition parse(Element element, ParserContext parserContext) {

			if (!parserContext.getRegistry().containsBeanDefinition(BEAN_CONFIGURER_ASPECT_BEAN_NAME)) {

				if (!ClassUtils.isPresent(BEAN_CONFIGURER_ASPECT_CLASS_NAME, getClass().getClassLoader())) {
					parserContext.getReaderContext().error(
							"Could not configure Spring Data JPA auditing-feature because"
									+ " spring-aspects.jar is not on the classpath!\n"
									+ "If you want to use auditing please add spring-aspects.jar to the classpath.", element);
				}

				RootBeanDefinition def = new RootBeanDefinition();
				def.setBeanClassName(BEAN_CONFIGURER_ASPECT_CLASS_NAME);
				def.setFactoryMethodName("aspectOf");

//				def.setRole(IBeanDefinition.ROLE_INFRASTRUCTURE);
				def.setSource(parserContext.extractSource(element));
				parserContext.registerBeanComponent(new BeanComponentDefinition(def, BEAN_CONFIGURER_ASPECT_BEAN_NAME));
			}
			return null;
		}
	}
}
