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

import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.variable.ExposedFieldVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.Statement;

/**
 * Test for {@link ExposedFieldVariableSupport}.
 *
 * @author scheglov_ke
 */
public class ExposedFieldTest extends AbstractVariableTest {
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
	public void test_object() throws Exception {
		m_javaProject.setOption(JavaCore.CODEASSIST_FIELD_PREFIXES, "m_");
		setFileContentSrc(
				"test/MyPanel.java",
				getTestSource(
						"public class MyPanel extends JPanel {",
						"  public final JButton m_button = new JButton();",
						"  public MyPanel() {",
						"    add(m_button);",
						"  }",
						"}"));
		waitForAutoBuild();
		String[] lines = {"public class Test extends MyPanel {", "  public Test() {", "  }", "}"};
		// parse
		ContainerInfo panel = parseContainer(lines);
		assertHierarchy(
				"{this: test.MyPanel} {this} {}",
				"  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
				"  {field: javax.swing.JButton} {m_button} {}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		//
		ExposedFieldVariableSupport variableSupport =
				(ExposedFieldVariableSupport) button.getVariableSupport();
		assertEquals("m_button", variableSupport.toString());
		assertEquals("m_button", variableSupport.getTitle());
		// we can request expression
		assertTrue(variableSupport.hasExpression(null));
		// expressions
		{
			NodeTarget target = getNodeBlockTarget(panel, true);
			assertEquals("m_button", variableSupport.getReferenceExpression(target));
			assertEquals("m_button.", variableSupport.getAccessExpression(target));
		}
		// component name
		assertEquals("thisButton", variableSupport.getComponentName());
		// name
		assertFalse(variableSupport.hasName());
		try {
			variableSupport.getName();
			fail();
		} catch (IllegalStateException e) {
		}
		try {
			variableSupport.setName("foo");
			fail();
		} catch (IllegalStateException e) {
		}
		// local -> field
		assertFalse(variableSupport.canConvertLocalToField());
		try {
			variableSupport.convertLocalToField();
			fail();
		} catch (IllegalStateException e) {
		}
		// field -> local
		assertFalse(variableSupport.canConvertFieldToLocal());
		try {
			variableSupport.convertFieldToLocal();
			fail();
		} catch (IllegalStateException e) {
		}
		// target
		{
			StatementTarget target = variableSupport.getStatementTarget();
			StatementTarget frameTarget = panel.getVariableSupport().getStatementTarget();
			assertSame(frameTarget.getBlock(), target.getBlock());
			assertSame(frameTarget.getStatement(), target.getStatement());
			assertEquals(frameTarget.isBefore(), target.isBefore());
		}
	}

	public void test_getChildTarget() throws Exception {
		setFileContentSrc(
				"test/MyPanel.java",
				getTestSource(
						"public class MyPanel extends JPanel {",
						"  public final JButton m_button = new JButton();",
						"  public MyPanel() {",
						"    add(m_button);",
						"  }",
						"}"));
		waitForAutoBuild();
		String[] lines =
			{
					"public class Test extends MyPanel {",
					"  public Test() {",
					"    setEnabled(false);",
					"  }",
			"}"};
		// parse
		ContainerInfo panel = parseContainer(lines);
		ComponentInfo button = panel.getChildrenComponents().get(0);
		VariableSupport variableSupport = button.getVariableSupport();
		// target
		{
			StatementTarget target = variableSupport.getChildTarget();
			assertEquals(JavaInfoUtils.getTarget(panel, null).toString(), target.toString());
		}
	}

	/**
	 * Test that we add new component after last {@link Statement} of "button".
	 */
	public void test_addButton() throws Exception {
		setFileContentSrc(
				"test/MyPanel.java",
				getTestSource(
						"public class MyPanel extends JPanel {",
						"  public final JPanel m_container = new JPanel();",
						"  public MyPanel() {",
						"    add(m_container);",
						"  }",
						"}"));
		waitForAutoBuild();
		String[] lines =
			{
					"public class Test extends MyPanel {",
					"  public Test() {",
					"    m_container.setEnabled(true);",
					"  }",
			"}"};
		// parse
		ContainerInfo frame = parseContainer(lines);
		ContainerInfo container = (ContainerInfo) frame.getChildrenComponents().get(0);
		FlowLayoutInfo layout = (FlowLayoutInfo) container.getLayout();
		//
		ComponentInfo button = createJButton();
		layout.add(button, null);
		assertEditor(
				"public class Test extends MyPanel {",
				"  public Test() {",
				"    m_container.setEnabled(true);",
				"    {",
				"      JButton button = new JButton();",
				"      m_container.add(button);",
				"    }",
				"  }",
				"}");
	}
}
