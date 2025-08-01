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

import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.AssociationObject;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.core.model.association.CompoundAssociation;
import org.eclipse.wb.core.model.association.ConstructorParentAssociation;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.description.MorphingTargetDescription;
import org.eclipse.wb.internal.core.model.util.MorphingSupport;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.List;

/**
 * Tests for {@link CompoundAssociation}.
 *
 * @author scheglov_ke
 */
public class CompoundAssociationTest extends SwingModelTest {
	@Test
	public void test_morph() throws Exception {
		configureProject();
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    MyPanel myPanel = new MyPanel();",
						"    add(myPanel);",
						"    MyButton myButton = new MyButton(myPanel);",
						"    myPanel.setMyChild(myButton);",
						"  }",
						"}");
		// get components
		ContainerInfo myPanel = (ContainerInfo) panel.getChildrenComponents().get(0);
		ComponentInfo myButton = myPanel.getChildrenComponents().get(0);
		// prepare and morph
		Class<?> clazz = EditorState.get(m_lastEditor).getEditorLoader().loadClass("test.MyButton2");
		MorphingSupport.morph(
				"java.awt.Container",
				myButton,
				new MorphingTargetDescription(clazz, null));
		// reassign
		myButton = myPanel.getChildrenComponents().get(0);
		// test association
		CompoundAssociation compoundAssociation = (CompoundAssociation) myButton.getAssociation();
		assertSame(myButton, compoundAssociation.getJavaInfo());
		assertEquals("new MyButton2(myPanel)", compoundAssociation.getSource());
		assertEquals(
				"MyButton2 myButton = new MyButton2(myPanel);",
				m_lastEditor.getSource(compoundAssociation.getStatement()));
		// check sub-associations
		List<Association> associations = compoundAssociation.getAssociations();
		Assertions.assertThat(associations).hasSize(2);
		{
			ConstructorParentAssociation association = (ConstructorParentAssociation) associations.get(0);
			assertSame(myButton, association.getJavaInfo());
			assertEquals("new MyButton2(myPanel)", association.getSource());
			assertEquals(
					"MyButton2 myButton = new MyButton2(myPanel);",
					m_lastEditor.getSource(association.getStatement()));
		}
		{
			InvocationChildAssociation association = (InvocationChildAssociation) associations.get(1);
			assertSame(myButton, association.getJavaInfo());
			assertEquals("myPanel.setMyChild(myButton)", association.getSource());
			assertEquals(
					"myPanel.setMyChild(myButton);",
					m_lastEditor.getSource(association.getStatement()));
		}
	}

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
						"    MyPanel myPanel = new MyPanel();",
						"    add(myPanel);",
						"    MyButton myButton = new MyButton(myPanel);",
						"    myPanel.setMyChild(myButton);",
						"  }",
						"}");
		ContainerInfo myPanel = (ContainerInfo) panel.getChildrenComponents().get(0);
		ComponentInfo myButton = myPanel.getChildrenComponents().get(0);
		// check compound association
		CompoundAssociation compoundAssociation;
		{
			compoundAssociation = (CompoundAssociation) myButton.getAssociation();
			assertSame(myButton, compoundAssociation.getJavaInfo());
			assertEquals("new MyButton(myPanel)", compoundAssociation.getSource());
			assertEquals(
					"MyButton myButton = new MyButton(myPanel);",
					m_lastEditor.getSource(compoundAssociation.getStatement()));
		}
		// check sub-associations
		List<Association> associations = compoundAssociation.getAssociations();
		assertEquals(2, associations.size());
		{
			ConstructorParentAssociation association = (ConstructorParentAssociation) associations.get(0);
			assertSame(myButton, association.getJavaInfo());
			assertEquals("new MyButton(myPanel)", association.getSource());
			assertEquals(
					"MyButton myButton = new MyButton(myPanel);",
					m_lastEditor.getSource(association.getStatement()));
		}
		{
			InvocationChildAssociation association = (InvocationChildAssociation) associations.get(1);
			assertSame(myButton, association.getJavaInfo());
			assertEquals("myPanel.setMyChild(myButton)", association.getSource());
			assertEquals(
					"myPanel.setMyChild(myButton);",
					m_lastEditor.getSource(association.getStatement()));
		}
	}

	@Test
	public void test_create() throws Exception {
		configureProject();
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    MyPanel myPanel = new MyPanel();",
						"    add(myPanel);",
						"  }",
						"}");
		ContainerInfo myPanel = (ContainerInfo) panel.getChildrenComponents().get(0);
		// add new button
		ComponentInfo myButton = createComponent("test.MyButton");
		AssociationObject associationObject =
				AssociationObjects.invocationChild("%parent%.setMyChild(%child%)", true);
		JavaInfoUtils.add(myButton, associationObject, myPanel, null);
		// check source
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    MyPanel myPanel = new MyPanel();",
				"    add(myPanel);",
				"    {",
				"      MyButton myButton = new MyButton(myPanel);",
				"      myPanel.setMyChild(myButton);",
				"    }",
				"  }",
				"}");
		// check association
		{
			CompoundAssociation compoundAssociation = (CompoundAssociation) myButton.getAssociation();
			// check sub-associations
			List<Association> associations = compoundAssociation.getAssociations();
			Assertions.assertThat(associations).hasSize(2);
			{
				ConstructorParentAssociation association =
						(ConstructorParentAssociation) associations.get(0);
				assertSame(myButton, association.getJavaInfo());
				assertEquals("new MyButton(myPanel)", association.getSource());
				assertEquals(
						"MyButton myButton = new MyButton(myPanel);",
						m_lastEditor.getSource(association.getStatement()));
			}
			{
				InvocationChildAssociation association = (InvocationChildAssociation) associations.get(1);
				assertSame(myButton, association.getJavaInfo());
				assertEquals("myPanel.setMyChild(myButton)", association.getSource());
				assertEquals(
						"myPanel.setMyChild(myButton);",
						m_lastEditor.getSource(association.getStatement()));
			}
		}
	}

	/**
	 * Test that if one of the {@link Association}'s in {@link CompoundAssociation} can not be
	 * deleted, then {@link CompoundAssociation} also can not be deleted.
	 */
	@Test
	public void test_canNotDelete() throws Exception {
		// prepare mocks
		Association association_1 = mock(Association.class);
		Association association_2 = mock(Association.class);
		InOrder inOrder = inOrder(association_1, association_2);
		// record expectations: one association can not be deleted
		when(association_1.canDelete()).thenReturn(true);
		// verify: ...so compound association also can not be deleted
		CompoundAssociation compoundAssociation = new CompoundAssociation(association_1, association_2);
		assertFalse(compoundAssociation.canDelete());
		//
		inOrder.verify(association_1).canDelete();
		inOrder.verify(association_2).canDelete();
		inOrder.verifyNoMoreInteractions();
	}

	/**
	 * Test that if all {@link Association}'s in {@link CompoundAssociation} can be deleted, then
	 * {@link CompoundAssociation} also can be deleted.
	 */
	@Test
	public void test_canDelete() throws Exception {
		// prepare mocks
		Association association_1 = mock(Association.class);
		Association association_2 = mock(Association.class);
		InOrder inOrder = inOrder(association_1, association_2);
		// record expectations: all associations can be deleted
		when(association_1.canDelete()).thenReturn(true);
		when(association_2.canDelete()).thenReturn(true);
		// verify: ...so compound association also can be deleted
		CompoundAssociation compoundAssociation = new CompoundAssociation(association_1, association_2);
		assertTrue(compoundAssociation.canDelete());
		//
		inOrder.verify(association_1).canDelete();
		inOrder.verify(association_2).canDelete();
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void test_delete() throws Exception {
		configureProject();
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    MyPanel myPanel = new MyPanel();",
						"    add(myPanel);",
						"    MyButton myButton = new MyButton(myPanel);",
						"    myPanel.setMyChild(myButton);",
						"  }",
						"}");
		ContainerInfo myPanel = (ContainerInfo) panel.getChildrenComponents().get(0);
		ComponentInfo myButton = myPanel.getChildrenComponents().get(0);
		// do delete
		assertTrue(myButton.canDelete());
		myButton.delete();
		// check source
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    MyPanel myPanel = new MyPanel();",
				"    add(myPanel);",
				"  }",
				"}");
	}

	/**
	 * When all sub-associations are removed during {@link Association#remove()},
	 * {@link CompoundAssociation} also removed.
	 */
	@Test
	public void test_removeAll() throws Exception {
		final ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		Association association_1 = mock(Association.class);
		Association association_2 = mock(Association.class);
		InOrder inOrder = inOrder(association_1, association_2);
		Association compoundAssociation = new CompoundAssociation(association_1, association_2);
		//
		panel.setAssociation(compoundAssociation);
		//
		when(association_1.remove()).thenReturn(true);
		when(association_2.remove()).thenReturn(true);
		//
		assertTrue(compoundAssociation.remove());
		assertNull(panel.getAssociation());
		//
		inOrder.verify(association_1).remove();
		inOrder.verify(association_2).remove();
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void test_moveInner() throws Exception {
		configureProject();
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    MyPanel myPanel = new MyPanel();",
						"    add(myPanel);",
						"    {",
						"      MyButton myButton_1 = new MyButton(myPanel);",
						"      myPanel.setMyChild(myButton_1);",
						"    }",
						"    {",
						"      MyButton myButton_2 = new MyButton(myPanel);",
						"      myPanel.setMyChild(myButton_2);",
						"    }",
						"  }",
						"}");
		ContainerInfo myPanel = (ContainerInfo) panel.getChildrenComponents().get(0);
		ComponentInfo myButton_1 = myPanel.getChildrenComponents().get(0);
		ComponentInfo myButton_2 = myPanel.getChildrenComponents().get(1);
		// do move
		((FlowLayoutInfo) myPanel.getLayout()).move(myButton_2, myButton_1);
		// check source
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    MyPanel myPanel = new MyPanel();",
				"    add(myPanel);",
				"    {",
				"      MyButton myButton_2 = new MyButton(myPanel);",
				"      myPanel.setMyChild(myButton_2);",
				"    }",
				"    {",
				"      MyButton myButton_1 = new MyButton(myPanel);",
				"      myPanel.setMyChild(myButton_1);",
				"    }",
				"  }",
				"}");
		// check new association
		{
			CompoundAssociation compoundAssociation = (CompoundAssociation) myButton_2.getAssociation();
			for (Association association : compoundAssociation.getAssociations()) {
				assertSame(myButton_2, association.getJavaInfo());
			}
		}
	}

	@Test
	public void test_moveReparent() throws Exception {
		configureProject();
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    MyPanel myPanel_1 = new MyPanel();",
						"    add(myPanel_1);",
						"    {",
						"      MyButton myButton = new MyButton(myPanel_1);",
						"      myPanel_1.setMyChild(myButton);",
						"    }",
						"    //",
						"    MyPanel myPanel_2 = new MyPanel();",
						"    add(myPanel_2);",
						"  }",
						"}");
		ContainerInfo myPanel_1 = (ContainerInfo) panel.getChildrenComponents().get(0);
		ContainerInfo myPanel_2 = (ContainerInfo) panel.getChildrenComponents().get(1);
		ComponentInfo myButton = myPanel_1.getChildrenComponents().get(0);
		// do move
		{
			AssociationObject newAssociation =
					AssociationObjects.invocationChild("%parent%.setMyChild(%child%)", true);
			JavaInfoUtils.move(myButton, newAssociation, myPanel_2, null);
		}
		// check source
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    MyPanel myPanel_1 = new MyPanel();",
				"    add(myPanel_1);",
				"    //",
				"    MyPanel myPanel_2 = new MyPanel();",
				"    add(myPanel_2);",
				"    {",
				"      MyButton myButton = new MyButton(myPanel_2);",
				"      myPanel_2.setMyChild(myButton);",
				"    }",
				"  }",
				"}");
		// check new association
		{
			CompoundAssociation compoundAssociation = (CompoundAssociation) myButton.getAssociation();
			List<Association> associations = compoundAssociation.getAssociations();
			assertEquals(2, associations.size());
			{
				ConstructorParentAssociation association =
						(ConstructorParentAssociation) associations.get(0);
				assertSame(myButton, association.getJavaInfo());
				assertEquals("new MyButton(myPanel_2)", association.getSource());
			}
			{
				InvocationChildAssociation association = (InvocationChildAssociation) associations.get(1);
				assertSame(myButton, association.getJavaInfo());
				assertEquals("myPanel_2.setMyChild(myButton)", association.getSource());
			}
		}
	}

	/**
	 * When move component with <em>NOT</em> {@link CompoundAssociation} to container that requires
	 * {@link CompoundAssociation}, we should morph this single {@link Association} into
	 * {@link CompoundAssociation}.
	 */
	@Test
	public void test_moveReparentFromNormalContainer() throws Exception {
		configureProject();
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JPanel panel_1 = new JPanel();",
						"    add(panel_1);",
						"    {",
						"      MyButton myButton = new MyButton(panel_1);",
						"    }",
						"    //",
						"    MyPanel myPanel_2 = new MyPanel();",
						"    add(myPanel_2);",
						"  }",
						"}");
		ContainerInfo myPanel_1 = (ContainerInfo) panel.getChildrenComponents().get(0);
		ContainerInfo myPanel_2 = (ContainerInfo) panel.getChildrenComponents().get(1);
		ComponentInfo myButton = myPanel_1.getChildrenComponents().get(0);
		// do move
		{
			AssociationObject newAssociation =
					AssociationObjects.invocationChild("%parent%.setMyChild(%child%)", true);
			JavaInfoUtils.move(myButton, newAssociation, myPanel_2, null);
		}
		// check source
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    JPanel panel_1 = new JPanel();",
				"    add(panel_1);",
				"    //",
				"    MyPanel myPanel_2 = new MyPanel();",
				"    add(myPanel_2);",
				"    {",
				"      MyButton myButton = new MyButton(myPanel_2);",
				"      myPanel_2.setMyChild(myButton);",
				"    }",
				"  }",
				"}");
		// check new association
		{
			CompoundAssociation compoundAssociation = (CompoundAssociation) myButton.getAssociation();
			List<Association> associations = compoundAssociation.getAssociations();
			assertEquals(2, associations.size());
			{
				ConstructorParentAssociation association =
						(ConstructorParentAssociation) associations.get(0);
				assertSame(myButton, association.getJavaInfo());
				assertEquals("new MyButton(myPanel_2)", association.getSource());
			}
			{
				InvocationChildAssociation association = (InvocationChildAssociation) associations.get(1);
				assertSame(myButton, association.getJavaInfo());
				assertEquals("myPanel_2.setMyChild(myButton)", association.getSource());
			}
		}
	}

	private void configureProject() throws Exception {
		// prepare container
		setFileContentSrc(
				"test/MyPanel.java",
				getTestSource(
						"public class MyPanel extends JPanel {",
						"  public void setMyChild(Component child) {",
						"  }",
						"}"));
		setFileContentSrc(
				"test/MyPanel.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <!-- METHODS -->",
						"  <methods>",
						"    <method name='setMyChild'>",
						"      <parameter type='java.awt.Component' child='true'/>",
						"    </method>",
						"  </methods>",
						"</component>"));
		// prepare child
		setFileContentSrc(
				"test/MyButton.java",
				getTestSource(
						"public class MyButton extends JButton {",
						"  public MyButton(Container parent) {",
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
		setFileContentSrc(
				"test/MyButton2.java",
				getTestSource(
						"public class MyButton2 extends JButton {",
						"  public MyButton2(Container parent) {",
						"  }",
						"}"));
		setFileContentSrc(
				"test/MyButton2.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <!-- CREATION -->",
						"  <creation>",
						"    <source><![CDATA[new test.MyButton2(%parent%)]]></source>",
						"  </creation>",
						"  <!-- CONSTRUCTORS -->",
						"  <constructors>",
						"    <constructor>",
						"      <parameter type='java.awt.Container' parent='true'/>",
						"    </constructor>",
						"  </constructors>",
						"</component>"));
		// build project
		waitForAutoBuild();
	}
}
