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
import org.eclipse.wb.internal.core.model.variable.FieldUniqueVariableSupport;
import org.eclipse.wb.internal.core.model.variable.LocalReuseVariableSupport;
import org.eclipse.wb.internal.core.model.variable.LocalUniqueVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.junit.Test;

/**
 * Test for {@link LocalReuseVariableSupport}.
 *
 * @author scheglov_ke
 */
public class LocalReuseTest extends AbstractVariableTest {
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
						"  public Test() {",
						"    JButton button;",
						"    {",
						"      button = new JButton('button 1');",
						"      add(button);",
						"    }",
						"    {",
						"      button = new JButton('button 2');",
						"      add(button);",
						"    }",
						"  }",
						"}");
		assertEquals(2, panel.getChildrenComponents().size());
		// check child: 0
		{
			JavaInfo button = panel.getChildrenComponents().get(0);
			VariableSupport variableSupport = button.getVariableSupport();
			assertTrue(variableSupport instanceof LocalReuseVariableSupport);
			assertTrue(variableSupport.hasName());
			assertEquals("local-reused: button", variableSupport.toString());
			assertEquals("button", variableSupport.getName());
			{
				NodeTarget target = getNodeStatementTarget(panel, false, 1);
				assertEquals("button", variableSupport.getReferenceExpression(target));
				assertEquals("button.", variableSupport.getAccessExpression(target));
			}
			assertTrue(variableSupport.canConvertLocalToField());
			assertFalse(variableSupport.canConvertFieldToLocal());
			try {
				variableSupport.convertFieldToLocal();
				fail();
			} catch (IllegalStateException e) {
			}
		}
		// check child: 1
		{
			JavaInfo button = panel.getChildrenComponents().get(1);
			VariableSupport variableSupport = button.getVariableSupport();
			assertTrue(variableSupport instanceof LocalReuseVariableSupport);
			assertEquals("button", variableSupport.getName());
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// setName
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_setName_justField() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JButton button;",
						"    {",
						"      button = new JButton('button 1');",
						"      add(button);",
						"    }",
						"    {",
						"      button = new JButton('button 2');",
						"      add(button);",
						"    }",
						"  }",
						"}");
		//
		JavaInfo button = panel.getChildrenComponents().get(0);
		button.getVariableSupport().setName("abc");
		assertTrue(button.getVariableSupport() instanceof FieldUniqueVariableSupport);
		assertAST(m_lastEditor);
		assertEquals(
				getTestSource(
						"public class Test extends JPanel {",
						"  private JButton abc;",
						"  public Test() {",
						"    JButton button;",
						"    {",
						"      abc = new JButton('button 1');",
						"      add(abc);",
						"    }",
						"    {",
						"      button = new JButton('button 2');",
						"      add(button);",
						"    }",
						"  }",
						"}"),
				m_lastEditor.getSource());
	}

	@Test
	public void test_setName_split() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JButton button = new JButton('button 1');",
						"    add(button);",
						"    //",
						"    button = new JButton('button 2');",
						"    add(button);",
						"  }",
						"}");
		//
		JavaInfo button = panel.getChildrenComponents().get(0);
		button.getVariableSupport().setName("abc");
		assertTrue(button.getVariableSupport() instanceof FieldUniqueVariableSupport);
		assertEditor(
				"public class Test extends JPanel {",
				"  private JButton abc;",
				"  public Test() {",
				"    JButton button;",
				"    abc = new JButton('button 1');",
				"    add(abc);",
				"    //",
				"    button = new JButton('button 2');",
				"    add(button);",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// getReferenceExpression
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Ask expression directly in block "1".
	 */
	@Test
	public void test_getReferenceExpression_local_1() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JButton button;",
						"    {",
						"      button = new JButton('button 1');",
						"      add(button);",
						"    }",
						"    {",
						"      button = new JButton('button 2');",
						"      add(button);",
						"    }",
						"  }",
						"}");
		String expectedSource = m_lastEditor.getSource();
		JavaInfo button = panel.getChildrenComponents().get(0);
		//
		NodeTarget target = getNodeStatementTarget(panel, false, 1, 1);
		assertEquals("button", button.getVariableSupport().getReferenceExpression(target));
		assertTrue(button.getVariableSupport() instanceof LocalReuseVariableSupport);
		assertEditor(expectedSource, m_lastEditor);
	}

	/**
	 * Ask expression directly after block "1".
	 */
	@Test
	public void test_getReferenceExpression_local_2() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JButton button;",
						"    {",
						"      button = new JButton('button 1');",
						"      add(button);",
						"    }",
						"    {",
						"      button = new JButton('button 2');",
						"      add(button);",
						"    }",
						"  }",
						"}");
		String expectedSource = m_lastEditor.getSource();
		JavaInfo button = panel.getChildrenComponents().get(0);
		//
		NodeTarget target = getNodeStatementTarget(panel, false, 1);
		assertEquals("button", button.getVariableSupport().getReferenceExpression(target));
		assertTrue(button.getVariableSupport() instanceof LocalReuseVariableSupport);
		assertEditor(expectedSource, m_lastEditor);
	}

	/**
	 * Ask expression after block "2", so variable was already reassigned and should be converted into
	 * field.
	 */
	@Test
	public void test_getReferenceExpression_remote() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JButton button;",
						"    {",
						"      button = new JButton('button 1');",
						"      add(button);",
						"    }",
						"    {",
						"      button = new JButton('button 2');",
						"      add(button);",
						"    }",
						"  }",
						"}");
		JavaInfo button = panel.getChildrenComponents().get(0);
		//
		NodeTarget target = getNodeStatementTarget(panel, false, 2);
		assertEquals("button_1", button.getVariableSupport().getReferenceExpression(target));
		assertTrue(button.getVariableSupport() instanceof FieldUniqueVariableSupport);
		assertEquals(
				getTestSource(
						"public class Test extends JPanel {",
						"  private JButton button_1;",
						"  public Test() {",
						"    JButton button;",
						"    {",
						"      button_1 = new JButton('button 1');",
						"      add(button_1);",
						"    }",
						"    {",
						"      button = new JButton('button 2');",
						"      add(button);",
						"    }",
						"  }",
						"}"),
				m_lastEditor.getSource());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// toField
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_toField() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JButton button;",
						"    {",
						"      button = new JButton('button 1');",
						"      add(button);",
						"    }",
						"    {",
						"      button = new JButton('button 2');",
						"      add(button);",
						"    }",
						"  }",
						"}");
		//
		ComponentInfo button = panel.getChildrenComponents().get(0);
		button.getVariableSupport().convertLocalToField();
		//
		{
			VariableSupport variableSupport = button.getVariableSupport();
			assertTrue(variableSupport instanceof FieldUniqueVariableSupport);
			assertEquals("button_1", variableSupport.getName());
		}
		assertEquals(
				getTestSource(
						"public class Test extends JPanel {",
						"  private JButton button_1;",
						"  public Test() {",
						"    JButton button;",
						"    {",
						"      button_1 = new JButton('button 1');",
						"      add(button_1);",
						"    }",
						"    {",
						"      button = new JButton('button 2');",
						"      add(button);",
						"    }",
						"  }",
						"}"),
				m_lastEditor.getSource());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// setType()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link LocalReuseVariableSupport#setType(String)}, new variable in block, so same name
	 * can be used.
	 */
	@Test
	public void test_setType_1() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JButton button;",
						"    {",
						"      button = new JButton('button 1');",
						"      add(button);",
						"    }",
						"    {",
						"      button = new JButton('button 2');",
						"      add(button);",
						"    }",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// set type
		{
			LocalReuseVariableSupport variable = (LocalReuseVariableSupport) button.getVariableSupport();
			variable.setType("javax.swing.JTextField");
		}
		// checks
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    JButton button;",
				"    {",
				"      JTextField button_1 = new JButton('button 1');",
				"      add(button_1);",
				"    }",
				"    {",
				"      button = new JButton('button 2');",
				"      add(button);",
				"    }",
				"  }",
				"}");
		{
			LocalUniqueVariableSupport variable =
					(LocalUniqueVariableSupport) button.getVariableSupport();
			assertEquals("button_1", variable.getName());
		}
	}

	/**
	 * Test for {@link LocalReuseVariableSupport#setType(String)}, no blocks, so new unique name
	 * should be generated.
	 */
	@Test
	public void test_setType_2() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JButton button;",
						"    //",
						"    button = new JButton('button 1');",
						"    add(button);",
						"    //",
						"    button = new JButton('button 2');",
						"    add(button);",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// set type
		{
			LocalReuseVariableSupport variable = (LocalReuseVariableSupport) button.getVariableSupport();
			variable.setType("javax.swing.JTextField");
		}
		// checks
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    JButton button;",
				"    //",
				"    JTextField button_1 = new JButton('button 1');",
				"    add(button_1);",
				"    //",
				"    button = new JButton('button 2');",
				"    add(button);",
				"  }",
				"}");
		{
			LocalUniqueVariableSupport variable =
					(LocalUniqueVariableSupport) button.getVariableSupport();
			assertEquals("button_1", variable.getName());
		}
	}

	/**
	 * Test for {@link LocalReuseVariableSupport#setType(String)}, no blocks, initialization in
	 * declaration, so new unique name should be generated.
	 */
	@Test
	public void test_setType_3() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JButton button = new JButton('button 1');",
						"    add(button);",
						"    //",
						"    button = new JButton('button 2');",
						"    add(button);",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// set type
		{
			LocalReuseVariableSupport variable = (LocalReuseVariableSupport) button.getVariableSupport();
			variable.setType("javax.swing.JTextField");
		}
		// checks
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    JButton button;",
				"    JTextField button_1 = new JButton('button 1');",
				"    add(button_1);",
				"    //",
				"    button = new JButton('button 2');",
				"    add(button);",
				"  }",
				"}");
		{
			LocalUniqueVariableSupport variable =
					(LocalUniqueVariableSupport) button.getVariableSupport();
			assertEquals("button_1", variable.getName());
		}
	}
}
