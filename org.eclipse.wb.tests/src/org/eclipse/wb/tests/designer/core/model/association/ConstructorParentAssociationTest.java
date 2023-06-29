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
package org.eclipse.wb.tests.designer.core.model.association;

import org.eclipse.wb.core.model.association.CompoundAssociation;
import org.eclipse.wb.core.model.association.ConstructorParentAssociation;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

/**
 * Tests for {@link ConstructorParentAssociation}.
 *
 * @author scheglov_ke
 */
public class ConstructorParentAssociationTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	public void test_parse() throws Exception {
		configureProject();
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    new MyButton(this);",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// check association
		ConstructorParentAssociation association =
				(ConstructorParentAssociation) button.getAssociation();
		assertSame(button, association.getJavaInfo());
		assertEquals("new MyButton(this)", association.getSource());
		assertEquals("new MyButton(this)", m_lastEditor.getSource(association.getCreation()));
		assertEquals("new MyButton(this);", m_lastEditor.getSource(association.getStatement()));
	}

	public void test_delete() throws Exception {
		configureProject();
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"    new MyButton(this);",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// do delete
		assertTrue(button.canDelete());
		button.delete();
		// check source
		assertEditor(
				"// filler filler filler",
				"public class Test extends JPanel {",
				"  public Test() {",
				"  }",
				"}");
	}

	public void test_morph() throws Exception {
		configureProject();
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    new MyButton(this);",
						"  }",
						"}");
		// prepare "old" button
		ComponentInfo oldButton = panel.getChildrenComponents().get(0);
		ConstructorParentAssociation oldAssociation =
				(ConstructorParentAssociation) oldButton.getAssociation();
		// test correct type
		{
			assertInstanceOf(ConstructorParentAssociation.class, oldAssociation.getCopy());
		}
	}

	public void test_add() throws Exception {
		configureProject();
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		FlowLayoutInfo flowLayout = (FlowLayoutInfo) panel.getLayout();
		// add new MyButton
		ComponentInfo button =
				(ComponentInfo) JavaInfoUtils.createJavaInfo(
						m_lastEditor,
						m_lastLoader.loadClass("test.MyButton"),
						new ConstructorCreationSupport());
		flowLayout.add(button, null);
		// check source
		assertEditor(
				"// filler filler filler",
				"public class Test extends JPanel {",
				"  public Test() {",
				"    {",
				"      MyButton myButton = new MyButton(this);",
				"    }",
				"  }",
				"}");
		// check association
		ConstructorParentAssociation association =
				(ConstructorParentAssociation) button.getAssociation();
		assertSame(button, association.getJavaInfo());
		assertEquals("new MyButton(this)", association.getSource());
	}

	public void test_moveInner() throws Exception {
		configureProject();
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    {",
						"      MyButton button = new MyButton(this);",
						"    }",
						"    {",
						"      JPanel container = new JPanel();",
						"      add(container);",
						"    }",
						"  }",
						"}");
		FlowLayoutInfo panelLayout = (FlowLayoutInfo) panel.getLayout();
		// move "button"
		ComponentInfo button = panel.getChildrenComponents().get(0);
		panelLayout.move(button, null);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    {",
				"      JPanel container = new JPanel();",
				"      add(container);",
				"    }",
				"    {",
				"      MyButton button = new MyButton(this);",
				"    }",
				"  }",
				"}");
		// check association
		ConstructorParentAssociation association =
				(ConstructorParentAssociation) button.getAssociation();
		assertSame(button, association.getJavaInfo());
		assertEquals("new MyButton(this)", association.getSource());
	}

	public void test_moveReparent() throws Exception {
		configureProject();
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    {",
						"      MyButton button = new MyButton(this);",
						"    }",
						"    {",
						"      JPanel container = new JPanel();",
						"      add(container);",
						"    }",
						"  }",
						"}");
		// prepare  "container"
		ContainerInfo container = (ContainerInfo) panel.getChildrenComponents().get(1);
		FlowLayoutInfo containerLayout = (FlowLayoutInfo) container.getLayout();
		// move "button"
		ComponentInfo button = panel.getChildrenComponents().get(0);
		containerLayout.move(button, null);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    {",
				"      JPanel container = new JPanel();",
				"      add(container);",
				"      {",
				"        MyButton button = new MyButton(container);",
				"        container.add(button);",
				"      }",
				"    }",
				"  }",
				"}");
		// check association
		CompoundAssociation compoundAssociation = (CompoundAssociation) button.getAssociation();
		ConstructorParentAssociation association =
				(ConstructorParentAssociation) compoundAssociation.getAssociations().get(0);
		assertSame(button, association.getJavaInfo());
		assertEquals("new MyButton(container)", association.getSource());
	}

	/**
	 * Test for reparenting that causes using different constructor.
	 */
	public void test_moveReparent_differentConstructor() throws Exception {
		setFileContentSrc(
				"test/MyButton.java",
				getTestSource(
						"public class MyButton extends JButton {",
						"  public MyButton(JButton button) {",
						"  }",
						"  public MyButton(JPanel panel) {",
						"  }",
						"}"));
		setFileContentSrc(
				"test/MyButton.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <!-- CREATION -->",
						"  <creation>",
						"    <source><![CDATA[new test.MyButton(%parent%)]]></source>",
						"  </creation>",
						"  <!-- CONSTRUCTORS -->",
						"  <constructors>",
						"    <constructor>",
						"      <parameter type='javax.swing.JButton' parent='true'/>",
						"    </constructor>",
						"    <constructor>",
						"      <parameter type='javax.swing.JPanel' parent='true'/>",
						"    </constructor>",
						"  </constructors>",
						"</component>"));
		waitForAutoBuild();
		//
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    {",
						"      JButton button = new JButton();",
						"      add(button);",
						"      {",
						"        MyButton myButton = new MyButton(button);",
						"      }",
						"    }",
						"    {",
						"      JPanel innerPanel = new JPanel();",
						"      add(innerPanel);",
						"    }",
						"  }",
						"}");
		// prepare  components
		ContainerInfo button = (ContainerInfo) panel.getChildrenComponents().get(0);
		ComponentInfo myButton = button.getChildrenComponents().get(0);
		ContainerInfo innerPanel = (ContainerInfo) panel.getChildrenComponents().get(1);
		// initially "myButton" uses constructor with JButton
		{
			ConstructorParentAssociation association =
					(ConstructorParentAssociation) myButton.getAssociation();
			assertEquals(
					"<init>(javax.swing.JButton)",
					AstNodeUtils.getCreationSignature(association.getCreation()));
		}
		// move "myButton"
		FlowLayoutInfo containerLayout = (FlowLayoutInfo) innerPanel.getLayout();
		containerLayout.move(myButton, null);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    {",
				"      JButton button = new JButton();",
				"      add(button);",
				"    }",
				"    {",
				"      JPanel innerPanel = new JPanel();",
				"      add(innerPanel);",
				"      {",
				"        MyButton myButton = new MyButton(innerPanel);",
				"        innerPanel.add(myButton);",
				"      }",
				"    }",
				"  }",
				"}");
		// check association
		CompoundAssociation compoundAssociation = (CompoundAssociation) myButton.getAssociation();
		ConstructorParentAssociation association =
				(ConstructorParentAssociation) compoundAssociation.getAssociations().get(0);
		assertSame(myButton, association.getJavaInfo());
		assertEquals("new MyButton(innerPanel)", association.getSource());
		// binding for constructor should reflect fact that we moved from JButton to JPanel
		assertEquals(
				"<init>(javax.swing.JPanel)",
				AstNodeUtils.getCreationSignature(association.getCreation()));
	}

	private void configureProject() throws Exception {
		setFileContentSrc(
				"test/MyButton.java",
				getTestSource(
						"public class MyButton extends JButton {",
						"  public MyButton(Container container) {",
						"    container.add(this);",
						"  }",
						"}"));
		setFileContentSrc(
				"test/MyButton.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <!-- CREATION -->",
						"  <creation>",
						"    <source><![CDATA[new test.MyButton(%parent%)]]></source>",
						"  </creation>",
						"  <!-- CONSTRUCTORS -->",
						"  <constructors>",
						"    <constructor>",
						"      <parameter type='java.awt.Container' parent='true'/>",
						"    </constructor>",
						"  </constructors>",
						"</component>"));
		waitForAutoBuild();
	}
}
