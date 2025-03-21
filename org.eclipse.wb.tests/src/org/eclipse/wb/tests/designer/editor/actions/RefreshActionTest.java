/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.editor.actions;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.editor.actions.RefreshAction;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;

import org.eclipse.jface.action.IAction;

import org.junit.Test;

/**
 * Test for {@link RefreshAction}.
 *
 * @author mitin_aa
 */
public class RefreshActionTest extends SwingGefTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_1() throws Exception {
		JavaInfo currentRoot = openContainer("""
				// filler filler filler
				public class Test extends JPanel {
					public Test() {
					}
				}""");
		// do refresh
		{
			IAction refreshAction = m_designPageActions.getRefreshAction();
			refreshAction.run();
			waitEventLoop(10);
		}
		// different root JavaInfo expected
		fetchContentFields();
		assertNotSame(currentRoot, m_contentJavaInfo);
	}
}
