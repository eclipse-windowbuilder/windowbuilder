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
package org.eclipse.wb.internal.xwt.refactoring;

import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;

/**
 * Abstract {@link IMethod} rename participant.
 *
 * @author scheglov_ke
 * @coverage XWT.refactoring
 */
public abstract class AbstractRenameParticipant extends RenameParticipant {
  protected IMethod m_method;

  ////////////////////////////////////////////////////////////////////////////
  //
  // RefactoringParticipant
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getName() {
    return "XWT rename participant";
  }

  @Override
  protected boolean initialize(Object element) {
    Assert.isTrue(
        element instanceof IMethod,
        "Only IMethod can be renamed, but {0} received. Check participant enablement filters.",
        element);
    m_method = (IMethod) element;
    return true;
  }

  @Override
  public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context) {
    return new RefactoringStatus();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Change
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final Change createChange(final IProgressMonitor pm) {
    return ExecutionUtils.runObjectLog(new RunnableObjectEx<Change>() {
      public Change runObject() throws Exception {
        return createChangeEx(pm);
      }
    }, null);
  }

  /**
   * Implementation of {@link #createChange(IProgressMonitor)} that can throw {@link Exception}.
   */
  public abstract Change createChangeEx(IProgressMonitor pm) throws Exception;
}
