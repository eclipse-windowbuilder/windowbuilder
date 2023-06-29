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
package org.eclipse.wb.tests.designer.core.model.util;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.util.StackContainerSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import java.util.List;

import javax.swing.JTextField;

/**
 * Tests for {@link StackContainerSupport}.
 *
 * @author scheglov_ke
 */
public class StackContainerSupportTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void configureNewProject() throws Exception {
		super.configureNewProject();
		prepareCardPanel();
		forgetCreatedResources();
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
	public void test_noChildren() throws Exception {
		CardPanel_Info panel =
				parseJavaInfo(
						"// filler filler filler filler filler",
						"public class Test extends CardPanel {",
						"  public Test() {",
						"  }",
						"}");
		panel.refresh();
		// initially no "active"
		assertSame(null, panel.m_activeComponent);
	}

	public void test_notifySelecting_null() throws Exception {
		CardPanel_Info panel =
				parseJavaInfo(
						"// filler filler filler filler filler",
						"public class Test extends CardPanel {",
						"  public Test() {",
						"  }",
						"}");
		panel.refresh();
		// fire "null" selecting
		boolean[] refresh = new boolean[]{false};
		panel.getBroadcastObject().selecting(null, refresh);
	}

	/**
	 * Test for {@link StackContainerSupport#setActive(JavaInfo)}.
	 */
	public void test_setActiveInfo() throws Exception {
		CardPanel_Info panel =
				parseJavaInfo(
						"public class Test extends CardPanel {",
						"  public Test() {",
						"    add(new JButton());",
						"    add(new JButton());",
						"  }",
						"}");
		panel.refresh();
		List<ComponentInfo> components = panel.getChildrenComponents();
		ComponentInfo button_0 = components.get(0);
		ComponentInfo button_1 = components.get(1);
		// initially "button_0"
		assertSame(button_0, panel.m_activeComponent);
		// set "button_1"
		{
			panel.m_stackContainer.setActive(button_1);
			assertSame(button_1, panel.m_activeComponent);
		}
		// set "button_0"
		{
			panel.m_stackContainer.setActive(button_0);
			assertSame(button_0, panel.m_activeComponent);
		}
	}

	/**
	 * Test for {@link StackContainerSupport#getNext()}.
	 */
	public void test_getNext() throws Exception {
		CardPanel_Info panel =
				parseJavaInfo(
						"public class Test extends CardPanel {",
						"  public Test() {",
						"    add(new JButton('A'));",
						"    add(new JButton('B'));",
						"    add(new JButton('C'));",
						"  }",
						"}");
		panel.refresh();
		List<ComponentInfo> components = panel.getChildrenComponents();
		ComponentInfo button_0 = components.get(0);
		ComponentInfo button_1 = components.get(1);
		ComponentInfo button_2 = components.get(2);
		// initially "button_0"
		assertSame(button_0, panel.m_activeComponent);
		// next "button_1"
		assertSame(button_1, panel.m_stackContainer.getNext());
		panel.m_stackContainer.setActive(button_1);
		assertSame(button_1, panel.m_activeComponent);
		// next "button_2"
		assertSame(button_2, panel.m_stackContainer.getNext());
		panel.m_stackContainer.setActive(button_2);
		assertSame(button_2, panel.m_activeComponent);
		// next again "button_0"
		assertSame(button_0, panel.m_stackContainer.getNext());
		panel.m_stackContainer.setActive(button_0);
		assertSame(button_0, panel.m_activeComponent);
	}

	/**
	 * Test for {@link StackContainerSupport#getPrev()}.
	 */
	public void test_getPrev() throws Exception {
		CardPanel_Info panel =
				parseJavaInfo(
						"public class Test extends CardPanel {",
						"  public Test() {",
						"    add(new JButton('A'));",
						"    add(new JButton('B'));",
						"    add(new JButton('C'));",
						"  }",
						"}");
		panel.refresh();
		List<ComponentInfo> components = panel.getChildrenComponents();
		ComponentInfo button_0 = components.get(0);
		ComponentInfo button_1 = components.get(1);
		ComponentInfo button_2 = components.get(2);
		// initially "button_0"
		assertSame(button_0, panel.m_activeComponent);
		// prev "button_2"
		assertSame(button_2, panel.m_stackContainer.getPrev());
		panel.m_stackContainer.setActive(button_2);
		assertSame(button_2, panel.m_activeComponent);
		// prev "button_1"
		assertSame(button_1, panel.m_stackContainer.getPrev());
		panel.m_stackContainer.setActive(button_1);
		assertSame(button_1, panel.m_activeComponent);
		// prev "button_0"
		assertSame(button_0, panel.m_stackContainer.getPrev());
		panel.m_stackContainer.setActive(button_0);
		assertSame(button_0, panel.m_activeComponent);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Selecting
	//
	////////////////////////////////////////////////////////////////////////////
	public void test_notifySelecting_directChild() throws Exception {
		CardPanel_Info panel =
				parseJavaInfo(
						"public class Test extends CardPanel {",
						"  public Test() {",
						"    add(new JButton());",
						"    add(new JButton());",
						"  }",
						"}");
		panel.refresh();
		List<ComponentInfo> components = panel.getChildrenComponents();
		ComponentInfo button_0 = components.get(0);
		ComponentInfo button_1 = components.get(1);
		// initially "button_0"
		assertSame(button_0, panel.m_activeComponent);
		// notify about "button_1"
		{
			boolean shouldRefresh = notifySelecting(button_1);
			assertTrue(shouldRefresh);
			panel.refresh();
			// now "button_1" is active
			assertSame(button_1, panel.m_activeComponent);
		}
		// second notification about "button_1" does not cause refresh()
		{
			boolean shouldRefresh = notifySelecting(button_1);
			assertFalse(shouldRefresh);
			assertSame(button_1, panel.m_activeComponent);
		}
	}

	public void test_notifySelecting_indirectChild() throws Exception {
		CardPanel_Info panel =
				parseJavaInfo(
						"public class Test extends CardPanel {",
						"  public Test() {",
						"    add(new JButton());",
						"    {",
						"      JPanel inner = new JPanel();",
						"      add(inner);",
						"      inner.add(new JButton());",
						"    }",
						"  }",
						"}");
		panel.refresh();
		List<ComponentInfo> components = panel.getChildrenComponents();
		ComponentInfo button_0 = components.get(0);
		ContainerInfo inner = (ContainerInfo) components.get(1);
		ComponentInfo innerButton = inner.getChildrenComponents().get(0);
		// initially "button_0"
		assertSame(button_0, panel.m_activeComponent);
		// notify about "innerButton"
		{
			boolean shouldRefresh = notifySelecting(innerButton);
			assertTrue(shouldRefresh);
			panel.refresh();
			// now "inner" is active
			assertSame(inner, panel.m_activeComponent);
		}
		// second notification about "innerButton" does not cause refresh()
		{
			boolean shouldRefresh = notifySelecting(innerButton);
			assertFalse(shouldRefresh);
			assertSame(inner, panel.m_activeComponent);
		}
	}

	/**
	 * Not all children are "stack children", some of them may be not managed by "stack".
	 */
	public void test_notifySelecting_notStackChild() throws Exception {
		CardPanel_Info panel =
				parseJavaInfo(
						"public class Test extends CardPanel {",
						"  public Test() {",
						"    add(new JTextField('not stack child'));",
						"    add(new JButton('1'));",
						"    add(new JButton('2'));",
						"  }",
						"}");
		panel.refresh();
		List<ComponentInfo> components = panel.getChildrenComponents();
		ComponentInfo textField = components.get(0);
		ComponentInfo button_1 = components.get(1);
		ComponentInfo button_2 = components.get(2);
		// initially "button_1"
		assertSame(button_1, panel.m_activeComponent);
		// notify about "button_2"
		{
			boolean shouldRefresh = notifySelecting(button_2);
			assertTrue(shouldRefresh);
			panel.refresh();
			// now "button_2" is active
			assertSame(button_2, panel.m_activeComponent);
		}
		// notify about "textField", ignored, it is not "stack child"
		{
			boolean shouldRefresh = notifySelecting(textField);
			assertFalse(shouldRefresh);
			assertSame(button_2, panel.m_activeComponent);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Delete
	//
	////////////////////////////////////////////////////////////////////////////
	public void test_deleteNotActive() throws Exception {
		CardPanel_Info panel =
				parseJavaInfo(
						"public class Test extends CardPanel {",
						"  public Test() {",
						"    add(new JButton());",
						"    add(new JButton());",
						"    add(new JButton());",
						"  }",
						"}");
		panel.refresh();
		List<ComponentInfo> components = panel.getChildrenComponents();
		ComponentInfo button_1 = components.get(1);
		ComponentInfo button_2 = components.get(2);
		// notify about "button_1"
		{
			notifySelecting(button_1);
			panel.refresh();
			assertSame(button_1, panel.m_activeComponent);
		}
		// delete "button_2", but "button_1" still active
		button_2.delete();
		assertSame(button_1, panel.m_activeComponent);
	}

	public void test_deleteActive_selectFirst() throws Exception {
		CardPanel_Info panel =
				parseJavaInfo(
						"public class Test extends CardPanel {",
						"  public Test() {",
						"    add(new JButton());",
						"    add(new JButton());",
						"    add(new JButton());",
						"  }",
						"}");
		panel.refresh();
		List<ComponentInfo> components = panel.getChildrenComponents();
		ComponentInfo button_0 = components.get(0);
		ComponentInfo button_1 = components.get(1);
		// notify about "button_1"
		{
			notifySelecting(button_1);
			panel.refresh();
			assertSame(button_1, panel.m_activeComponent);
		}
		// delete "button_1", so "button_0" active
		button_1.delete();
		assertSame(button_0, panel.m_activeComponent);
	}

	public void test_deleteActive_noOther() throws Exception {
		CardPanel_Info panel =
				parseJavaInfo(
						"// filler filler filler filler filler",
						"public class Test extends CardPanel {",
						"  public Test() {",
						"    add(new JButton());",
						"  }",
						"}");
		panel.refresh();
		List<ComponentInfo> components = panel.getChildrenComponents();
		ComponentInfo button_0 = components.get(0);
		// notify about "button_0"
		{
			notifySelecting(button_0);
			panel.refresh();
			assertSame(button_0, panel.m_activeComponent);
		}
		// delete "button_0", no other, so no active
		button_0.delete();
		assertSame(null, panel.m_activeComponent);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Add/move
	//
	////////////////////////////////////////////////////////////////////////////
	public void test_addChild_andActivate() throws Exception {
		CardPanel_Info panel =
				parseJavaInfo(
						"// filler filler filler filler filler",
						"public class Test extends CardPanel {",
						"  public Test() {",
						"  }",
						"}");
		panel.refresh();
		// initially no "active"
		assertSame(null, panel.m_activeComponent);
		// add new JButton
		ComponentInfo button = createJButton();
		((FlowLayoutInfo) panel.getLayout()).add(button, null);
		panel.refresh();
		// ...it should become active
		assertSame(button, panel.m_activeComponent);
	}

	public void test_moveChild_inner() throws Exception {
		CardPanel_Info panel =
				parseJavaInfo(
						"public class Test extends CardPanel {",
						"  public Test() {",
						"    {",
						"      JButton button_0 = new JButton('0');",
						"      add(button_0);",
						"    }",
						"    add(new JButton('1'));",
						"    add(new JButton('2'));",
						"  }",
						"}");
		panel.refresh();
		List<ComponentInfo> components = panel.getChildrenComponents();
		ComponentInfo button_0 = components.get(0);
		// initially "button_0"
		assertSame(button_0, panel.m_activeComponent);
		// move "button_0" at the end
		((FlowLayoutInfo) panel.getLayout()).move(button_0, null);
		panel.refresh();
		// "button_0" still active
		assertSame(button_0, panel.m_activeComponent);
		assertEditor(
				"public class Test extends CardPanel {",
				"  public Test() {",
				"    add(new JButton('1'));",
				"    add(new JButton('2'));",
				"    {",
				"      JButton button_0 = new JButton('0');",
				"      add(button_0);",
				"    }",
				"  }",
				"}");
	}

	public void test_moveChild_fromOuter() throws Exception {
		ContainerInfo panel =
				parseJavaInfo(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    {",
						"      JButton button_0 = new JButton('0');",
						"      add(button_0);",
						"    }",
						"    {",
						"      CardPanel cardPanel = new CardPanel();",
						"      add(cardPanel);",
						"      cardPanel.add(new JButton('1'));",
						"      cardPanel.add(new JButton('2'));",
						"    }",
						"  }",
						"}");
		panel.refresh();
		List<ComponentInfo> components = panel.getChildrenComponents();
		ComponentInfo button_0 = components.get(0);
		CardPanel_Info cardPanel = (CardPanel_Info) components.get(1);
		ComponentInfo button_1 = cardPanel.getChildrenComponents().get(0);
		// initially "button_1"
		assertSame(button_1, cardPanel.m_activeComponent);
		// move "button_0" at the end
		((FlowLayoutInfo) cardPanel.getLayout()).move(button_0, null);
		panel.refresh();
		// "button_0" should become active
		assertSame(button_0, cardPanel.m_activeComponent);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    {",
				"      CardPanel cardPanel = new CardPanel();",
				"      add(cardPanel);",
				"      cardPanel.add(new JButton('1'));",
				"      cardPanel.add(new JButton('2'));",
				"      {",
				"        JButton button_0 = new JButton('0');",
				"        cardPanel.add(button_0);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	public void test_moveChild_toOuter() throws Exception {
		ContainerInfo panel =
				parseJavaInfo(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    {",
						"      CardPanel cardPanel = new CardPanel();",
						"      add(cardPanel);",
						"      {",
						"        JButton button_0 = new JButton('0');",
						"        cardPanel.add(button_0);",
						"      }",
						"      cardPanel.add(new JButton('1'));",
						"      cardPanel.add(new JButton('2'));",
						"    }",
						"  }",
						"}");
		panel.refresh();
		List<ComponentInfo> components = panel.getChildrenComponents();
		CardPanel_Info cardPanel = (CardPanel_Info) components.get(0);
		ComponentInfo button_0 = cardPanel.getChildrenComponents().get(0);
		ComponentInfo button_1 = cardPanel.getChildrenComponents().get(1);
		// select "button_0"
		notifySelecting(button_0);
		panel.refresh();
		assertSame(button_0, cardPanel.m_activeComponent);
		// move "button_0" before "cardPanel"
		((FlowLayoutInfo) panel.getLayout()).move(button_0, cardPanel);
		panel.refresh();
		// "button_1" should become active
		assertSame(button_1, cardPanel.m_activeComponent);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    {",
				"      JButton button_0 = new JButton('0');",
				"      add(button_0);",
				"    }",
				"    {",
				"      CardPanel cardPanel = new CardPanel();",
				"      add(cardPanel);",
				"      cardPanel.add(new JButton('1'));",
				"      cardPanel.add(new JButton('2'));",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Model
	//
	////////////////////////////////////////////////////////////////////////////
	private void prepareCardPanel() throws Exception {
		setJavaContentSrc("test", "CardPanel", new String[]{
				"public class CardPanel extends JPanel {",
				"  public CardPanel() {",
				"  }",
		"}"}, new String[]{
				"<?xml version='1.0' encoding='UTF-8'?>",
				"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
				"  <model class='" + CardPanel_Info.class.getName() + "'/>",
		"</component>"});
	}

	public static class CardPanel_Info extends ContainerInfo {
		ComponentInfo m_activeComponent;
		private final StackContainerSupport<ComponentInfo> m_stackContainer =
				new StackContainerSupport<ComponentInfo>(this) {
			@Override
			protected List<ComponentInfo> getChildren() {
				List<ComponentInfo> stackComponents = Lists.newArrayList();
				for (ComponentInfo component : getChildrenComponents()) {
					if (component.getDescription().getComponentClass() != JTextField.class) {
						stackComponents.add(component);
					}
				}
				return stackComponents;
			}
		};

		public CardPanel_Info(AstEditor editor,
				ComponentDescription description,
				CreationSupport creationSupport) throws Exception {
			super(editor, description, creationSupport);
		}

		@Override
		protected void refresh_afterCreate() throws Exception {
			super.refresh_afterCreate();
			m_activeComponent = m_stackContainer.getActive();
		}
	}
}
