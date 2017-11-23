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

import com.google.common.collect.Lists;

import static org.eclipse.wb.core.eval.ExecutionFlowUtils.getAssignments;
import static org.eclipse.wb.core.eval.ExecutionFlowUtils.getDeclaration;
import static org.eclipse.wb.core.eval.ExecutionFlowUtils.getExecutionFlowConstructor;
import static org.eclipse.wb.core.eval.ExecutionFlowUtils.getFinalExpression;
import static org.eclipse.wb.core.eval.ExecutionFlowUtils.getLastAssignment;
import static org.eclipse.wb.core.eval.ExecutionFlowUtils.getReferences;
import static org.eclipse.wb.core.eval.ExecutionFlowUtils.hasVariableStamp;
import static org.eclipse.wb.core.eval.ExecutionFlowUtils.visit;

import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.core.eval.ExecutionFlowUtils;
import org.eclipse.wb.core.eval.ExecutionFlowUtils.ExecutionFlowFrameVisitor;
import org.eclipse.wb.core.eval.ExecutionFlowUtils.VisitingContext;
import org.eclipse.wb.internal.core.eval.ExecutionFlowProvider;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.exception.MultipleConstructorsError;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.tests.designer.core.TestBundle;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang.StringUtils;
import org.assertj.core.api.Assertions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Tests for {@link ExecutionFlowUtils}.
 *
 * @author scheglov_ke
 */
public class ExecutionFlowUtilsTest extends AbstractEngineTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
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
  // getExecutionFlowConstructor()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ExecutionFlowUtils#getExecutionFlowConstructor(TypeDeclaration)}.
   */
  public void test_getExecutionFlowConstructor_single() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "// filler filler filler",
            "public class Test {",
            "  public Test() {",
            "  }",
            "}");
    assertSame(
        typeDeclaration.getMethods()[0],
        ExecutionFlowUtils.getExecutionFlowConstructor(typeDeclaration));
  }

  /**
   * Test for {@link ExecutionFlowUtils#getExecutionFlowConstructor(TypeDeclaration)}.
   */
  public void test_getExecutionFlowConstructor_noConstructors() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "// filler filler filler",
            "public class Test {",
            "  // filler",
            "}");
    assertSame(null, ExecutionFlowUtils.getExecutionFlowConstructor(typeDeclaration));
  }

  /**
   * Test for {@link ExecutionFlowUtils#getExecutionFlowConstructor(TypeDeclaration)}.
   */
  public void test_getExecutionFlowConstructor_multipleWithTag() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  public Test(int a) {",
            "  }",
            "  /**",
            "  * @wbp.parser.constructor",
            "  */",
            "  public Test(double b) {",
            "  }",
            "}");
    assertSame(
        typeDeclaration.getMethods()[1],
        ExecutionFlowUtils.getExecutionFlowConstructor(typeDeclaration));
  }

  /**
   * Test for {@link ExecutionFlowUtils#getExecutionFlowConstructor(TypeDeclaration)}.
   */
  public void test_getExecutionFlowConstructor_multipleNoTag_noDefault() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  public Test(int a) {",
            "  }",
            "  public Test(double b) {",
            "  }",
            "}");
    try {
      ExecutionFlowUtils.getExecutionFlowConstructor(typeDeclaration);
      fail();
    } catch (MultipleConstructorsError e) {
      assertSame(null, e.getEditor());
      assertSame(null, e.getTypeDeclaration());
    }
  }
  /**
   * {@link ExecutionFlowProvider} that selects constructor with single <code>int</code> parameter
   * as default.
   */
  public static class ExecutionFlowProvider_forDefaultConstructor extends ExecutionFlowProvider {
    @Override
    public MethodDeclaration getDefaultConstructor(TypeDeclaration typeDeclaration) {
      for (MethodDeclaration constructor : AstNodeUtils.getConstructors(typeDeclaration)) {
        if ("<init>(int)".equals(AstNodeUtils.getMethodSignature(constructor))) {
          return constructor;
        }
      }
      return null;
    }
  }

  /**
   * Test for {@link ExecutionFlowUtils#getExecutionFlowConstructor(TypeDeclaration)} and using
   * {@link ExecutionFlowProvider#getDefaultConstructor(TypeDeclaration)}.
   */
  public void test_getExecutionFlowConstructor_multipleNoTag_useExecutionFlowProvider()
      throws Exception {
    TestBundle testBundle = new TestBundle();
    try {
      Class<?> providerClass = ExecutionFlowProvider_forDefaultConstructor.class;
      testBundle.addClass(providerClass);
      testBundle.addExtension(
          "org.eclipse.wb.core.java.executionFlowProviders",
          "<provider class='" + providerClass.getName() + "'/>");
      testBundle.install();
      try {
        TypeDeclaration typeDeclaration =
            createTypeDeclaration_Test(
                "public class Test {",
                "  public Test() {",
                "  }",
                "  public Test(int a) {",
                "  }",
                "}");
        assertSame(
            typeDeclaration.getMethods()[1],
            ExecutionFlowUtils.getExecutionFlowConstructor(typeDeclaration));
      } finally {
        testBundle.uninstall();
      }
    } finally {
      testBundle.dispose();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getExecutionFlow_entryPoint()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ExecutionFlowUtils#getExecutionFlow_entryPoint(TypeDeclaration)}.
   */
  public void test_getExecutionFlow_entryPoint_forConstructor_0() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  public Test(int a) {",
            "  }",
            "  public Test(double b) {",
            "  }",
            "}");
    assertSame(null, ExecutionFlowUtils.getExecutionFlow_entryPoint(typeDeclaration));
  }

  /**
   * Test for {@link ExecutionFlowUtils#getExecutionFlow_entryPoint(TypeDeclaration)}.
   */
  public void test_getExecutionFlow_entryPoint_forConstructor_1() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  /**",
            "  * @wbp.parser.entryPoint",
            "  */",
            "  public Test(int a) {",
            "  }",
            "  public Test(double b) {",
            "  }",
            "}");
    assertSame(
        typeDeclaration.getMethods()[0],
        ExecutionFlowUtils.getExecutionFlow_entryPoint(typeDeclaration));
  }

  /**
   * Test for {@link ExecutionFlowUtils#getExecutionFlow_entryPoint(TypeDeclaration)}.
   */
  public void test_getExecutionFlow_entryPoint_forConstructor_2() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  public Test(int a) {",
            "  }",
            "  /**",
            "  * @wbp.parser.entryPoint",
            "  */",
            "  public Test(double b) {",
            "  }",
            "}");
    assertSame(
        typeDeclaration.getMethods()[1],
        ExecutionFlowUtils.getExecutionFlow_entryPoint(typeDeclaration));
  }

  /**
   * Test for {@link ExecutionFlowUtils#getExecutionFlow_entryPoint(TypeDeclaration)}.
   */
  public void test_getExecutionFlow_entryPoint_forMethod() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  /**",
            "  * @wbp.parser.entryPoint",
            "  */",
            "  public void foo() {",
            "  }",
            "  public void bar() {",
            "  }",
            "}");
    assertSame(
        typeDeclaration.getMethods()[0],
        ExecutionFlowUtils.getExecutionFlow_entryPoint(typeDeclaration));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // visit
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_visit_empty() throws Exception {
    String code = "Test() {} void root() {}";
    String expectedNodes[] = {};
    check_visitNodes(code, expectedNodes, "root()");
  }

  public void test_visit_statement() throws Exception {
    String code = "Test() {} void root() {int value = 5;}";
    String expectedNodes[] = {"int value = 5;"};
    check_visitNodes(code, expectedNodes, "root()");
  }

  public void test_visit_TryStatement() throws Exception {
    String code =
        getSourceDQ(
            "void root() {",
            "  try {",
            "    int value = 5;",
            "  } catch (Exception e) {",
            "  } catch (Throwable e) {",
            "  }",
            "}");
    String expectedNodes[] = {"int value = 5;"};
    check_visitNodes(code, expectedNodes, "root()");
  }

  public void test_visit_fields() throws Exception {
    String code =
        "int m_value = 1; Test() {} void root() {int value = 5;} private String m_string;";
    String expectedNodes[] = {"int m_value = 1;", "private String m_string;", "int value = 5;"};
    check_visitNodes(code, expectedNodes, "root()");
  }

  public void test_visit_fields2() throws Exception {
    String code =
        getSourceDQ(
            "Test() {",
            "}",
            "void root() {",
            "  int value = 5;",
            "}",
            "int m_value = 1;",
            "private String m_string;");
    String expectedNodes[] = {"int m_value = 1;", "private String m_string;", "int value = 5;"};
    check_visitNodes(code, expectedNodes, "root()");
  }

  public void test_visit_initializers_static() throws Exception {
    String code =
        getSourceDQ(
            "// filler filler filler filler filler",
            "static int foo;",
            "static {",
            "  int bar;",
            "}",
            "void root() {",
            "}");
    String expectedNodes[] = {"static int foo;", "int bar;"};
    check_visitNodes(code, expectedNodes, "root()");
  }

  public void test_visit_initializers_instance() throws Exception {
    String code =
        getSourceDQ(
            "// filler filler filler filler filler",
            "int foo;",
            "{",
            "  int bar;",
            "}",
            "void root() {",
            "}");
    String expectedNodes[] = {"int foo;", "int bar;"};
    check_visitNodes(code, expectedNodes, "root()");
  }

  public void test_visit_statements() throws Exception {
    String code = "Test() {} void root() {System.out.println();  System.exit(0);}";
    String expectedNodes[] = {"System.out.println();", "System.exit(0);"};
    check_visitNodes(code, expectedNodes, "root()");
  }

  public void test_visit_single_constructor() throws Exception {
    String code = "int f; Test() {System.out.println();} void root() {System.exit(0);}";
    String expectedNodes[] = {"int f;", "System.out.println();", "System.exit(0);"};
    check_visitNodes(code, expectedNodes, "<init>", "root()");
  }

  /**
   * Even when there are no constructor, instance field still should be visited.
   */
  public void test_visit_noConstructor() throws Exception {
    String code = "int f; void root() {System.exit(0);}";
    String expectedNodes[] = {"int f;", "System.exit(0);"};
    check_visitNodes(code, expectedNodes, "root()");
  }

  /**
   * No constructor, but static and instance fields should be visited.
   */
  public void test_visit_noConstructor_withStatic() throws Exception {
    String code = "static int s; int f; void root() {System.exit(0);}";
    String expectedNodes[] = {"static int s;", "int f;", "System.exit(0);"};
    check_visitNodes(code, expectedNodes, "root()");
  }

  public void test_visit_local_constructor() throws Exception {
    String code =
        getSourceDQ(
            "public static void main(String[] args) {",
            "  Test test = new Test();",
            "}",
            "public Test() {",
            "  System.out.println();",
            "}");
    String expectedNodes[] = {"System.out.println();", "Test test = new Test();"};
    check_visitNodes(code, expectedNodes, "main(java.lang.String[])");
  }

  public void test_visit_localMethod_fromMain() throws Exception {
    String code =
        getSourceDQ(
            "private int field;",
            "public static void main(String[] args) {",
            "  Test test = new Test();",
            "  test.open();",
            "}",
            "public Test() {",
            "  int a;",
            "}",
            "public void open() {",
            "  field = 0;",
            "}");
    String expectedNodes[] =
        {"private int field;", "int a;", "Test test = new Test();", "field = 0;", "test.open();"};
    check_visitNodes(code, expectedNodes, "main(java.lang.String[])");
  }

  public void test_visit_several_constructors_good() throws Exception {
    String code =
        getSourceDQ(
            "/** @wbp.parser.constructor */",
            "Test() {",
            "  System.out.println();",
            "}",
            "Test(int foo) {",
            "}",
            "void root() {",
            "  System.exit(0);",
            "}");
    String expectedNodes[] = {"System.out.println();", "System.exit(0);"};
    check_visitNodes(code, expectedNodes, "<init>", "root()");
  }

  /**
   * When super() constructor invokes some protected method, <code>createContents()</code> in this
   * test, we visit constructor <em>after</em> this method.<br>
   * Note also, that in theory we should visit fields after <code>createContents()</code>, but
   * practically it is possible, that there are assignments in method to fields, so to use variable
   * declaration/assignment tracking, we should visit fields before.
   */
  public void test_visit_constructorInTheMiddle() throws Exception {
    String code =
        getSourceDQ(
            "private int f;",
            "Test() {",
            "  int a;",
            "}",
            "void createContents() {",
            "  int b;",
            "  f = 1;",
            "}");
    String expectedNodes[] = {"private int f;", "int b;", "f = 1;", "int a;"};
    check_visitNodes(code, expectedNodes, "createContents()", "<init>");
  }

  public void test_visit_static() throws Exception {
    String code = "Test() {System.out.println();} static int m_value; static void root() {}";
    String expectedNodes[] = {"static int m_value;"};
    check_visitNodes(code, expectedNodes, "root()");
  }

  public void test_visit_constructor() throws Exception {
    String code = "Test() {System.out.println();}";
    String expectedNodes[] = {"System.out.println();"};
    check_visitNodes(code, expectedNodes, "<init>()");
  }

  public void test_visit_ifStatement_genericExpression() throws Exception {
    String code =
        getSourceDQ(
            "void root() {",
            "  int a;",
            "  if (1 == 2) {",
            "    int b;",
            "  } else {",
            "    int c;",
            "  }",
            "}");
    String expectedNodes[] = {"int a;"};
    check_visitNodes(code, expectedNodes, "root()");
  }

  public void test_visit_ifStatement_true() throws Exception {
    String code =
        getSourceDQ(
            "void root() {",
            "  if (true) {",
            "    int a;",
            "  } else {",
            "    int b;",
            "  }",
            "}");
    String expectedNodes[] = {"int a;"};
    check_visitNodes(code, expectedNodes, "root()");
  }

  public void test_visit_ifStatement_false() throws Exception {
    String code =
        getSourceDQ(
            "void root() {",
            "  if (false) {",
            "    int a;",
            "  } else {",
            "    int b;",
            "  }",
            "}");
    String expectedNodes[] = {"int b;"};
    check_visitNodes(code, expectedNodes, "root()");
  }

  public void test_visit_ifStatement_Beans_isDesignTime() throws Exception {
    String code =
        getSourceDQ(
            "void root() {",
            "  if (java.beans.Beans.isDesignTime()) {",
            "    int a;",
            "  } else {",
            "    int b;",
            "  }",
            "}");
    String expectedNodes[] = {"int a;"};
    check_visitNodes(code, expectedNodes, "root()");
  }

  public void test_visit_ifStatement_Beans_isDesignTime_not() throws Exception {
    String code =
        getSourceDQ(
            "void root() {",
            "  if (!java.beans.Beans.isDesignTime()) {",
            "    int a;",
            "  } else {",
            "    int b;",
            "  }",
            "}");
    String expectedNodes[] = {"int b;"};
    check_visitNodes(code, expectedNodes, "root()");
  }

  public void test_visit_ifStatement_local_isDesignTime() throws Exception {
    String code =
        getSourceDQ(
            "void root() {",
            "  if (isDesignTime()) {",
            "    int a;",
            "  } else {",
            "    int b;",
            "  }",
            "}",
            "private static boolean isDesignTime() {",
            "  return false;",
            "}");
    String expectedNodes[] = {"int a;"};
    check_visitNodes(code, expectedNodes, "root()");
  }

  public void test_visit_ifStatement_local_isDesignTime_not() throws Exception {
    String code =
        getSourceDQ(
            "void root() {",
            "  if (!isDesignTime()) {",
            "    int a;",
            "  } else {",
            "    int b;",
            "  }",
            "}",
            "private static boolean isDesignTime() {",
            "  return false;",
            "}");
    String expectedNodes[] = {"int b;"};
    check_visitNodes(code, expectedNodes, "root()");
  }

  public void test_visit_ifStatement_lazy() throws Exception {
    String code =
        getSourceDQ(
            "Object lazy;",
            "Object getLazy() {",
            "  if (lazy == null) {",
            "    lazy = null;",
            "  }",
            "  return lazy;",
            "}");
    String expectedNodes[] = {"Object lazy;", "lazy = null;", "return lazy;"};
    check_visitNodes(code, expectedNodes, "getLazy()");
  }

  public void test_visit_ifStatement_otherCondition() throws Exception {
    String code = "void root() {int a; if (1 == 2) {int b;}}";
    String expectedNodes[] = {"int a;"};
    check_visitNodes(code, expectedNodes, "root()");
  }

  public void test_visit_invocation() throws Exception {
    String code = "void root() {System.out.println(); foo();}  void foo() {System.exit(0);}";
    String expectedNodes[] = {"System.out.println();", "System.exit(0);", "foo();"};
    check_visitNodes(code, expectedNodes, "root()");
  }

  public void test_visit_invocation_recursion() throws Exception {
    String code =
        getSourceDQ(
            "void root() {",
            "  System.out.println(0);",
            "  foo();",
            "}",
            "void foo() {",
            "  System.out.println(1);",
            "  foo();",
            "}");
    String expectedNodes[] =
        {"System.out.println(0);", "System.out.println(1);", "foo();", "foo();"};
    check_visitNodes(code, expectedNodes, "root()");
  }

  private void check_visitNodes(String code,
      final String expectedNodes[],
      String... methodSignatures) throws Exception {
    // prepare root methods
    MethodDeclaration rootMethods[];
    {
      TypeDeclaration typeDeclaration = createTypeDeclaration_TestC(code);
      rootMethods = new MethodDeclaration[methodSignatures.length];
      for (int i = 0; i < methodSignatures.length; i++) {
        String methodSignature = methodSignatures[i];
        if (methodSignature.equals("<init>")) {
          // when just "<init>" name, without parameters, find constructor using ExecutionFlowUtils
          rootMethods[i] = getExecutionFlowConstructor(typeDeclaration);
        } else {
          // normal method
          rootMethods[i] = AstNodeUtils.getMethodBySignature(typeDeclaration, methodSignature);
        }
      }
    }
    // do check
    check_visitNodes(rootMethods, expectedNodes);
  }

  private void check_visitNodes(MethodDeclaration[] rootMethods, final String[] expectedNodes) {
    // visit and check that visited nodes are same as expected
    final int indexWrapper[] = new int[]{0};
    ExecutionFlowDescription flowDescription = new ExecutionFlowDescription(rootMethods);
    visit(new VisitingContext(true), flowDescription, new ExecutionFlowFrameVisitor() {
      @Override
      public void postVisit(ASTNode node) {
        if (node instanceof Statement && !(node instanceof Block)) {
          assertNextNode(node);
        }
      }

      @Override
      public void endVisit(FieldDeclaration node) {
        assertNextNode(node);
      }

      private void assertNextNode(ASTNode node) {
        String actualNodeSource;
        try {
          actualNodeSource = m_lastEditor.getSource(node);
        } catch (Throwable e) {
          throw ReflectionUtils.propagate(e);
        }
        int index = indexWrapper[0]++;
        assertEquals(expectedNodes[index], actualNodeSource);
      }
    });
    // all expected nodes should be visited
    assertEquals(expectedNodes.length, indexWrapper[0]);
  }

  public void test_visit_abstractMethod() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public abstract class Test {",
            "  public void root() {",
            "    someAbstractMethod();",
            "  }",
            "  protected abstract void someAbstractMethod();",
            "}");
    MethodDeclaration rootMethod = typeDeclaration.getMethods()[0];
    //
    MethodDeclaration[] rootMethods = new MethodDeclaration[]{rootMethod};
    String expectedNodes[] = {"someAbstractMethod();"};
    check_visitNodes(rootMethods, expectedNodes);
  }

  public void test_visit_ConstructorInvocation() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  public Test() {",
            "    this(false);",
            "  }",
            "  public Test(boolean b) {",
            "    int a;",
            "  }",
            "}");
    MethodDeclaration rootMethod = typeDeclaration.getMethods()[0];
    //
    MethodDeclaration[] rootMethods = new MethodDeclaration[]{rootMethod};
    String expectedNodes[] = {"int a;", "this(false);"};
    check_visitNodes(rootMethods, expectedNodes);
  }

  public void test_visit_ConstructorInvocation_visitArgumentsFirst() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration(
            "test",
            "Test.java",
            getSource(
                "public class Test {",
                "  public Test() {",
                "    this(false);",
                "  }",
                "  public Test(boolean parameter) {",
                "  }",
                "}"));
    // prepare flow
    ExecutionFlowDescription flowDescription;
    {
      MethodDeclaration rootMethod = typeDeclaration.getMethods()[0];
      MethodDeclaration[] rootMethods = new MethodDeclaration[]{rootMethod};
      flowDescription = new ExecutionFlowDescription(rootMethods);
    }
    // visit
    final List<String> nodes = Lists.newArrayList();
    visit(new VisitingContext(true), flowDescription, new ExecutionFlowFrameVisitor() {
      @Override
      public void postVisit(ASTNode node) {
        boolean isSingleStatement = node instanceof Statement && !(node instanceof Block);
        if (isSingleStatement || node instanceof Expression) {
          nodes.add(m_lastEditor.getSource(node));
        }
      }
    });
    assertThat(nodes).containsExactly("false", "parameter", "this(false);");
  }

  /**
   * There is implementation detail of {@link ExecutionFlowUtils} - use use
   * {@link Method#invoke(Object, Object...)}, so this wraps original exception into
   * {@link InvocationTargetException}. This is not good, we should not expose this.
   */
  public void test_visit_unwrapInvocationTargetException() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration(
            "test",
            "Test.java",
            getSource(
                "public class Test {",
                "  public Test() {",
                "    System.out.println();",
                "  }",
                "}"));
    // prepare flow
    ExecutionFlowDescription flowDescription;
    {
      MethodDeclaration rootMethod = typeDeclaration.getMethods()[0];
      MethodDeclaration[] rootMethods = new MethodDeclaration[]{rootMethod};
      flowDescription = new ExecutionFlowDescription(rootMethods);
    }
    // visit
    final Error exception = new Error();
    try {
      visit(new VisitingContext(true), flowDescription, new ExecutionFlowFrameVisitor() {
        @Override
        public void endVisit(SimpleName node) {
          if (node.toString().equals("println")) {
            throw exception;
          }
        }
      });
    } catch (Throwable e) {
      assertSame(exception, e);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visiting and AnonymousClassDeclaration
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for ignoring {@link AnonymousClassDeclaration}.
   * <p>
   * {@link Runnable} in {@link Thread} should not be visited.
   */
  public void test_visit_AnonymousClassDeclaration_noVisit() throws Exception {
    String threadCode = "new Thread(new Runnable() {public void run() {int a;}});";
    String code = "void root() {" + threadCode + "}";
    String expectedNodes[] = {threadCode};
    check_visitNodes(code, expectedNodes, "root()");
  }

  /**
   * Test for ignoring {@link AnonymousClassDeclaration}.
   * <p>
   * {@link Runnable} in {@link Thread} should not be visited, and invocations also should not be
   * followed.
   */
  public void test_visit_AnonymousClassDeclaration_withInvocation() throws Exception {
    String threadCode = "new Thread(new Runnable() {public void run() {foo();}});";
    String code = "void root() {" + threadCode + "} void foo() {int b;}";
    String expectedNodes[] = {threadCode};
    check_visitNodes(code, expectedNodes, "root()");
  }
  /**
   * {@link ExecutionFlowProvider} that allows visiting anonymous {@link Runnable} as argument of
   * {@link Thread} creation.
   */
  public static class ExecutionFlowProvider_RunnableInThread extends ExecutionFlowProvider {
    @Override
    public boolean shouldVisit(AnonymousClassDeclaration anonymous) throws Exception {
      if (AstNodeUtils.isSuccessorOf(anonymous.resolveBinding(), "java.lang.Runnable")) {
        ASTNode anonymousCreation = anonymous.getParent();
        if (anonymousCreation.getLocationInParent() == ClassInstanceCreation.ARGUMENTS_PROPERTY) {
          Expression enclosingCreation = (ClassInstanceCreation) anonymousCreation.getParent();
          if (AstNodeUtils.isSuccessorOf(enclosingCreation, "java.lang.Thread")) {
            return true;
          }
        }
      }
      return false;
    }
  }

  /**
   * Test for ignoring {@link AnonymousClassDeclaration}.
   * <p>
   * In general {@link Runnable} in {@link Thread} should not be visited, and invocations also
   * should not be followed. However we install special {@link ExecutionFlowProvider} that allows
   * such visiting.
   */
  public void test_visit_AnonymousClassDeclaration_withInvocation_doVisit() throws Exception {
    TestBundle testBundle = new TestBundle();
    try {
      Class<?> providerClass = ExecutionFlowProvider_RunnableInThread.class;
      testBundle.addClass(providerClass);
      testBundle.addExtension(
          "org.eclipse.wb.core.java.executionFlowProviders",
          "<provider class='" + providerClass.getName() + "'/>");
      testBundle.install();
      try {
        String threadCode = "new Thread(new Runnable() {public void run() {foo();}});";
        String code = "void root() {" + threadCode + "} void foo() {int b;}";
        String expectedNodes[] = {"int b;", "foo();", threadCode};
        check_visitNodes(code, expectedNodes, "root()");
      } finally {
        testBundle.uninstall();
      }
    } finally {
      testBundle.dispose();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // findLastAssignment
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_findLastAssignment_variable() throws Exception {
    String code = "void root() {int value = 0; System.out.println(value);}";
    check_findLastAssignment(code, 1, new I_findLastAssignment() {
      public ASTNode getExpected(TypeDeclaration typeDeclaration,
          MethodDeclaration methodDeclaration,
          Statement[] statements) {
        return (ASTNode) ((VariableDeclarationStatement) statements[0]).fragments().get(0);
      }
    });
  }

  /**
   * If "left" side of assignment is variable, its last assignment is this assignment.
   */
  public void test_findLastAssignment_variable_recursiveAssignment_leftSide() throws Exception {
    // prepare root methods
    TypeDeclaration typeDeclaration;
    MethodDeclaration rootMethod;
    MethodDeclaration rootMethods[];
    {
      String code = "void root() {int value; value = 1;}";
      typeDeclaration = createTypeDeclaration_TestC(code);
      rootMethod = AstNodeUtils.getMethodBySignature(typeDeclaration, "root()");
      assertNotNull(rootMethod);
      rootMethods = new MethodDeclaration[]{rootMethod};
    }
    // prepare statements
    List<Statement> statementsList = DomGenerics.statements(rootMethod.getBody());
    Statement statements[] = statementsList.toArray(new Statement[statementsList.size()]);
    // prepare variable and assignment
    Assignment assignment;
    SimpleName variable;
    {
      assignment = (Assignment) ((ExpressionStatement) statements[1]).getExpression();
      variable = (SimpleName) assignment.getLeftHandSide();
    }
    //
    ASTNode lastAssignment = getLastAssignment(new ExecutionFlowDescription(rootMethods), variable);
    assertSame(assignment, lastAssignment);
  }

  /**
   * Variable in "right" side of assignment should see previous value.
   */
  public void test_findLastAssignment_variable_recursiveAssignment_rightSide() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "class Test {",
            "  // filler filler filler",
            "  void root() {",
            "    int value = 0;",
            "    value = value + 1;",
            "  }",
            "}");
    MethodDeclaration rootMethod = AstNodeUtils.getMethodBySignature(typeDeclaration, "root()");
    // prepare variable
    ASTNode variable = m_lastEditor.getEnclosingNode("value + 1");
    VariableDeclarationFragment expected =
        (VariableDeclarationFragment) m_lastEditor.getEnclosingNode("value = 0").getParent();
    // check last assignment
    {
      ASTNode lastAssignment =
          getLastAssignment(new ExecutionFlowDescription(rootMethod), variable);
      assertNotNull(lastAssignment);
      assertSame(expected, lastAssignment);
    }
  }

  public void test_findLastAssignment_variable_reassign() throws Exception {
    String code = "void root() {int value = 0; value = 1; System.out.println(value);}";
    check_findLastAssignment(code, 2, new I_findLastAssignment() {
      public ASTNode getExpected(TypeDeclaration typeDeclaration,
          MethodDeclaration methodDeclaration,
          Statement[] statements) {
        return ((ExpressionStatement) statements[1]).getExpression();
      }
    });
  }

  public void test_findLastAssignment_variable_reassign2() throws Exception {
    String code =
        "void root() {int value; value = 0; System.out.println(value); value = 1; System.out.println(value);}";
    check_findLastAssignment(code, 4, new I_findLastAssignment() {
      public ASTNode getExpected(TypeDeclaration typeDeclaration,
          MethodDeclaration methodDeclaration,
          Statement[] statements) {
        return ((ExpressionStatement) statements[3]).getExpression();
      }
    });
  }

  public void test_findLastAssignment_variable_reassign_later() throws Exception {
    String code = "void root() {int value = 0; System.out.println(value); value = 1;}";
    check_findLastAssignment(code, 1, new I_findLastAssignment() {
      public ASTNode getExpected(TypeDeclaration typeDeclaration,
          MethodDeclaration methodDeclaration,
          Statement[] statements) {
        return (ASTNode) ((VariableDeclarationStatement) statements[0]).fragments().get(0);
      }
    });
  }

  public void test_findLastAssignment_variable_sameInDifferentMethod() throws Exception {
    String code =
        "void root() {int value = 0; foo(); System.out.println(value);} void foo() {int value = 1;}";
    check_findLastAssignment(code, 2, new I_findLastAssignment() {
      public ASTNode getExpected(TypeDeclaration typeDeclaration,
          MethodDeclaration methodDeclaration,
          Statement[] statements) {
        return (ASTNode) ((VariableDeclarationStatement) statements[0]).fragments().get(0);
      }
    });
  }

  public void test_findLastAssignment_FieldDeclaration() throws Exception {
    String code = "int value = 0; void root() {System.out.println(value);}";
    check_findLastAssignment(code, 0, new I_findLastAssignment() {
      public ASTNode getExpected(TypeDeclaration typeDeclaration,
          MethodDeclaration methodDeclaration,
          Statement[] statements) {
        return (ASTNode) typeDeclaration.getFields()[0].fragments().get(0);
      }
    });
  }

  /**
   * Check for "this.fieldName" itself.
   */
  public void test_FieldDeclaration_this_1() throws Exception {
    CompilationUnit compilationUnit =
        createASTCompilationUnit(
            "test",
            "Test.java",
            getSourceDQ(
                "package test;",
                "class Test {",
                "  int value;",
                "  void root() {",
                "    int value = 0;",
                "    value = 1;",
                "    System.out.println(this.value);",
                "  }",
                "}"));
    TypeDeclaration typeDeclaration = (TypeDeclaration) compilationUnit.types().get(0);
    MethodDeclaration rootMethod = AstNodeUtils.getMethodBySignature(typeDeclaration, "root()");
    // prepare variable
    ASTNode variable;
    {
      ExpressionStatement statement =
          (ExpressionStatement) rootMethod.getBody().statements().get(2);
      MethodInvocation invocation = (MethodInvocation) statement.getExpression();
      variable = (ASTNode) invocation.arguments().get(0);
    }
    // check last assignment
    {
      ASTNode lastAssignment =
          getLastAssignment(new ExecutionFlowDescription(rootMethod), variable);
      assertNotNull(lastAssignment);
      assertSame(typeDeclaration.getFields()[0], lastAssignment.getParent());
    }
    // check references
    {
      List<Expression> references =
          getReferences(new ExecutionFlowDescription(rootMethod), variable);
      String[] expectedReferences = new String[]{"int value;", "System.out.println(this.value);"};
      assertEquals(expectedReferences.length, references.size());
      for (int i = 0; i < references.size(); i++) {
        ASTNode reference = references.get(i);
        String expectedReference = expectedReferences[i];
        String actualReference = m_lastEditor.getSource(reference.getParent().getParent());
        assertEquals(expectedReference, actualReference);
      }
    }
  }

  /**
   * Check for local variable that hides field with same name.
   */
  public void test_FieldDeclaration_this_2() throws Exception {
    CompilationUnit compilationUnit =
        createASTCompilationUnit(
            "test",
            "Test.java",
            getSourceDQ(
                "package test;",
                "class Test {",
                "  int value;",
                "  void root() {",
                "    int value = 0;",
                "    value = 1;",
                "    this.value = 2;",
                "    System.out.println(value);",
                "  }",
                "}"));
    TypeDeclaration typeDeclaration = (TypeDeclaration) compilationUnit.types().get(0);
    MethodDeclaration rootMeethod = AstNodeUtils.getMethodBySignature(typeDeclaration, "root()");
    // prepare variable
    ASTNode variable;
    {
      ExpressionStatement statement =
          (ExpressionStatement) rootMeethod.getBody().statements().get(3);
      MethodInvocation invocation = (MethodInvocation) statement.getExpression();
      variable = (ASTNode) invocation.arguments().get(0);
    }
    // check last assignment
    {
      ASTNode lastAssignment =
          getLastAssignment(new ExecutionFlowDescription(rootMeethod), variable);
      assertNotNull(lastAssignment);
      //
      ExpressionStatement statement =
          (ExpressionStatement) rootMeethod.getBody().statements().get(1);
      assertSame(statement.getExpression(), lastAssignment);
    }
    // check references
    {
      List<Expression> references =
          getReferences(new ExecutionFlowDescription(rootMeethod), variable);
      String[] expectedReferences =
          new String[]{"int value = 0;", "value = 1;", "System.out.println(value);"};
      assertEquals(expectedReferences.length, references.size());
      for (int i = 0; i < references.size(); i++) {
        SimpleName reference = (SimpleName) references.get(i);
        String expectedReference = expectedReferences[i];
        String actualReference = m_lastEditor.getSource(reference.getParent().getParent());
        assertEquals(expectedReference, actualReference);
      }
    }
  }

  public void test_findLastAssignment_FieldDeclaration_noValue() throws Exception {
    String code = "int value; void root() {System.out.println(value);}";
    check_findLastAssignment(code, 0, new I_findLastAssignment() {
      public ASTNode getExpected(TypeDeclaration typeDeclaration,
          MethodDeclaration methodDeclaration,
          Statement[] statements) {
        return (ASTNode) typeDeclaration.getFields()[0].fragments().get(0);
      }
    });
  }

  public void test_findLastAssignment_FieldDeclaration_variable_thisMethod() throws Exception {
    String code = "int value = 0; void root() {int value = 1; System.out.println(value);}";
    check_findLastAssignment(code, 1, new I_findLastAssignment() {
      public ASTNode getExpected(TypeDeclaration typeDeclaration,
          MethodDeclaration methodDeclaration,
          Statement[] statements) {
        return (ASTNode) ((VariableDeclarationStatement) statements[0]).fragments().get(0);
      }
    });
  }

  public void test_findLastAssignment_FieldDeclaration_reassign_thisMethod() throws Exception {
    String code = "int value = 0; void root() {value = 1; System.out.println(value);}";
    check_findLastAssignment(code, 1, new I_findLastAssignment() {
      public ASTNode getExpected(TypeDeclaration typeDeclaration,
          MethodDeclaration methodDeclaration,
          Statement[] statements) {
        return ((ExpressionStatement) statements[0]).getExpression();
      }
    });
  }

  public void test_findLastAssignment_FieldDeclaration_reassign_otherMethod() throws Exception {
    String code =
        "int value = 0; void root() {foo(); System.out.println(value);} void foo() {value = 1;}";
    check_findLastAssignment(code, 1, new I_findLastAssignment() {
      public ASTNode getExpected(TypeDeclaration typeDeclaration,
          MethodDeclaration methodDeclaration,
          Statement[] statements) {
        Statement statement =
            (Statement) typeDeclaration.getMethods()[1].getBody().statements().get(0);
        return ((ExpressionStatement) statement).getExpression();
      }
    });
  }

  public void test_findLastAssignment_FieldDeclaration_reassign_otherMethod2() throws Exception {
    String code =
        "int value = 0; void root() {System.out.println(value); foo();} void foo() {value = 1;}";
    check_findLastAssignment(code, 0, new I_findLastAssignment() {
      public ASTNode getExpected(TypeDeclaration typeDeclaration,
          MethodDeclaration methodDeclaration,
          Statement[] statements) {
        return (ASTNode) typeDeclaration.getFields()[0].fragments().get(0);
      }
    });
  }

  public void test_findLastAssignment_FieldDeclaration_reassign_otherMethod3() throws Exception {
    String code =
        "int value = 0; void foo() {value = 1;} void root() {System.out.println(value); foo();}";
    check_findLastAssignment(code, 0, new I_findLastAssignment() {
      public ASTNode getExpected(TypeDeclaration typeDeclaration,
          MethodDeclaration methodDeclaration,
          Statement[] statements) {
        return (ASTNode) typeDeclaration.getFields()[0].fragments().get(0);
      }
    });
  }

  public void test_findLastAssignment_FieldDeclaration_reassign_otherMethod4() throws Exception {
    String code =
        "int value = 0; void root() {foo(); System.out.println(value);} void foo() {int value = 1;}";
    check_findLastAssignment(code, 1, new I_findLastAssignment() {
      public ASTNode getExpected(TypeDeclaration typeDeclaration,
          MethodDeclaration methodDeclaration,
          Statement[] statements) {
        return (ASTNode) typeDeclaration.getFields()[0].fragments().get(0);
      }
    });
  }

  public void test_findLastAssignment_parameters() throws Exception {
    String code = "void root(int value) {System.out.println(value);}";
    check_findLastAssignment(code, "root(int)", 0, new I_findLastAssignment() {
      public ASTNode getExpected(TypeDeclaration typeDeclaration,
          MethodDeclaration methodDeclaration,
          Statement[] statements) {
        return (ASTNode) methodDeclaration.parameters().get(0);
      }
    });
  }

  public void test_findLastAssignment_parameterName() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  public void foo(int value) {",
            "  }",
            "}");
    MethodDeclaration fooMethod = typeDeclaration.getMethods()[0];
    // prepare variable
    SingleVariableDeclaration valueDeclaration = DomGenerics.parameters(fooMethod).get(0);
    SimpleName valueName = valueDeclaration.getName();
    // simple flow
    ExecutionFlowDescription flowDescription = new ExecutionFlowDescription(fooMethod);
    // get last assignment
    ASTNode lastAssignment = getLastAssignment(flowDescription, valueName);
    assertSame(valueDeclaration, lastAssignment);
    assertTrue(hasVariableStamp(valueName));
  }

  public void test_findLastAssignment_parameterName_inBinaryFlowMethod() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  public Test() {",
            "  }",
            "  public void foo(int value) {",
            "  }",
            "}");
    MethodDeclaration constructorMethod = typeDeclaration.getMethods()[0];
    MethodDeclaration fooMethod = typeDeclaration.getMethods()[1];
    // prepare variable
    SingleVariableDeclaration valueDeclaration = DomGenerics.parameters(fooMethod).get(0);
    SimpleName valueName = valueDeclaration.getName();
    // flow with linked binary flow
    ExecutionFlowDescription flowDescription = new ExecutionFlowDescription(constructorMethod);
    flowDescription.addBinaryFlowMethodBefore(constructorMethod.getBody(), fooMethod);
    // get last assignment
    ASTNode lastAssignment = getLastAssignment(flowDescription, valueName);
    assertSame(valueDeclaration, lastAssignment);
    assertTrue(hasVariableStamp(valueName));
  }

  public void test_findLastAssignment_parameters_hide_field() throws Exception {
    String code = "int value = 1; void root(int value) {System.out.println(value);}";
    check_findLastAssignment(code, "root(int)", 0, new I_findLastAssignment() {
      public ASTNode getExpected(TypeDeclaration typeDeclaration,
          MethodDeclaration methodDeclaration,
          Statement[] statements) {
        return (ASTNode) methodDeclaration.parameters().get(0);
      }
    });
  }

  /**
   * Test of last assignment for argument of {@link SuperConstructorInvocation}.
   */
  public void test_findLastAssignment_SuperConstructorInvocation_argument() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test extends java.util.ArrayList {",
            "  public Test(int size) {",
            "    super(size);",
            "  }",
            "}");
    MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
    SingleVariableDeclaration sizeDeclaration = DomGenerics.parameters(methodDeclaration).get(0);
    //
    SimpleName sizeArgument = (SimpleName) m_lastEditor.getEnclosingNode("size);");
    ExecutionFlowDescription flowDescription = new ExecutionFlowDescription(methodDeclaration);
    ASTNode lastAssignment = getLastAssignment(flowDescription, sizeArgument);
    assertSame(sizeDeclaration, lastAssignment);
  }

  /**
   * Follow to local constructor using {@link ConstructorInvocation}.
   */
  public void test_findLastAssignment_followConstructorInvocation() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test extends java.util.ArrayList {",
            "  public Test() {",
            "    this(0);",
            "  }",
            "  public Test(int size) {",
            "    System.out.println(size);",
            "  }",
            "}");
    MethodDeclaration entryPointConstructor = typeDeclaration.getMethods()[0];
    MethodDeclaration sizeConstructor = typeDeclaration.getMethods()[1];
    SingleVariableDeclaration sizeDeclaration = DomGenerics.parameters(sizeConstructor).get(0);
    //
    SimpleName sizeArgument = (SimpleName) m_lastEditor.getEnclosingNode("size);");
    ASTNode lastAssignment =
        getLastAssignment(new ExecutionFlowDescription(entryPointConstructor), sizeArgument);
    assertSame(sizeDeclaration, lastAssignment);
  }

  /**
   * In theory, when we ask for last assignment to variable that is not in execution flow, we can
   * not say this 100% correct. However in some situations, when variable is assigned in constructor
   * (so, always visited), we know last assignment.
   */
  public void test_findLastAssignment_notInFlow_initializedFromConstructor() throws Exception {
    CompilationUnit compilationUnit =
        createASTCompilationUnit(
            "test",
            "Test.java",
            getSourceDQ(
                "package test;",
                "public class Test {",
                "  int m_field;",
                "  public Test() {",
                "    m_field = 5;",
                "  }",
                "  private void disconnectedMethod() {",
                "    System.out.println(m_field);",
                "  }",
                "}"));
    TypeDeclaration typeDeclaration = DomGenerics.types(compilationUnit).get(0);
    MethodDeclaration constructorMethod = typeDeclaration.getMethods()[0];
    MethodDeclaration disconnectedMethod = typeDeclaration.getMethods()[1];
    // prepare expected result
    Assignment expectedAssignment;
    {
      Statement statement = DomGenerics.statements(constructorMethod.getBody()).get(0);
      expectedAssignment = (Assignment) ((ExpressionStatement) statement).getExpression();
    }
    // prepare last assignment visible in "disconnectedMethod"
    ASTNode actualAssignment;
    {
      Statement statement = DomGenerics.statements(disconnectedMethod.getBody()).get(0);
      MethodInvocation invocation =
          (MethodInvocation) ((ExpressionStatement) statement).getExpression();
      ASTNode variable = DomGenerics.arguments(invocation).get(0);
      actualAssignment =
          getLastAssignment(new ExecutionFlowDescription(constructorMethod), variable);
    }
    // assert
    assertSame(expectedAssignment, actualAssignment);
  }

  /**
   * In theory, when we ask for last assignment to variable that is not in execution flow, we can
   * not say this 100% correct. However in some situations, when variable is assigned in field (so,
   * always visited), we know last assignment.
   */
  public void test_findLastAssignment_notInFlow_initializedInField() throws Exception {
    CompilationUnit compilationUnit =
        createASTCompilationUnit(
            "test",
            "Test.java",
            getSourceDQ(
                "package test;",
                "public class Test {",
                "  int m_field = 5;",
                "  public Test() {",
                "  }",
                "  private void disconnectedMethod() {",
                "    System.out.println(m_field);",
                "  }",
                "}"));
    TypeDeclaration typeDeclaration = DomGenerics.types(compilationUnit).get(0);
    MethodDeclaration constructorMethod = typeDeclaration.getMethods()[0];
    MethodDeclaration disconnectedMethod = typeDeclaration.getMethods()[1];
    // prepare expected result
    VariableDeclaration expectedAssignment;
    {
      FieldDeclaration fieldDeclaration = typeDeclaration.getFields()[0];
      expectedAssignment = DomGenerics.fragments(fieldDeclaration).get(0);
    }
    // prepare last assignment visible in "disconnectedMethod"
    ASTNode actualAssignment;
    {
      Statement statement = DomGenerics.statements(disconnectedMethod.getBody()).get(0);
      MethodInvocation invocation =
          (MethodInvocation) ((ExpressionStatement) statement).getExpression();
      ASTNode variable = DomGenerics.arguments(invocation).get(0);
      actualAssignment =
          getLastAssignment(new ExecutionFlowDescription(constructorMethod), variable);
    }
    // assert
    assertSame(expectedAssignment, actualAssignment);
  }

  /**
   * There was problem that we were not able to find last assignment for field, when application
   * pattern and class also has constructor.
   * <p>
   * Here no constructor, just to check that in this case we have no problem.
   */
  public void test_findLastAssignment_applicationPattern_noConstructor() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "import javax.swing.*;",
            "public class Test {",
            "  private JPanel shell;",
            "  public static void main(String[] args) {",
            "    Test app = new Test();",
            "    app.open();",
            "  }",
            "  public void open() {",
            "    shell = new JPanel();",
            "    System.out.println(shell);",
            "  }",
            "}");
    MethodDeclaration entryPointConstructor = typeDeclaration.getMethods()[0];
    //
    Assignment shellAssignment = (Assignment) m_lastEditor.getEnclosingNode("shell =").getParent();
    SimpleName shellArgument = (SimpleName) m_lastEditor.getEnclosingNode("shell);");
    //
    ExecutionFlowDescription flowDescription = new ExecutionFlowDescription(entryPointConstructor);
    ASTNode lastAssignment = getLastAssignment(flowDescription, shellArgument);
    assertSame(shellAssignment, lastAssignment);
  }

  /**
   * There was problem that we were not able to find last assignment for field, when application
   * pattern and class also has constructor.
   */
  public void test_findLastAssignment_applicationPattern_withConstructor() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "import javax.swing.*;",
            "public class Test {",
            "  private JPanel shell;",
            "  public static void main(String[] args) {",
            "    Test app = new Test();",
            "    app.open();",
            "  }",
            "  public Test() {",
            "  }",
            "  public void open() {",
            "    shell = new JPanel();",
            "    System.out.println(shell);",
            "  }",
            "}");
    MethodDeclaration entryPointConstructor = typeDeclaration.getMethods()[0];
    //
    Assignment shellAssignment = (Assignment) m_lastEditor.getEnclosingNode("shell =").getParent();
    SimpleName shellArgument = (SimpleName) m_lastEditor.getEnclosingNode("shell);");
    //
    ExecutionFlowDescription flowDescription = new ExecutionFlowDescription(entryPointConstructor);
    ASTNode lastAssignment = getLastAssignment(flowDescription, shellArgument);
    assertSame(shellAssignment, lastAssignment);
  }
  private interface I_findLastAssignment {
    ASTNode getExpected(TypeDeclaration typeDeclaration,
        MethodDeclaration methodDeclaration,
        Statement statements[]);
  }

  private void check_findLastAssignment(String code,
      int variableStatementIndex,
      I_findLastAssignment helper) throws Exception {
    check_findLastAssignment(code, "root()", variableStatementIndex, helper);
  }

  private void check_findLastAssignment(String code,
      String methodSignature,
      int variableStatementIndex,
      I_findLastAssignment helper) throws Exception {
    // prepare root methods
    TypeDeclaration typeDeclaration;
    MethodDeclaration rootMethod;
    MethodDeclaration rootMethods[];
    {
      typeDeclaration = createTypeDeclaration_TestC(code);
      rootMethod = AstNodeUtils.getMethodBySignature(typeDeclaration, methodSignature);
      assertNotNull(rootMethod);
      rootMethods = new MethodDeclaration[]{rootMethod};
    }
    // prepare statements
    List<Statement> statementsList = DomGenerics.statements(rootMethod.getBody());
    Statement statements[] = statementsList.toArray(new Statement[statementsList.size()]);
    // prepare expected assignment and variable
    ASTNode expectedAssignment = helper.getExpected(typeDeclaration, rootMethod, statements);
    SimpleName variable =
        (SimpleName) ((MethodInvocation) ((ExpressionStatement) statements[variableStatementIndex]).getExpression()).arguments().get(
            0);
    // do check
    assertSame(
        expectedAssignment,
        getLastAssignment(new ExecutionFlowDescription(rootMethods), variable));
  }

  public void test_findLastAssignment_cache() throws Exception {
    // prepare root methods
    TypeDeclaration typeDeclaration;
    MethodDeclaration rootMethod;
    ExecutionFlowDescription flowDescription;
    {
      String code = "void root() {int value = 0; System.out.println(value);}";
      typeDeclaration = createTypeDeclaration_TestC(code);
      rootMethod = AstNodeUtils.getMethodBySignature(typeDeclaration, "root()");
      assertNotNull(rootMethod);
      flowDescription = new ExecutionFlowDescription(rootMethod);
    }
    // prepare statements
    List<Statement> statementsList = DomGenerics.statements(rootMethod.getBody());
    Statement statements[] = statementsList.toArray(new Statement[statementsList.size()]);
    // prepare expected assignment and variable
    ASTNode expectedAssignment =
        (ASTNode) ((VariableDeclarationStatement) statements[0]).fragments().get(0);
    SimpleName variable =
        (SimpleName) ((MethodInvocation) ((ExpressionStatement) statements[1]).getExpression()).arguments().get(
            0);
    // do check
    assertSame(expectedAssignment, getLastAssignment(flowDescription, variable));
    // this should be cached value
    assertSame(expectedAssignment, getLastAssignment(flowDescription, variable));
    // change AST to make cached value stale
    rootMethod.getName().setIdentifier("root2");
    assertSame(expectedAssignment, getLastAssignment(flowDescription, variable));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getFinalExpression
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ExecutionFlowUtils#getFinalExpression(ExecutionFlowDescription, Expression)}.<br>
   * Direct value.
   */
  public void test_getFinalExpression_1() throws Exception {
    check_getFinalExpression("555", new String[]{
        "package test;",
        "public class Test {",
        "  public Test() {",
        "    int a = !555;",
        "  }",
        "}"});
  }

  /**
   * Test for {@link ExecutionFlowUtils#getFinalExpression(ExecutionFlowDescription, Expression)}.<br>
   * Value declared as single variable.
   */
  public void test_getFinalExpression_2() throws Exception {
    check_getFinalExpression("555", new String[]{
        "package test;",
        "public class Test {",
        "  public Test() {",
        "    int a = 555;",
        "    System.out.println(!a);",
        "  }",
        "}"});
  }

  /**
   * Test for {@link ExecutionFlowUtils#getFinalExpression(ExecutionFlowDescription, Expression)}.<br>
   * Value declared via two variables.
   */
  public void test_getFinalExpression_3() throws Exception {
    check_getFinalExpression("555", new String[]{
        "package test;",
        "public class Test {",
        "  public Test() {",
        "    int a = 555;",
        "    int b = a;",
        "    System.out.println(!b);",
        "  }",
        "}"});
  }

  /**
   * Test for {@link ExecutionFlowUtils#getFinalExpression(ExecutionFlowDescription, Expression)}.<br>
   * Value assigned to single variable (not at declaration).
   */
  public void test_getFinalExpression_4() throws Exception {
    check_getFinalExpression("555", new String[]{
        "package test;",
        "public class Test {",
        "  public Test() {",
        "    int a;",
        "    a = 555;",
        "    System.out.println(!a);",
        "  }",
        "}"});
  }

  /**
   * Test for {@link ExecutionFlowUtils#getFinalExpression(ExecutionFlowDescription, Expression)}.<br>
   * Value assigned to single field (not at declaration).
   */
  public void test_getFinalExpression_5() throws Exception {
    check_getFinalExpression("555", new String[]{
        "package test;",
        "public class Test {",
        "  private int a;",
        "  public Test() {",
        "    a = 555;",
        "    System.out.println(!a);",
        "  }",
        "}"});
  }

  private void check_getFinalExpression(String expectedSource, String[] lines) throws Exception {
    TypeDeclaration typeDeclaration;
    Expression expression;
    {
      String source = getSource(lines);
      int expressionPosition = source.indexOf('!');
      source = StringUtils.remove(source, '!');
      typeDeclaration = createTypeDeclaration("test", "Test.java", source);
      expression = (Expression) AstNodeUtils.getEnclosingNode(typeDeclaration, expressionPosition);
    }
    //
    ExecutionFlowDescription flowDescription =
        new ExecutionFlowDescription(typeDeclaration.getMethods()[0]);
    Expression firstAssignment = getFinalExpression(flowDescription, expression);
    assertEquals(expectedSource, m_lastEditor.getSource(firstAssignment));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getAssignments
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getAssignments_variable() throws Exception {
    String code = "void root() {int value = 0; System.out.println(value);}";
    String[] expected = new String[]{"value = 0"};
    check_getAssignments(code, 1, expected);
  }

  public void test_getAssignments_variable2() throws Exception {
    String code = "void root() {int value = 0; value = 1; System.out.println(value);}";
    String[] expected = new String[]{"value = 0", "value = 1"};
    check_getAssignments(code, 2, expected);
  }

  public void test_getAssignments_variable3() throws Exception {
    String code = "void root() {int value; value = 1; System.out.println(value);}";
    String[] expected = new String[]{"value = 1"};
    check_getAssignments(code, 2, expected);
  }

  public void test_getAssignments_variable_call() throws Exception {
    String code =
        "void root() {int value = 0; foo(); System.out.println(value);} void foo() {int value = 2;}";
    String[] expected = new String[]{"value = 0"};
    check_getAssignments(code, 2, expected);
  }

  /**
   * Assignment to {@link FieldAccess} was caused {@link NullPointerException}, so we use this test
   * to ensure that this {@link NullPointerException} will not return again.
   */
  public void test_getAssignments_variable_usingFieldAccess_inThisUnit() throws Exception {
    setFileContentSrc(
        "test/MyObject.java",
        getSourceDQ("package test;", "public class MyObject {", "  public int m_value;", "}"));
    waitForAutoBuild();
    String code =
        "void root() {int value = 0; System.out.println(value); new MyObject().m_value = 2;}";
    String[] expected = new String[]{"value = 0"};
    check_getAssignments(code, 1, expected);
  }

  public void test_getAssignments_field() throws Exception {
    String code = "int value = 0; void root() {System.out.println(value);}";
    String[] expected = new String[]{"value = 0"};
    check_getAssignments(code, 0, expected);
  }

  public void test_getAssignments_field_assign() throws Exception {
    String code = "int value = 0; void root() {value = 1; System.out.println(value);}";
    String[] expected = new String[]{"value = 0", "value = 1"};
    check_getAssignments(code, 1, expected);
  }

  public void test_getAssignments_field_assign2() throws Exception {
    String code =
        "int value = 0; void root() {foo(); System.out.println(value);} void foo() {value = 1;}";
    String[] expected = new String[]{"value = 0", "value = 1"};
    check_getAssignments(code, 1, expected);
  }

  public void test_getAssignments_field_hideVariable() throws Exception {
    String code = "int value = 0; void root() {int value = 1; System.out.println(value);}";
    String[] expected = new String[]{"value = 1"};
    check_getAssignments(code, 1, expected);
  }

  private void check_getAssignments(String code,
      int variableStatementIndex,
      String[] expectedAssignments) throws Exception {
    check_getAssignments(code, "root()", variableStatementIndex, expectedAssignments);
  }

  private void check_getAssignments(String code,
      String methodSignature,
      int variableStatementIndex,
      String[] expectedAssignments) throws Exception {
    // prepare root methods
    TypeDeclaration typeDeclaration;
    MethodDeclaration rootMethod;
    ExecutionFlowDescription flowDescription;
    {
      typeDeclaration = createTypeDeclaration_TestC(code);
      rootMethod = AstNodeUtils.getMethodBySignature(typeDeclaration, methodSignature);
      assertNotNull(rootMethod);
      flowDescription = new ExecutionFlowDescription(rootMethod);
    }
    // prepare statements
    List<Statement> statementsList = DomGenerics.statements(rootMethod.getBody());
    Statement statements[] = statementsList.toArray(new Statement[statementsList.size()]);
    // prepare variable
    SimpleName variable =
        (SimpleName) ((MethodInvocation) ((ExpressionStatement) statements[variableStatementIndex]).getExpression()).arguments().get(
            0);
    // do check
    List<Expression> actualAssignments = getAssignments(flowDescription, variable);
    assertNotNull(actualAssignments);
    assertEquals(expectedAssignments.length, actualAssignments.size());
    for (int i = 0; i < expectedAssignments.length; i++) {
      String expectedAssignment = expectedAssignments[i];
      String actualAssignment = m_lastEditor.getSource(actualAssignments.get(i));
      assertEquals(expectedAssignment, actualAssignment);
    }
    // check for cache
    assertSame(actualAssignments, getAssignments(flowDescription, variable));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getReferences
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getReferences_variable() throws Exception {
    String code = "void root() {int value = 0; System.out.println(value);}";
    String[] expected = new String[]{"value = 0", "System.out.println(value)"};
    check_getReferences(code, 1, expected);
  }

  public void test_getReferences_field() throws Exception {
    String code = "int value = 0; void root() {value = 1; System.out.println(value);}";
    String[] expected = new String[]{"value = 0", "value = 1", "System.out.println(value)"};
    check_getReferences(code, 1, expected);
  }

  public void test_getReferences_field_2() throws Exception {
    String code = "int value; void root() {value = 1; System.out.println(value);}";
    String[] expected = new String[]{"value", "value = 1", "System.out.println(value)"};
    check_getReferences(code, 1, expected);
  }

  public void test_getReferences_field_3() throws Exception {
    String code =
        "int value; void root() {value = 1; System.out.println(value);} void foo() {value = 2;}";
    String[] expected =
        new String[]{"value", "value = 1", "System.out.println(value)", "value = 2"};
    check_getReferences(code, 1, expected);
  }

  /**
   * References on field declared after use.
   */
  public void test_getReferences_field_4() throws Exception {
    String code = "void root() {System.out.println(value);} int value;";
    String[] expected = new String[]{"value", "System.out.println(value)"};
    check_getReferences(code, 0, expected);
  }

  /**
   * References on field in {@link AnonymousClassDeclaration}.
   */
  public void test_getReferences_field_inAnonymous() throws Exception {
    String code =
        getSource(
            "int value;",
            "void root() {",
            "  System.out.println(value);",
            "  new Object() {",
            "    public int hashCode() {",
            "      return value;",
            "    }",
            "  };",
            "}");
    String[] expected = new String[]{"value", "System.out.println(value)", "return value;"};
    check_getReferences(code, 0, expected);
  }

  /**
   * References on field in {@link AnonymousClassDeclaration}.
   */
  public void test_getReferences_field_usingThisObject() throws Exception {
    String code =
        getSource(
            "int value;",
            "void root() {",
            "  System.out.println(value);",
            "  new Object() {",
            "    public int hashCode() {",
            "      Test test = new Test();",
            "      return test.value;",
            "    }",
            "  };",
            "}");
    String[] expected = new String[]{"value", "System.out.println(value)", "test.value"};
    check_getReferences(code, 0, expected);
  }

  private void check_getReferences(String code,
      int variableStatementIndex,
      String[] expectedAssignments) throws Exception {
    check_getReferences(code, "root()", variableStatementIndex, expectedAssignments);
  }

  private void check_getReferences(String code,
      String methodSignature,
      int variableStatementIndex,
      String[] expectedReferences) throws Exception {
    // prepare root methods
    TypeDeclaration typeDeclaration;
    MethodDeclaration rootMethod;
    ExecutionFlowDescription flowDescription;
    {
      typeDeclaration = createTypeDeclaration_TestC(code);
      rootMethod = AstNodeUtils.getMethodBySignature(typeDeclaration, methodSignature);
      assertNotNull(rootMethod);
      flowDescription = new ExecutionFlowDescription(rootMethod);
    }
    // prepare statements
    List<Statement> statementsList = DomGenerics.statements(rootMethod.getBody());
    Statement statements[] = statementsList.toArray(new Statement[statementsList.size()]);
    // prepare variable
    SimpleName variable =
        (SimpleName) ((MethodInvocation) ((ExpressionStatement) statements[variableStatementIndex]).getExpression()).arguments().get(
            0);
    // do check
    List<Expression> actualReferences = getReferences(flowDescription, variable);
    assertNotNull(actualReferences);
    assertEquals(expectedReferences.length, actualReferences.size());
    for (int i = 0; i < expectedReferences.length; i++) {
      String expectedAssignment = expectedReferences[i];
      ASTNode actualReference = actualReferences.get(i);
      String actualAssignment = m_lastEditor.getSource(actualReference.getParent());
      assertEquals(expectedAssignment, actualAssignment);
    }
    // check for cache
    assertSame(actualReferences, getReferences(flowDescription, variable));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getDeclaration
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getDeclaration_variable() throws Exception {
    String code = "void root() {int value = 0; System.out.println(value);}";
    check_getDeclaration(code, 1, "value = 0");
  }

  public void test_getDeclaration_methodParameter() throws Exception {
    String code = "void root(int value) {System.out.println(value);}";
    check_getDeclaration(code, "root(int)", 0, "int value");
  }

  public void test_getDeclaration_lazy() throws Exception {
    createTypeDeclaration_Test(
        "public class Test {",
        "  private Object lazy;",
        "  private Object getLazy() {",
        "    if (lazy == null) {",
        "      lazy = new Object();",
        "    }",
        "    return lazy;",
        "  }",
        "  public void root() {",
        "    getLazy();",
        "  }",
        "}");
    VariableDeclaration expectedDeclaration = getNode("lazy;", VariableDeclaration.class);
    SimpleName variable = getNode("lazy = new", SimpleName.class);
    MethodDeclaration rootMethod = getNode("getLazy()", MethodDeclaration.class);
    assertSame(
        expectedDeclaration,
        getDeclaration(new ExecutionFlowDescription(rootMethod), variable));
  }

  /**
   * Test that we can ask for declaration directly {@link SimpleName} of
   * {@link SingleVariableDeclaration}.
   */
  public void test_getDeclaration_methodParameter_direct() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  public void root(int value) {",
            "  }",
            "}");
    MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
    SingleVariableDeclaration variableDeclaration =
        (SingleVariableDeclaration) methodDeclaration.parameters().get(0);
    assertSame(
        variableDeclaration,
        getDeclaration(
            new ExecutionFlowDescription(methodDeclaration),
            variableDeclaration.getName()));
  }

  private void check_getDeclaration(String code,
      int variableStatementIndex,
      String expectedDeclaration) throws Exception {
    check_getDeclaration(code, "root()", variableStatementIndex, expectedDeclaration);
  }

  private void check_getDeclaration(String code,
      String methodSignature,
      int variableStatementIndex,
      String expectedDeclaration) throws Exception {
    // prepare root methods
    TypeDeclaration typeDeclaration;
    MethodDeclaration rootMethod;
    ExecutionFlowDescription flowDescription;
    {
      typeDeclaration = createTypeDeclaration_TestC(code);
      rootMethod = AstNodeUtils.getMethodBySignature(typeDeclaration, methodSignature);
      assertNotNull(rootMethod);
      flowDescription = new ExecutionFlowDescription(rootMethod);
    }
    // prepare statements
    List<Statement> statementsList = DomGenerics.statements(rootMethod.getBody());
    Statement statements[] = statementsList.toArray(new Statement[statementsList.size()]);
    // prepare variable
    SimpleName variable =
        (SimpleName) ((MethodInvocation) ((ExpressionStatement) statements[variableStatementIndex]).getExpression()).arguments().get(
            0);
    // do check
    VariableDeclaration declaration = getDeclaration(flowDescription, variable);
    assertEquals(expectedDeclaration, m_lastEditor.getSource(declaration));
    // check for cache
    assertSame(declaration, getDeclaration(flowDescription, variable));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Special case for variable information
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_noVariableInformationForDanglingNode() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  public Test() {",
            "    int foo = 1;",
            "    System.out.println(foo);",
            "  }",
            "}");
    MethodDeclaration rootMethod = typeDeclaration.getMethods()[0];
    ExecutionFlowDescription flowDescription = new ExecutionFlowDescription(rootMethod);
    ASTNode fooNode = m_lastEditor.getEnclosingNode("foo)");
    // initial state
    {
      VariableDeclaration declaration = getDeclaration(flowDescription, fooNode);
      assertThat(declaration).isNotNull();
      Assertions.<ASTNode>assertThat(getReferences(flowDescription, fooNode)).containsOnly(
          declaration.getName(),
          fooNode);
      Assertions.<ASTNode>assertThat(getAssignments(flowDescription, fooNode)).containsOnly(declaration);
      assertThat(getLastAssignment(flowDescription, fooNode)).isEqualTo(declaration);
    }
    // delete Statement for "fooNode", no more variables information
    m_lastEditor.removeEnclosingStatement(fooNode);
    {
      assertThat(getDeclaration(flowDescription, fooNode)).isNull();
      assertThat(getReferences(flowDescription, fooNode)).isEmpty();
      assertThat(getAssignments(flowDescription, fooNode)).isEmpty();
      assertThat(getLastAssignment(flowDescription, fooNode)).isNull();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getInvocations()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ExecutionFlowUtils#getInvocations(ExecutionFlowDescription, MethodDeclaration)}
   * .
   * <p>
   * {@link ConstructorInvocation}, same signature.
   */
  public void test_getInvocations_ConstructorInvocation_1() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  public Test() {",
            "    this(1);",
            "  }",
            "  public Test(int value) {",
            "  }",
            "}");
    MethodDeclaration entryPoint = typeDeclaration.getMethods()[0];
    MethodDeclaration target = typeDeclaration.getMethods()[1];
    ExecutionFlowDescription flowDescription = new ExecutionFlowDescription(entryPoint);
    //
    List<ASTNode> invocations = ExecutionFlowUtils.getInvocations(flowDescription, target);
    assertThat(invocations).hasSize(1);
    assertEquals("this(1);", m_lastEditor.getSource(invocations.get(0)));
  }

  /**
   * Test for {@link ExecutionFlowUtils#getInvocations(ExecutionFlowDescription, MethodDeclaration)}
   * .
   * <p>
   * {@link ConstructorInvocation}, different signature.
   */
  public void test_getInvocations_ConstructorInvocation_2() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  public Test() {",
            "    this(false);",
            "  }",
            "  public Test(int value) {",
            "  }",
            "  public Test(boolean value) {",
            "  }",
            "}");
    MethodDeclaration entryPoint = typeDeclaration.getMethods()[0];
    MethodDeclaration target = typeDeclaration.getMethods()[1];
    ExecutionFlowDescription flowDescription = new ExecutionFlowDescription(entryPoint);
    //
    List<ASTNode> invocations = ExecutionFlowUtils.getInvocations(flowDescription, target);
    assertThat(invocations).isEmpty();
  }

  /**
   * Test for {@link ExecutionFlowUtils#getInvocations(ExecutionFlowDescription, MethodDeclaration)}
   * .
   * <p>
   * {@link ClassInstanceCreation}, same signature.
   */
  public void test_getInvocations_ClassInstanceCreation_1() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  public static void main(String[] args) {",
            "    Test test = new Test(1);",
            "  }",
            "  public Test(int value) {",
            "  }",
            "  public static void main2() {",
            "    Test test = new Test(2);",
            "  }",
            "}");
    MethodDeclaration entryPoint = typeDeclaration.getMethods()[0];
    MethodDeclaration target = typeDeclaration.getMethods()[1];
    ExecutionFlowDescription flowDescription = new ExecutionFlowDescription(entryPoint);
    //
    List<ASTNode> invocations = ExecutionFlowUtils.getInvocations(flowDescription, target);
    assertThat(invocations).hasSize(1);
    assertEquals("new Test(1)", m_lastEditor.getSource(invocations.get(0)));
  }

  /**
   * Test for {@link ExecutionFlowUtils#getInvocations(ExecutionFlowDescription, MethodDeclaration)}
   * .
   * <p>
   * {@link ClassInstanceCreation}, different signature.
   */
  public void test_getInvocations_ClassInstanceCreation_2() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  public static void main(String[] args) {",
            "    Test test = new Test(false);",
            "  }",
            "  public Test(int value) {",
            "  }",
            "  public Test(boolean value) {",
            "  }",
            "}");
    MethodDeclaration entryPoint = typeDeclaration.getMethods()[0];
    MethodDeclaration target = typeDeclaration.getMethods()[1];
    ExecutionFlowDescription flowDescription = new ExecutionFlowDescription(entryPoint);
    //
    List<ASTNode> invocations = ExecutionFlowUtils.getInvocations(flowDescription, target);
    assertThat(invocations).isEmpty();
  }

  /**
   * Test for {@link ExecutionFlowUtils#getInvocations(ExecutionFlowDescription, MethodDeclaration)}
   * .
   * <p>
   * {@link MethodInvocation}, same signature.
   */
  public void test_getInvocations_MethodInvocation_1() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  public Test() {",
            "    target(1);",
            "  }",
            "  public void target(int value) {",
            "  }",
            "  public void disconnectedMethod() {",
            "    target(2);",
            "  }",
            "}");
    MethodDeclaration entryPoint = typeDeclaration.getMethods()[0];
    MethodDeclaration target = typeDeclaration.getMethods()[1];
    ExecutionFlowDescription flowDescription = new ExecutionFlowDescription(entryPoint);
    //
    List<ASTNode> invocations = ExecutionFlowUtils.getInvocations(flowDescription, target);
    assertThat(invocations).hasSize(1);
    assertEquals("target(1)", m_lastEditor.getSource(invocations.get(0)));
  }

  /**
   * Test for {@link ExecutionFlowUtils#getInvocations(ExecutionFlowDescription, MethodDeclaration)}
   * .
   * <p>
   * {@link MethodInvocation}, different signature.
   */
  public void test_getInvocations_MethodInvocation_2() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  public Test() {",
            "    notTarget(false);",
            "  }",
            "  public void targetMethod(int value) {",
            "  }",
            "  public void notTarget(boolean value) {",
            "  }",
            "}");
    MethodDeclaration entryPoint = typeDeclaration.getMethods()[0];
    MethodDeclaration target = typeDeclaration.getMethods()[1];
    ExecutionFlowDescription flowDescription = new ExecutionFlowDescription(entryPoint);
    //
    List<ASTNode> invocations = ExecutionFlowUtils.getInvocations(flowDescription, target);
    assertThat(invocations).isEmpty();
  }
}
