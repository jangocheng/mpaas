package ghost.framework.maven.aether.concurrency;

/*******************************************************************************
 * Copyright (c) 2010-2014 Takari, Inc., Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import ghost.framework.maven.filemanager.FileManager;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.SyncContext;
import org.eclipse.aether.impl.SyncContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * A synchronization context factory that employs OS-level file locks to control access to artifacts/metadatas.
 */
@Named
@Singleton
public class LockingSyncContextFactory implements SyncContextFactory {

  private Logger logger = LoggerFactory.getLogger(LockingFileProcessor.class);

  private FileManager fileLockManager;

  @Inject
  public LockingSyncContextFactory(FileManager fileLockManager) {
    this.fileLockManager = fileLockManager;
  }

  public SyncContext newInstance(RepositorySystemSession session, boolean shared) {
    return new LockingSyncContext(shared, session, fileLockManager, logger);
  }

}
