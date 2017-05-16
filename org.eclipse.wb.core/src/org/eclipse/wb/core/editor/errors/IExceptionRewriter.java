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
