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
package org.eclipse.wb.internal.core.eval.evaluators;

import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;

/**
 * We don't want to evaluate anonymous {@link ClassInstanceCreation}.
 *
 * @author scheglov_ke
 * @coverage core.evaluation
 */
public final class AnonymousEvaluationError extends Error {
  private static final long serialVersionUID = 0L;

  public static boolean is(Throwable e) {
    return DesignerExceptionUtils.getRootCause(e) instanceof AnonymousEvaluationError;
  }
}
