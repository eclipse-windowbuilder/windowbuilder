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
package org.eclipse.wb.draw2d;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

/**
 * A collection of color-related constants.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public interface IColorConstants {
	/**
	 * System color used to paint highlight shadow areas.
	 */
	Color buttonLightest = Utils.getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW);
	/**
	 * System color used to paint background areas.
	 */
	Color button = Utils.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
	/**
	 * System color used to paint normal shadow areas.
	 */
	Color buttonDarker = Utils.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
	/**
	 * System color used to paint dark shadow areas.
	 */
	Color buttonDarkest = Utils.getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW);
	/**
	 * System color used to paint list background areas.
	 */
	Color listBackground = Utils.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
	/**
	 * System color used to paint list foreground areas.
	 */
	Color listForeground = Utils.getSystemColor(SWT.COLOR_LIST_FOREGROUND);
	/**
	 * System color used to paint list selection area.
	 */
	Color listSelection = Utils.getSystemColor(SWT.COLOR_LIST_SELECTION);
	/**
	 * System color used to paint list selection text.
	 */
	Color listSelectionText = Utils.getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT);
	/**
	 * System color used to paint background areas.
	 */
	Color menuBackground = Utils.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
	/**
	 * System color used to paint foreground areas.
	 */
	Color menuForeground = Utils.getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
	/**
	 * System color used to paint list selection background areas.
	 */
	Color menuBackgroundSelected = Utils.getSystemColor(SWT.COLOR_LIST_SELECTION);
	/**
	 * System color used to paint list selected text.
	 */
	Color menuForegroundSelected = Utils.getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT);
	/**
	 * System color used to paint title background areas.
	 */
	Color titleBackground = Utils.getSystemColor(SWT.COLOR_TITLE_BACKGROUND);
	/**
	 * System color used to paint title background gradient.
	 */
	Color titleGradient = Utils.getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT);
	/**
	 * System color used to paint title text.
	 */
	Color titleForeground = Utils.getSystemColor(SWT.COLOR_TITLE_FOREGROUND);
	/**
	 * System color used to paint inactive title text.
	 */
	Color titleInactiveForeground = Utils.getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND);
	/**
	 * System color used to paint inactive title text.
	 */
	Color titleInactiveBackground = Utils.getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND);
	/**
	 * System color used to paint inactive title text.
	 */
	Color titleInactiveGradient = Utils.getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND);
	/**
	 * System color used to paint tooltip text.
	 */
	Color tooltipForeground = Utils.getSystemColor(SWT.COLOR_INFO_FOREGROUND);
	/**
	 * System color used to paint tooltip background areas.
	 */
	Color tooltipBackground = Utils.getSystemColor(SWT.COLOR_INFO_BACKGROUND);
	/**
	 * Miscellaneous colors.
	 */
	Color white = new Color(null, 255, 255, 255);
	Color lightGray = new Color(null, 192, 192, 192);
	Color gray = new Color(null, 128, 128, 128);
	Color darkGray = new Color(null, 64, 64, 64);
	Color black = new Color(null, 0, 0, 0);
	Color red = new Color(null, 255, 0, 0);
	Color orange = new Color(null, 255, 196, 0);
	Color yellow = new Color(null, 255, 255, 0);
	Color green = new Color(null, 0, 255, 0);
	Color lightGreen = new Color(null, 96, 255, 96);
	Color darkGreen = new Color(null, 0, 127, 0);
	Color cyan = new Color(null, 0, 255, 255);
	Color lightBlue = new Color(null, 127, 127, 255);
	Color blue = new Color(null, 0, 0, 255);
	Color darkBlue = new Color(null, 0, 0, 127);
	Color ghostFillColor = new Color(null, 31, 31, 31);

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Internal helper.
	 */
	public static class Utils {
		/**
		 * Invokes {@link Display#getSystemColor(int)} in UI thread.
		 */
		private static Color getSystemColor(final int id) {
			final Color[] color = new Color[1];
			final Display display = Display.getDefault();
			display.syncExec(new Runnable() {
				@Override
				public void run() {
					color[0] = display.getSystemColor(id);
				}
			});
			return color[0];
		}
	}
}