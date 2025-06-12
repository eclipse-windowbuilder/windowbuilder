/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.tests.designer.editor.actions;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.editor.actions.RefreshAction;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;

import org.eclipse.jface.action.IAction;

import org.junit.jupiter.api.Test;

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
