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
package org.eclipse.wb.tests.designer.editor;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.editor.multi.MultiMode;
import org.eclipse.wb.internal.core.editor.structure.property.JavaPropertiesToolBarContributor;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;

import static org.eclipse.swtbot.swt.finder.matchers.WithTooltip.withTooltip;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link JavaPropertiesToolBarContributor}.
 *
 * @author scheglov_ke
 */
public class JavaPropertiesToolBarContributorTest extends SwingGefTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Exit zone :-) XXX
	//
	////////////////////////////////////////////////////////////////////////////
	public void _test_exit() throws Exception {
		System.exit(0);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for "Goto definition" action.
	 */
	@Test
	public void test_gotoDefinition() throws Exception {
		openContainer("""
				// filler filler filler filler filler
				// filler filler filler filler filler
				public class Test extends JPanel {
					public Test() {
						{
							JButton button = new JButton();
							add(button);
						}
					}
				}""");
		JavaInfo button = getJavaInfoByName("button");
		// prepare swt bot
		SWTBot bot = new SWTBot(m_designerEditor.getRootControl());
		// no selection initially, so no action
		{
			assertTrue(bot.getFinder().findControls(withTooltip("Goto definition")).isEmpty());
		}
		// select "button", show actions
		canvas.select(button);
		// use action
		{
			assertFalse(bot.getFinder().findControls(withTooltip("Goto definition")).isEmpty());
			SWTBotToolbarButton toolItem = bot.toolbarButtonWithTooltip("Goto definition");
			toolItem.click();
			waitEventLoop(0);
		}
		// assert that position in XML source was opened
		{
			// "Source" is active
			MultiMode multiMode = (MultiMode) m_designerEditor.getMultiMode();
			assertTrue(multiMode.isSourceActive());
			// selection in source
			int expectedPosition = button.getCreationSupport().getNode().getStartPosition();
			assertJavaSelection(expectedPosition, 0);
		}
	}

	/**
	 * Test for "Local to field" action.
	 */
	@Test
	public void test_convertLocalToField() throws Exception {
		openContainer("""
				// filler filler filler filler filler
				// filler filler filler filler filler
				public class Test extends JPanel {
					public Test() {
						{
							JButton button = new JButton();
							add(button);
						}
					}
				}""");
		JavaInfo button = getJavaInfoByName("button");
		// prepare UiContext
		SWTBot bot = new SWTBot(m_designerEditor.getRootControl());
		// no selection initially, so no action
		{
			assertTrue(bot.getFinder().findControls(withTooltip("Convert local to field")).isEmpty());
		}
		// select "button", show actions
		canvas.select(button);
		// use action
		{
			assertFalse(bot.getFinder().findControls(withTooltip("Convert local to field")).isEmpty());
			SWTBotToolbarButton toolItem = bot.toolbarButtonWithTooltip("Convert local to field");
			toolItem.click();
		}
		assertEditor("""
				// filler filler filler filler filler
				// filler filler filler filler filler
				public class Test extends JPanel {
					private JButton button;
					public Test() {
						{
							button = new JButton();
							add(button);
						}
					}
				}""");
		// use action
		{
			SWTBotToolbarButton toolItem = bot.toolbarButtonWithTooltip("Convert field to local");
			toolItem.click();
		}
		assertEditor("""
				// filler filler filler filler filler
				// filler filler filler filler filler
				public class Test extends JPanel {
					public Test() {
						{
							JButton button = new JButton();
							add(button);
						}
					}
				}""");
	}
}
