///*
// * Copyright 2002-2018 the original author or authors.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      https://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package ghost.framework.data.jdbc.config;
//
//import ghost.framework.context.io.Resource;
//import ghost.framework.context.io.IResourceLoader;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
///**
// * {@link FactoryBean} implementation that takes a list of location Strings
// * and creates a sorted array of {@link Resource} instances.
// *
// * @author Dave Syer
// * @author Juergen Hoeller
// * @author Christian Dupuis
// * @since 3.0
// */
//public class SortedResourcesFactoryBean extends AbstractFactoryBean<Resource[]> implements ResourceLoaderAware {
//
//	private final List<String> locations;
//
//	private IResourcePatternResolver resourcePatternResolver;
//
//
//	public SortedResourcesFactoryBean(List<String> locations) {
//		this.locations = locations;
//		this.resourcePatternResolver = new PathMatchingIResourcePatternResolver();
//	}
//
//	public SortedResourcesFactoryBean(IResourceLoader resourceLoader, List<String> locations) {
//		this.locations = locations;
//		this.resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
//	}
//
//
//	@Override
//	public void setIResourceLoader(IResourceLoader resourceLoader) {
//		this.resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
//	}
//
//
//	@Override
//	public Class<? extends Resource[]> getObjectType() {
//		return Resource[].class;
//	}
//
//	@Override
//	protected Resource[] createInstance() throws Exception {
//		List<Resource> scripts = new ArrayList<>();
//		for (String location : this.locations) {
//			List<Resource> resources = new ArrayList<>(
//					Arrays.asList(this.resourcePatternResolver.getResources(location)));
//			resources.sort((r1, r2) -> {
//				try {
//					return r1.getURL().toString().compareTo(r2.getURL().toString());
//				}
//				catch (IOException ex) {
//					return 0;
//				}
//			});
//			scripts.addAll(resources);
//		}
//		return scripts.toArray(new Resource[0]);
//	}
//
//}
