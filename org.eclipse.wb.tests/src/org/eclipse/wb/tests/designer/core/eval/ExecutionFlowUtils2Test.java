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
package org.eclipse.wb.tests.designer.core.eval;

import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.core.eval.ExecutionFlowUtils2;
import org.eclipse.wb.core.eval.ExpressionValue;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link ExecutionFlowUtils2}.
 *
 * @author scheglov_ke
 */
public class ExecutionFlowUtils2Test extends AbstractEngineTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		if (m_testProject == null) {
			do_projectCreate();
		}
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
	// getValue()
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_getValue_valueInDeclaration() throws Exception {
		String expressionSource = "a =";
		String expected = "1";
		check_getValue(new String[]{
				"public class Test {",
				"  public Test() {",
				"    int a = 1;",
				"    System.out.println(a);",
				"  }",
		"}"}, expressionSource, expected);
	}

	@Test
	public void test_getValue_useDeclaredVariable() throws Exception {
		String expressionSource = "a);";
		String expected = "1";
		check_getValue(new String[]{
				"public class Test {",
				"  public Test() {",
				"    int a = 1;",
				"    System.out.println(a);",
				"  }",
		"}"}, expressionSource, expected);
	}

	@Test
	public void test_getValue_valueInAssignment() throws Exception {
		String expressionSource = "b =";
		String expected = "1";
		check_getValue(new String[]{
				"public class Test {",
				"  public Test() {",
				"    int a = 1;",
				"    int b;",
				"    b = a;",
				"    System.out.println(b);",
				"  }",
		"}"}, expressionSource, expected);
	}

	/**
	 * We don't see declaration of field, so we can not find it in any frame. However this should not
	 * produce {@link NullPointerException}.
	 */
	@Test
	public void test_getValue_assignInheritedField() throws Exception {
		setFileContentSrc(
				"test/Base.java",
				getSource(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"package test;",
						"public class Base {",
						"  protected int m_field;",
						"}"));
		waitForAutoBuild();
		//
		String expressionSource = "b =";
		String expected = "2";
		check_getValue(new String[]{
				"package test;",
				"public class Test extends Base {",
				"  public Test() {",
				"    m_field = 1;",
				"    int b = 2;",
				"    System.out.println(b);",
				"  }",
		"}"}, expressionSource, expected);
	}

	@Test
	public void test_getValue_useAssignedValue() throws Exception {
		String expressionSource = "b);";
		String expected = "1";
		check_getValue(new String[]{
				"public class Test {",
				"  public Test() {",
				"    int a = 1;",
				"    int b;",
				"    b = a;",
				"    System.out.println(b);",
				"  }",
		"}"}, expressionSource, expected);
	}

	@Test
	public void test_getValue_useAssignedValue_separateDeclaration() throws Exception {
		String expressionSource = "a);";
		String expected = "1";
		check_getValue(new String[]{
				"public class Test {",
				"  public Test() {",
				"    int a;",
				"    a = 1;",
				"    System.out.println(a);",
				"  }",
		"}"}, expressionSource, expected);
	}

	@Test
	public void test_getValue_useInInnerBlock() throws Exception {
		String expressionSource = "a);";
		String expected = "1";
		check_getValue(new String[]{
				"public class Test {",
				"  public Test() {",
				"    int a = 1;",
				"    {",
				"      System.out.println(a);",
				"    }",
				"  }",
		"}"}, expressionSource, expected);
	}

	@Test
	public void test_getValue_assignInInnerBlock() throws Exception {
		String expressionSource = "a);";
		String expected = "2";
		check_getValue(new String[]{
				"public class Test {",
				"  public Test() {",
				"    int a = 1;",
				"    {",
				"      a = 2;",
				"    }",
				"    System.out.println(a);",
				"  }",
		"}"}, expressionSource, expected);
	}

	@Test
	public void test_getValue_followMethodInvocation() throws Exception {
		String expressionSource = "p);";
		String expected = "1";
		check_getValue(new String[]{
				"public class Test {",
				"  public Test() {",
				"    int a = 1;",
				"    foo(a);",
				"  }",
				"  private void foo(int p) {",
				"    System.out.println(p);",
				"  }",
		"}"}, expressionSource, expected);
	}

	@Test
	public void test_getValue_followConstructorInvocation() throws Exception {
		String expressionSource = "p);";
		String expected = "1";
		check_getValue(new String[]{
				"public class Test {",
				"  public Test() {",
				"    this(1);",
				"  }",
				"  public Test(int p) {",
				"    System.out.println(p);",
				"  }",
		"}"}, expressionSource, expected);
	}

	/**
	 * Right now we handle "varArgs" parameter as first argument.
	 */
	@Test
	public void test_getValue_followMethodInvocation_varArgs_hasValues() throws Exception {
		String expressionSource = "p);";
		String expected = "1";
		check_getValue(new String[]{
				"public class Test {",
				"  public Test() {",
				"    foo(1, 2, 3);",
				"  }",
				"  private void foo(int... p) {",
				"    System.out.println(p);",
				"  }",
		"}"}, expressionSource, expected);
	}

	/**
	 * We should correctly handle situation when "varArgs" is empty.
	 */
	@Test
	public void test_getValue_followMethodInvocation_varArgs_noValues() throws Exception {
		String expressionSource = "p);";
		String expected = null;
		check_getValue(new String[]{
				"public class Test {",
				"  public Test() {",
				"    foo();",
				"  }",
				"  private void foo(int... p) {",
				"    System.out.println(p);",
				"  }",
		"}"}, expressionSource, expected);
	}

	@Test
	public void test_getValue_reTrack_afterFlowChange() throws Exception {
		String expressionSource = "p);";
		String expected = "1";
		// prepare flow
		ExecutionFlowDescription executionFlow =
				prepare_flowDescription(new String[]{
						"public class Test {",
						"  public Test() {",
						"  }",
						"  private void foo() {",
						"    bar(1);",
						"  }",
						"  private void bar(int p) {",
						"    System.out.println(p);",
						"  }",
				"}"});
		// initially no value
		{
			ExpressionValue value = getValue(executionFlow, expressionSource);
			assertNull(value);
		}
		// include foo(), so OK
		{
			MethodDeclaration fooMethod = getNode("foo", MethodDeclaration.class);
			executionFlow.addStartMethod(fooMethod);
			check_getValue(executionFlow, expressionSource, expected);
		}
	}

	@Test
	public void test_getValue_useDeclaredField() throws Exception {
		String expressionSource = "a);";
		String expected = "1";
		check_getValue(new String[]{
				"public class Test {",
				"  int a = 1;",
				"  public Test() {",
				"    System.out.println(a);",
				"  }",
		"}"}, expressionSource, expected);
	}

	@Test
	public void test_getValue_fieldAssign() throws Exception {
		String expressionSource = "a);";
		String expected = "1";
		check_getValue(new String[]{
				"public class Test {",
				"  int a;",
				"  public Test() {",
				"    a = 1;",
				"    System.out.println(a);",
				"  }",
		"}"}, expressionSource, expected);
	}

	/**
	 * Assign using "this", access without it.
	 */
	@Test
	public void test_getValue_fieldAssign_withThisQualifier_1() throws Exception {
		String expressionSource = "a);";
		String expected = "1";
		check_getValue(new String[]{
				"public class Test {",
				"  int a;",
				"  public Test() {",
				"    this.a = 1;",
				"    System.out.println(a);",
				"  }",
		"}"}, expressionSource, expected);
	}

	/**
	 * Assign using "this", access using "this".
	 */
	@Test
	public void test_getValue_fieldAssign_withThisQualifier_2() throws Exception {
		String expressionSource = ".a);";
		String expected = "1";
		check_getValue(new String[]{
				"public class Test {",
				"  int a;",
				"  public Test() {",
				"    this.a = 1;",
				"    System.out.println(this.a);",
				"  }",
		"}"}, expressionSource, expected);
	}

	/**
	 * Assign using "this", access using "this". Application pattern.
	 */
	@Test
	public void test_getValue_fieldAssign_withThisQualifier_applicationPattern() throws Exception {
		String expressionSource = "a);";
		String expected = "1";
		check_getValue(new String[]{
				"public class Test {",
				"  private int a;",
				"  public static void main(String[] args) {",
				"    new Test();",
				"  }",
				"  public Test() {",
				"    this.a = 1;",
				"    System.out.println(this.a);",
				"  }",
		"}"}, expressionSource, expected);
	}

	/**
	 * We should understand pattern <code>this.value = value</code>.
	 */
	@Test
	public void test_getValue_fieldAssign_withThisQualifier_4() throws Exception {
		String expressionSource = "a);";
		String expected = "1";
		check_getValue(new String[]{
				"public class Test {",
				"  int a;",
				"  public Test() {",
				"    setValue(1);",
				"    System.out.println(a);",
				"  }",
				"  private void setValue(int a) {",
				"    this.a = a;",
				"  }",
		"}"}, expressionSource, expected);
	}

	@Test
	public void test_getValue_fieldWithoutInitilizer() throws Exception {
		String expressionSource = "a);";
		String expected = "{p}{p}int a;";
		check_getValue(new String[]{
				"public class Test {",
				"  int a;",
				"  public Test() {",
				"    System.out.println(a);",
				"  }",
		"}"}, expressionSource, expected);
	}

	@Test
	public void test_getValue_applicationPattern_withConstructor() throws Exception {
		String expressionSource = "a.";
		String expected = "Integer.valueOf(123)";
		check_getValue(new String[]{
				"public class Test {",
				"  protected Object a;",
				"  public static void main(String[] args) {",
				"    Test window = new Test();",
				"    window.open();",
				"  }",
				"  public Test() {",
				"  }",
				"  public void open() {",
				"    a = Integer.valueOf(123);",
				"    a.hashCode();",
				"  }",
		"}"}, expressionSource, expected);
	}

	/**
	 * There was problem that at second visit of type (to visit "other methods", not visited in main
	 * execution flow, to find references on fields) we ignore assignments, but this means that for
	 * separate declaration of "var" we remember "var" as its own value, instead of using assigned
	 * value.
	 * <p>
	 * In this case problem causes infinite recursive evaluation and "soft" failure.
	 */
	@Test
	public void test_getValue_applicationPattern_separateDeclaration() throws Exception {
		String expressionSource = "a);";
		String expected = "1";
		check_getValue(new String[]{
				"public class Test {",
				"  public static void main(String[] args) {",
				"    Test window = new Test();",
				"    window.open();",
				"  }",
				"  public void open() {",
				"    int a;",
				"    a = 1;",
				"    System.out.println(a);",
				"  }",
		"}"}, expressionSource, expected);
	}

	@Test
	public void test_getValue_fieldAssign_visibleInOtherMethod() throws Exception {
		String expressionSource = "a);";
		String expected = "1";
		check_getValue(new String[]{
				"public class Test {",
				"  int a;",
				"  public Test() {",
				"    a = 1;",
				"  }",
				"  public void foo() {",
				"    System.out.println(a);",
				"  }",
		"}"}, expressionSource, expected);
	}

	@Test
	public void test_getValue_fieldAssign_noOverrideInOtherMethod_1() throws Exception {
		String expressionSource = "a);";
		String expected = "1";
		check_getValue(new String[]{
				"public class Test {",
				"  int a;",
				"  public Test() {",
				"    a = 1;",
				"  }",
				"  public void foo() {",
				"    this.a = 2;",
				"  }",
				"  public void bar() {",
				"    System.out.println(a);",
				"  }",
		"}"}, expressionSource, expected);
	}

	@Test
	public void test_getValue_fieldAssign_noOverrideInOtherMethod_2() throws Exception {
		String expressionSource = "a);";
		String expected = "1";
		check_getValue(new String[]{
				"public class Test {",
				"  int a;",
				"  public Test() {",
				"    a = 1;",
				"  }",
				"  public void foo() {",
				"    a = 2;",
				"  }",
				"  public void bar() {",
				"    System.out.println(a);",
				"  }",
		"}"}, expressionSource, expected);
	}

	@Test
	public void test_getValue_noMethodInvocation_parameterUse() throws Exception {
		String expressionSource = "a);";
		String expected = "{p}int a";
		check_getValue(new String[]{
				"public class Test {",
				"  public Test(int a) {",
				"    System.out.println(a);",
				"  }",
		"}"}, expressionSource, expected);
	}

	@Test
	public void test_getValue_usePermanentValue_local() throws Exception {
		ExecutionFlowDescription executionFlow;
		{
			TypeDeclaration typeDeclaration =
					createTypeDeclaration(
							"test",
							"Test.java",
							getSource(
									"import javax.swing.*;",
									"public class Test {",
									"  public Test() {",
									"    JButton a = new JButton();",
									"    System.out.println(a);",
									"  }",
									"}"));
			MethodDeclaration entryPoint = typeDeclaration.getMethods()[0];
			executionFlow = new ExecutionFlowDescription(entryPoint);
		}
		// set permanent value for "new JButton()"
		ExpressionValue permanentValue;
		{
			ClassInstanceCreation creation = getNode("new JButton()");
			permanentValue = ExecutionFlowUtils2.ensurePermanentValue(creation);
			assertSame(creation, permanentValue.getExpression());
		}
		// ask getValue()
		Expression target = getNode("a);");
		ExpressionValue value = ExecutionFlowUtils2.getValue(executionFlow, target);
		assertSame(permanentValue, value);
	}

	@Test
	public void test_getValue_usePermanentValue_parameter() throws Exception {
		ExecutionFlowDescription executionFlow;
		{
			TypeDeclaration typeDeclaration =
					createTypeDeclaration(
							"test",
							"Test.java",
							getSource(
									"import javax.swing.*;",
									"public class Test {",
									"  public Test(JButton a) {",
									"    System.out.println(a);",
									"  }",
									"}"));
			MethodDeclaration entryPoint = typeDeclaration.getMethods()[0];
			executionFlow = new ExecutionFlowDescription(entryPoint);
		}
		// set permanent value for "new JButton()"
		ExpressionValue permanentValue;
		{
			SimpleName parameterName = getNode("a) {");
			permanentValue = ExecutionFlowUtils2.ensurePermanentValue(parameterName);
			assertSame(parameterName, permanentValue.getExpression());
		}
		// ask getValue()
		Expression target = getNode("a);");
		ExpressionValue value = ExecutionFlowUtils2.getValue(executionFlow, target);
		assertSame(permanentValue, value);
	}

	@Test
	public void test_getValue_useDeclaredVariable_inParenthesis() throws Exception {
		String expressionSource = "(a)";
		String expected = "1";
		check_getValue(new String[]{
				"public class Test {",
				"  public Test() {",
				"    int a = 1;",
				"    System.out.println((a));",
				"  }",
		"}"}, expressionSource, expected);
	}

	@Test
	public void test_getValue_useDeclaredVariable_casted() throws Exception {
		String expressionSource = "(int) a";
		String expected = "1";
		check_getValue(new String[]{
				"public class Test {",
				"  public Test() {",
				"    int a = 1;",
				"    System.out.println((int) a);",
				"  }",
		"}"}, expressionSource, expected);
	}

	@Test
	public void test_getValue_lazy() throws Exception {
		String expressionSource = "{p}getButton());";
		String expected = "new JButton()";
		check_getValue(new String[]{
				"import javax.swing.*;",
				"public class Test extends JPanel {",
				"  private JButton button;",
				"  public Test() {",
				"    add(getButton());",
				"  }",
				"  private JButton getButton() {",
				"    if (button == null) {",
				"      button = new JButton();",
				"    }",
				"    return button;",
				"  }",
		"}"}, expressionSource, expected);
	}

	@Test
	public void test_getValue_returnFromMethod() throws Exception {
		String expressionSource = "{p}getButton());";
		String expected = "new JButton()";
		check_getValue(new String[]{
				"import javax.swing.*;",
				"public class Test extends JPanel {",
				"  public Test() {",
				"    add(getButton());",
				"  }",
				"  private JButton getButton() {",
				"    return new JButton();",
				"  }",
		"}"}, expressionSource, expected);
	}

	@Test
	public void test_getValue_returnFromMethod_ignoreStatic() throws Exception {
		String expressionSource = "{p}createButton());";
		String expected = null;
		check_getValue(new String[]{
				"import javax.swing.*;",
				"public class Test extends JPanel {",
				"  public Test() {",
				"    add(createButton());",
				"  }",
				"  private static JButton createButton() {",
				"    return new JButton();",
				"  }",
		"}"}, expressionSource, expected);
	}

	/**
	 * We need special support for {@link PostfixExpression}, we need to know value before it,
	 * implemented as {@link ExecutionFlowUtils2#getValuePrev(ExecutionFlowDescription, Expression)}.
	 */
	@Test
	public void test_PostfixExpression_getValue() throws Exception {
		ExecutionFlowDescription executionFlow;
		{
			TypeDeclaration typeDeclaration =
					createTypeDeclaration(
							"test",
							"Test.java",
							getSource(
									"import javax.swing.*;",
									"public class Test {",
									"  public Test() {",
									"    int a = 1;",
									"    a++;",
									"  }",
									"}"));
			MethodDeclaration entryPoint = typeDeclaration.getMethods()[0];
			executionFlow = new ExecutionFlowDescription(entryPoint);
		}
		// prepare nodes
		Expression operandInIncrement = getNode("a++");
		PostfixExpression increment = (PostfixExpression) operandInIncrement.getParent();
		// getValue()
		{
			ExpressionValue value = ExecutionFlowUtils2.getValue(executionFlow, operandInIncrement);
			assertNotNull(value);
			assertSame(increment, value.getExpression());
		}
		// getValuePrev()
		{
			ExpressionValue value = ExecutionFlowUtils2.getValuePrev(executionFlow, operandInIncrement);
			assertNotNull(value);
			assertEquals("1", m_lastEditor.getSource(value.getExpression()));
		}
	}

	/**
	 * When we visit {@link MethodDeclaration} with {@link ReturnStatement} without expression, this
	 * caused {@link NullPointerException}.
	 */
	@Test
	public void test_getValue_whenReturn_withoutExpression() throws Exception {
		String expressionSource = "a =";
		String expected = "1";
		check_getValue(new String[]{
				"public class Test {",
				"  public Test() {",
				"    int a = 1;",
				"    System.out.println(a);",
				"    foo();",
				"  }",
				"  private void foo() {",
				"    return;",
				"  }",
		"}"}, expressionSource, expected);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link ExecutionFlowUtils2#getPermanentValue0(Expression)} and
	 * {@link ExecutionFlowUtils2#clearPermanentValue(Expression)}.
	 */
	@Test
	public void test_getPermanentValue0_clearPermanentValue() throws Exception {
		createTypeDeclaration(
				"test",
				"Test.java",
				getSource(
						"public class Test {",
						"  public Test() {",
						"    int a = 1;",
						"    System.out.println(a);",
						"  }",
						"}"));
		Expression target = getTarget("a)");
		// ensure value
		ExpressionValue permanentValue = ExecutionFlowUtils2.ensurePermanentValue(target);
		assertSame(permanentValue, ExecutionFlowUtils2.getPermanentValue0(target));
		// clear value
		ExecutionFlowUtils2.clearPermanentValue(target);
		assertSame(null, ExecutionFlowUtils2.getPermanentValue0(target));
	}

	/**
	 * Test for {@link ExecutionFlowUtils2#getValue0(Expression)}.
	 */
	@Test
	public void test_getValue0() throws Exception {
		createTypeDeclaration(
				"test",
				"Test.java",
				getSource(
						"public class Test {",
						"  public Test() {",
						"    int a = 1;",
						"    System.out.println(a);",
						"  }",
						"}"));
		Expression target = getTarget("a)");
		// set value
		ExpressionValue value = new ExpressionValue(null);
		ExpressionValue permanentValue = new ExpressionValue(null);
		// initially no value
		assertSame(null, ExecutionFlowUtils2.getValue0(target));
		// set "permanent"
		ExecutionFlowUtils2.setPermanentValue0(target, permanentValue);
		assertSame(permanentValue, ExecutionFlowUtils2.getValue0(target));
		// if has "value", use it, not "permanent"
		ExecutionFlowUtils2.setValue0(target, value);
		assertSame(value, ExecutionFlowUtils2.getValue0(target));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private void check_getValue(String[] lines, String expressionSource, String expected)
			throws Exception {
		ExecutionFlowDescription executionFlow = prepare_flowDescription(lines);
		check_getValue(executionFlow, expressionSource, expected);
	}

	private ExecutionFlowDescription prepare_flowDescription(String[] lines) throws Exception {
		ExecutionFlowDescription executionFlow;
		{
			TypeDeclaration typeDeclaration =
					createTypeDeclaration("test", "Test.java", getSource(lines));
			MethodDeclaration entryPoint = typeDeclaration.getMethods()[0];
			executionFlow = new ExecutionFlowDescription(entryPoint);
		}
		return executionFlow;
	}

	private void check_getValue(ExecutionFlowDescription executionFlow,
			String expressionSource,
			String expected) {
		ExpressionValue value = getValue(executionFlow, expressionSource);
		// validate
		if (expected != null) {
			assertNotNull(value);
			ASTNode actual = value.getExpression();
			//
			while (expected.startsWith("{p}")) {
				assertNotNull(actual);
				expected = expected.substring("{p}".length());
				actual = actual.getParent();
			}
			//
			assertNotNull(actual);
			assertEquals(expected, actual.toString().trim());
		} else {
			assertNull(value);
		}
	}

	private ExpressionValue getValue(ExecutionFlowDescription executionFlow, String expressionSource) {
		Expression target = getTarget(expressionSource);
		return ExecutionFlowUtils2.getValue(executionFlow, target);
	}

	private Expression getTarget(String expressionSource) {
		// prepare level
		int parentLevel = 0;
		while (expressionSource.startsWith("{p}")) {
			parentLevel++;
			expressionSource = expressionSource.substring("{p}".length());
		}
		// get target using source
		Expression target = getNode(expressionSource);
		// to to parents
		for (int i = 0; i < parentLevel; i++) {
			target = (Expression) target.getParent();
		}
		// done
		return target;
	}
}
