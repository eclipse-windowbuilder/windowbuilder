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

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.Messages;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.ContentStamp;
import org.eclipse.ltk.core.refactoring.UndoTextFileChange;
import org.eclipse.text.edits.UndoEdit;

import java.text.MessageFormat;

/**
 * Copy of <code>UndoCompilationUnitChange</code> from internal JDT.
 *
 * @author scheglov_ke
 * @coverage core.util.refactoring
 */
/* package */class UndoCompilationUnitChange extends UndoTextFileChange {
  private final ICompilationUnit fCUnit;

  public UndoCompilationUnitChange(String name,
      ICompilationUnit unit,
      UndoEdit undo,
      ContentStamp stampToRestore,
      int saveMode) throws CoreException {
    super(name, getFile(unit), undo, stampToRestore, saveMode);
    fCUnit = unit;
  }

  private static IFile getFile(ICompilationUnit cunit) throws CoreException {
    IFile file = (IFile) cunit.getResource();
    if (file == null) {
      throw new CoreException(new Status(IStatus.ERROR,
          DesignerPlugin.PLUGIN_ID,
          IStatus.OK,
          MessageFormat.format(Messages.UndoCompilationUnitChange_noFile, cunit.getElementName()),
          null));
    }
    return file;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object getModifiedElement() {
    return fCUnit;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Change createUndoChange(UndoEdit edit, ContentStamp stampToRestore)
      throws CoreException {
    return new UndoCompilationUnitChange(getName(), fCUnit, edit, stampToRestore, getSaveMode());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Change perform(IProgressMonitor pm) throws CoreException {
    pm.beginTask("", 2); //$NON-NLS-1$
    fCUnit.becomeWorkingCopy(null, new SubProgressMonitor(pm, 1));
    try {
      return super.perform(new SubProgressMonitor(pm, 1));
    } finally {
      fCUnit.discardWorkingCopy();
    }
  }
}
