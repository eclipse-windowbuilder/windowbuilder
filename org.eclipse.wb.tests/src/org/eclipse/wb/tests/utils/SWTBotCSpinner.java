/*******************************************************************************
 * Copyright (c) 2025 Patrick Ziegler
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
package org.eclipse.wb.tests.utils;

import org.eclipse.wb.core.controls.CSpinner;

import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBot;

/**
 * Wrapped for the {@link CSpinner} to allow access when not in the UI thread.
 */
public class SWTBotCSpinner extends AbstractSWTBot<CSpinner> {
	public SWTBotCSpinner(CSpinner w) {
		super(w);
	}

	/**
	 * Sets the <em>value</em>, which is the receiver's position, to the argument.
	 * If the argument is not within the range specified by minimum and maximum, it
	 * will be adjusted to fall within this range.
	 */
	public void setSelection(int newValue) {
		syncExec(() -> widget.setSelection(newValue));
	}
}
