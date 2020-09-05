///*
// * Copyright 2014-2019 the original author or authors.
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
//package ghost.framework.web.session.core.config;
//
//import ghost.framework.web.session.core.ReactiveSessionRepository;
//
///**
// * Strategy that can be used to customize the {@link ReactiveSessionRepository} before it
// * is fully initialized, in particular to tune its configuration.
// *
// * @param <T> the {@link ReactiveSessionRepository} type
// * @author Vedran Pavic
// * @since 2.2.0
// */
//@FunctionalInterface
//public interface ReactiveSessionRepositoryCustomizer<T extends ReactiveSessionRepository> {
//
//	/**
//	 * Customize the {@link ReactiveSessionRepository}.
//	 * @param sessionRepository the {@link ReactiveSessionRepository} to customize
//	 */
//	void customize(T sessionRepository);
//
//}
