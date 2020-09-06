/*
 * Copyright 2002-2018 the original author or authors.
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

package ghost.framework.web.module.handler;

import ghost.framework.beans.annotation.constraints.Nullable;
import ghost.framework.util.Assert;
import ghost.framework.web.context.cors.CorsConfigurationSource;
import ghost.framework.web.context.cors.ICorsConfiguration;
import ghost.framework.web.module.servlet.HandlerExecutionChain;
import ghost.framework.web.module.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Helper class to get information from the {@code HandlerMapping} that would
 * serve a specific request.
 *
 * <p>Provides the following methods:
 * <ul>
 * <li>{@link #getMatchableHandlerMapping} &mdash; obtain a {@code HandlerMapping}
 * to check request-matching criteria against.
 * <li>{@link #getCorsConfiguration} &mdash; obtain the CORS configuration for the
 * request.
 * </ul>
 *
 * @author Rossen Stoyanchev
 * @since 4.3.1
 */
public class HandlerMappingIntrospector implements CorsConfigurationSource {

	@Nullable
	private List<HandlerMapping> handlerMappings;


	/**
	 * Constructor for use with {@link ApplicationContextAware}.
	 */
	public HandlerMappingIntrospector() {
	}

//	/**
//	 * Constructor that detects the configured {@code HandlerMapping}s in the
//	 * given {@code ApplicationContext} or falls back on
//	 * "DispatcherServlet.properties" like the {@code DispatcherServlet}.
//	 * @deprecated as of 4.3.12, in favor of {@link #setApplicationContext}
//	 */
//	@Deprecated
//	public HandlerMappingIntrospector(ApplicationContext context) {
//		this.handlerMappings = initHandlerMappings(context);
//	}


	/**
	 * Return the configured HandlerMapping's.
	 */
	public List<HandlerMapping> getHandlerMappings() {
		return (this.handlerMappings != null ? this.handlerMappings : Collections.emptyList());
	}

//
//	@Override
//	public void setApplicationContext(ApplicationContext applicationContext) {
//		this.applicationContext = applicationContext;
//	}
//
//	@Override
//	public void afterPropertiesSet() {
//		if (this.handlerMappings == null) {
//			Assert.notNull(this.applicationContext, "No ApplicationContext");
//			this.handlerMappings = initHandlerMappings(this.applicationContext);
//		}
//	}


	/**
	 * Find the {@link HandlerMapping} that would handle the given request and
	 * return it as a {@link MatchableHandlerMapping} that can be used to test
	 * request-matching criteria.
	 * <p>If the matching HandlerMapping is not an instance of
	 * {@link MatchableHandlerMapping}, an IllegalStateException is raised.
	 * @param request the current request
	 * @return the resolved matcher, or {@code null}
	 * @throws Exception if any of the HandlerMapping's raise an exception
	 */
	@Nullable
	public MatchableHandlerMapping getMatchableHandlerMapping(HttpServletRequest request) throws Exception {
		Assert.notNull(this.handlerMappings, "Handler mappings not initialized");
		HttpServletRequest wrapper = new RequestAttributeChangeIgnoringWrapper(request);
		for (HandlerMapping handlerMapping : this.handlerMappings) {
			Object handler = handlerMapping.getHandler(wrapper);
			if (handler == null) {
				continue;
			}
			if (handlerMapping instanceof MatchableHandlerMapping) {
				return ((MatchableHandlerMapping) handlerMapping);
			}
			throw new IllegalStateException("HandlerMapping is not a MatchableHandlerMapping");
		}
		return null;
	}

	@Override
	@Nullable
	public ICorsConfiguration getCorsConfiguration(HttpServletRequest request) {
		Assert.notNull(this.handlerMappings, "Handler mappings not initialized");
		HttpServletRequest wrapper = new RequestAttributeChangeIgnoringWrapper(request);
		for (HandlerMapping handlerMapping : this.handlerMappings) {
			HandlerExecutionChain handler = null;
			try {
				handler = handlerMapping.getHandler(wrapper);
			}
			catch (Exception ex) {
				// Ignore
			}
			if (handler == null) {
				continue;
			}
			if (handler.getInterceptors() != null) {
//				for (HandlerInterceptor interceptor : handler.getInterceptors()) {
//					if (interceptor instanceof CorsConfigurationSource) {
//						return ((CorsConfigurationSource) interceptor).getCorsConfiguration(wrapper);
//					}
//				}
			}
			if (handler.getHandler() instanceof CorsConfigurationSource) {
				return ((CorsConfigurationSource) handler.getHandler()).getCorsConfiguration(wrapper);
			}
		}
		return null;
	}


//	private static List<HandlerMapping> initHandlerMappings(ApplicationContext applicationContext) {
//		Map<String, HandlerMapping> beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(
//				applicationContext, HandlerMapping.class, true, false);
//		if (!beans.isEmpty()) {
//			List<HandlerMapping> mappings = new ArrayList<>(beans.values());
//			AnnotationAwareOrderComparator.sort(mappings);
//			return Collections.unmodifiableList(mappings);
//		}
//		return Collections.unmodifiableList(initFallback(applicationContext));
//	}

	private static List<HandlerMapping> initFallback() {
		Properties props;
//		String path = "DispatcherServlet.properties";
//		try {
//			Resource resource = new ClassPathResource(path, DispatcherServlet.class);
//			props = PropertiesLoaderUtils.loadProperties(resource);
//		}
//		catch (IOException ex) {
//			throw new IllegalStateException("Could not load '" + path + "': " + ex.getMessage());
//		}

//		String value = props.getProperty(HandlerMapping.class.getName());
//		String[] names = StringUtils.commaDelimitedListToStringArray(value);
//		List<HandlerMapping> result = new ArrayList<>(names.length);
//		for (String name : names) {
//			try {
//				Class<?> clazz = ClassUtils.forName(name, DispatcherServlet.class.getClassLoader());
//				Object mapping = applicationContext.getAutowireCapableBeanFactory().createBean(clazz);
//				result.add((HandlerMapping) mapping);
//			}
//			catch (ClassNotFoundException ex) {
//				throw new IllegalStateException("Could not find default HandlerMapping [" + name + "]");
//			}
//		}
//		return result;
		return null;
	}


	/**
	 * Request wrapper that ignores request attribute changes.
	 */
	private static class RequestAttributeChangeIgnoringWrapper extends HttpServletRequestWrapper {

		public RequestAttributeChangeIgnoringWrapper(HttpServletRequest request) {
			super(request);
		}

		@Override
		public void setAttribute(String name, Object value) {
			// Ignore attribute change...
		}
	}

}