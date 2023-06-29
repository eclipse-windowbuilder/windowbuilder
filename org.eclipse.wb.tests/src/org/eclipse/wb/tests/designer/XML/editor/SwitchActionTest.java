/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.XML.editor;

import org.eclipse.wb.internal.core.xml.editor.actions.SwitchAction;
import org.eclipse.wb.tests.designer.XWT.gef.XwtGefTest;

/**
 * Test for {@link SwitchAction}.
 *
 * @author scheglov_ke
 */
public class SwitchActionTest extends XwtGefTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for switching to "Source" and back to "Design".
	 */
	public void test_run() throws Exception {
		openEditor("<Shell/>");
		// prepare action
		SwitchAction switchAction;
		{
			switchAction = new SwitchAction();
			switchAction.setActiveEditor(null, m_designerEditor);
		}
		// after "openDesign" the "Design" page is active
		assertEquals(1, m_designerEditor.getActivePage());
		waitEventLoop(0);
		// switch to "Source" using action
		switchAction.run(null);
		waitEventLoop(0);
		assertEquals(0, m_designerEditor.getActivePage());
		// switch to "Design" using action
		switchAction.run(null);
		waitEventLoop(0);
		assertEquals(1, m_designerEditor.getActivePage());
	}

	/**
	 * Test for {@link SwitchAction#showSource()}.
	 */
	public void test_showSource() throws Exception {
		openEditor("<Shell/>");
		// "Design" is active
		assertEquals(1, m_designerEditor.getActivePage());
		// switch to "Source"
		SwitchAction.showSource();
		assertEquals(0, m_designerEditor.getActivePage());
		assertXMLSelection(0, 0);
	}

	/**
	 * Test for {@link SwitchAction#showSource(int)}.
	 */
	public void test_showSource_withOffset() throws Exception {
		openEditor("<Shell/>");
		// "Design" is active
		assertEquals(1, m_designerEditor.getActivePage());
		// switch to "Source"
		SwitchAction.showSource(5);
		waitEventLoop(0);
		assertEquals(0, m_designerEditor.getActivePage());
		assertXMLSelection(5, 0);
	}
}
