/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
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
