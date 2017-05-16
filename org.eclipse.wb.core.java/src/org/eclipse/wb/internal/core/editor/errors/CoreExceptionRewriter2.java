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
package org.eclipse.wb.internal.core.editor.errors;

import org.eclipse.wb.core.editor.errors.IExceptionRewriter;
import org.eclipse.wb.internal.core.eval.evaluators.AnonymousEvaluationError;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.exception.ICoreExceptionConstants;

/**
 * {@link IExceptionRewriter} for core exceptions.
 *
 * @author scheglov_ke
 * @coverage core.editor.errors
 */
public class CoreExceptionRewriter2 implements IExceptionRewriter {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final IExceptionRewriter INSTANCE = new CoreExceptionRewriter2();

  private CoreExceptionRewriter2() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IExceptionRewriter
  //
  ////////////////////////////////////////////////////////////////////////////
  public Throwable rewrite(Throwable e) {
    Throwable rootException = DesignerExceptionUtils.getRootCause(e);
    if (rootException instanceof AnonymousEvaluationError) {
      return new DesignerException(ICoreExceptionConstants.EVAL_ANONYMOUS, e, e.getMessage());
    }
    return e;
  }
}
