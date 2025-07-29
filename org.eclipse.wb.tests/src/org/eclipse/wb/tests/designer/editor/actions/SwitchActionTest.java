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

import org.eclipse.wb.internal.core.editor.actions.SwitchAction;
import org.eclipse.wb.internal.core.editor.multi.MultiMode;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link SwitchAction}.
 *
 * @author mitin_aa
 */
public class SwitchActionTest extends SwingGefTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for switching to source and back to design.
	 */
	@Test
	public void test_1() throws Exception {
		openContainer("""
				// filler filler filler
				public class Test extends JPanel {
					public Test() {
					}
				}""");
		m_designerEditor.getSite().getShell().forceActive();
		MultiMode multiMode = (MultiMode) m_designerEditor.getMultiMode();
		// prepare action
		SwitchAction switchAction;
		{
			switchAction = new SwitchAction();
			switchAction.setActiveEditor(null, m_designerEditor);
		}
		// after "openDesign" the "Design" page is active
		assertFalse(multiMode.getSourcePage().isActive());
		waitEventLoop(10);
		// switch to "Source" using action
		switchAction.run(null);
		waitEventLoop(10);
		assertTrue(multiMode.getSourcePage().isActive());
		// switch to "Design" using action
		switchAction.run(null);
		waitEventLoop(10);
		assertFalse(multiMode.getSourcePage().isActive());
	}
}
