/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.core.model.util;

import static org.eclipse.wb.internal.core.model.util.TemplateUtils.addStatement;
import static org.eclipse.wb.internal.core.model.util.TemplateUtils.evaluate;
import static org.eclipse.wb.internal.core.model.util.TemplateUtils.getExpression;
import static org.eclipse.wb.internal.core.model.util.TemplateUtils.resolve;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfoUtils;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.apache.commons.collections.bidimap.UnmodifiableBidiMap;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;

import java.text.MessageFormat;
import java.util.List;

/**
 * Tests for {@link TemplateUtils}.
 *
 * @author scheglov_ke
 */
public class TemplateUtilsTest extends SwingModelTest {
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
	// New templates
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link TemplateUtils#getExpression(JavaInfo)}.
	 */
	@Test
	public void test_getExpression() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		String id = ObjectInfoUtils.getId(panel);
		assertEquals(TemplateUtils.ID_PREFIX + id, getExpression(panel));
	}

	/**
	 * Test for {@link TemplateUtils#format(String, Object...)}.
	 */
	@Ignore
	@Test
	public void test_format() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		String id = ObjectInfoUtils.getId(panel);
		{
			String expected = TemplateUtils.ID_PREFIX + id + ".setEnabled(false)";
			assertEquals(expected, MessageFormat.format("{0}.setEnabled({1})", panel, "false"));
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Resolve
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link TemplateUtils#resolve(NodeTarget, String)}.
	 */
	@Test
	public void test_resolve_referenceExpression() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    int target;",
						"  }",
						"}");
		NodeTarget nodeTarget = getNodeStatementTarget(panel, false, 0);
		// do resolve
		String template = "foo(" + TemplateUtils.ID_PREFIX + ObjectInfoUtils.getId(panel) + ")";
		assertEquals("foo(this)", resolve(nodeTarget, template));
	}

	/**
	 * Test for {@link TemplateUtils#resolve(JavaInfo, StatementTarget, String)}.
	 */
	@Test
	public void test_resolve_referenceExpression_usingStatementTarget() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    int target;",
						"  }",
						"}");
		StatementTarget target = getStatementTarget(panel, false, 0);
		// do resolve
		String template = "foo(" + TemplateUtils.ID_PREFIX + ObjectInfoUtils.getId(panel) + ")";
		assertEquals("foo(this)", resolve(target, template));
	}

	/**
	 * Test for {@link TemplateUtils#resolve(BodyDeclarationTarget, String)}.
	 */
	@Test
	public void test_resolve_referenceExpression_usingBodyDeclarationTarget() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    int target;",
						"  }",
						"}");
		BodyDeclarationTarget target = getBodyDeclarationTarget(panel, false, 0);
		// do resolve
		String template = "foo(" + TemplateUtils.ID_PREFIX + ObjectInfoUtils.getId(panel) + ")";
		assertEquals("foo(this)", resolve(target, template));
	}

	/**
	 * Test for {@link TemplateUtils#resolve(NodeTarget, String)}.
	 */
	@Test
	public void test_resolve_thisAccessExpression() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    int target;",
						"  }",
						"}");
		NodeTarget nodeTarget = getNodeStatementTarget(panel, false, 0);
		// do resolve
		String template = TemplateUtils.ID_PREFIX + ObjectInfoUtils.getId(panel) + ".setEnabled(false)";
		assertEquals("setEnabled(false)", resolve(nodeTarget, template));
	}

	/**
	 * Test for {@link TemplateUtils#resolve(NodeTarget, String)}.
	 */
	@Test
	public void test_resolve_accessExpression() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JButton button = new JButton();",
						"    add(button);",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		NodeTarget nodeTarget = getNodeStatementTarget(panel, false, 0);
		// do resolve
		String template =
				TemplateUtils.ID_PREFIX + ObjectInfoUtils.getId(button) + ".setEnabled(false)";
		assertEquals("button.setEnabled(false)", resolve(nodeTarget, template));
	}

	/**
	 * Test for {@link TemplateUtils#resolve(List, StatementTarget)}.
	 */
	@Test
	public void test_resolve_StringList() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    int target;",
						"  }",
						"}");
		NodeTarget nodeTarget = getNodeStatementTarget(panel, false, 0);
		// do resolve
		List<String> lines = List.of(getExpression(panel) + " a", getExpression(panel) + " b");
		List<String> result = List.of("this a", "this b");
		Assertions.assertThat(resolve(nodeTarget, lines)).isEqualTo(result);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Format + resolve
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link TemplateUtils#resolve(StatementTarget, String, Object...)}.
	 */
	@Test
	public void test_formatResolve_StatementTarget() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    int target;",
						"  }",
						"}");
		StatementTarget target = getStatementTarget(panel, false, 0);
		// do resolve
		assertEquals("foo(this)", TemplateUtils.resolve(target, "foo({0})", panel));
	}

	/**
	 * Test for {@link TemplateUtils#resolve(StatementTarget, String, Object...)}.
	 */
	@Test
	public void test_formatResolve_BodyDeclarationTarget() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    int target;",
						"  }",
						"}");
		BodyDeclarationTarget target = getBodyDeclarationTarget(panel, false, 0);
		// do resolve
		assertEquals("foo(this)", TemplateUtils.resolve(target, "foo({0})", panel));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ASTNode operations for source with templates
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link TemplateUtils#addStatement(JavaInfo, StatementTarget, List)}.
	 */
	@Test
	public void test_addStatement() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		StatementTarget target = getBlockTarget(panel, true);
		// do resolve
		addStatement(panel, target, List.of(TemplateUtils.format("{0}", panel), "\t.setEnabled(false);"));
		assertEditor(
				"// filler filler filler",
				"public class Test extends JPanel {",
				"  public Test() {",
				"    this",
				"      .setEnabled(false);",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Evaluate OLD
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_evaluate() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JLabel myLabel = new JLabel();",
						"    add(myLabel);",
						"    //",
						"    JButton myButton = new JButton();",
						"    add(myButton);",
						"  }",
						"}");
		ComponentInfo label = panel.getChildrenComponents().get(0);
		ComponentInfo button = panel.getChildrenComponents().get(1);
		// good variables
		BidiMap bidi = new DualHashBidiMap();
		bidi.put("1", "foo");
		bidi.put("2", "bar");
		assertEquals("foo bar", evaluate("${1} ${2}", null, UnmodifiableBidiMap.decorate(bidi)));
		// good expressions
		{
			String expected = "a " + getExpression(button) + " b";
			String actual = evaluate("a ${expression} b", button);
			assertEquals(expected, actual);
		}
		{
			String expected = "first " + getExpression(button) + ", second " + getExpression(button);
			String actual = evaluate("first ${expression}, second ${expression}", button);
			assertEquals(expected, actual);
		}
		{
			String expected = "a " + getExpression(panel) + " b";
			String actual = evaluate("a ${parent.expression} b", button);
			assertEquals(expected, actual);
		}
		{
			String expected = "a " + getExpression(label) + " b";
			String actual = evaluate("a ${parent.firstChild[javax.swing.JLabel].expression} b", button);
			assertEquals(expected, actual);
		}
		{
			String expected = "a " + getExpression(button) + " b";
			String actual =
					evaluate("a ${parent.firstChild[javax.swing.AbstractButton].expression} b", button);
			assertEquals(expected, actual);
		}
		// bad: invalid operator
		try {
			evaluate("${noSuchOperator}", button);
			fail();
		} catch (IllegalArgumentException e) {
		}
		// bad: no component with given class
		try {
			evaluate("${firstChild[no.such.ComponentClass].expression}", button);
			fail();
		} catch (IllegalArgumentException e) {
		}
		// bad: not evaluated into String
		try {
			evaluate("${parent}", button);
			fail();
		} catch (IllegalArgumentException e) {
		}
	}
}
