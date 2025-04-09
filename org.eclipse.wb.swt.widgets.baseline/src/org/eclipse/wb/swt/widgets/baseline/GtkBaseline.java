/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
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
package org.eclipse.wb.swt.widgets.baseline;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * package private class used to fetch baseline values on Gtk platform
 *
 * @author mitin_aa
 */
final class GtkBaseline extends Baseline {
	static {
		Library.loadLibrary("baseline");
	}

	private native static int fetchBaseline(long widgetHandle);

	private native static int fetchBaselineFromLayout(long layoutHandle);

	@Override
	public int fetchBaseline(Control control, int width, int height) {
		int baseline = NO_BASELINE;
		try {
			// we need to check if any text set for widget. If no text set we will set
			// our own to fetch baseline value properly (actually I suppose that we should not do baseline
			// fetch from widgets without "text" property).
			Class<?> clazz = control.getClass();
			Method setTextMethod = null;
			try {
				Method getTextMethod = clazz.getMethod("getText", new Class[]{});
				if (getTextMethod != null) {
					String oldText = (String) getTextMethod.invoke(control, new Object[]{});
					if (oldText == null || oldText.length() == 0) {
						setTextMethod = clazz.getMethod("setText", new Class[]{String.class});
						setTextMethod.invoke(control, "a");
						// wait for deferred Gtk events to complete
						while (Display.getDefault().readAndDispatch()) {
							// noop
						}
					}
				}
			} catch (Throwable e) {
				return NO_BASELINE;
			}
			// fetch baseline
			if (control instanceof org.eclipse.swt.widgets.Link linkControl) {
				Field layoutField = org.eclipse.swt.widgets.Link.class.getDeclaredField("layout");
				layoutField.setAccessible(true);
				org.eclipse.swt.graphics.TextLayout textLayout =
						(org.eclipse.swt.graphics.TextLayout) layoutField.get(linkControl);
				Field layoutHandleField =
						org.eclipse.swt.graphics.TextLayout.class.getDeclaredField("layout");
				layoutHandleField.setAccessible(true);
				long layoutHandle = layoutHandleField.getLong(textLayout);
				baseline = fetchBaselineFromLayout(layoutHandle);
			} else {
				Field controlHandleField = Widget.class.getDeclaredField("handle");
				long controlHandle = controlHandleField.getLong(control);
				baseline = fetchBaseline(controlHandle);
			}
			// bring back the empty string if needed
			if (setTextMethod != null) {
				setTextMethod.invoke(control, "");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return baseline;
	}
}
