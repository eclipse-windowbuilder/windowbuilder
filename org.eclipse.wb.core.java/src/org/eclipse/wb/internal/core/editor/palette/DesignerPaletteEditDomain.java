/*******************************************************************************
 * Copyright (c) 2025 Patrick Ziegler and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.editor.palette;

import org.eclipse.gef.EditDomain;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.Tool;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;

/**
 * Custom edit domain used for the {@link PaletteViewer} that keeps track of
 * whether the CTRL key is pressed while creating a tool. If so, the tool
 * remains active after being used, allowing the user to add multiple elements
 * without having to re-select the entry every time.
 */
public class DesignerPaletteEditDomain extends EditDomain {

	private boolean reload;

	@Override
	public void mouseUp(MouseEvent mouseEvent, EditPartViewer viewer) {
		Tool tool = getActiveTool();
		if (tool != null) {
			try {
				reload = (mouseEvent.stateMask & SWT.CTRL) != 0;
				tool.mouseUp(mouseEvent, viewer);
			} finally {
				reload = false;
			}
		}
	}

	/**
	 * Returns {@code true}, when called from within the
	 * {@link #mouseUp(MouseEvent, EditPartViewer)} method while the CTRL key is
	 * pressed.
	 */
	public boolean isReload() {
		return reload;
	}
}
