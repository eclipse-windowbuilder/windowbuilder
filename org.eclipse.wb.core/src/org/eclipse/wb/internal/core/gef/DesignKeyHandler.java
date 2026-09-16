/*******************************************************************************
 * Copyright (c) 2011, 2026 Google, Inc. and others.
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
package org.eclipse.wb.internal.core.gef;

import org.eclipse.wb.gef.core.requests.KeyRequest;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.tools.SelectionTool;
import org.eclipse.swt.events.KeyEvent;

import java.util.List;

/**
 * Key handler to perform a {@link KeyRequest} on all selected edit parts.
 */
public class DesignKeyHandler extends KeyHandler {
	private final EditPartViewer viewer;

	public DesignKeyHandler(EditPartViewer viewer) {
		this.viewer = viewer;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (viewer.getEditDomain().getActiveTool() instanceof SelectionTool) {
			handleKeyEvent(true, event, viewer.getSelectedEditParts());
		}
		return super.keyPressed(event);
	}

	@Override
	public boolean keyReleased(KeyEvent event) {
		if (viewer.getEditDomain().getActiveTool() instanceof SelectionTool) {
			handleKeyEvent(false, event, viewer.getSelectedEditParts());
		}
		return super.keyReleased(event);
	}

	private static void handleKeyEvent(boolean pressed, KeyEvent event, List<? extends EditPart> selection) {
		KeyRequest request = new KeyRequest(pressed, event);
		for (EditPart part : selection) {
			part.performRequest(request);
		}
	}

}
