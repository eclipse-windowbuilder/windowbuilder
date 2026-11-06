/*******************************************************************************
 * Copyright (c) 2026 Lars Vogel and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Lars Vogel - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.gef;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Menu;

import org.junit.jupiter.api.Test;

public class TreeViewerTest extends TreeToolTest {
	/**
	 * The components tree shares the context menu of the design canvas, which must survive the menu
	 * being set again on every parse.
	 */
	@Test
	public void test_setContextMenu_sharedWithCanvas() {
		Canvas canvas = new Canvas(m_shell, SWT.NONE);
		MenuManager manager = new MenuManager();
		Menu menu = manager.createContextMenu(canvas);
		canvas.setMenu(menu);
		//
		m_viewer.setContextMenu(manager);
		m_viewer.setContextMenu(manager);
		assertFalse(menu.isDisposed());
		assertSame(menu, canvas.getMenu());
		assertSame(menu, m_viewer.getControl().getMenu());
	}
}