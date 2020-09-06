/*
 * Copyright 2019-2020 the original author or authors.
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
package ghost.framework.data.mongodb.core;

import com.mongodb.ReadPreference;
import ghost.framework.beans.annotation.constraints.Nullable;

/**
 * Interface to be implemented by any object that wishes to expose the {@link ReadPreference}.
 * <p>
 * Typically implemented by cursor or query preparer objects.
 *
 * @author Christoph Strobl
 * @author Mark Paluch
 * @since 2.2
 */
public interface ReadPreferenceAware {

	/**
	 * @return {@literal true} if a {@link ReadPreference} is set.
	 */
	default boolean hasReadPreference() {
		return getReadPreference() != null;
	}

	/**
	 * @return the {@link ReadPreference} to apply or {@literal null} if none set.
	 */
	@Nullable
	ReadPreference getReadPreference();
}