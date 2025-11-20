/*******************************************************************************
 * Copyright (c) 2025 Patrick Ziegler and others.
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
package org.eclipse.wb.internal.os.linux;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

/**
 * GtkWidget is the base class all widgets in GTK+ derive from. It manages the
 * widget lifecycle, states and style.
 */
public record GtkWidget(long handle) {

	public GtkWidget(Widget widget) {
		this(getHandle(widget));
	}

	/**
	 * @return the handle value of the {@link Widget} using reflection.
	 */
	private static long getHandle(Widget widget) {
		long widgetHandle = getHandleValue(widget, "fixedHandle");
		if (widgetHandle == 0) {
			// may be null, roll back to "handle"
			if (widget instanceof Shell) {
				widgetHandle = getHandleValue(widget, "shellHandle");
			} else {
				widgetHandle = getHandleValue(widget, "handle");
			}
		}
		return widgetHandle;
	}

	/**
	 * @return the widget as native pointer for native handles. Note: returns 0 if
	 *         handle cannot be obtained.
	 */
	private static long getHandleValue(Object widget, String fieldName) {
		if (ReflectionUtils.getFieldObject(widget, fieldName) instanceof Long longValue) {
			return longValue;
		}
		// field might be shadowed (e.g. in ImageBasedFrame)
		return 0L;
	}
}
