/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
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
package org.eclipse.wb.tests.designer.swing.model.util;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.ui.MenuIntersector;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.LayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.absolute.AbsoluteLayoutInfo;
import org.eclipse.wb.internal.swing.model.util.surround.SwingSurroundSupport;
import org.eclipse.wb.tests.designer.Expectations;
import org.eclipse.wb.tests.designer.Expectations.StrValue;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

/**
 * Tests for {@link SwingSurroundSupport}.
 *
 * @author scheglov_ke
 */
public class SurroundSupportTest extends SwingModelTest {
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
	// Invalid selection
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Empty selection, so no "surround" menu.
	 */
	@Test
	public void test_emptySelection() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		panel.refresh();
		//
		assertNoSurroundManager(panel, Collections.emptyList());
	}

	/**
	 * Try to give {@link LayoutInfo} instead of {@link ComponentInfo}, so no "surround" menu.
	 */
	@Test
	public void test_notComponent() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		panel.refresh();
		LayoutInfo layout = panel.getLayout();
		//
		assertNoSurroundManager(panel, List.of(layout));
	}

	/**
	 * Components that have different parents, so none of these parents contribute "surround" menu.
	 */
	@Test
	public void test_notSameParent() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    {",
						"      JButton button = new JButton();",
						"      add(button);",
						"      button.setBounds(10, 20, 100, 50);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		//
		assertNoSurroundManager(panel, List.of(panel, button));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Flow layouts
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Single {@link ComponentInfo} on {@link FlowLayoutInfo}.
	 */
	@Test
	public void test_flow_singleComponent() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    {",
						"      JButton button = new JButton();",
						"      add(button);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// run action
		runSurround_JPanel(button);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    {",
				"      JPanel panel = new JPanel();",
				"      add(panel);",
				"      {",
				"        JButton button = new JButton();",
				"        panel.add(button);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Single {@link ComponentInfo} on {@link FlowLayoutInfo}.
	 */
	@Test
	public void test_flow_singleComponent_onTitledJPanel() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    {",
						"      JButton button = new JButton();",
						"      add(button);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// run action
		runSurround("javax.swing.JPanel (border)", button);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    {",
				"      JPanel panel = new JPanel();",
				"      panel.setBorder(new TitledBorder(null, 'JPanel title', TitledBorder.LEADING, TitledBorder.TOP, null, null));",
				"      add(panel);",
				"      {",
				"        JButton button = new JButton();",
				"        panel.add(button);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Two adjacent components, good case.
	 */
	@Test
	public void test_flow_twoComponents() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    {",
						"      JButton button_1 = new JButton();",
						"      add(button_1);",
						"    }",
						"    {",
						"      JButton button_2 = new JButton();",
						"      add(button_2);",
						"    }",
						"    {",
						"      JButton button_3 = new JButton();",
						"      add(button_3);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button_1 = panel.getChildrenComponents().get(0);
		ComponentInfo button_2 = panel.getChildrenComponents().get(1);
		// run action
		runSurround_JPanel(button_1, button_2);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    {",
				"      JPanel panel = new JPanel();",
				"      add(panel);",
				"      {",
				"        JButton button_1 = new JButton();",
				"        panel.add(button_1);",
				"      }",
				"      {",
				"        JButton button_2 = new JButton();",
				"        panel.add(button_2);",
				"      }",
				"    }",
				"    {",
				"      JButton button_3 = new JButton();",
				"      add(button_3);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Not an adjacent components, so can not surround.
	 */
	@Test
	public void test_flow_notAdjacentComponents() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    {",
						"      JButton button_1 = new JButton();",
						"      add(button_1);",
						"    }",
						"    {",
						"      JButton button_2 = new JButton();",
						"      add(button_2);",
						"    }",
						"    {",
						"      JButton button_3 = new JButton();",
						"      add(button_3);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button_1 = panel.getChildrenComponents().get(0);
		ComponentInfo button_3 = panel.getChildrenComponents().get(2);
		// can not surround
		assertNoSurroundManager(button_3, List.of(button_1, button_3));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Absolute layout
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Single {@link ComponentInfo} on {@link AbsoluteLayoutInfo}.
	 */
	@Test
	public void test_absolute_singleControl() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(null);",
						"    {",
						"      JButton button = new JButton();",
						"      add(button);",
						"      button.setBounds(10, 20, 100, 50);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// "button" is on absolute layout, so has "Bounds" property
		assertNotNull(button.getPropertyByTitle("Bounds"));
		// run action
		runSurround_JPanel(button);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(null);",
				"    {",
				"      JPanel panel = new JPanel();",
				"      panel.setBounds(10, 20, 100, 50);",
				"      add(panel);",
				"      panel.setLayout(null);",
				"      {",
				"        JButton button = new JButton();",
				"        button.setBounds(0, 0, 100, 50);",
				"        panel.add(button);",
				"      }",
				"    }",
				"  }",
				"}");
		// "button" is on new absolute layout, so still has "Bounds" property
		assertNotNull(button.getPropertyByTitle("Bounds"));
	}

	/**
	 * Single {@link ComponentInfo} on {@link AbsoluteLayoutInfo}.
	 */
	@Disabled
	@Test
	public void test_absolute_singleControl_onTitledJPanel() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(null);",
						"    {",
						"      JButton button = new JButton();",
						"      add(button);",
						"      button.setBounds(50, 50, 100, 50);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// run action
		runSurround("javax.swing.JPanel (border)", button);
		String expectedPanelBounds =
				Expectations.get("44, 28, 112, 79", new StrValue[]{
						new StrValue("flanker-windows", "44, 30, 112, 77"),
						new StrValue("scheglov-win", "44, 30, 112, 77")});
		String expectedButtonBounds =
				Expectations.get("6, 22, 100, 50", new StrValue[]{
						new StrValue("flanker-windows", "6, 20, 100, 50"),
						new StrValue("scheglov-win", "6, 20, 100, 50")});
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(null);",
				"    {",
				"      JPanel panel = new JPanel();",
				"      panel.setBorder(new TitledBorder(null, 'JPanel title', TitledBorder.LEADING, TitledBorder.TOP, null, null));",
				"      panel.setBounds(" + expectedPanelBounds + ");",
				"      add(panel);",
				"      panel.setLayout(null);",
				"      {",
				"        JButton button = new JButton();",
				"        button.setBounds(" + expectedButtonBounds + ");",
				"        panel.add(button);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Two {@link ComponentInfo}'s on {@link AbsoluteLayoutInfo}.
	 */
	@Test
	public void test_absolute_twoControls() throws Exception {
		ContainerInfo shell =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(null);",
						"    {",
						"      JButton button_1 = new JButton();",
						"      add(button_1);",
						"      button_1.setBounds(150, 50, 100, 20);",
						"    }",
						"    {",
						"      JButton button_2 = new JButton();",
						"      add(button_2);",
						"      button_2.setBounds(10, 10, 100, 20);",
						"    }",
						"    {",
						"      JButton button_3 = new JButton();",
						"      add(button_3);",
						"      button_3.setBounds(160, 100, 110, 50);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ComponentInfo button_1 = shell.getChildrenComponents().get(0);
		ComponentInfo button_3 = shell.getChildrenComponents().get(2);
		// run action
		runSurround_JPanel(button_1, button_3);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(null);",
				"    {",
				"      JPanel panel = new JPanel();",
				"      panel.setBounds(150, 50, 120, 100);",
				"      add(panel);",
				"      panel.setLayout(null);",
				"      {",
				"        JButton button_1 = new JButton();",
				"        button_1.setBounds(0, 0, 100, 20);",
				"        panel.add(button_1);",
				"      }",
				"      {",
				"        JButton button_3 = new JButton();",
				"        button_3.setBounds(10, 50, 110, 50);",
				"        panel.add(button_3);",
				"      }",
				"    }",
				"    {",
				"      JButton button_2 = new JButton();",
				"      add(button_2);",
				"      button_2.setBounds(10, 10, 100, 20);",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// JSplitPane
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * {@link JSplitPane} as target container.<br>
	 * Good: one component, placed as "left".
	 */
	@Test
	public void test_JSplitPane_oneComponent() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    {",
						"      JButton button_1 = new JButton();",
						"      add(button_1);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button_1 = panel.getChildrenComponents().get(0);
		// run action
		runSurround("javax.swing.JSplitPane", button_1);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    {",
				"      JSplitPane splitPane = new JSplitPane();",
				"      add(splitPane);",
				"      {",
				"        JButton button_1 = new JButton();",
				"        splitPane.setLeftComponent(button_1);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	/**
	 * {@link JSplitPane} as target container.<br>
	 * Good: exactly two components.
	 */
	@Test
	public void test_JSplitPane_twoComponents() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    {",
						"      JButton button_1 = new JButton();",
						"      add(button_1);",
						"    }",
						"    {",
						"      JButton button_2 = new JButton();",
						"      add(button_2);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button_1 = panel.getChildrenComponents().get(0);
		ComponentInfo button_2 = panel.getChildrenComponents().get(1);
		// run action
		runSurround("javax.swing.JSplitPane", button_1, button_2);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    {",
				"      JSplitPane splitPane = new JSplitPane();",
				"      add(splitPane);",
				"      {",
				"        JButton button_1 = new JButton();",
				"        splitPane.setLeftComponent(button_1);",
				"      }",
				"      {",
				"        JButton button_2 = new JButton();",
				"        splitPane.setRightComponent(button_2);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	/**
	 * {@link JSplitPane} as target container.<br>
	 * Bad: at max two components can be placed on {@link JSplitPane}.
	 */
	@Test
	public void test_JSplitPane_threeComponents() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    {",
						"      JButton button_1 = new JButton();",
						"      add(button_1);",
						"    }",
						"    {",
						"      JButton button_2 = new JButton();",
						"      add(button_2);",
						"    }",
						"    {",
						"      JButton button_3 = new JButton();",
						"      add(button_3);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button_1 = panel.getChildrenComponents().get(0);
		ComponentInfo button_2 = panel.getChildrenComponents().get(1);
		ComponentInfo button_3 = panel.getChildrenComponents().get(2);
		// run action
		assertNoSurroundAction("javax.swing.JSplitPane", button_1, button_2, button_3);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// JScrollPane
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * {@link JScrollPane} as target container.<br>
	 * One component, placed as "viewportView".
	 */
	@Test
	public void test_JScrollPane_oneComponent() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    {",
						"      JButton button_1 = new JButton();",
						"      add(button_1);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button_1 = panel.getChildrenComponents().get(0);
		// run action
		runSurround("javax.swing.JScrollPane", button_1);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    {",
				"      JScrollPane scrollPane = new JScrollPane();",
				"      add(scrollPane);",
				"      {",
				"        JButton button_1 = new JButton();",
				"        scrollPane.setViewportView(button_1);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	/**
	 * {@link JScrollPane} as target container.<br>
	 * Two components, placed on {@link JPanel}.
	 */
	@Test
	public void test_JScrollPane_twoComponents() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    {",
						"      JButton button_1 = new JButton();",
						"      add(button_1);",
						"    }",
						"    {",
						"      JButton button_2 = new JButton();",
						"      add(button_2);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button_1 = panel.getChildrenComponents().get(0);
		ComponentInfo button_2 = panel.getChildrenComponents().get(1);
		// run action
		runSurround("javax.swing.JScrollPane", button_1, button_2);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    {",
				"      JScrollPane scrollPane = new JScrollPane();",
				"      add(scrollPane);",
				"      {",
				"        JPanel panel = new JPanel();",
				"        scrollPane.setViewportView(panel);",
				"        {",
				"          JButton button_1 = new JButton();",
				"          panel.add(button_1);",
				"        }",
				"        {",
				"          JButton button_2 = new JButton();",
				"          panel.add(button_2);",
				"        }",
				"      }",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// JTabbedPane
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * {@link JTabbedPane} as target container.<br>
	 * Two components, placed on {@link JPanel}.
	 */
	@Test
	public void test_JTabbedPane_twoComponents() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    {",
						"      JButton button_1 = new JButton();",
						"      add(button_1);",
						"    }",
						"    {",
						"      JButton button_2 = new JButton();",
						"      add(button_2);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button_1 = panel.getChildrenComponents().get(0);
		ComponentInfo button_2 = panel.getChildrenComponents().get(1);
		// run action
		runSurround("javax.swing.JTabbedPane", button_1, button_2);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    {",
				"      JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);",
				"      add(tabbedPane);",
				"      {",
				"        JButton button_1 = new JButton();",
				"        tabbedPane.addTab('New tab', null, button_1, null);",
				"      }",
				"      {",
				"        JButton button_2 = new JButton();",
				"        tabbedPane.addTab('New tab', null, button_2, null);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// JGoodies FormLayout
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Bad: two components on diagonal, and other component in same rectangle.
	 */
	@Test
	public void test_FormLayout_0() throws Exception {
		ContainerInfo panel =
				parseTestSourceJGFL(new String[]{
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new FormLayout(new ColumnSpec[] {",
						"        FormSpecs.RELATED_GAP_COLSPEC,",
						"        FormSpecs.DEFAULT_COLSPEC,",
						"        FormSpecs.RELATED_GAP_COLSPEC,",
						"        FormSpecs.DEFAULT_COLSPEC,},",
						"      new RowSpec[] {",
						"        FormSpecs.RELATED_GAP_ROWSPEC,",
						"        FormSpecs.DEFAULT_ROWSPEC,",
						"        FormSpecs.RELATED_GAP_ROWSPEC,",
						"        FormSpecs.DEFAULT_ROWSPEC,}));",
						"    {",
						"      JButton button_22 = new JButton();",
						"      add(button_22, \"2, 2\");",
						"    }",
						"    {",
						"      JButton button_BAD = new JButton();",
						"      add(button_BAD, \"4, 2\");",
						"    }",
						"    {",
						"      JButton button_44 = new JButton();",
						"      add(button_44, \"4, 4\");",
						"    }",
						"  }",
				"}"});
		panel.refresh();
		ComponentInfo button_22 = getButtons(panel).get(0);
		ComponentInfo button_44 = getButtons(panel).get(2);
		// no surround
		assertNoSurroundManager(panel, List.of(button_22, button_44));
	}

	/**
	 * Wrap {@link JTable} with {@link JScrollPane}.
	 */
	@Test
	public void test_FormLayout_1() throws Exception {
		ContainerInfo panel =
				parseTestSourceJGFL(new String[]{
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new FormLayout(new ColumnSpec[] {",
						"        FormSpecs.DEFAULT_COLSPEC,},",
						"      new RowSpec[] {",
						"        FormSpecs.DEFAULT_ROWSPEC,}));",
						"    {",
						"      JTable table = new JTable();",
						"      add(table, \"1, 1\");",
						"    }",
						"  }",
				"}"});
		panel.refresh();
		ComponentInfo table = panel.getChildrenComponents().get(0);
		// run action
		runSurround("javax.swing.JScrollPane", table);
		assertEditor(
				getTestSourceJGFL(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new FormLayout(new ColumnSpec[] {",
						"        FormSpecs.DEFAULT_COLSPEC,},",
						"      new RowSpec[] {",
						"        FormSpecs.DEFAULT_ROWSPEC,}));",
						"    {",
						"      JScrollPane scrollPane = new JScrollPane();",
						"      add(scrollPane, \"1, 1, fill, fill\");",
						"      {",
						"        JTable table = new JTable();",
						"        scrollPane.setViewportView(table);",
						"      }",
						"    }",
						"  }",
						"}"),
				m_lastEditor);
	}

	/**
	 * Good: two components in single row, no other components.
	 */
	@Test
	public void test_FormLayout_2() throws Exception {
		ContainerInfo panel =
				parseTestSourceJGFL(new String[]{
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new FormLayout(new ColumnSpec[] {",
						"        FormSpecs.RELATED_GAP_COLSPEC,",
						"        FormSpecs.DEFAULT_COLSPEC,",
						"        FormSpecs.UNRELATED_GAP_COLSPEC,",
						"        FormSpecs.PREF_COLSPEC,},",
						"      new RowSpec[] {",
						"        FormSpecs.RELATED_GAP_ROWSPEC,",
						"        FormSpecs.DEFAULT_ROWSPEC,}));",
						"    {",
						"      JButton button_22 = new JButton();",
						"      add(button_22, \"2, 2\");",
						"    }",
						"    {",
						"      JButton button_42 = new JButton();",
						"      add(button_42, \"4, 2\");",
						"    }",
						"  }",
				"}"});
		panel.refresh();
		ComponentInfo button_22 = getButtons(panel).get(0);
		ComponentInfo button_42 = getButtons(panel).get(1);
		// run action
		runSurround_JPanel(button_22, button_42);
		assertEditor(
				getTestSourceJGFL(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new FormLayout(new ColumnSpec[] {",
						"        FormSpecs.RELATED_GAP_COLSPEC,",
						"        FormSpecs.DEFAULT_COLSPEC,},",
						"      new RowSpec[] {",
						"        FormSpecs.RELATED_GAP_ROWSPEC,",
						"        FormSpecs.DEFAULT_ROWSPEC,}));",
						"    {",
						"      JPanel panel = new JPanel();",
						"      add(panel, \"2, 2, fill, fill\");",
						"      panel.setLayout(new FormLayout(new ColumnSpec[] {",
						"          FormSpecs.DEFAULT_COLSPEC,",
						"          FormSpecs.UNRELATED_GAP_COLSPEC,",
						"          FormSpecs.PREF_COLSPEC,},",
						"        new RowSpec[] {",
						"          FormSpecs.DEFAULT_ROWSPEC,}));",
						"      {",
						"        JButton button_22 = new JButton();",
						"        panel.add(button_22, \"1, 1\");",
						"      }",
						"      {",
						"        JButton button_42 = new JButton();",
						"        panel.add(button_42, \"3, 1\");",
						"      }",
						"    }",
						"  }",
						"}"),
				m_lastEditor);
	}

	/**
	 * Good: two components on diagonal, no other components.
	 */
	@Test
	public void test_FormLayout_3() throws Exception {
		ContainerInfo panel =
				parseTestSourceJGFL(new String[]{
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new FormLayout(new ColumnSpec[] {",
						"        FormSpecs.RELATED_GAP_COLSPEC,",
						"        FormSpecs.DEFAULT_COLSPEC,",
						"        FormSpecs.UNRELATED_GAP_COLSPEC,",
						"        FormSpecs.PREF_COLSPEC,},",
						"      new RowSpec[] {",
						"        FormSpecs.RELATED_GAP_ROWSPEC,",
						"        FormSpecs.DEFAULT_ROWSPEC,",
						"        FormSpecs.UNRELATED_GAP_ROWSPEC,",
						"        FormSpecs.PREF_ROWSPEC,}));",
						"    {",
						"      JButton button_22 = new JButton();",
						"      add(button_22, \"2, 2\");",
						"    }",
						"    {",
						"      JButton button_44 = new JButton();",
						"      add(button_44, \"4, 4\");",
						"    }",
						"  }",
				"}"});
		panel.refresh();
		ComponentInfo button_22 = getButtons(panel).get(0);
		ComponentInfo button_44 = getButtons(panel).get(1);
		// run action
		runSurround_JPanel(button_22, button_44);
		assertEditor(
				getTestSourceJGFL(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new FormLayout(new ColumnSpec[] {",
						"        FormSpecs.RELATED_GAP_COLSPEC,",
						"        FormSpecs.DEFAULT_COLSPEC,},",
						"      new RowSpec[] {",
						"        FormSpecs.RELATED_GAP_ROWSPEC,",
						"        FormSpecs.DEFAULT_ROWSPEC,}));",
						"    {",
						"      JPanel panel = new JPanel();",
						"      add(panel, \"2, 2, fill, fill\");",
						"      panel.setLayout(new FormLayout(new ColumnSpec[] {",
						"          FormSpecs.DEFAULT_COLSPEC,",
						"          FormSpecs.UNRELATED_GAP_COLSPEC,",
						"          FormSpecs.PREF_COLSPEC,},",
						"        new RowSpec[] {",
						"          FormSpecs.DEFAULT_ROWSPEC,",
						"          FormSpecs.UNRELATED_GAP_ROWSPEC,",
						"          FormSpecs.PREF_ROWSPEC,}));",
						"      {",
						"        JButton button_22 = new JButton();",
						"        panel.add(button_22, \"1, 1\");",
						"      }",
						"      {",
						"        JButton button_44 = new JButton();",
						"        panel.add(button_44, \"3, 3\");",
						"      }",
						"    }",
						"  }",
						"}"),
				m_lastEditor);
	}

	/**
	 * Good: three components, one spanned horizontally.
	 */
	@Test
	public void test_FormLayout_4() throws Exception {
		ContainerInfo panel =
				parseTestSourceJGFL(new String[]{
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new FormLayout(new ColumnSpec[] {",
						"        FormSpecs.RELATED_GAP_COLSPEC,",
						"        FormSpecs.DEFAULT_COLSPEC,",
						"        FormSpecs.UNRELATED_GAP_COLSPEC,",
						"        FormSpecs.PREF_COLSPEC,},",
						"      new RowSpec[] {",
						"        FormSpecs.RELATED_GAP_ROWSPEC,",
						"        FormSpecs.DEFAULT_ROWSPEC,",
						"        FormSpecs.UNRELATED_GAP_ROWSPEC,",
						"        FormSpecs.PREF_ROWSPEC,}));",
						"    {",
						"      JButton button_22 = new JButton();",
						"      add(button_22, \"2, 2\");",
						"    }",
						"    {",
						"      JButton button_42 = new JButton();",
						"      add(button_42, \"4, 2\");",
						"    }",
						"    {",
						"      JButton button_24 = new JButton();",
						"      add(button_24, \"2, 4, 3, 1, fill, bottom\");",
						"    }",
						"  }",
				"}"});
		panel.refresh();
		ComponentInfo button_22 = getButtons(panel).get(0);
		ComponentInfo button_42 = getButtons(panel).get(1);
		ComponentInfo button_24 = getButtons(panel).get(2);
		// run action
		runSurround_JPanel(button_22, button_42, button_24);
		assertEditor(
				getTestSourceJGFL(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new FormLayout(new ColumnSpec[] {",
						"        FormSpecs.RELATED_GAP_COLSPEC,",
						"        FormSpecs.DEFAULT_COLSPEC,},",
						"      new RowSpec[] {",
						"        FormSpecs.RELATED_GAP_ROWSPEC,",
						"        FormSpecs.DEFAULT_ROWSPEC,}));",
						"    {",
						"      JPanel panel = new JPanel();",
						"      add(panel, \"2, 2, fill, fill\");",
						"      panel.setLayout(new FormLayout(new ColumnSpec[] {",
						"          FormSpecs.DEFAULT_COLSPEC,",
						"          FormSpecs.UNRELATED_GAP_COLSPEC,",
						"          FormSpecs.PREF_COLSPEC,},",
						"        new RowSpec[] {",
						"          FormSpecs.DEFAULT_ROWSPEC,",
						"          FormSpecs.UNRELATED_GAP_ROWSPEC,",
						"          FormSpecs.PREF_ROWSPEC,}));",
						"      {",
						"        JButton button_22 = new JButton();",
						"        panel.add(button_22, \"1, 1\");",
						"      }",
						"      {",
						"        JButton button_42 = new JButton();",
						"        panel.add(button_42, \"3, 1\");",
						"      }",
						"      {",
						"        JButton button_24 = new JButton();",
						"        panel.add(button_24, \"1, 3, 3, 1, fill, bottom\");",
						"      }",
						"    }",
						"  }",
						"}"),
				m_lastEditor);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// JGoodies FormLayout utils
	//
	////////////////////////////////////////////////////////////////////////////
	private String getTestSourceJGFL(String... lines) throws Exception {
		lines =
				CodeUtils.join(new String[]{
						"import com.jgoodies.forms.layout.*;",
				"import com.jgoodies.forms.factories.*;"}, lines);
		return getTestSource(lines);
	}

	private ContainerInfo parseTestSourceJGFL(String[] lines) throws Exception {
		// ensure FormLayout
		{
			m_testProject.addPlugin("com.jgoodies.common");
			m_testProject.addPlugin("com.jgoodies.forms");
		}
		// do parse
		lines =
				CodeUtils.join(new String[]{
						"import com.jgoodies.forms.layout.*;",
				"import com.jgoodies.forms.factories.*;"}, lines);
		return parseContainer(lines);
	}

	/**
	 * @return the {@link ComponentInfo} models for {@link JButton} components.
	 */
	private static List<ComponentInfo> getButtons(ContainerInfo parent) {
		List<ComponentInfo> buttons = new ArrayList<>();
		for (ComponentInfo control : parent.getChildrenComponents()) {
			if (control.getDescription().getComponentClass().getName().equals("javax.swing.JButton")) {
				buttons.add(control);
			}
		}
		return buttons;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the "Surround with" {@link IMenuManager} for given {@link ComponentInfo}'s.
	 */
	private static IMenuManager createSurroundManager(ObjectInfo object,
			List<? extends ObjectInfo> objects) throws Exception {
		IMenuManager resultMenuManager;
		if (objects.size() < 2) {
			resultMenuManager = getDesignerMenuManager();
			object.getBroadcastObject().addContextMenu(objects, object, resultMenuManager);
		} else {
			resultMenuManager = new MenuManager();
			// handle multi selection
			List<IMenuManager> managers = new ArrayList<>();
			for (ObjectInfo object_ : objects) {
				IMenuManager manager = getDesignerMenuManager();
				object.getBroadcastObject().addContextMenu(objects, object_, manager);
				managers.add(manager);
			}
			// select common parts
			MenuIntersector.merge(resultMenuManager, managers);
		}
		// select "Surround with" sub-menu
		return findChildMenuManager(resultMenuManager, "Surround with");
	}

	/**
	 * @return the surround {@link IAction} with given title.
	 */
	private static IAction getSurroundAction(String actionText, ObjectInfo... objects)
			throws Exception {
		assertFalse(objects.length == 0);
		IMenuManager surroundManager = createSurroundManager(objects[0], List.of(objects));
		assertNotNull(surroundManager);
		return findChildAction(surroundManager, actionText);
	}

	/**
	 * Asserts that there are no "Surround with" {@link IMenuManager} for given input.
	 */
	public static void assertNoSurroundManager(ObjectInfo object, List<? extends ObjectInfo> objects)
			throws Exception {
		IMenuManager surroundManager = createSurroundManager(object, objects);
		assertNull(surroundManager);
	}

	/**
	 * Asserts that there are no "Surround with" {@link IAction} for given input.
	 */
	private static void assertNoSurroundAction(String actionText, ObjectInfo... objects)
			throws Exception {
		IAction surroundAction = getSurroundAction(actionText, objects);
		assertNull(surroundAction);
	}

	/**
	 * Runs action from "Surround with" {@link IMenuManager}.
	 */
	public static void runSurround(String actionText, ObjectInfo... objects) throws Exception {
		IAction surroundAction = getSurroundAction(actionText, objects);
		assertNotNull(surroundAction);
		// run action
		surroundAction.run();
	}

	/**
	 * Runs action from "Surround with" {@link IMenuManager} for {@link JPanel}.
	 */
	public static void runSurround_JPanel(ObjectInfo... objects) throws Exception {
		runSurround("javax.swing.JPanel", objects);
	}
}
