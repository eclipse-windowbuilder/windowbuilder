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
package org.eclipse.wb.tests.designer.core.model.variables;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.generation.statement.block.BlockStatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.variable.FieldUniqueVariableSupport;
import org.eclipse.wb.internal.core.model.variable.FieldVariableSupport;
import org.eclipse.wb.internal.core.model.variable.LocalUniqueVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.model.variable.description.FieldUniqueVariableDescription;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.tests.designer.swing.SwingTestUtils;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;

import org.junit.Test;

import java.util.Map;

/**
 * Test for {@link FieldUniqueVariableSupport}.
 *
 * @author scheglov_ke
 */
public class FieldUniqueTest extends AbstractVariableTest {
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
	public void test_object() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  private JButton button_1;",
						"  private JButton button_2;",
						"  public Test() {",
						"    {",
						"      button_1 = new JButton('button 1');",
						"      add(button_1);",
						"    }",
						"    {",
						"      button_2 = new JButton('button 2');",
						"      add(button_2);",
						"    }",
						"  }",
						"}");
		assertEquals(2, panel.getChildrenComponents().size());
		// check child: 0
		{
			JavaInfo button = panel.getChildrenComponents().get(0);
			VariableSupport variableSupport = button.getVariableSupport();
			assertTrue(variableSupport instanceof FieldUniqueVariableSupport);
			assertTrue(variableSupport.hasName());
			assertEquals("field-unique: button_1", variableSupport.toString());
			assertEquals("button_1", variableSupport.getName());
			{
				NodeTarget target = getNodeStatementTarget(panel, false, 0, 1);
				assertEquals("button_1", variableSupport.getReferenceExpression(target));
				assertEquals("button_1.", variableSupport.getAccessExpression(target));
			}
			assertFalse(variableSupport.canConvertLocalToField());
			assertTrue(variableSupport.canConvertFieldToLocal());
			try {
				variableSupport.convertLocalToField();
				fail();
			} catch (IllegalStateException e) {
			}
		}
		// check child: 1
		{
			JavaInfo button = panel.getChildrenComponents().get(1);
			VariableSupport variableSupport = button.getVariableSupport();
			assertTrue(variableSupport instanceof FieldUniqueVariableSupport);
			assertEquals("button_2", variableSupport.getName());
		}
	}

	@Test
	public void test_componentName() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  private JButton m_myButton_Q;",
						"  public Test() {",
						"    {",
						"      m_myButton_Q = new JButton();",
						"      add(m_myButton_Q);",
						"    }",
						"  }",
						"}");
		IJavaProject javaProject = m_lastEditor.getJavaProject();
		// prepare "button"
		JavaInfo button = panel.getChildrenComponents().get(0);
		FieldUniqueVariableSupport variableSupport =
				(FieldUniqueVariableSupport) button.getVariableSupport();
		// check plain/component names
		Map<String, String> options;
		{
			options = ProjectUtils.getOptions(javaProject);
			javaProject.setOption(JavaCore.CODEASSIST_FIELD_PREFIXES, "m_");
			javaProject.setOption(JavaCore.CODEASSIST_FIELD_SUFFIXES, "_Q");
		}
		try {
			assertEquals("m_myButton_Q", variableSupport.getName());
			assertEquals("myButton", variableSupport.getComponentName());
		} finally {
			javaProject.setOptions(options);
		}
	}

	/**
	 * Test for {@link FieldVariableSupport#isValidStatementForChild(Statement)}.
	 */
	@Test
	public void test_isValidStatementForChild() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"class Test {",
						"  private static JPanel panel;",
						"  public static void main(String args[]) {",
						"    {",
						"      panel = new JPanel();",
						"    }",
						"  }",
						"}");
		VariableSupport variableSupport = panel.getVariableSupport();
		Block mainBlock = getMethod("main(java.lang.String[])").getBody();
		// we can reference: panel = new JPanel();
		{
			Statement statement = getStatement(mainBlock, 0, 0);
			assertTrue(variableSupport.isValidStatementForChild(statement));
		}
		// we can not leave Block with "panel" creation
		// (if this is not required by other explicit Statement, but this is not so in current case)
		{
			Statement statement = getStatement(mainBlock, 0);
			assertFalse(variableSupport.isValidStatementForChild(statement));
		}
	}

	/**
	 * Test for case when field referenced from two methods, so can not be converted to local.
	 */
	@Test
	public void test_noLocal() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  private JButton button;",
						"  public Test() {",
						"    button = new JButton();",
						"    add(button);",
						"  }",
						"  void foo() {",
						"    button.setEnabled(false);",
						"  }",
						"}");
		//
		JavaInfo button = panel.getChildrenComponents().get(0);
		VariableSupport variableSupport = button.getVariableSupport();
		assertFalse(variableSupport.canConvertFieldToLocal());
	}

	@Test
	public void test_setName() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  private JButton button;",
						"  public Test() {",
						"    button = new JButton();",
						"    add(button);",
						"  }",
						"}");
		//
		JavaInfo button = panel.getChildrenComponents().get(0);
		VariableSupport variableSupport = button.getVariableSupport();
		assertTrue(variableSupport instanceof FieldUniqueVariableSupport);
		//
		variableSupport.setName("abc");
		assertSame(variableSupport, button.getVariableSupport());
		assertAST(m_lastEditor);
		assertEquals(
				getTestSource(
						"public class Test extends JPanel {",
						"  private JButton abc;",
						"  public Test() {",
						"    abc = new JButton();",
						"    add(abc);",
						"  }",
						"}"),
				m_lastEditor.getSource());
	}

	@Test
	public void test_setName_withApplicationStyleReference() throws Exception {
		parseContainer(
				"public class Test extends JPanel {",
				"  private JButton button;",
				"  public Test() {",
				"    new Thread() {",
				"      public void run() {",
				"        Test test = new Test();",
				"        test.button.setEnabled(true);",
				"      }",
				"    };",
				"    button = new JButton();",
				"    add(button);",
				"  }",
				"}");
		JavaInfo button = getJavaInfoByName("button");
		VariableSupport variableSupport = button.getVariableSupport();
		assertTrue(variableSupport instanceof FieldUniqueVariableSupport);
		//
		variableSupport.setName("abc");
		assertSame(variableSupport, button.getVariableSupport());
		assertEditor(
				getTestSource(
						"public class Test extends JPanel {",
						"  private JButton abc;",
						"  public Test() {",
						"    new Thread() {",
						"      public void run() {",
						"        Test test = new Test();",
						"        test.abc.setEnabled(true);",
						"      }",
						"    };",
						"    abc = new JButton();",
						"    add(abc);",
						"  }",
						"}"),
				m_lastEditor);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// isJavaInfo
	//
	////////////////////////////////////////////////////////////////////////////
	/*public void test_isJavaInfo() throws Exception {
  	ContainerInfo panel =
  			parseTestSource(new String[]{
  					"public class Test extends JPanel {",
  					"  private JButton button;",
  					"  Test() {",
  					"    button = new JButton();",
  					"    add(button);",
  					"  }",
  					"}"});
  	ComponentInfo button = panel.getChildrenComponents().get(0);
  	VariableSupport variableSupport = button.getVariableSupport();
  	// test declaration of "button" variable
  	{
  		FieldDeclaration fieldDeclaration = JavaInfoUtils.getTypeDeclaration(panel).getFields()[0];
  		VariableDeclarationFragment fragment = DomGenerics.fragments(fieldDeclaration).get(0);
  		assertEquals("button", fragment.getName().getIdentifier());
  		assertTrue(variableSupport.isJavaInfo(fragment.getName()));
  	}
  	// invalid node
  	assertFalse(variableSupport.isJavaInfo(null));
  }*/
	////////////////////////////////////////////////////////////////////////////
	//
	// toLocal
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_toLocal_1_simple() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  private JButton button;",
						"  public Test() {",
						"    button = new JButton();",
						"    add(button);",
						"  }",
						"}");
		//
		JavaInfo button = panel.getChildrenComponents().get(0);
		button.getVariableSupport().convertFieldToLocal();
		//
		assertTrue(button.getVariableSupport() instanceof LocalUniqueVariableSupport);
		assertAST(m_lastEditor);
		assertEquals(
				getTestSource(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JButton button = new JButton();",
						"    add(button);",
						"  }",
						"}"),
				m_lastEditor.getSource());
	}

	@Test
	public void test_toLocal_2_withPrefixes() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  private JButton m_button_Q;",
						"  public Test() {",
						"    m_button_Q = new JButton();",
						"    add(m_button_Q);",
						"  }",
						"}");
		IJavaProject javaProject = m_lastEditor.getJavaProject();
		//
		Map<String, String> options;
		{
			options = ProjectUtils.getOptions(javaProject);
			javaProject.setOption(JavaCore.CODEASSIST_FIELD_PREFIXES, "m_");
			javaProject.setOption(JavaCore.CODEASSIST_FIELD_SUFFIXES, "_Q");
		}
		JavaInfo button = panel.getChildrenComponents().get(0);
		// convert
		try {
			button.getVariableSupport().convertFieldToLocal();
			assertTrue(button.getVariableSupport() instanceof LocalUniqueVariableSupport);
		} finally {
			javaProject.setOptions(options);
		}
		//
		assertTrue(button.getVariableSupport() instanceof LocalUniqueVariableSupport);
		assertAST(m_lastEditor);
		assertEquals(
				getTestSource(
						"class Test extends JPanel {",
						"  public Test() {",
						"    JButton button = new JButton();",
						"    add(button);",
						"  }",
						"}"),
				m_lastEditor.getSource());
	}

	@Test
	public void test_converts() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  Test() {",
						"    JButton button = new JButton();",
						"    add(button);",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// local -> field
		{
			assertTrue(button.getVariableSupport().canConvertLocalToField());
			assertFalse(button.getVariableSupport().canConvertFieldToLocal());
			button.getVariableSupport().convertLocalToField();
			assertEquals(
					getTestSource(
							"class Test extends JPanel {",
							"  private JButton button;",
							"  Test() {",
							"    button = new JButton();",
							"    add(button);",
							"  }",
							"}"),
					m_lastEditor.getSource());
			assertAST(m_lastEditor);
		}
		// field -> local
		{
			assertFalse(button.getVariableSupport().canConvertLocalToField());
			assertTrue(button.getVariableSupport().canConvertFieldToLocal());
			button.getVariableSupport().convertFieldToLocal();
			assertEquals(
					getTestSource(
							"class Test extends JPanel {",
							"  Test() {",
							"    JButton button = new JButton();",
							"    add(button);",
							"  }",
							"}"),
					m_lastEditor.getSource());
			assertAST(m_lastEditor);
		}
		// local -> field
		{
			assertTrue(button.getVariableSupport().canConvertLocalToField());
			assertFalse(button.getVariableSupport().canConvertFieldToLocal());
			button.getVariableSupport().convertLocalToField();
			assertEquals(
					getTestSource(
							"class Test extends JPanel {",
							"  private JButton button;",
							"  Test() {",
							"    button = new JButton();",
							"    add(button);",
							"  }",
							"}"),
					m_lastEditor.getSource());
			assertAST(m_lastEditor);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// "this."
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for parsing source with "this" prefix.
	 */
	@Test
	public void test_thisQualifier_parse() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  JButton button;",
						"  Test() {",
						"    this.button = new JButton();",
						"    add(this.button);",
						"  }",
						"}");
		assertEquals(1, panel.getChildrenComponents().size());
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// check current variable
		{
			VariableSupport variableSupport = button.getVariableSupport();
			assertInstanceOf(FieldUniqueVariableSupport.class, variableSupport);
			assertEquals("button", variableSupport.getName());
			// setName
			{
				variableSupport.setName("button2");
				assertEditor(
						"class Test extends JPanel {",
						"  JButton button2;",
						"  Test() {",
						"    this.button2 = new JButton();",
						"    add(this.button2);",
						"  }",
						"}");
			}
		}
		// check toLocal
		{
			assertTrue(button.getVariableSupport().canConvertFieldToLocal());
			button.getVariableSupport().convertFieldToLocal();
			assertEditor(
					"class Test extends JPanel {",
					"  Test() {",
					"    JButton button2 = new JButton();",
					"    add(button2);",
					"  }",
					"}");
		}
	}

	/**
	 * Test {@link VariableSupport#getReferenceExpression(NodeTarget)} with "this." prefix.
	 */
	@Test
	public void test_thisQualifier_getReferenceExpression() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  JButton m_button;",
						"  Test() {",
						"    this.m_button = new JButton();",
						"    add(this.m_button);",
						"  }",
						"}");
		//
		ComponentInfo button = panel.getChildrenComponents().get(0);
		VariableSupport variableSupport = button.getVariableSupport();
		NodeTarget target = getNodeStatementTarget(panel, false, 1);
		// no "this." prefix option
		assertEquals("m_button", variableSupport.getReferenceExpression(target));
		// enable "this." prefix
		button.getDescription().getToolkit().getPreferences().setValue(
				FieldUniqueVariableSupport.P_PREFIX_THIS,
				true);
		assertEquals("this.m_button", variableSupport.getReferenceExpression(target));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Target
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_getTarget() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test {",
						"  static JPanel panel;",
						"  public static void main(String args[]){",
						"    panel = new JPanel();",
						"  }",
						"}");
		TypeDeclaration typeDeclaration = AstNodeUtils.getTypeByName(m_lastEditor.getAstUnit(), "Test");
		MethodDeclaration mainMethod = typeDeclaration.getMethods()[0];
		assertStatementTarget(panel, null, (Statement) mainMethod.getBody().statements().get(0), false);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ADD
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test adding new component, with "private" method modifier.
	 */
	@Test
	public void test_ADD_private() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		FlowLayoutInfo flowLayout = (FlowLayoutInfo) panel.getLayout();
		ComponentInfo newComponent = createJButton();
		// add component
		SwingTestUtils.setGenerations(
				FieldUniqueVariableDescription.INSTANCE,
				BlockStatementGeneratorDescription.INSTANCE);
		SwingTestUtils.setFieldUniqueModifier(FieldVariableSupport.V_FIELD_MODIFIER_PRIVATE);
		try {
			flowLayout.add(newComponent, null);
		} finally {
			SwingTestUtils.setGenerationDefaults();
		}
		// check
		assertEditor(
				"// filler filler filler filler filler",
				"public class Test extends JPanel {",
				"  private JButton button;",
				"  public Test() {",
				"    {",
				"      button = new JButton();",
				"      add(button);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Test adding new component, with "protected" method modifier.
	 */
	@Test
	public void test_ADD_public() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		FlowLayoutInfo flowLayout = (FlowLayoutInfo) panel.getLayout();
		ComponentInfo newComponent = createJButton();
		// add component
		SwingTestUtils.setGenerations(
				FieldUniqueVariableDescription.INSTANCE,
				BlockStatementGeneratorDescription.INSTANCE);
		SwingTestUtils.setFieldUniqueModifier(FieldVariableSupport.V_FIELD_MODIFIER_PROTECTED);
		try {
			flowLayout.add(newComponent, null);
		} finally {
			SwingTestUtils.setGenerationDefaults();
		}
		// check
		assertEditor(
				"// filler filler filler filler filler",
				"public class Test extends JPanel {",
				"  protected JButton button;",
				"  public Test() {",
				"    {",
				"      button = new JButton();",
				"      add(button);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Test adding new component, static context.
	 */
	@Test
	public void test_ADD_static() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test {",
						"  public static void main(String args[]) {",
						"    JPanel panel = new JPanel();",
						"  }",
						"}");
		FlowLayoutInfo flowLayout = (FlowLayoutInfo) panel.getLayout();
		ComponentInfo newComponent = createJButton();
		// add component
		SwingTestUtils.setGenerations(
				FieldUniqueVariableDescription.INSTANCE,
				BlockStatementGeneratorDescription.INSTANCE);
		flowLayout.add(newComponent, null);
		// check
		assertEditor(
				"public class Test {",
				"  private static JButton button;",
				"  public static void main(String args[]) {",
				"    JPanel panel = new JPanel();",
				"    {",
				"      button = new JButton();",
				"      panel.add(button);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Test adding new component, static context.
	 */
	@Test
	public void test_ADD_static_withNonStaticAnonymous() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test {",
						"  public static void main(String args[]) {",
						"    EventQueue.invokeLater(new Runnable() {",
						"      public void run() {",
						"        JPanel panel = new JPanel();",
						"      }",
						"    });",
						"  }",
						"}");
		FlowLayoutInfo flowLayout = (FlowLayoutInfo) panel.getLayout();
		ComponentInfo newComponent = createJButton();
		// add component
		SwingTestUtils.setGenerations(
				FieldUniqueVariableDescription.INSTANCE,
				BlockStatementGeneratorDescription.INSTANCE);
		flowLayout.add(newComponent, null);
		// check
		assertEditor(
				"public class Test {",
				"  private static JButton button;",
				"  public static void main(String args[]) {",
				"    EventQueue.invokeLater(new Runnable() {",
				"      public void run() {",
				"        JPanel panel = new JPanel();",
				"        {",
				"          button = new JButton();",
				"          panel.add(button);",
				"        }",
				"      }",
				"    });",
				"  }",
				"}");
	}

	/**
	 * Test adding new component, with "this." prefix.
	 */
	@Test
	public void test_ADD_thisPrefix() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		FlowLayoutInfo flowLayout = (FlowLayoutInfo) panel.getLayout();
		ComponentInfo newComponent = createJButton();
		// add component
		SwingTestUtils.setGenerations(
				FieldUniqueVariableDescription.INSTANCE,
				BlockStatementGeneratorDescription.INSTANCE);
		panel.getDescription().getToolkit().getPreferences().setValue(
				FieldUniqueVariableSupport.P_PREFIX_THIS,
				true);
		flowLayout.add(newComponent, null);
		// check
		assertEditor(
				"// filler filler filler",
				"public class Test extends JPanel {",
				"  private JButton button;",
				"  public Test() {",
				"    {",
				"      this.button = new JButton();",
				"      add(this.button);",
				"    }",
				"  }",
				"}");
		{
			FieldUniqueVariableSupport variableSupport =
					(FieldUniqueVariableSupport) newComponent.getVariableSupport();
			NodeTarget target = getNodeStatementTarget(panel, false, 0, 1);
			assertEquals("this.button", variableSupport.getReferenceExpression(target));
			assertEquals("this.button.", variableSupport.getAccessExpression(target));
		}
	}

	/**
	 * Test adding new component, with "m_" prefix.
	 */
	@Test
	public void test_ADD_configuredPrefix() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		FlowLayoutInfo flowLayout = (FlowLayoutInfo) panel.getLayout();
		ComponentInfo newComponent = createJButton();
		// add component
		SwingTestUtils.setGenerations(
				FieldUniqueVariableDescription.INSTANCE,
				BlockStatementGeneratorDescription.INSTANCE);
		m_javaProject.setOption(JavaCore.CODEASSIST_FIELD_PREFIXES, "m_");
		flowLayout.add(newComponent, null);
		// check
		assertEditor(
				"// filler filler filler",
				"public class Test extends JPanel {",
				"  private JButton m_button;",
				"  public Test() {",
				"    {",
				"      m_button = new JButton();",
				"      add(m_button);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Support for "%variable-name%" in creation source.
	 */
	@Test
	public void test_ADD_variableName_inCreationSource() throws Exception {
		setFileContentSrc(
				"test/MyButton.java",
				getTestSource(
						"// filler filler filler filler filler",
						"public class MyButton extends JButton {",
						"  public MyButton(String text) {",
						"  }",
						"}"));
		setFileContentSrc(
				"test/MyButton.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <creation>",
						"    <source><![CDATA[new test.MyButton('%variable-name%')]]></source>",
						"  </creation>",
						"</component>"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		// add new MyButton
		ComponentInfo button = createJavaInfo("test.MyButton");
		SwingTestUtils.setGenerations(
				FieldUniqueVariableDescription.INSTANCE,
				BlockStatementGeneratorDescription.INSTANCE);
		((FlowLayoutInfo) panel.getLayout()).add(button, null);
		assertEditor(
				"// filler filler filler",
				"public class Test extends JPanel {",
				"  private MyButton myButton;",
				"  public Test() {",
				"    {",
				"      myButton = new MyButton('myButton');",
				"      add(myButton);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Support for generic components and type arguments.
	 */
	@Test
	public void test_ADD_typeArguments() throws Exception {
		setFileContentSrc(
				"test/MyButton.java",
				getTestSource(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"public class MyButton<K, V> extends JButton {",
						"}"));
		setFileContentSrc(
				"test/MyButton.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <creation>",
						"    <source><![CDATA[new test.MyButton<%keyType%, %valueType%>()]]></source>",
						"  </creation>",
						"</component>"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		// add new MyButton
		SwingTestUtils.setGenerations(
				FieldUniqueVariableDescription.INSTANCE,
				BlockStatementGeneratorDescription.INSTANCE);
		{
			ComponentInfo newButton = createJavaInfo("test.MyButton");
			newButton.putTemplateArgument("keyType", "java.lang.String");
			newButton.putTemplateArgument("valueType", "java.util.List<java.lang.Double>");
			((FlowLayoutInfo) panel.getLayout()).add(newButton, null);
		}
		assertEditor(
				"import java.util.List;",
				"// filler filler filler filler filler",
				"public class Test extends JPanel {",
				"  private MyButton<String, List<Double>> myButton;",
				"  public Test() {",
				"    {",
				"      myButton = new MyButton<String, List<Double>>();",
				"      add(myButton);",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Delete
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * {@link FieldDeclaration} should be removed.
	 */
	@Test
	public void test_delete_1() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class  Test extends JPanel {",
						"  private JButton button;",
						"  public Test() {",
						"    button = new JButton();",
						"    add(button);",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		//
		assertTrue(button.canDelete());
		button.delete();
		assertEditor(
				"// filler filler filler",
				"public class  Test extends JPanel {",
				"  public Test() {",
				"  }",
				"}");
	}

	/**
	 * {@link FieldDeclaration} should be removed.
	 */
	@Test
	public void test_delete_2() throws Exception {
		parseContainer(
				"// filler filler filler",
				"public class  Test extends JPanel {",
				"  private JButton button_2;",
				"  public Test() {",
				"    JPanel inner = new JPanel();",
				"    add(inner);",
				"    //",
				"    JButton button_1 = new JButton();",
				"    inner.add(button_1);",
				"    //",
				"    button_2 = new JButton(button_1.getText());",
				"    inner.add(button_2);",
				"  }",
				"}");
		ComponentInfo inner = getJavaInfoByName("inner");
		//
		assertTrue(inner.canDelete());
		inner.delete();
		assertEditor(
				"// filler filler filler",
				"public class  Test extends JPanel {",
				"  public Test() {",
				"  }",
				"}");
	}

	/**
	 * Component is root, so its variable should not be removed.
	 */
	@Test
	public void test_delete_3() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"public class Test {",
						"  private static JPanel rootPanel;",
						"  public static void main(String[] args) {",
						"    rootPanel = new JPanel();",
						"  }",
						"}");
		//
		assertTrue(panel.canDelete());
		panel.delete();
		assertEditor(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"public class Test {",
				"  private static JPanel rootPanel;",
				"  public static void main(String[] args) {",
				"    rootPanel = new JPanel();",
				"  }",
				"}");
	}

	/**
	 * {@link VariableDeclaration} should be removed by one.
	 */
	@Test
	public void test_delete_4() throws Exception {
		parseContainer(
				"// filler filler filler",
				"public class  Test extends JPanel {",
				"  private JButton button_1, button_2, button_3;",
				"  public Test() {",
				"    button_1 = new JButton();",
				"    add(button_1);",
				"    //",
				"    button_2 = new JButton();",
				"    add(button_2);",
				"    //",
				"    button_3 = new JButton();",
				"    add(button_3);",
				"  }",
				"}");
		//
		{
			ComponentInfo button_1 = getJavaInfoByName("button_1");
			assertTrue(button_1.canDelete());
			button_1.delete();
			assertEditor(
					"// filler filler filler",
					"public class  Test extends JPanel {",
					"  private JButton button_2, button_3;",
					"  public Test() {",
					"    //",
					"    button_2 = new JButton();",
					"    add(button_2);",
					"    //",
					"    button_3 = new JButton();",
					"    add(button_3);",
					"  }",
					"}");
		}
		{
			ComponentInfo button_3 = getJavaInfoByName("button_3");
			assertTrue(button_3.canDelete());
			button_3.delete();
			assertEditor(
					"// filler filler filler",
					"public class  Test extends JPanel {",
					"  private JButton button_2;",
					"  public Test() {",
					"    //",
					"    button_2 = new JButton();",
					"    add(button_2);",
					"  }",
					"}");
		}
		{
			ComponentInfo button_2 = getJavaInfoByName("button_2");
			assertTrue(button_2.canDelete());
			button_2.delete();
			assertEditor(
					"// filler filler filler",
					"public class  Test extends JPanel {",
					"  public Test() {",
					"  }",
					"}");
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// setType()
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_setType() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  private JButton button;",
						"  public Test() {",
						"    button = new JButton();",
						"    add(button);",
						"  }",
						"}");
		JavaInfo button = panel.getChildrenComponents().get(0);
		// check
		FieldUniqueVariableSupport variable = (FieldUniqueVariableSupport) button.getVariableSupport();
		variable.setType("javax.swing.JTextField");
		assertEditor(
				"public class Test extends JPanel {",
				"  private JTextField button;",
				"  public Test() {",
				"    button = new JButton();",
				"    add(button);",
				"  }",
				"}");
	}
}
