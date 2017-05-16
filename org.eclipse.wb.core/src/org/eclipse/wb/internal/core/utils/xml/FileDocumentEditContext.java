/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.utils.xml;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

/**
 * Context for editing XML in {@link IFile}.
 *
 * @author scheglov_ke
 * @coverage core.util.xml
 */
public class FileDocumentEditContext extends AbstractDocumentEditContext {
  private final IFile m_file;
  private final ITextFileBuffer m_buffer;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public FileDocumentEditContext(IFile file) throws Exception {
    m_file = file;
    // initialize buffer and document
    {
      ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
      manager.connect(m_file.getFullPath(), null);
      m_buffer = manager.getTextFileBuffer(file.getFullPath());
    }
    // parse and prepare root
    parse(m_buffer.getDocument());
    // set charset
    {
      String charset = m_file.getCharset();
      getRoot().getModel().setCharset(charset);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Buffer operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Commits changes made in this context to file.
   */
  @Override
  public final void commit() {
    ExecutionUtils.runRethrowUI(new RunnableEx() {
      public void run() throws Exception {
        commit_super();
        m_buffer.commit(null, false);
      }
    });
  }

  /**
   * "super" version of {@link #commit()} to use in inner class.
   */
  private void commit_super() throws Exception {
    super.commit();
  }

  /**
   * Disconnects this context from file. All changes made in model after this point are ignored and
   * not reflected in document/file.
   */
  @Override
  public final void disconnect() throws CoreException {
    // disconnect buffer
    {
      ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
      manager.disconnect(m_file.getFullPath(), null);
    }
    // continue
    super.disconnect();
  }
}
