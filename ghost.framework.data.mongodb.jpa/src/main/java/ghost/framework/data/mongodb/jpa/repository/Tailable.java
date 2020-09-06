/*
 * Copyright 2016-2020 the original author or authors.
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
package ghost.framework.data.mongodb.jpa.repository;

import ghost.framework.data.annotation.QueryAnnotation;

import java.lang.annotation.*;

/**
 * Annotation to declare an infinite stream using MongoDB's {@link com.mongodb.CursorType#TailableAwait tailable}
 * cursors. An infinite stream can only be used with capped collections. Objects are emitted through the stream as data
 * is inserted into the collection. An infinite stream can only be used with streams that emit more than one element,
 * such as {@link reactor.core.publisher.Flux}.
 * <p>
 * The stream may become dead, or invalid, if either the query returns no match or the cursor returns the document at
 * the "end" of the collection and then the application deletes that document.
 * <p>
 * A stream that is no longer in use must be {@link reactor.core.Disposable#dispose()} disposed} otherwise the streams
 * will linger and exhaust resources.
 *
 * @author Mark Paluch
 * @see <a href="https://docs.mongodb.com/manual/core/tailable-cursors/">Tailable Cursors</a>
 * @since 2.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Documented
@QueryAnnotation
public @interface Tailable {

}