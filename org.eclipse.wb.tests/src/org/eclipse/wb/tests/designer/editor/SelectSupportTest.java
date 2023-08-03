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
package org.eclipse.wb.tests.designer.editor;

import org.eclipse.wb.core.editor.IDesignPageSite;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.editor.actions.SelectSupport;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;
import org.eclipse.wb.tests.gef.EventSender;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;

import org.junit.Test;

/**
 * Test for {@link SelectSupport}.
 *
 * @author scheglov_ke
 */
public class SelectSupportTest extends SwingGefTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_all() throws Exception {
		JavaInfo panel =
				openContainer(
						"// filler filler filler filler filler",
						"public class Test extends JPanel {",
						"	public Test() {",
						"	  {",
						"	    JButton button_1 = new JButton('Button 1');",
						"	    add(button_1);",
						"	  }",
						"	  {",
						"	    JButton button_2 = new JButton('Button 2');",
						"	    add(button_2);",
						"	  }",
						"	  {",
						"	    JTextField text_1 = new JTextField(15);",
						"	    add(text_1);",
						"	  }",
						"	}",
						"}");
		JavaInfo button_1 = getJavaInfoByName("button_1");
		JavaInfo button_2 = getJavaInfoByName("button_2");
		JavaInfo text_1 = getJavaInfoByName("text_1");
		// use hot keys
		{
			// all
			canvas.deselectAll();
			{
				sendSelectKey(SWT.CTRL);
				canvas.assertSelection(panel, button_1, button_2, text_1);
			}
			// same type
			canvas.deselectAll();
			{
				canvas.select(button_1);
				sendSelectKey(SWT.CTRL | SWT.SHIFT);
				canvas.assertSelection(button_1, button_2);
			}
			// same parent
			canvas.deselectAll();
			{
				canvas.select(text_1);
				sendSelectKey(SWT.CTRL | SWT.ALT);
				canvas.assertSelection(button_1, button_2, text_1);
			}
		}
		// use context menu
		{
			IMenuManager contextMenu = getContextMenu(panel);
			IMenuManager selectMenu = findChildMenuManager(contextMenu, "Select");
			// all
			canvas.deselectAll();
			{
				findChildAction(selectMenu, "All").run();
				canvas.assertSelection(panel, button_1, button_2, text_1);
			}
			// same type
			canvas.deselectAll();
			{
				canvas.select(button_1);
				findChildAction(selectMenu, "All of Same Type").run();
				canvas.assertSelection(button_1, button_2);
			}
			// same parent
			canvas.deselectAll();
			{
				canvas.select(text_1);
				findChildAction(selectMenu, "All on Same Parent").run();
				canvas.assertSelection(button_1, button_2, text_1);
			}
		}
	}

	@Test
	public void test_disposeHierarchy() throws Exception {
		openContainer(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"public class Test extends JPanel {",
				"	public Test() {",
				"	}",
				"}");
		// reparse
		{
			IDesignPageSite.Helper.getSite(m_contentJavaInfo).reparse();
			fetchContentFields();
		}
		// selection hot keys still work
		canvas.deselectAll();
		{
			sendSelectKey(SWT.CTRL);
			canvas.assertSelection(m_contentJavaInfo);
		}
	}

	private void sendSelectKey(int stateMask) {
		Control control = m_viewerCanvas.getControl();
		EventSender eventSender = new EventSender(control);
		eventSender.setStateMask(stateMask);
		eventSender.keyDown('a');
	}
}
