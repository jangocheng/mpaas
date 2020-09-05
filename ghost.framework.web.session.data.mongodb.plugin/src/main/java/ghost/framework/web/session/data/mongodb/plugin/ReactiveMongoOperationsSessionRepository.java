/*
 * Copyright 2017 the original author or authors.
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
package ghost.framework.web.session.data.mongodb.plugin;

import ghost.framework.data.mongodb.core.ReactiveMongoOperations;
import ghost.framework.session.ReactiveSessionRepository;

/**
 * This {@link ReactiveSessionRepository} implementation is kept to support migration to
 * {@link ReactiveMongoSessionRepository} in a backwards compatible manner.
 * 
 * @author Greg Turnquist
 * @deprecated since 2.2.0 in favor of {@link ReactiveMongoSessionRepository}.
 */
@Deprecated
public class ReactiveMongoOperationsSessionRepository extends ReactiveMongoSessionRepository {

	public ReactiveMongoOperationsSessionRepository(ReactiveMongoOperations mongoOperations) {
		super(mongoOperations);
	}
}
