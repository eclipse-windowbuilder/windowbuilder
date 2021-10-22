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
package org.eclipse.wb.tests.designer.databinding.rcp;

import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.check.AssertionFailedException;
import org.eclipse.wb.tests.designer.core.AbstractJavaTest;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.List;

/**
 * @author lobas_av
 *
 */
public class UtilsTest extends AbstractJavaTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Project creation
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_setUp() throws Exception {
    do_projectCreate();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getNodeReference() throws Exception {
    TypeDeclaration type =
        createTypeDeclaration_Test(
            "public class Test {",
            "  private String m_name;",
            "  private String m_name2 = '123';",
            "  private Test test = null;",
            "  public void foo() {",
            "    foo(m_name);",
            "    foo(this.m_name2);",
            "    foo(getName());",
            "    foo(this.test.getName());",
            "    foo(getTest().test.getName());",
            "    m_name = getName();",
            "    this.m_name = m_name2;",
            "    Test.this.m_name = m_name2;",
            "    test.m_name2 = '4567';",
            "    new String('aaa');",
            "  }",
            "  String getName() {",
            "    return m_name;",
            "  }",
            "  Test getTest() {",
            "    return this;",
            "  }",
            "  void foo(String value) {",
            "  }",
            "}");
    //
    MethodDeclaration method = AstNodeUtils.getMethodBySignature(type, "foo()");
    assertNotNull(method);
    //
    List<?> statements = method.getBody().statements();
    //
    assertNotNull(statements);
    assertEquals(10, statements.size());
    //
    MethodInvocation invocation0 = getMethodInvocation(statements, 0);
    assertEquals("m_name", CoreUtils.getNodeReference(DomGenerics.arguments(invocation0).get(0)));
    //
    MethodInvocation invocation1 = getMethodInvocation(statements, 1);
    assertEquals("m_name2", CoreUtils.getNodeReference(DomGenerics.arguments(invocation1).get(0)));
    //
    MethodInvocation invocation2 = getMethodInvocation(statements, 2);
    assertEquals("getName()", CoreUtils.getNodeReference(DomGenerics.arguments(invocation2).get(0)));
    //
    MethodInvocation invocation3 = getMethodInvocation(statements, 3);
    assertEquals(
        "test.getName()",
        CoreUtils.getNodeReference(DomGenerics.arguments(invocation3).get(0)));
    //
    MethodInvocation invocation4 = getMethodInvocation(statements, 4);
    assertEquals(
        "getTest().test.getName()",
        CoreUtils.getNodeReference(DomGenerics.arguments(invocation4).get(0)));
    //
    Assignment assignment5 = getAssignment(statements, 5);
    assertEquals("m_name", CoreUtils.getNodeReference(assignment5.getLeftHandSide()));
    //
    Assignment assignment6 = getAssignment(statements, 6);
    assertEquals("m_name", CoreUtils.getNodeReference(assignment6.getLeftHandSide()));
    //
    Assignment assignment7 = getAssignment(statements, 7);
    assertEquals("m_name", CoreUtils.getNodeReference(assignment7.getLeftHandSide()));
    //
    Assignment assignment8 = getAssignment(statements, 8);
    assertEquals("test.Test.m_name2", CoreUtils.getNodeReference(assignment8.getLeftHandSide()));
    //
    ClassInstanceCreation creation = getClassInstanceCreation(statements, 9);
    //
    try {
      CoreUtils.getNodeReference(creation);
    } catch (AssertionFailedException e) {
      assertEquals("Unknown reference: " + creation, e.getMessage());
    }
  }

  public void test_getMethodSignature() throws Exception {
    TypeDeclaration type =
        createTypeDeclaration_Test(
            "public class Test {",
            "  void foo() {",
            "    System.getProperty('os.name');",
            "  }",
            "}");
    MethodDeclaration[] methods = type.getMethods();
    List<Statement> statements = DomGenerics.statements(methods[0].getBody());
    ExpressionStatement statement = (ExpressionStatement) statements.get(0);
    //
    assertEquals(
        "java.lang.System.getProperty(java.lang.String)",
        CoreUtils.getMethodSignature((MethodInvocation) statement.getExpression()));
  }

  public void test_getCreationSignature() throws Exception {
    TypeDeclaration type =
        createTypeDeclaration_Test(
            "public class Test {",
            "  void foo() {",
            "    new String('string');",
            "  }",
            "}");
    MethodDeclaration[] methods = type.getMethods();
    List<Statement> statements = DomGenerics.statements(methods[0].getBody());
    ExpressionStatement statement = (ExpressionStatement) statements.get(0);
    //
    assertEquals(
        "java.lang.String.<init>(java.lang.String)",
        CoreUtils.getCreationSignature((ClassInstanceCreation) statement.getExpression()));
  }

  public void test_getDefaultString() throws Exception {
    assertEquals("default", CoreUtils.getDefaultString(null, null, "default"));
    assertEquals("default", CoreUtils.getDefaultString(null, "prefix", "default"));
    assertEquals("|string|", CoreUtils.getDefaultString("string", "|", "default"));
    assertEquals("|string|", CoreUtils.getDefaultString("string", "|", null));
  }

  public void test_joinStrings() throws Exception {
    assertEquals("", CoreUtils.joinStrings(null));
    assertEquals("", CoreUtils.joinStrings("delimeter"));
    assertEquals("1, 2, 3, 4", CoreUtils.joinStrings(", ", "1", "2", null, "3", "4"));
    assertEquals("1, 2, 3, 4", CoreUtils.joinStrings(", ", null, "1", "2", "3", "4"));
    assertEquals("1, 2, 3, 4", CoreUtils.joinStrings(", ", "1", "2", "3", "4", null));
  }

  public void test_loadClass() throws Exception {
    assertSame(String.class, CoreUtils.loadClass(getClass().getClassLoader(), "java.lang.String"));
    assertNull(CoreUtils.loadClass(getClass().getClassLoader(), "java.lang.Strong"));
  }

  public void test_isAssignableFrom1() throws Exception {
    assertTrue(CoreUtils.isAssignableFrom(Object.class, String.class));
    assertFalse(CoreUtils.isAssignableFrom(null, String.class));
  }

  public void test_isAssignableFrom2() throws Exception {
    assertTrue(CoreUtils.isAssignableFrom(
        getClass().getClassLoader(),
        "java.lang.Object",
        String.class));
    assertFalse(CoreUtils.isAssignableFrom(
        getClass().getClassLoader(),
        "java.lang.OBJECT",
        String.class));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  static MethodInvocation getMethodInvocation(List<?> statements, int index) {
    ExpressionStatement statement = (ExpressionStatement) statements.get(index);
    return (MethodInvocation) statement.getExpression();
  }

  static Assignment getAssignment(List<?> statements, int index) {
    ExpressionStatement statement = (ExpressionStatement) statements.get(index);
    return (Assignment) statement.getExpression();
  }

  static ClassInstanceCreation getClassInstanceCreation(List<?> statements, int index) {
    ExpressionStatement statement = (ExpressionStatement) statements.get(index);
    return (ClassInstanceCreation) statement.getExpression();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Project disposing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void test_tearDown() throws Exception {
    do_projectDispose();
  }
}