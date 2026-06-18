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
package org.eclipse.wb.internal.os.linux.gtk3;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

/**
 * Represents the desired size of a widget.
 */
public record GtkRequisition(MemorySegment segment) {
	public static final GtkRequisition NULL = new GtkRequisition(MemorySegment.NULL);

	/**
	 * @return The widget’s desired width.
	 */
	public int width() {
		return segment.getAtIndex(ValueLayout.JAVA_INT, 0);
	}

	/**
	 * @return The widget’s desired height.
	 */
	public int height() {
		return segment.getAtIndex(ValueLayout.JAVA_INT, 0);
	}
}
