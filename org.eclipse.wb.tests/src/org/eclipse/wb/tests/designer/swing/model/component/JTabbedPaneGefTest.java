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
package org.eclipse.wb.tests.designer.swing.model.component;

import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.component.JTabbedPaneInfo;
import org.eclipse.wb.internal.swing.model.component.JTabbedPaneTabInfo;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;

import org.junit.Test;

import java.util.List;

/**
 * Test for {@link JTabbedPaneInfo} in GEF.
 *
 * @author scheglov_ke
 */
public class JTabbedPaneGefTest extends SwingGefTest {
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
	// Canvas
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_childrenForActiveTab() throws Exception {
		ContainerInfo panel =
				openContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new BorderLayout());",
						"    JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);",
						"    add(tabbedPane);",
						"    {",
						"      JPanel panel_1 = new JPanel();",
						"      panel_1.setLayout(null);",
						"      tabbedPane.addTab('AAAAAA', panel_1);",
						"      {",
						"        JButton button_1 = new JButton();",
						"        button_1.setBounds(10, 10, 100, 100);",
						"        panel_1.add(button_1);",
						"      }",
						"    }",
						"    {",
						"      JPanel panel_2 = new JPanel();",
						"      panel_2.setLayout(null);",
						"      tabbedPane.addTab('BBBBBB', panel_2);",
						"      {",
						"        JButton button_2 = new JButton();",
						"        button_2.setBounds(110, 10, 100, 100);",
						"        panel_2.add(button_2);",
						"      }",
						"    }",
						"  }",
						"}");
		JTabbedPaneInfo tabbedPane = (JTabbedPaneInfo) panel.getChildrenComponents().get(0);
		ContainerInfo panel_1 = (ContainerInfo) tabbedPane.getChildrenComponents().get(0);
		ContainerInfo panel_2 = (ContainerInfo) tabbedPane.getChildrenComponents().get(1);
		ComponentInfo button_1 = panel_1.getChildrenComponents().get(0);
		ComponentInfo button_2 = panel_2.getChildrenComponents().get(0);
		// only "panel_1" and "button_1" should have EditPart
		assertNotNull(canvas.getEditPart(panel_1));
		assertNotNull(canvas.getEditPart(button_1));
		//
		canvas.assertNullEditPart(panel_2);
		canvas.assertNullEditPart(button_2);
		// click position of "button_1", it should be selected
		canvas.target(tabbedPane).in(50, 50).move().click();
		assertSelectionModels(button_1);
		// click position of "button_2", it is on inactive tag, so "panel_1" should be selected
		canvas.target(tabbedPane).in(150, 50).move().click();
		assertSelectionModels(panel_1);
	}

	@Test
	public void test_tab_MOVE() throws Exception {
		ContainerInfo panel =
				openContainer(
						"class Test extends JPanel {",
						"  Test() {",
						"    JTabbedPane tabbed = new JTabbedPane();",
						"    add(tabbed);",
						"    {",
						"      JLabel label = new JLabel();",
						"      tabbed.addTab('111', label);",
						"    }",
						"    {",
						"      JButton button = new JButton();",
						"      tabbed.addTab('222', button);",
						"    }",
						"  }",
						"}");
		JTabbedPaneInfo tabbedPane = (JTabbedPaneInfo) panel.getChildrenComponents().get(0);
		ComponentInfo label = tabbedPane.getChildrenComponents().get(0);
		ComponentInfo button = tabbedPane.getChildrenComponents().get(1);
		canvas.assertNotNullEditPart(label);
		canvas.assertNullEditPart(button);
		// tabs
		List<JTabbedPaneTabInfo> tabs = tabbedPane.getTabs();
		JTabbedPaneTabInfo labelTab = tabs.get(0);
		JTabbedPaneTabInfo buttonTab = tabs.get(1);
		// move tab
		canvas.beginDrag(buttonTab).dragTo(labelTab, 5, 5).endDrag();
		// check
		canvas.assertNullEditPart(label);
		canvas.assertNotNullEditPart(button);
		assertEditor(
				"class Test extends JPanel {",
				"  Test() {",
				"    JTabbedPane tabbed = new JTabbedPane();",
				"    add(tabbed);",
				"    {",
				"      JButton button = new JButton();",
				"      tabbed.addTab('222', button);",
				"    }",
				"    {",
				"      JLabel label = new JLabel();",
				"      tabbed.addTab('111', label);",
				"    }",
				"  }",
				"}");
	}
}
