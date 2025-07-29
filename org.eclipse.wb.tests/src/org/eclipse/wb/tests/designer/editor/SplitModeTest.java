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

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.multi.MultiMode;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;

import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jface.preference.IPreferenceStore;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.awt.Component;

/**
 * Test for "split" layout editor mode.
 *
 * @author scheglov_ke
 */
public class SplitModeTest extends SwingGefTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@AfterEach
	public void tearDown() throws Exception {
		super.tearDown();
		{
			IPreferenceStore preferences = DesignerPlugin.getPreferences();
			preferences.setToDefault(IPreferenceConstants.P_EDITOR_LAYOUT);
			preferences.setToDefault(IPreferenceConstants.P_EDITOR_LAYOUT_SYNC_DELAY);
		}
	}

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
	@Test
	public void test_reparse_afterDelay() throws Exception {
		IPreferenceStore preferences = DesignerPlugin.getPreferences();
		preferences.setValue(
				IPreferenceConstants.P_EDITOR_LAYOUT,
				IPreferenceConstants.V_EDITOR_LAYOUT_SPLIT_VERTICAL_DESIGN);
		preferences.setValue(IPreferenceConstants.P_EDITOR_LAYOUT_SYNC_DELAY, 100);
		openContainer("""
				// filler filler filler
				public class Test extends JPanel {
					public Test() {
					} // marker
				}""");
		openSourcePage();
		// initially no setEnabled(false) invocation
		check_isEnabled(true);
		// set focus to Source, as if user does this
		{
			m_designerEditor.getSite().getShell().forceActive();
			MultiMode multiMode = (MultiMode) m_designerEditor.getMultiMode();
			multiMode.getSourcePage().setFocus();
		}
		// insert setEnabled(false) into buffer
		{
			IBuffer buffer = m_lastEditor.getModelUnit().getBuffer();
			int position = buffer.getContents().indexOf("} // marker");
			buffer.replace(position, 0, "setEnabled(false);");
		}
		// still not re-parsed
		check_isEnabled(true);
		// wait for re-parse
		waitEventLoop(1000);
		check_isEnabled(false);
	}

	@Test
	public void test_reparse_afterSave() throws Exception {
		IPreferenceStore preferences = DesignerPlugin.getPreferences();
		preferences.setValue(
				IPreferenceConstants.P_EDITOR_LAYOUT,
				IPreferenceConstants.V_EDITOR_LAYOUT_SPLIT_VERTICAL_DESIGN);
		preferences.setValue(IPreferenceConstants.P_EDITOR_LAYOUT_SYNC_DELAY, -1);
		openContainer("""
				// filler filler filler
				public class Test extends JPanel {
					public Test() {
					} // marker
				}""");
		openSourcePage();
		// initially no setEnabled(false) invocation
		check_isEnabled(true);
		// insert setEnabled(false) into buffer
		{
			IBuffer buffer = m_lastEditor.getModelUnit().getBuffer();
			int position = buffer.getContents().indexOf("} // marker");
			buffer.replace(position, 0, "setEnabled(false);");
		}
		// still not re-parsed
		check_isEnabled(true);
		// wait, but still not re-parsed
		waitEventLoop(300);
		check_isEnabled(true);
		// do save
		m_designerEditor.doSave(null);
		check_isEnabled(false);
	}

	private void check_isEnabled(boolean expected) {
		fetchContentFields();
		assertEquals(expected, ((Component) m_contentJavaInfo.getObject()).isEnabled());
	}
}
