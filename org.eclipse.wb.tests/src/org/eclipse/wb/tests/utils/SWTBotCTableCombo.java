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

import org.eclipse.wb.core.controls.CTableCombo;

import org.eclipse.swt.SWT;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBot;

/**
 * Wrapped for the {@link CTableCombo} to allow access when not in the UI
 * thread.
 */
public class SWTBotCTableCombo extends AbstractSWTBot<CTableCombo> {
	public SWTBotCTableCombo(CTableCombo w) {
		super(w);
	}

	public int getItemCount() {
		return syncExec(widget::getItemCount);
	}

	public String getItem(int i) {
		return syncExec(() -> widget.getItem(i));
	}

	public void select(int i) {
		syncExec(() -> {
			widget.select(i);
			widget.notifyListeners(SWT.Selection, null);
		});
	}
}
