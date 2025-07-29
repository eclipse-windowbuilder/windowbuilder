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

import org.eclipse.wb.internal.core.editor.actions.CutAction;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;

import org.eclipse.jface.action.IAction;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link CutAction}.
 *
 * @author scheglov_ke
 */
public class CutActionTest extends SwingGefTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * "Cut" action is disabled if no selection.
	 */
	@Test
	public void test_noSelection() throws Exception {
		openContainer("""
				// filler filler filler
				public class Test extends JPanel {
					public Test() {
					}
				}""");
		// prepare "Cut" action
		IAction cutAction = getCutAction();
		// no selection - disabled action
		canvas.select();
		assertFalse(cutAction.isEnabled());
	}

	/**
	 * "This" component can not be copied or deleted.
	 */
	@Test
	public void test_thisSelection() throws Exception {
		ContainerInfo panel = openContainer("""
				// filler filler filler
				public class Test extends JPanel {
					public Test() {
					}
				}""");
		// prepare "Cut" action
		IAction cutAction = getCutAction();
		// "this" selected - disabled action
		canvas.select(panel);
		assertFalse(cutAction.isEnabled());
	}

	/**
	 * Test for cut/paste single component.
	 */
	@Test
	public void test_cutSingle() throws Exception {
		ContainerInfo panel = openContainer("""
				// filler filler filler
				public class Test extends JPanel {
					public Test() {
						JButton button = new JButton("0");
						add(button);
					}
				}""");
		// select "button"
		{
			ComponentInfo button = panel.getChildrenComponents().get(0);
			canvas.select(button);
		}
		// cut
		{
			IAction cutAction = getCutAction();
			assertTrue(cutAction.isEnabled());
			cutAction.run();
			assertEditor("""
					// filler filler filler
					public class Test extends JPanel {
						public Test() {
						}
					}""");
		}
		// paste
		{
			IAction pasteAction = getPasteAction();
			assertTrue(pasteAction.isEnabled());
			pasteAction.run();
			// do paste
			canvas.moveTo(panel, 10, 10);
			canvas.click();
			assertEditor("""
					// filler filler filler
					public class Test extends JPanel {
						public Test() {
							{
								JButton button = new JButton("0");
								add(button);
							}
						}
					}""");
		}
	}
}
