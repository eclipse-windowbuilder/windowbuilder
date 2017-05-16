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
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.ContentStamp;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.UndoEdit;

/**
 * Copy of <code>CompilationUnitChange</code> from internal JDT.
 *
 * @author scheglov_ke
 * @coverage core.util.refactoring
 */
public class CompilationUnitChange extends TextFileChange {
  private final ICompilationUnit fCUnit;

  /**
   * Creates a new <code>CompilationUnitChange</code>.
   *
   * @param name
   *          the change's name mainly used to render the change in the UI
   * @param cunit
   *          the compilation unit this text change works on
   */
  public CompilationUnitChange(String name, ICompilationUnit cunit) {
    super(name, getFile(cunit));
    Assert.isNotNull(cunit);
    fCUnit = cunit;
    setTextType("java"); //$NON-NLS-1$
  }

  private static IFile getFile(ICompilationUnit cunit) {
    return (IFile) cunit.getResource();
  }

  /* non java-doc
   * Method declared in IChange.
   */
  @Override
  public Object getModifiedElement() {
    return fCUnit;
  }

  /**
   * Returns the compilation unit this change works on.
   *
   * @return the compilation unit this change works on
   */
  public ICompilationUnit getCompilationUnit() {
    return fCUnit;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected IDocument acquireDocument(IProgressMonitor pm) throws CoreException {
    pm.beginTask("", 2); //$NON-NLS-1$
    fCUnit.becomeWorkingCopy(null, new SubProgressMonitor(pm, 1));
    return super.acquireDocument(new SubProgressMonitor(pm, 1));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void releaseDocument(IDocument document, IProgressMonitor pm) throws CoreException {
    boolean isModified = isDocumentModified();
    super.releaseDocument(document, pm);
    try {
      fCUnit.discardWorkingCopy();
    } finally {
      if (isModified && !isDocumentAcquired()) {
        if (fCUnit.isWorkingCopy()) {
          fCUnit.reconcile(ICompilationUnit.NO_AST, false, null, null);
        } else {
          fCUnit.makeConsistent(pm);
        }
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Change createUndoChange(UndoEdit edit, ContentStamp stampToRestore) {
    try {
      return new UndoCompilationUnitChange(getName(), fCUnit, edit, stampToRestore, getSaveMode());
    } catch (CoreException e) {
      DesignerPlugin.log(e);
      return null;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings("rawtypes")
  public Object getAdapter(Class adapter) {
    if (ICompilationUnit.class.equals(adapter)) {
      return fCUnit;
    }
    return super.getAdapter(adapter);
  }
}
