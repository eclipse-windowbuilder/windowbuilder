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
package org.eclipse.wb.tests.designer.core.model.operations;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.generation.statement.block.BlockStatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.variable.description.LocalUniqueVariableDescription;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;
import org.eclipse.wb.tests.designer.swing.SwingTestUtils;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import javax.swing.JButton;

/**
 * Test for adding.
 *
 * @author scheglov_ke
 */
public class AddTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test that we don't leave block with <b>local</b> panel, because in other case it will become
	 * invisible.
	 */
	@Test
	public void test_localInnerPanel() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public final class Test extends JPanel {",
						"  public Test() {",
						"    {",
						"      JPanel innerPanel = new JPanel();",
						"      add(innerPanel);",
						"    }",
						"  }",
						"}");
		// prepare innerPanel
		assertEquals(1, panel.getChildrenComponents().size());
		ContainerInfo innerPanel = (ContainerInfo) panel.getChildrenComponents().get(0);
		// prepare FlowLayout
		assertEquals(0, innerPanel.getChildrenComponents().size());
		FlowLayoutInfo flowLayout = (FlowLayoutInfo) innerPanel.getLayout();
		// prepare new JButton
		ComponentInfo newButton;
		{
			ConstructorCreationSupport creationSupport = new ConstructorCreationSupport();
			newButton =
					(ComponentInfo) JavaInfoUtils.createJavaInfo(m_lastEditor, JButton.class, creationSupport);
		}
		// add JButton
		SwingTestUtils.setGenerations(
				LocalUniqueVariableDescription.INSTANCE,
				BlockStatementGeneratorDescription.INSTANCE);
		flowLayout.add(newButton, null);
		// check
		assertEquals(1, innerPanel.getChildrenComponents().size());
		assertEditor(
				"public final class Test extends JPanel {",
				"  public Test() {",
				"    {",
				"      JPanel innerPanel = new JPanel();",
				"      add(innerPanel);",
				"      {",
				"        JButton button = new JButton('New button');",
				"        innerPanel.add(button);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Test that we don't leave block with <b>field</b> panel, even if field mean that panel will stay
	 * visible.
	 */
	@Test
	public void test_fieldInnerPanel() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public final class Test extends JPanel {",
						"  private JPanel innerPanel;",
						"  public Test() {",
						"    {",
						"      innerPanel = new JPanel();",
						"      add(innerPanel);",
						"    }",
						"  }",
						"}");
		// prepare innerPanel
		assertEquals(1, panel.getChildrenComponents().size());
		ContainerInfo innerPanel = (ContainerInfo) panel.getChildrenComponents().get(0);
		// prepare FlowLayout
		assertEquals(0, innerPanel.getChildrenComponents().size());
		FlowLayoutInfo flowLayout = (FlowLayoutInfo) innerPanel.getLayout();
		// prepare new JButton
		ComponentInfo newButton;
		{
			ConstructorCreationSupport creationSupport = new ConstructorCreationSupport();
			newButton =
					(ComponentInfo) JavaInfoUtils.createJavaInfo(m_lastEditor, JButton.class, creationSupport);
		}
		// add JButton
		SwingTestUtils.setGenerations(
				LocalUniqueVariableDescription.INSTANCE,
				BlockStatementGeneratorDescription.INSTANCE);
		flowLayout.add(newButton, null);
		// check
		assertEquals(1, innerPanel.getChildrenComponents().size());
		assertEditor(
				"public final class Test extends JPanel {",
				"  private JPanel innerPanel;",
				"  public Test() {",
				"    {",
				"      innerPanel = new JPanel();",
				"      add(innerPanel);",
				"      {",
				"        JButton button = new JButton('New button');",
				"        innerPanel.add(button);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Test for {@link JavaEventListener#associationTemplate(JavaInfo, String[])}.
	 */
	@Test
	public void test_associationTemplateListener() throws Exception {
		setFileContentSrc(
				"test/MyButton.java",
				getTestSource(
						"public class MyButton extends JButton {",
						"  public MyButton(int value) {",
						"  }",
						"}"));
		setFileContentSrc(
				"test/MyButton.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <creation>",
						"    <source><![CDATA[new test.MyButton(%theValue%)]]></source>",
						"  </creation>",
						"</component>"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"public final class Test extends JPanel {",
						"  private JPanel innerPanel;",
						"  public Test() {",
						"  }",
						"}");
		FlowLayoutInfo flowLayout = (FlowLayoutInfo) panel.getLayout();
		// add new MyButton
		ComponentInfo newButton = createJavaInfo("test.MyButton");
		newButton.addBroadcastListener(new JavaEventListener() {
			@Override
			public void associationTemplate(JavaInfo component, String[] source) throws Exception {
				assertNotNull(component.getParent());
				source[0] = StringUtils.replace(source[0], "%theValue%", "555");
			}
		});
		flowLayout.add(newButton, null);
		assertEditor(
				"public final class Test extends JPanel {",
				"  private JPanel innerPanel;",
				"  public Test() {",
				"    {",
				"      MyButton myButton = new MyButton(555);",
				"      add(myButton);",
				"    }",
				"  }",
				"}");
	}
}
