/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.core.model;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.InvocationChildArrayAssociation;
import org.eclipse.wb.internal.core.model.nonvisual.ArrayObjectInfo;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.LocalVariableSupport;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.MethodInvocation;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;

import javax.swing.JButton;

/**
 * Test {@link ArrayObjectInfo} and {@link InvocationChildArrayAssociation}.
 *
 * @author sablin_aa
 */
public class ArrayObjectTest extends SwingModelTest {
	private ContainerInfo myPanel;
	private ArrayObjectInfo arrayInfo;
	private ComponentInfo localButton;
	private ComponentInfo emptyButton;

	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@After
	public void tearDown() throws Exception {
		// clear fields
		for (Field field : getClass().getDeclaredFields()) {
			if (field.getName().indexOf('$') == -1 && Object.class.isAssignableFrom(field.getType())) {
				field.setAccessible(true);
				field.set(this, null);
			}
		}
		super.tearDown();
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
	public void test_parse() throws Exception {
		getParsedContainer();
		// ArrayObjectInfo
		{
			assertSame(myPanel, arrayInfo.getParent());
			Assertions.assertThat(arrayInfo.getChildren()).isEmpty();
			// check tuning
			assertTrue(arrayInfo.isRemoveOnEmpty());
			assertTrue(!arrayInfo.isHideInTree());
			// check item type
			assertEquals(arrayInfo.getItemClass(), JButton.class);
		}
		// button with {@link LocalVariableSupport}
		{
			checkArrayItem(localButton);
			assertInstanceOf(LocalVariableSupport.class, localButton.getVariableSupport());
		}
		// button with {@link EmptyVariableSupport}
		{
			checkArrayItem(emptyButton);
			assertInstanceOf(EmptyVariableSupport.class, emptyButton.getVariableSupport());
		}
	}

	@Test
	public void test_Presentation() throws Exception {
		getParsedContainer();
		IObjectPresentation presentation = arrayInfo.getPresentation();
		// icon/text
		assertNotNull(presentation.getIcon());
		assertEquals("addButtons(array)", presentation.getText());
		// tree children
		{
			List<ObjectInfo> childrenTree = presentation.getChildrenTree();
			Assertions.assertThat(childrenTree).hasSize(2);
			assertEquals(childrenTree, myPanel.getChildrenComponents());
			Assertions.assertThat(childrenTree).containsOnly(localButton, emptyButton);
		}
		// graphical children
		{
			List<ObjectInfo> childrenGraphical = presentation.getChildrenGraphical();
			Assertions.assertThat(childrenGraphical).isEmpty();
		}
		// when we ask tree children for MyPanel, ArrayObjectInof does not allow to see buttons
		Assertions.assertThat(myPanel.getPresentation().getChildrenTree()).containsOnly(arrayInfo);
	}

	@Test
	public void test_deleteItem_withLocal() throws Exception {
		getParsedContainer();
		// initially "localButton" is in "array"
		Assertions.assertThat(arrayInfo.getItems()).hasSize(2).contains(localButton);
		// do delete
		assertTrue(localButton.canDelete());
		localButton.delete();
		// check source
		assertEditor(
				"// filler filler filler",
				"public class Test extends MyPanel {",
				"  public Test() {",
				"    addButtons( new JButton[] { new JButton() } );",
				"  }",
				"}");
		// no "localButton" in "array"
		Assertions.assertThat(arrayInfo.getItems()).containsOnly(emptyButton);
		Assertions.assertThat(myPanel.getChildren()).doesNotContain(localButton);
	}

	@Test
	public void test_deleteItem_withEmpty() throws Exception {
		getParsedContainer();
		// initially "emptyButton" is in "array"
		Assertions.assertThat(arrayInfo.getItems()).hasSize(2).contains(emptyButton);
		// do delete
		assertTrue(emptyButton.canDelete());
		emptyButton.delete();
		// check source
		assertEditor(
				"// filler filler filler",
				"public class Test extends MyPanel {",
				"  public Test() {",
				"    JButton button = new JButton();",
				"    addButtons( new JButton[] { button } );",
				"  }",
				"}");
		// no "emptyButton" in "array"
		Assertions.assertThat(arrayInfo.getItems()).containsOnly(localButton);
		Assertions.assertThat(myPanel.getChildren()).doesNotContain(emptyButton);
	}

	@Test
	public void test_deleteAllItems() throws Exception {
		getParsedContainer();
		// delete all buttons
		localButton.delete();
		emptyButton.delete();
		// check source
		assertEditor(
				"// filler filler filler",
				"public class Test extends MyPanel {",
				"  public Test() {",
				"  }",
				"}");
		Assertions.assertThat(myPanel.getChildren()).doesNotContain(arrayInfo, localButton, emptyButton);
	}

	@Test
	public void test_deleteArrayObjectInfo() throws Exception {
		getParsedContainer();
		// do delete
		assertTrue(arrayInfo.canDelete());
		arrayInfo.delete();
		// check source
		assertEditor(
				"// filler filler filler",
				"public class Test extends MyPanel {",
				"  public Test() {",
				"  }",
				"}");
		Assertions.assertThat(myPanel.getChildren()).doesNotContain(arrayInfo, localButton, emptyButton);
	}

	@Test
	public void test_addItem() throws Exception {
		getParsedContainer();
		// check exists
		assertNotNull(localButton);
		assertNotNull(emptyButton);
		// add new button
		JavaInfo newButton = createJavaInfo("javax.swing.JButton");
		arrayInfo.command_CREATE(newButton, null);
		// check source
		assertEditor(
				"// filler filler filler",
				"public class Test extends MyPanel {",
				"  public Test() {",
				"    JButton button = new JButton();",
				"    addButtons( new JButton[] { button, new JButton(), new JButton('New button') } );",
				"  }",
				"}");
		Assertions.assertThat(myPanel.getChildren(ArrayObjectInfo.class)).containsOnly(arrayInfo);
		Assertions.<JavaInfo>assertThat(myPanel.getChildrenComponents()).containsOnly(localButton, emptyButton, newButton);
		// check objects
		myPanel.refresh();
		assertNotNull(localButton.getObject());
		assertNotNull(emptyButton.getObject());
		assertNotNull(newButton.getObject());
	}

	@Test
	public void test_moveItemInside() throws Exception {
		getParsedContainer();
		// check exists
		assertEquals(arrayInfo.getItems().indexOf(localButton), 0);
		assertEquals(arrayInfo.getItems().indexOf(emptyButton), 1);
		Assertions.assertThat(myPanel.getChildrenComponents().indexOf(localButton)).isLessThan(
				myPanel.getChildrenComponents().indexOf(emptyButton));
		// move button
		arrayInfo.command_MOVE(emptyButton, localButton);
		// check source
		assertEditor(
				"// filler filler filler",
				"public class Test extends MyPanel {",
				"  public Test() {",
				"    JButton button = new JButton();",
				"    addButtons( new JButton[] { new JButton(), button } );",
				"  }",
				"}");
		assertEquals(arrayInfo.getItems().indexOf(localButton), 1);
		assertEquals(arrayInfo.getItems().indexOf(emptyButton), 0);
		Assertions.assertThat(myPanel.getChildrenComponents().indexOf(localButton)).isGreaterThan(
				myPanel.getChildrenComponents().indexOf(emptyButton));
		// check objects
		myPanel.refresh();
		assertNotNull(localButton.getObject());
		assertNotNull(emptyButton.getObject());
	}

	@Test
	public void test_moveItemBetween() throws Exception {
		getParsedContainer();
		// check exists
		assertEquals(arrayInfo.getItems().indexOf(localButton), 0);
		assertEquals(arrayInfo.getItems().indexOf(emptyButton), 1);
		Assertions.assertThat(myPanel.getChildrenComponents().indexOf(localButton)).isLessThan(
				myPanel.getChildrenComponents().indexOf(emptyButton));
		// create new invocation
		MethodInvocation newMethodInvocation =
				myPanel.addMethodInvocation(
						"addButtons(javax.swing.JButton[])",
						"new javax.swing.JButton[]{}");
		// create new array info;
		Class<?> itemType =
				ReflectionUtils.getClassByName(
						EditorState.get(m_lastEditor).getEditorLoader(),
						"javax.swing.JButton");
		ArrayObjectInfo newArrayInfo =
				new ArrayObjectInfo(m_lastEditor,
						"addButtonsNew",
						itemType,
						(ArrayCreation) newMethodInvocation.arguments().get(0));
		myPanel.addChild(newArrayInfo);
		// check source
		assertEditor(
				"// filler filler filler",
				"public class Test extends MyPanel {",
				"  public Test() {",
				"    addButtons(new JButton[]{});",
				"    JButton button = new JButton();",
				"    addButtons( new JButton[] { button, new JButton() } );",
				"  }",
				"}");
		{
			// move local button
			newArrayInfo.command_MOVE(localButton, null);
			// check source
			assertEditor(
					"// filler filler filler",
					"public class Test extends MyPanel {",
					"  public Test() {",
					"    JButton button = new JButton();",
					"    addButtons(new JButton[]{button});",
					"    addButtons( new JButton[] { new JButton() } );",
					"  }",
					"}");
			Assertions.assertThat(arrayInfo.getItems()).containsOnly(emptyButton);
			Assertions.assertThat(newArrayInfo.getItems()).containsOnly(localButton);
			Assertions.assertThat(myPanel.getChildrenComponents()).containsOnly(localButton, emptyButton);
			Assertions.assertThat(myPanel.getPresentation().getChildrenTree()).containsOnly(arrayInfo, newArrayInfo);
			// check objects
			myPanel.refresh();
			assertNotNull(localButton.getObject());
			assertNotNull(emptyButton.getObject());
		}
		{
			// move empty button too
			newArrayInfo.command_MOVE(emptyButton, null);
			// check source
			assertEditor(
					"// filler filler filler",
					"public class Test extends MyPanel {",
					"  public Test() {",
					"    JButton button = new JButton();",
					"    addButtons(new JButton[]{button, new JButton()});",
					"  }",
					"}");
			Assertions.assertThat(myPanel.getChildrenComponents()).containsOnly(localButton, emptyButton);
			Assertions.assertThat(myPanel.getPresentation().getChildrenTree()).containsOnly(newArrayInfo);
			Assertions.assertThat(newArrayInfo.getItems()).containsOnly(localButton, emptyButton);
			// check objects
			myPanel.refresh();
			assertNotNull(localButton.getObject());
			assertNotNull(emptyButton.getObject());
		}
	}

	@Test
	public void test_moveItemOutside() throws Exception {
		getParsedContainer(new String[]{
				"public class Test extends MyPanel {",
				"  public Test() {",
				"    JButton button = new JButton();",
				"    addButtons( new JButton[] { button, new JButton() } );",
				"    {",
				"      JButton button_1 = new JButton();",
				"      button_1.setText('Button');",
				"      add(button_1);",
				"    }",
				"  }",
		"}"});
		// check exists
		assertEquals(arrayInfo.getItems().indexOf(localButton), 0);
		assertEquals(arrayInfo.getItems().indexOf(emptyButton), 1);
		Assertions.assertThat(myPanel.getChildrenComponents().indexOf(localButton)).isLessThan(
				myPanel.getChildrenComponents().indexOf(emptyButton));
		// outside JButton
		Assertions.assertThat(myPanel.getChildrenComponents()).hasSize(3);
		ComponentInfo newButton = myPanel.getChildrenComponents().get(2);
		// move button
		arrayInfo.command_MOVE(newButton, emptyButton);
		{
			// check source
			assertEditor(
					"public class Test extends MyPanel {",
					"  private JButton button_1;",
					"  public Test() {",
					"    JButton button = new JButton();",
					"    {",
					"      button_1 = new JButton();",
					"      button_1.setText('Button');",
					"    }",
					"    addButtons( new JButton[] { button, button_1, new JButton() } );",
					"  }",
					"}");
			Assertions.assertThat(arrayInfo.getItems()).containsOnly(localButton, emptyButton, newButton);
			Assertions.assertThat(myPanel.getChildrenComponents()).containsOnly(localButton, emptyButton, newButton);
			Assertions.assertThat(myPanel.getPresentation().getChildrenTree()).containsOnly(arrayInfo);
			// check objects
			myPanel.refresh();
			assertNotNull(localButton.getObject());
			assertNotNull(emptyButton.getObject());
			assertNotNull(newButton.getObject());
		}
		{
			// check source on delete
			newButton.delete();
			assertEditor(
					"public class Test extends MyPanel {",
					"  public Test() {",
					"    JButton button = new JButton();",
					"    addButtons( new JButton[] { button, new JButton() } );",
					"  }",
					"}");
			// check objects
			myPanel.refresh();
			assertNotNull(localButton.getObject());
			assertNotNull(emptyButton.getObject());
		}
	}

	@Test
	public void test_moveItemOutside_inlined() throws Exception {
		getParsedContainer(new String[]{
				"public class Test extends MyPanel {",
				"  public Test() {",
				"    JButton button = new JButton();",
				"    addButtons( new JButton[] { button, new JButton() } );",
				"    add(new JButton('Button'));",
				"  }",
		"}"});
		// check exists
		assertEquals(arrayInfo.getItems().indexOf(localButton), 0);
		assertEquals(arrayInfo.getItems().indexOf(emptyButton), 1);
		Assertions.assertThat(myPanel.getChildrenComponents().indexOf(localButton)).isLessThan(
				myPanel.getChildrenComponents().indexOf(emptyButton));
		// outside JButton
		Assertions.assertThat(myPanel.getChildrenComponents()).hasSize(3);
		ComponentInfo newButton = myPanel.getChildrenComponents().get(2);
		// move button
		arrayInfo.command_MOVE(newButton, emptyButton);
		{
			// check source
			assertEditor(
					"public class Test extends MyPanel {",
					"  public Test() {",
					"    JButton button = new JButton();",
					"    addButtons( new JButton[] { button, new JButton('Button'), new JButton() } );",
					"  }",
					"}");
			Assertions.assertThat(arrayInfo.getItems()).containsOnly(localButton, emptyButton, newButton);
			Assertions.assertThat(myPanel.getChildrenComponents()).containsOnly(localButton, emptyButton, newButton);
			Assertions.assertThat(myPanel.getPresentation().getChildrenTree()).containsOnly(arrayInfo);
			// check objects
			myPanel.refresh();
			assertNotNull(localButton.getObject());
			assertNotNull(emptyButton.getObject());
			assertNotNull(newButton.getObject());
		}
		{
			// check source on delete
			newButton.delete();
			assertEditor(
					"public class Test extends MyPanel {",
					"  public Test() {",
					"    JButton button = new JButton();",
					"    addButtons( new JButton[] { button, new JButton() } );",
					"  }",
					"}");
			// check objects
			myPanel.refresh();
			assertNotNull(localButton.getObject());
			assertNotNull(emptyButton.getObject());
		}
	}

	@Test
	public void test_moveItemOutside_inlining() throws Exception {
		getParsedContainer();
		// check exists
		assertEquals(arrayInfo.getItems().indexOf(localButton), 0);
		assertEquals(arrayInfo.getItems().indexOf(emptyButton), 1);
		Assertions.assertThat(myPanel.getChildrenComponents().indexOf(localButton)).isLessThan(
				myPanel.getChildrenComponents().indexOf(emptyButton));
		// add new JButton
		ComponentInfo newButton;
		{
			FlowLayoutInfo flowLayout = (FlowLayoutInfo) myPanel.getLayout();
			newButton = createComponent("javax.swing.JButton");
			flowLayout.add(newButton, null);
		}
		// check source
		assertEditor(
				"// filler filler filler",
				"public class Test extends MyPanel {",
				"  public Test() {",
				"    JButton button = new JButton();",
				"    addButtons( new JButton[] { button, new JButton() } );",
				"    {",
				"      JButton button_1 = new JButton('New button');",
				"      add(button_1);",
				"    }",
				"  }",
				"}");
		Assertions.assertThat(myPanel.getPresentation().getChildrenTree()).containsOnly(arrayInfo, newButton);
		Assertions.assertThat(myPanel.getChildrenComponents()).containsOnly(localButton, emptyButton, newButton);
		// move button
		arrayInfo.command_MOVE(newButton, emptyButton);
		{
			// check source
			assertEditor(
					"// filler filler filler",
					"public class Test extends MyPanel {",
					"  public Test() {",
					"    JButton button = new JButton();",
					"    addButtons( new JButton[] { button, new JButton('New button'), new JButton() } );",
					"  }",
					"}");
			Assertions.assertThat(arrayInfo.getItems()).containsOnly(localButton, emptyButton, newButton);
			Assertions.assertThat(myPanel.getChildrenComponents()).containsOnly(localButton, emptyButton, newButton);
			Assertions.assertThat(myPanel.getPresentation().getChildrenTree()).containsOnly(arrayInfo);
			// check objects
			myPanel.refresh();
			assertNotNull(localButton.getObject());
			assertNotNull(emptyButton.getObject());
			assertNotNull(newButton.getObject());
		}
		{
			// check source on delete
			newButton.delete();
			assertEditor(
					"// filler filler filler",
					"public class Test extends MyPanel {",
					"  public Test() {",
					"    JButton button = new JButton();",
					"    addButtons( new JButton[] { button, new JButton() } );",
					"  }",
					"}");
			// check objects
			myPanel.refresh();
			assertNotNull(localButton.getObject());
			assertNotNull(emptyButton.getObject());
		}
	}

	/* this feature is not allowed :(
	@Test
  public void test_OutsideInitialization() throws Exception {
  	getParsedContainer(new String[]{
  			"public class Test extends MyPanel {",
  			"  public Test() {",
  			"    JButton button = new JButton();",
  			"    JButton[] buttons = new JButton[] { button, new JButton() };",
  			"    addButtons( buttons );",
  			"  }",
  			"}"});
  	// 1 array + 2 buttons
  	Assertions.assertThat(myPanel.getChildren(ArrayObjectInfo.class)).hasSize(1);
  	Assertions.assertThat(myPanel.getChildren(ComponentInfo.class)).hasSize(2);
  	// get models
  	arrayInfo = myPanel.getChildren(ArrayObjectInfo.class).get(0);
  	localButton = myPanel.getChildrenComponents().get(0);
  	emptyButton = myPanel.getChildrenComponents().get(1);
  }*/
	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private void checkArrayItem(ComponentInfo item) {
		Association association = item.getAssociation();
		assertInstanceOf(InvocationChildArrayAssociation.class, association);
		assertSame(item, association.getJavaInfo());
	}

	private void getParsedContainer() throws Exception {
		getParsedContainer(new String[]{
				"// filler filler filler",
				"public class Test extends MyPanel {",
				"  public Test() {",
				"    JButton button = new JButton();",
				"    addButtons( new JButton[] { button, new JButton() } );",
				"  }",
		"}"});
	}

	private void getParsedContainer(String[] sourceLines) throws Exception {
		configureProject2();
		myPanel = parseContainer(sourceLines);
		// 1 array + 2 buttons
		Assertions.assertThat(myPanel.getChildren(ArrayObjectInfo.class)).hasSize(1);
		Assertions.assertThat(myPanel.getChildren(ComponentInfo.class).size()).isGreaterThanOrEqualTo(2);
		// get models
		arrayInfo = myPanel.getChildren(ArrayObjectInfo.class).get(0);
		localButton = myPanel.getChildrenComponents().get(0);
		emptyButton = myPanel.getChildrenComponents().get(1);
	}

	private void configureProject2() throws Exception {
		setFileContentSrc(
				"test/MyPanel.java",
				getTestSource(
						"public class MyPanel extends JPanel {",
						"  public void addButtons(JButton[] buttons) {",
						"  }",
						"}"));
		setFileContentSrc(
				"test/MyPanel.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <methods>",
						"    <method name='addButtons'>",
						"      <parameter type='javax.swing.JButton[]' child='true'>",
						"        <tag name='arrayObject.removeOnEmpty' value='true'/>",
						"      </parameter>",
						"    </method>",
						"  </methods>",
						"</component>"));
		waitForAutoBuild();
	}
}
