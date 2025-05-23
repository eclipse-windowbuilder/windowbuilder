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

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Text;

import java.lang.reflect.Method;

/**
 * Baseline class used to determine text baseline value of widgets. Singleton. Usage:
 * Baseline.getBaseline(someControl) Baseline.getBaseline(someControl, controlWidth, controlHeight)
 *
 * @author mitin_aa
 */
public sealed class Baseline implements IBaseline
		permits CarbonBaseline, CocoaBaseline, DefaultBaseline, GtkBaseline, WindowsBaseline {
	// spinner class may not exist in early versions of SWT
	protected static Class<?> spinnerClass = null;
	protected static Class<?> datetimeClass = null;
	static {
		try {
			spinnerClass = Class.forName("org.eclipse.swt.widgets.Spinner");
		} catch (Throwable e) {
			// ignore all
		}
		try {
			datetimeClass = Class.forName("org.eclipse.swt.widgets.DateTime");
		} catch (Throwable e) {
			// ignore all
		}
	}
	// private instance of Baseline
	private static IBaseline m_instance;

	// protected ctor
	protected Baseline() {
	}

	/**
	 * return true for widget classes which text is top-aligned
	 */
	protected boolean topAlignedText(Class<?> clazz, int style) {
		return false;
	}

	/**
	 * return true for widget classes which text is center-aligned
	 */
	protected boolean centerAlignedText(Class<?> clazz, int style) {
		return true;
	}

	/**
	 * Does some baseline adjustments for baselines found in empiric way
	 */
	protected int adjustBaseline(Control control, int baseline) {
		return baseline;
	}

	//
	private static synchronized IBaseline getInstance() {
		if (m_instance == null) {
			String platform = SWT.getPlatform();
			if ("win32".equalsIgnoreCase(platform)) {
				m_instance = new WindowsBaseline();
			} else if ("gtk".equalsIgnoreCase(platform)) {
				m_instance = new GtkBaseline();
			} else if ("carbon".equalsIgnoreCase(platform)) {
				m_instance = new CarbonBaseline();
			} else if ("cocoa".equalsIgnoreCase(platform)) {
				m_instance = new CocoaBaseline();
			} else {
				m_instance = new DefaultBaseline();
			}
		}
		return m_instance;
	}

	public static int getBaseline(Control control) {
		if (control == null || control.isDisposed()) {
			return NO_BASELINE;
		}
		Rectangle controlBounds = control.getBounds();
		return getBaseline(control, controlBounds.width, controlBounds.height);
	}

	public static int getBaseline(Control control, int width, int height) {
		if (control == null || control.isDisposed()) {
			return NO_BASELINE;
		}
		try {
			try {
				// first of all try to get and invoke 'getBaseline()' method of control (if any).
				Method baselineMethod = control.getClass().getMethod("getBaseline", new Class[]{});
				if (baselineMethod != null) {
					int baseline = ((Integer) baselineMethod.invoke(control, new Object[]{})).intValue();
					if (baseline != NO_BASELINE) {
						return baseline;
					}
				}
			} catch (Throwable e) {
				// just ignore and try usual way
			}
			return getInstance().fetchBaseline(control, width, height);
		} catch (Throwable e) {
			Platform.getLog(Baseline.class).error(e.getMessage(), e);
			return NO_BASELINE;
		}
	}

	/**
	 * Main baseline value fetch method. Basically it fetches baseline value from controls using
	 * control's font metrics and specific info (e.g. top or center alignments of text (I have never
	 * seen bottom alignment)) then does baseline adjustments. This is very approximate value of
	 * baseline we get in this method.
	 */
	@Override
	public int fetchBaseline(Control control, int width, int height) {
		int baseline = NO_BASELINE;
		GC gc = new GC(control);
		Font font = control.getFont();
		gc.setFont(font);
		FontMetrics fontMetrics = gc.getFontMetrics();
		gc.dispose();
		int fontAscent = fontMetrics.getAscent() + fontMetrics.getLeading();
		int fontHeight = fontMetrics.getHeight();
		if (topAlignedText(control.getClass(), control.getStyle())) {
			baseline = fontAscent;
		}
		if (centerAlignedText(control.getClass(), control.getStyle())) {
			baseline = height / 2 - fontHeight / 2 + fontAscent;
		}
		baseline = adjustBaseline(control, baseline);
		return baseline;
	}

	/**
	 * Returns a constant indicating how the baseline varies with the size of the component.
	 *
	 * @param c
	 *          the Control to get the baseline resize behavior for
	 * @return one of BRB_CONSTANT_ASCENT, BRB_CONSTANT_DESCENT, BRB_CENTER_OFFSET or BRB_OTHER
	 */
	public static int getBaselineResizeBehavior(Control control) {
		if (control instanceof Text && (control.getStyle() & SWT.V_SCROLL) != 0) {
			return BRB_CONSTANT_ASCENT;
		}
		return BRB_OTHER;
	}

	/**
	 * @param control
	 * @return Control's insets as an array of int, where index: 0 - left side, 1 - top side, 2 -
	 *         right side, 3 - bottom side
	 */
	public static int[] getInsets(Control control) {
		int[] is = new int[4];
		if (control instanceof Scrollable scrollable) {
			Rectangle bounds = scrollable.getBounds();
			Rectangle clientArea = scrollable.getClientArea();
			Point clientAreaInDisplay = scrollable.toDisplay(new Point(clientArea.x, clientArea.y));
			Point boundsInDisplay;
			Composite parent = scrollable.getParent();
			if (parent == null) {
				boundsInDisplay = new Point(bounds.x, bounds.y);
			} else {
				boundsInDisplay = parent.toDisplay(new Point(bounds.x, bounds.y));
			}
			clientArea.x = clientAreaInDisplay.x;
			clientArea.y = clientAreaInDisplay.y;
			is[0] = clientArea.x - boundsInDisplay.x; // left
			is[1] = clientArea.y - boundsInDisplay.y; // top
			is[2] = boundsInDisplay.x + bounds.width - clientArea.x - clientArea.width; // right
			is[3] = boundsInDisplay.y + bounds.height - clientArea.y - clientArea.height; // bottom
		}
		return is;
	}
}
