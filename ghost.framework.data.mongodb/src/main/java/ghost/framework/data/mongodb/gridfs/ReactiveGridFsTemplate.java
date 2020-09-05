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
package ghost.framework.data.mongodb.gridfs;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.reactivestreams.client.gridfs.GridFSBucket;
import com.mongodb.reactivestreams.client.gridfs.GridFSBuckets;
import com.mongodb.reactivestreams.client.gridfs.GridFSFindPublisher;
import com.mongodb.reactivestreams.client.gridfs.GridFSUploadPublisher;
import ghost.framework.beans.annotation.constraints.Nullable;
import ghost.framework.context.io.buffer.DataBuffer;
import ghost.framework.context.io.buffer.DataBufferFactory;
import ghost.framework.context.io.buffer.DefaultDataBufferFactory;
import ghost.framework.data.dao.IncorrectResultSizeDataAccessException;
import ghost.framework.data.mongodb.ReactiveMongoDatabaseFactory;
import ghost.framework.data.mongodb.core.convert.MongoConverter;
import ghost.framework.data.mongodb.core.query.Query;
import ghost.framework.data.mongodb.core.query.SerializationUtils;
import ghost.framework.data.mongodb.util.BsonUtils;
import ghost.framework.util.Assert;
import ghost.framework.util.StringUtils;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

import static ghost.framework.data.mongodb.core.query.Query.query;
import static ghost.framework.data.mongodb.gridfs.GridFsCriteria.whereFilename;

/**
 * {@link ReactiveGridFsOperations} implementation to store content into MongoDB GridFS. Uses by default
 * {@link DefaultDataBufferFactory} to create {@link DataBuffer buffers}.
 *
 * @author Mark Paluch
 * @author Nick Stolwijk
 * @author Denis Zavedeev
 * @author Christoph Strobl
 * @author Mathieu Ouellet
 * @since 2.2
 */
public class ReactiveGridFsTemplate extends GridFsOperationsSupport implements ReactiveGridFsOperations {

	private final ReactiveMongoDatabaseFactory dbFactory;
	private final DataBufferFactory dataBufferFactory;
	private final @Nullable
	String bucket;

	/**
	 * Creates a new {@link ReactiveGridFsTemplate} using the given {@link ReactiveMongoDatabaseFactory} and
	 * {@link MongoConverter}.
	 *
	 * @param dbFactory must not be {@literal null}.
	 * @param converter must not be {@literal null}.
	 */
	public ReactiveGridFsTemplate(ReactiveMongoDatabaseFactory dbFactory, MongoConverter converter) {
		this(dbFactory, converter, null);
	}

	/**
	 * Creates a new {@link ReactiveGridFsTemplate} using the given {@link ReactiveMongoDatabaseFactory} and
	 * {@link MongoConverter}.
	 *
	 * @param dbFactory must not be {@literal null}.
	 * @param converter must not be {@literal null}.
	 * @param bucket
	 */
	public ReactiveGridFsTemplate(ReactiveMongoDatabaseFactory dbFactory, MongoConverter converter,
			@Nullable String bucket) {
		this(new DefaultDataBufferFactory(), dbFactory, converter, bucket);
	}

	/**
	 * Creates a new {@link ReactiveGridFsTemplate} using the given {@link DataBufferFactory},
	 * {@link ReactiveMongoDatabaseFactory} and {@link MongoConverter}.
	 *
	 * @param dataBufferFactory must not be {@literal null}.
	 * @param dbFactory must not be {@literal null}.
	 * @param converter must not be {@literal null}.
	 * @param bucket
	 */
	public ReactiveGridFsTemplate(DataBufferFactory dataBufferFactory, ReactiveMongoDatabaseFactory dbFactory,
			MongoConverter converter, @Nullable String bucket) {

		super(converter);

		Assert.notNull(dataBufferFactory, "DataBufferFactory must not be null!");
		Assert.notNull(dbFactory, "ReactiveMongoDatabaseFactory must not be null!");

		this.dataBufferFactory = dataBufferFactory;
		this.dbFactory = dbFactory;
		this.bucket = bucket;
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.gridfs.ReactiveGridFsOperations#store(org.reactivestreams.Publisher, java.lang.String, java.lang.String, java.lang.Object)
	 */
	@Override
	public Mono<ObjectId> store(Publisher<DataBuffer> content, @Nullable String filename, @Nullable String contentType,
			@Nullable Object metadata) {
		return store(content, filename, contentType, toDocument(metadata));
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.gridfs.ReactiveGridFsOperations#save(ghost.framework.data.mongodb.gridfs.GridFsObject)
	 */
	public <T> Mono<T> store(GridFsObject<T, Publisher<DataBuffer>> upload) {

		GridFSUploadOptions uploadOptions = computeUploadOptionsFor(upload.getOptions().getContentType(),
				upload.getOptions().getMetadata());

		if (upload.getOptions().getChunkSize() > 0) {
			uploadOptions.chunkSizeBytes(upload.getOptions().getChunkSize());
		}

		String filename = upload.getFilename();
		Flux<ByteBuffer> source = Flux.from(upload.getContent()).map(DataBuffer::asByteBuffer);
		T fileId = upload.getFileId();

		if (fileId == null) {
			return (Mono<T>) createMono(new AutoIdCreatingUploadCallback(filename, source, uploadOptions));
		}

		UploadCallback callback = new UploadCallback(BsonUtils.simpleToBsonValue(fileId), filename, source, uploadOptions);
		return createMono(callback).thenReturn(fileId);
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.gridfs.ReactiveGridFsOperations#find(ghost.framework.data.mongodb.core.query.Query)
	 */
	@Override
	public Flux<GridFSFile> find(Query query) {

		Document queryObject = getMappedQuery(query.getQueryObject());
		Document sortObject = getMappedQuery(query.getSortObject());

		return createFlux(new FindCallback(query, queryObject, sortObject));
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.gridfs.ReactiveGridFsOperations#findOne(ghost.framework.data.mongodb.core.query.Query)
	 */
	@Override
	public Mono<GridFSFile> findOne(Query query) {

		Document queryObject = getMappedQuery(query.getQueryObject());
		Document sortObject = getMappedQuery(query.getSortObject());

		return createFlux(new FindLimitCallback(query, queryObject, sortObject, 2)) //
				.collectList() //
				.handle((files, sink) -> {

					if (files.size() == 1) {
						sink.next(files.get(0));
						return;
					}

					if (files.size() > 1) {
						sink.error(new IncorrectResultSizeDataAccessException(
								"Query " + SerializationUtils.serializeToJsonSafely(query) + " returned non unique result.", 1));
					}
				});
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.gridfs.ReactiveGridFsOperations#findFirst(ghost.framework.data.mongodb.core.query.Query)
	 */
	@Override
	public Mono<GridFSFile> findFirst(Query query) {

		Document queryObject = getMappedQuery(query.getQueryObject());
		Document sortObject = getMappedQuery(query.getSortObject());

		return createFlux(new FindLimitCallback(query, queryObject, sortObject, 1)).next();
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.gridfs.ReactiveGridFsOperations#delete(ghost.framework.data.mongodb.core.query.Query)
	 */
	@Override
	public Mono<Void> delete(Query query) {
		return find(query).flatMap(it -> createMono(new DeleteCallback(it.getId()))).then();
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.gridfs.ReactiveGridFsOperations#getResource(java.lang.String)
	 */
	@Override
	public Mono<ReactiveGridFsResource> getResource(String location) {

		Assert.notNull(location, "Filename must not be null!");

		return findOne(query(whereFilename().is(location))).flatMap(this::getResource)
				.defaultIfEmpty(ReactiveGridFsResource.absent(location));
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.gridfs.ReactiveGridFsOperations#getResource(com.mongodb.client.gridfs.entity.GridFSFile)
	 */
	@Override
	public Mono<ReactiveGridFsResource> getResource(GridFSFile file) {

		Assert.notNull(file, "GridFSFile must not be null!");

		return doGetBucket()
				.map(it -> new ReactiveGridFsResource(file, it.downloadToPublisher(file.getId()), dataBufferFactory));
	}

	/*
	 * (non-Javadoc)
	 * @see ghost.framework.data.mongodb.gridfs.ReactiveGridFsOperations#getResources(java.lang.String)
	 */
	@Override
	public Flux<ReactiveGridFsResource> getResources(String locationPattern) {

		if (!StringUtils.hasText(locationPattern)) {
			return Flux.empty();
		}

		AntPath path = new AntPath(locationPattern);

		if (path.isPattern()) {

			Flux<GridFSFile> files = find(query(whereFilename().regex(path.toRegex())));
			return files.flatMap(this::getResource);
		}

		return getResource(locationPattern).flux();
	}

	/**
	 * Create a reusable Mono for a {@link ReactiveBucketCallback}. It's up to the developer to choose to obtain a new
	 * {@link Flux} or to reuse the {@link Flux}.
	 *
	 * @param callback must not be {@literal null}
	 * @return a {@link Mono} wrapping the {@link ReactiveBucketCallback}.
	 */
	public <T> Mono<T> createMono(ReactiveBucketCallback<T> callback) {

		Assert.notNull(callback, "ReactiveBucketCallback must not be null!");

		return doGetBucket().flatMap(bucket -> Mono.from(callback.doInBucket(bucket)));
	}

	/**
	 * Create a reusable Flux for a {@link ReactiveBucketCallback}. It's up to the developer to choose to obtain a new
	 * {@link Flux} or to reuse the {@link Flux}.
	 *
	 * @param callback must not be {@literal null}
	 * @return a {@link Flux} wrapping the {@link ReactiveBucketCallback}.
	 */
	public <T> Flux<T> createFlux(ReactiveBucketCallback<T> callback) {

		Assert.notNull(callback, "ReactiveBucketCallback must not be null!");

		return doGetBucket().flatMapMany(callback::doInBucket);
	}

	protected Mono<GridFSBucket> doGetBucket() {
		return dbFactory.getMongoDatabase()
				.map(db -> bucket == null ? GridFSBuckets.create(db) : GridFSBuckets.create(db, bucket));
	}

	/**
	 * @param <T>
	 * @author Mathieu Ouellet
	 * @since 3.0
	 */
	interface ReactiveBucketCallback<T> {
		Publisher<T> doInBucket(GridFSBucket bucket);
	}

	private static class FindCallback implements ReactiveBucketCallback<GridFSFile> {

		private final Query query;
		private final Document queryObject;
		private final Document sortObject;

		public FindCallback(Query query, Document queryObject, Document sortObject) {

			this.query = query;
			this.queryObject = queryObject;
			this.sortObject = sortObject;
		}

		public GridFSFindPublisher doInBucket(GridFSBucket bucket) {

			GridFSFindPublisher findPublisher = bucket.find(queryObject).sort(sortObject);

			if (query.getLimit() > 0) {
				findPublisher = findPublisher.limit(query.getLimit());
			}

			if (query.getSkip() > 0) {
				findPublisher = findPublisher.skip(Math.toIntExact(query.getSkip()));
			}

			Integer cursorBatchSize = query.getMeta().getCursorBatchSize();
			if (cursorBatchSize != null) {
				findPublisher = findPublisher.batchSize(cursorBatchSize);
			}

			return findPublisher;
		}
	}

	private static class FindLimitCallback extends FindCallback {

		private final int limit;

		public FindLimitCallback(Query query, Document queryObject, Document sortObject, int limit) {

			super(query, queryObject, sortObject);
			this.limit = limit;
		}

		@Override
		public GridFSFindPublisher doInBucket(GridFSBucket bucket) {
			return super.doInBucket(bucket).limit(limit);
		}
	}

	private static class UploadCallback implements ReactiveBucketCallback<Void> {

		private final BsonValue fileId;
		private final String filename;
		private final Publisher<ByteBuffer> source;
		private final GridFSUploadOptions uploadOptions;

		public UploadCallback(BsonValue fileId, String filename, Publisher<ByteBuffer> source,
				GridFSUploadOptions uploadOptions) {

			this.fileId = fileId;
			this.filename = filename;
			this.source = source;
			this.uploadOptions = uploadOptions;
		}

		@Override
		public GridFSUploadPublisher<Void> doInBucket(GridFSBucket bucket) {
			return bucket.uploadFromPublisher(fileId, filename, source, uploadOptions);
		}
	}

	private static class AutoIdCreatingUploadCallback implements ReactiveBucketCallback<ObjectId> {

		private final String filename;
		private final Publisher<ByteBuffer> source;
		private final GridFSUploadOptions uploadOptions;

		public AutoIdCreatingUploadCallback(String filename, Publisher<ByteBuffer> source,
				GridFSUploadOptions uploadOptions) {

			this.filename = filename;
			this.source = source;
			this.uploadOptions = uploadOptions;
		}

		@Override
		public GridFSUploadPublisher<ObjectId> doInBucket(GridFSBucket bucket) {
			return bucket.uploadFromPublisher(filename, source, uploadOptions);
		}
	}

	private static class DeleteCallback implements ReactiveBucketCallback<Void> {

		private final BsonValue id;

		public DeleteCallback(BsonValue id) {
			this.id = id;
		}

		@Override
		public Publisher<Void> doInBucket(GridFSBucket bucket) {
			return bucket.delete(id);
		}
	}

}
