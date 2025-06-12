/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.tests.designer.core.model.association;

import org.eclipse.wb.core.model.association.CompoundAssociation;
import org.eclipse.wb.core.model.association.ConstructorParentAssociation;
import org.eclipse.wb.core.model.association.FactoryParentAssociation;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.factory.StaticFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.description.factory.FactoryMethodDescription;
import org.eclipse.wb.internal.core.model.description.helpers.FactoryDescriptionHelper;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link FactoryParentAssociation}.
 *
 * @author scheglov_ke
 */
public class FactoryParentAssociationTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_parse() throws Exception {
		configureProject();
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    MyFactory.addButton(this);",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// check association
		FactoryParentAssociation association = (FactoryParentAssociation) button.getAssociation();
		assertSame(button, association.getJavaInfo());
		assertEquals("MyFactory.addButton(this)", association.getSource());
		assertEquals("MyFactory.addButton(this)", m_lastEditor.getSource(association.getInvocation()));
		assertEquals("MyFactory.addButton(this);", m_lastEditor.getSource(association.getStatement()));
		// check variable
		assertInstanceOf(EmptyVariableSupport.class, button.getVariableSupport());
	}

	@Test
	public void test_delete() throws Exception {
		configureProject();
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"    MyFactory.addButton(this);",
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

	/**
	 * Note, that when we morph from "factory", target {@link CreationSupport} is
	 * {@link ConstructorCreationSupport}.
	 */
	@Test
	public void test_morph() throws Exception {
		configureProject();
		setFileContentSrc(
				"test/MyButton.java",
				getTestSource(
						"public class MyButton extends JButton {",
						"  public MyButton(Container container) {",
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
		// parse
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    MyFactory.addButton(this);",
						"  }",
						"}");
		// prepare "old" button
		ComponentInfo oldButton = panel.getChildrenComponents().get(0);
		FactoryParentAssociation oldAssociation = (FactoryParentAssociation) oldButton.getAssociation();
		// test correct type
		{
			assertInstanceOf(ConstructorParentAssociation.class, oldAssociation.getCopy());
		}
	}

	@Test
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
		ComponentInfo button = createNewButton();
		flowLayout.add(button, null);
		// check source
		assertEditor(
				"// filler filler filler",
				"public class Test extends JPanel {",
				"  public Test() {",
				"    {",
				"      JButton button = MyFactory.addButton(this);",
				"    }",
				"  }",
				"}");
		// check association
		FactoryParentAssociation association = (FactoryParentAssociation) button.getAssociation();
		assertSame(button, association.getJavaInfo());
		assertEquals("MyFactory.addButton(this)", association.getSource());
	}

	@Test
	public void test_moveInner() throws Exception {
		configureProject();
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    {",
						"      JButton button = MyFactory.addButton(this);",
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
				"      JButton button = MyFactory.addButton(this);",
				"    }",
				"  }",
				"}");
		// check association
		FactoryParentAssociation association = (FactoryParentAssociation) button.getAssociation();
		assertSame(button, association.getJavaInfo());
		assertEquals("MyFactory.addButton(this)", association.getSource());
	}

	@Test
	public void test_moveReparent() throws Exception {
		configureProject();
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    {",
						"      JButton button = MyFactory.addButton(this);",
						"      add(button);",
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
				"        JButton button = MyFactory.addButton(container);",
				"        container.add(button);",
				"      }",
				"    }",
				"  }",
				"}");
		// check association
		CompoundAssociation compoundAssociation = (CompoundAssociation) button.getAssociation();
		FactoryParentAssociation association =
				(FactoryParentAssociation) compoundAssociation.getAssociations().get(0);
		assertSame(button, association.getJavaInfo());
		assertEquals("MyFactory.addButton(container)", association.getSource());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private void configureProject() throws Exception {
		setFileContentSrc(
				"test/MyFactory.java",
				getTestSource(
						"public class MyFactory {",
						"  public static JButton addButton(Container parent) {",
						"    JButton button = new JButton();",
						"    parent.add(button);",
						"    return button;",
						"  }",
						"}"));
		waitForAutoBuild();
	}

	private ComponentInfo createNewButton() throws Exception, ClassNotFoundException {
		FactoryMethodDescription factoryDescription =
				FactoryDescriptionHelper.getDescription(
						m_lastEditor,
						m_lastLoader.loadClass("test.MyFactory"),
						"addButton(java.awt.Container)",
						true);
		return (ComponentInfo) JavaInfoUtils.createJavaInfo(
				m_lastEditor,
				factoryDescription.getReturnClass(),
				new StaticFactoryCreationSupport(factoryDescription));
	}
}
