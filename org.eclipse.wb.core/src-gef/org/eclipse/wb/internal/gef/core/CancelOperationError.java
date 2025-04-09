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
