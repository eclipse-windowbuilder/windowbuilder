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
package org.eclipse.wb.internal.gef.core;

import org.eclipse.wb.internal.draw2d.EventManager;

/**
 * Special {@link Error} - signal that event handler in {@link EventManager} caused exception, that
 * was already handled, so should be ignored.
 *
 * @author scheglov_ke
 * @coverage gef.core
 */
public final class CancelOperationError extends Error {
  private static final long serialVersionUID = 0L;
}
