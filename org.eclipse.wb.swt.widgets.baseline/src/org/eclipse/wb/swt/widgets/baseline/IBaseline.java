/*******************************************************************************
 * Copyright (c) 2024 Patrick Ziegler and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Patrick Ziegle - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.swt.widgets.baseline;

import org.eclipse.swt.widgets.Control;

public sealed interface IBaseline permits Baseline {
	/**
	 * Constant used for widgets which have no baseline or their baseline can't be
	 * determined
	 */
	public static final int NO_BASELINE = -1;
	/**
	 * No resize behavior
	 */
	public static final int BRB_NONE = 0;
	/**
	 * Baseline resize behavior constant. Indicates as the size of the component
	 * changes the baseline remains a fixed distance from the top of the component.
	 */
	public static final int BRB_CONSTANT_ASCENT = 1;
	/**
	 * Baseline resize behavior constant. Indicates as the size of the component
	 * changes the baseline remains a fixed distance from the bottom of the
	 * component.
	 */
	public static final int BRB_CONSTANT_DESCENT = 2;
	/**
	 * Baseline resize behavior constant. Indicates as the size of the component
	 * changes the baseline remains a fixed distance from the center of the
	 * component.
	 */
	public static final int BRB_CENTER_OFFSET = 3;
	/**
	 * Baseline resize behavior constant. Indicates as the size of the component
	 * changes the baseline can not be determined using one of the other constants.
	 */
	public static final int BRB_OTHER = 4;

	/**
	 * Main baseline value fetch method. Basically it fetches baseline value from
	 * controls using control's font metrics and specific info (e.g. top or center
	 * alignments of text (I have never seen bottom alignment)) then does baseline
	 * adjustments. This is very approximate value of baseline we get in this
	 * method.
	 */
	int fetchBaseline(Control control, int width, int height);
}
