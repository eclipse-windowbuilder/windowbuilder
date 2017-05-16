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
package org.eclipse.wb.internal.core.utils.refactoring;

import org.eclipse.wb.internal.core.utils.Messages;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import java.text.MessageFormat;

/**
 * {@link Change} for deleting files. It ignores fact that file may be already does not exist. We
 * need such behavior because in GWT participants it is possible that use deletes both Async and
 * RemoteService classes in single selection, so when we try to delete Async it is already deleted,
 * so we should ignore this.
 *
 * @author scheglov_ke
 * @coverage core.util.refactoring
 */
public class DeleteFileChange extends Change {
  private final IFile m_file;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DeleteFileChange(IFile file) {
    m_file = file;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Change
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Object getModifiedElement() {
    return m_file;
  }

  @Override
  public String getName() {
    return MessageFormat.format(Messages.DeleteFileChange_name, m_file.getLocation().lastSegment());
  }

  @Override
  public void initializeValidationData(IProgressMonitor pm) {
  }

  @Override
  public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException {
    RefactoringStatus status = new RefactoringStatus();
    if (!m_file.exists()) {
      status.addError(MessageFormat.format(
          Messages.DeleteFileChange_errNoFile,
          m_file.getLocation().lastSegment()));
    }
    return status;
  }

  @Override
  public Change perform(IProgressMonitor pm) throws CoreException {
    m_file.delete(false, pm);
    return null;
  }
}
