/*******************************************************************************
 * Copyright (c) 2026 Patrick Ziegler and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/

package org.eclipse.wb.internal.core.model.broadcast;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.BroadcastSupport;

/**
 * Contains all internal events that may be fired by the
 * {@link BroadcastSupport}.
 */
public final class BroadcastConstants {
	/**
	 * Fired at the start of {@link ObjectInfo#refresh()} to indicate that no paint
	 * operations must happen on the underlying canvas while the model is being
	 * created. Paint operations must be suppressed as e.g. the snapshots might not
	 * have been created yet.
	 */
	public static final String PROP_OBJECT_INFO_REDRAW_ENABLE = "wbp/objectInfo/redraw/enable";
	/**
	 * Fired at the end of {@link ObjectInfo#refresh()} to indicate that paint
	 * operations may be executed again on the underlying canvas.
	 */
	public static final String PROP_OBJECT_INFO_REDRAW_DISABLE = "wbp/objectInfo/redraw/disable";

	private BroadcastConstants() {
		throw new IllegalStateException("Must never be instantiated");
	}
}
