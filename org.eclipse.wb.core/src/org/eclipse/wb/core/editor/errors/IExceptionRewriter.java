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
package org.eclipse.wb.core.editor.errors;

import org.eclipse.wb.internal.core.utils.exception.DesignerException;

/**
 * Implementations of this interface may analyze given {@link Throwable} and replace it with
 * different, better {@link Throwable}, usually {@link DesignerException}.
 *
 * @author scheglov_ke
 * @coverage core.editor.errors
 */
public interface IExceptionRewriter {
	/**
	 * @return the same or new {@link Throwable}.
	 */
	Throwable rewrite(Throwable e);
}
