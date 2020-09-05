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
package ghost.framework.data.mongodb.config;

import ghost.framework.context.bean.IBeanDefinition;
import ghost.framework.context.xml.BeanDefinitionParser;
import ghost.framework.context.xml.ParserContext;
import ghost.framework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * @author Mark Pollack
 * @author Thomas Risberg
 * @author John Brisbin
 * @author Oliver Gierke
 * @author Christoph Strobl
 */
public class MongoJmxParser implements BeanDefinitionParser {

	public IBeanDefinition parse(Element element, ParserContext parserContext) {
		String name = element.getAttribute("mongo-ref");
		if (!StringUtils.hasText(name)) {
			name = BeanNames.MONGO_BEAN_NAME;
		}
		registerJmxComponents(name, element, parserContext);
		return null;
	}

	protected void registerJmxComponents(String mongoRefName, Element element, ParserContext parserContext) {
		Object eleSource = parserContext.extractSource(element);

//		CompositeComponentDefinition compositeDef = new CompositeComponentDefinition(element.getTagName(), eleSource);
//
//		createBeanDefEntry(AssertMetrics.class, compositeDef, mongoRefName, eleSource, parserContext);
//		createBeanDefEntry(BackgroundFlushingMetrics.class, compositeDef, mongoRefName, eleSource, parserContext);
//		createBeanDefEntry(BtreeIndexCounters.class, compositeDef, mongoRefName, eleSource, parserContext);
//		createBeanDefEntry(ConnectionMetrics.class, compositeDef, mongoRefName, eleSource, parserContext);
//		createBeanDefEntry(GlobalLockMetrics.class, compositeDef, mongoRefName, eleSource, parserContext);
//		createBeanDefEntry(MemoryMetrics.class, compositeDef, mongoRefName, eleSource, parserContext);
//		createBeanDefEntry(OperationCounters.class, compositeDef, mongoRefName, eleSource, parserContext);
//		createBeanDefEntry(ServerInfo.class, compositeDef, mongoRefName, eleSource, parserContext);
//		createBeanDefEntry(MongoAdmin.class, compositeDef, mongoRefName, eleSource, parserContext);
//
//		parserContext.registerComponent(compositeDef);

	}

	protected void createBeanDefEntry(Class<?> clazz, Object compositeDef, String mongoRefName,
			Object eleSource, ParserContext parserContext) {
//		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
//		builder.getRawBeanDefinition().setSource(eleSource);
//		builder.addConstructorArgReference(mongoRefName);
//		IBeanDefinition assertDef = builder.getBeanDefinition();
//		String assertName = parserContext.getReaderContext().registerWithGeneratedName(assertDef);
//		compositeDef.addNestedComponent(new BeanComponentDefinition(assertDef, assertName));
	}

}
