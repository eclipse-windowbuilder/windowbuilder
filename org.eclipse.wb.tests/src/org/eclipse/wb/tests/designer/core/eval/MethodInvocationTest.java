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

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.core.eval.InvocationEvaluatorInterceptor;
import org.eclipse.wb.internal.core.eval.evaluators.AnonymousEvaluationError;
import org.eclipse.wb.internal.core.eval.evaluators.InvocationEvaluator;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.exception.ICoreExceptionConstants;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.tests.designer.core.TestBundle;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Test for {@link InvocationEvaluator}.
 * 
 * @author scheglov_ke
 */
public class MethodInvocationTest extends AbstractEngineTest {
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
  // MethodInvocation
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_System_currentTimeMillis() throws Exception {
    Object actual = evaluateExpression("System.currentTimeMillis()", "long");
    assertTrue(actual instanceof Long);
  }

  public void test_staticPublicMethod() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "class Test {",
            "  public int call_staticPublicMethod() {",
            "    return staticPublicMethod(5);",
            "  }",
            "  public static int staticPublicMethod(int i) {",
            "    return 2 * i;",
            "  }",
            "}");
    waitForAutoBuild();
    Object actual = evaluateSingleMethod(typeDeclaration, "call_staticPublicMethod()");
    assertEquals(10, actual);
  }

  public void test_instancePublicMethod_bad() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "class Test {",
            "  public int root() {",
            "    return instancePublicMethod(5);",
            "  }",
            "  public int instancePublicMethod(int i) {",
            "    return 2 * i;",
            "  }",
            "}");
    waitForAutoBuild();
    try {
      evaluateSingleMethod(typeDeclaration, "root()");
      fail();
    } catch (Throwable e_) {
      DesignerException e = DesignerExceptionUtils.getDesignerException(e_);
      assertEquals(ICoreExceptionConstants.EVAL_LOCAL_METHOD_INVOCATION, e.getCode());
    }
  }

  /**
   * If method just returns some value, we can try to evaluate it.
   */
  public void test_instancePublicMethod_simpleReturn() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "class Test {",
            "  public int root() {",
            "    return instancePublicMethod();",
            "  }",
            "  public int instancePublicMethod() {",
            "    return 5;",
            "  }",
            "}");
    waitForAutoBuild();
    //
    assertEquals(5, evaluateSingleMethod(typeDeclaration, "root()"));
  }

  /**
   * We try to evaluate {@link ReturnStatement}, but it causes exception. So, we wrap it into usual
   * one.
   */
  public void test_instancePublicMethod_simpleReturn_fail() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "class Test {",
            "  public int root() {",
            "    return instancePublicMethod();",
            "  }",
            "  public int instancePublicMethod() {",
            "    return 5 / 0;",
            "  }",
            "}");
    waitForAutoBuild();
    //
    try {
      evaluateSingleMethod(typeDeclaration, "root()");
      fail();
    } catch (Throwable e) {
      {
        Throwable rootCause = DesignerExceptionUtils.getRootCause(e);
        assertThat(rootCause).isExactlyInstanceOf(ArithmeticException.class);
      }
      {
        DesignerException de = DesignerExceptionUtils.getDesignerException(e);
        assertEquals(ICoreExceptionConstants.EVAL_LOCAL_METHOD_INVOCATION, de.getCode());
      }
    }
  }

  /**
   * Test for <code>@wbp.eval.method.return parameterName</code> support.
   */
  public void test_instancePublicMethod_withReturnTag() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "class Test {",
            "  public int root() {",
            "    return instancePublicMethod(5);",
            "  }",
            "  /**",
            "  * @wbp.eval.method.return value",
            "  */",
            "  public int instancePublicMethod(int value) {",
            "    return 2 * value;",
            "  }",
            "}");
    waitForAutoBuild();
    //
    assertEquals(5, evaluateSingleMethod(typeDeclaration, "root()"));
  }

  /**
   * Test for <code>@wbp.eval.method.return parameterName</code> support.
   * <p>
   * Some text before tag.
   */
  public void test_instancePublicMethod_withReturnTag2() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "class Test {",
            "  public int root() {",
            "    return instancePublicMethod(5);",
            "  }",
            "  /**",
            "  * Some description.",
            "  * @wbp.eval.method.return value",
            "  */",
            "  public int instancePublicMethod(int value) {",
            "    return 2 * value;",
            "  }",
            "}");
    waitForAutoBuild();
    //
    assertEquals(5, evaluateSingleMethod(typeDeclaration, "root()"));
  }

  public void test_staticPublicMethod_inner() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "class Test {",
            "  public int call_innerMethod() {",
            "    return Foo.innerMethod(5);",
            "  }",
            "  public static class Foo {",
            "    public static int innerMethod(int i) {",
            "      return 3 * i;",
            "    }",
            "  }",
            "}");
    waitForAutoBuild();
    Object actual = evaluateSingleMethod(typeDeclaration, "call_innerMethod()");
    assertEquals(15, actual);
  }

  public void test_publicMethod_1() throws Exception {
    assertTrue(evaluateExpression("Runtime.getRuntime().totalMemory()", "long") instanceof Long);
  }

  public void test_staticImportMethod() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "import static java.lang.System.currentTimeMillis;",
            "class Test {",
            "  public long callMe() {",
            "    return currentTimeMillis();",
            "  }",
            "}");
    long actual = (Long) evaluateSingleMethod(typeDeclaration, "callMe()");
    assertThat(actual).isGreaterThan(1200L * 1000L * 1000L * 1000L);
  }

  public void test_methodInvocation_invalidArguments() throws Exception {
    try {
      m_ignoreModelCompileProblems = true;
      evaluateExpression("Runtime.getRuntime().totalMemory(123)", "long");
      fail();
    } catch (Throwable e_) {
      DesignerException e = DesignerExceptionUtils.getDesignerException(e_);
      assertEquals(ICoreExceptionConstants.EVAL_METHOD, e.getCode());
    }
  }

  public void test_methodInvocation_nullExpression() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "class Test {",
            "  public int root() {",
            "    Object obj = null;",
            "    return obj.hashCode();",
            "  }",
            "}");
    try {
      evaluateSingleMethod(typeDeclaration, "root()");
      fail();
    } catch (Throwable e_) {
      DesignerException e = DesignerExceptionUtils.getDesignerException(e_);
      assertEquals(ICoreExceptionConstants.EVAL_NULL_INVOCATION_EXPRESSION, e.getCode());
    }
  }

  public void test_methodInvocation_generic() throws Exception {
    Object actual =
        evaluateExpression("java.util.Arrays.asList(\"a\", \"b\", \"c\")", "java.util.List");
    assertEquals("[a, b, c]", actual.toString());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MethodInvocation: varArgs
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_staticPublicMethod_varArgsObject() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration("test", "Test.java", getSource(getDoubleQuotes(new String[]{
            "package test;",
            "class Test {",
            "  public static String getValue(int value, Object... parameters) {",
            "    String result = '';",
            "    for (Object p : parameters) {",
            "      result += p;",
            "    }",
            "    return result;",
            "  }",
            "  public String callMe() {",
            "    return getValue(5, 'p1', 'p2', 'p3');",
            "  }",
            "}"})));
    waitForAutoBuild();
    Object actual = evaluateSingleMethod(typeDeclaration, "callMe()");
    assertEquals("p1p2p3", actual);
  }

  public void test_staticPublicMethod_varArgsObject_useArray() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration("test", "Test.java", getSource(getDoubleQuotes(new String[]{
            "package test;",
            "class Test {",
            "  public static String getValue(int value, String... parameters) {",
            "    String result = '';",
            "    for (Object p : parameters) {",
            "      result += p;",
            "    }",
            "    return result;",
            "  }",
            "  public String callMe() {",
            "    return getValue(5, new String[]{'p1', 'p2', 'p3'});",
            "  }",
            "}"})));
    waitForAutoBuild();
    Object actual = evaluateSingleMethod(typeDeclaration, "callMe()");
    assertEquals("p1p2p3", actual);
  }

  public void test_staticPublicMethod_varArgsEmpty() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration("test", "Test.java", getSource(getDoubleQuotes(new String[]{
            "package test;",
            "class Test {",
            "  public static String getValue(Object... parameters) {",
            "    String result = 'parameters: ';",
            "    for (Object p : parameters) {",
            "      result += p;",
            "    }",
            "    return result;",
            "  }",
            "  public String callMe() {",
            "    return getValue();",
            "  }",
            "}"})));
    waitForAutoBuild();
    Object actual = evaluateSingleMethod(typeDeclaration, "callMe()");
    assertEquals("parameters: ", actual);
  }

  public void test_staticPublicMethod_varArgsInt() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration("test", "Test.java", getSource(getDoubleQuotes(new String[]{
            "package test;",
            "class Test {",
            "  public static int getValue(int value, int... parameters) {",
            "    int result = 0;",
            "    for (int p : parameters) {",
            "      result += p;",
            "    }",
            "    return result;",
            "  }",
            "  public int callMe() {",
            "    return getValue(5, 1, 2, 3);",
            "  }",
            "}"})));
    waitForAutoBuild();
    Object actual = evaluateSingleMethod(typeDeclaration, "callMe()");
    assertEquals(1 + 2 + 3, actual);
  }

  public void test_staticPublicMethod_varArgsDouble() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration("test", "Test.java", getSource(getDoubleQuotes(new String[]{
            "package test;",
            "class Test {",
            "  public static double getValue(int value, double... parameters) {",
            "    double result = 0;",
            "    for (double p : parameters) {",
            "      result += p;",
            "    }",
            "    return result;",
            "  }",
            "  public double callMe() {",
            "    return getValue(5, 1.0, 2.0, 3.0);",
            "  }",
            "}"})));
    waitForAutoBuild();
    Object actual = evaluateSingleMethod(typeDeclaration, "callMe()");
    assertEquals(1.0 + 2.0 + 3.0, actual);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Use InvocationEvaluatorInterceptor to resolve Method 
  //
  ////////////////////////////////////////////////////////////////////////////
  public static class Test_InvocationEvaluatorInterceptor_resolveMethod
      extends
        InvocationEvaluatorInterceptor {
    @Override
    public Method resolveMethod(Class<?> clazz, String signature) throws Exception {
      if (clazz.getName().equals("test.MyObject") && signature.equals("foo()")) {
        return ReflectionUtils.getMethodBySignature(clazz, "bar()");
      }
      return null;
    }
  }

  /**
   * Using {@link InvocationEvaluatorInterceptor#resolveMethod(Class, String)}.
   */
  public void test_InvocationEvaluatorInterceptor_resolveMethod() throws Exception {
    setFileContentSrc(
        "test/MyObject.java",
        getSourceDQ(
            "package test;",
            "public class MyObject {",
            "  public static int foo() {",
            "    return 1;",
            "  }",
            "  public static int bar() {",
            "    return 2;",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    TestBundle testBundle = new TestBundle();
    try {
      Class<?> interceptorClass = Test_InvocationEvaluatorInterceptor_resolveMethod.class;
      testBundle.addClass(interceptorClass);
      testBundle.addExtension(
          "org.eclipse.wb.core.invocationEvaluatorInterceptors",
          "<interceptor class='" + interceptorClass.getName() + "'/>");
      testBundle.install();
      try {
        assertEquals(2, evaluateExpression("MyObject.foo()", "int"));
      } finally {
        testBundle.uninstall();
      }
    } finally {
      testBundle.dispose();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // InvocationEvaluatorInterceptor: intercept MethodInvocation
  //
  ////////////////////////////////////////////////////////////////////////////
  public static class Test_InvocationEvaluatorInterceptor_MI extends InvocationEvaluatorInterceptor {
    @Override
    public Object evaluate(EvaluationContext context,
        MethodInvocation invocation,
        IMethodBinding methodBinding,
        Class<?> clazz,
        Method method,
        Object[] argumentValues) {
      if (method.getName().equals("foo")) {
        return 2;
      }
      return AstEvaluationEngine.UNKNOWN;
    }
  }

  /**
   * Using {@link InvocationEvaluatorInterceptor#evaluate(MethodInvocation)} .
   */
  public void test_InvocationEvaluatorInterceptor_MethodInvocation() throws Exception {
    setFileContentSrc(
        "test/MyObject.java",
        getSourceDQ(
            "package test;",
            "public class MyObject {",
            "  public static int foo() {",
            "    return 1;",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    TestBundle testBundle = new TestBundle();
    try {
      Class<?> interceptorClass = Test_InvocationEvaluatorInterceptor_MI.class;
      testBundle.addClass(interceptorClass);
      testBundle.addExtension(
          "org.eclipse.wb.core.invocationEvaluatorInterceptors",
          "<interceptor class='" + interceptorClass.getName() + "'/>");
      testBundle.install();
      try {
        assertEquals(2, evaluateExpression("MyObject.foo()", "int"));
      } finally {
        testBundle.uninstall();
      }
    } finally {
      testBundle.dispose();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Use InvocationEvaluatorInterceptor to rewrite Exception 
  //
  ////////////////////////////////////////////////////////////////////////////
  public static class Test_InvocationEvaluatorInterceptor_rewriteException
      extends
        InvocationEvaluatorInterceptor {
    @Override
    public Throwable rewriteException(Throwable e) {
      if ("original".equals(e.getMessage())) {
        return new Error("rewrite");
      }
      return null;
    }
  }

  /**
   * Using {@link InvocationEvaluatorInterceptor#rewriteException(Throwable)}.
   */
  public void test_InvocationEvaluatorInterceptor_rewriteException() throws Exception {
    setFileContentSrc(
        "test/MyObject.java",
        getSourceDQ(
            "package test;",
            "public class MyObject {",
            "  public MyObject() {",
            "    throw new Error('original');",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    TestBundle testBundle = new TestBundle();
    try {
      Class<?> interceptorClass = Test_InvocationEvaluatorInterceptor_rewriteException.class;
      testBundle.addClass(interceptorClass);
      testBundle.addExtension(
          "org.eclipse.wb.core.invocationEvaluatorInterceptors",
          "<interceptor class='" + interceptorClass.getName() + "'/>");
      testBundle.install();
      try {
        evaluateExpression("new MyObject()", "Object");
        fail();
      } catch (Throwable e) {
        e = DesignerExceptionUtils.getRootCause(e);
        assertEquals("rewrite", e.getMessage());
      } finally {
        testBundle.uninstall();
      }
    } finally {
      testBundle.dispose();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ClassInstanceCreation
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_creation_1() throws Exception {
    assertEquals(0, evaluateExpression("new java.util.ArrayList().size()", "int"));
  }

  public void test_creation_2() throws Exception {
    assertEquals(0, evaluateExpression("new java.util.ArrayList(5).size()", "int"));
  }

  public void test_creation_invalidArguments() throws Exception {
    try {
      m_ignoreModelCompileProblems = true;
      evaluateExpression("new java.util.ArrayList(-3)", "java.lang.Object");
      fail();
    } catch (Throwable e_) {
      DesignerException e = DesignerExceptionUtils.getDesignerException(e_);
      assertEquals(ICoreExceptionConstants.EVAL_CONSTRUCTOR, e.getCode());
    }
  }

  public void test_creation_String() throws Exception {
    assertEquals(
        new String(new char[]{'a', 'b', 'c', 'd'}, 1, 2),
        evaluateExpression(
            "new String(new char[]{\'a\', \'b\', \'c\', \'d\'}, 1, 2)",
            "java.lang.String"));
  }

  public void test_creation_innerStatic() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "class Test {",
            "  public Object root() {",
            "    return new Foo();",
            "  }",
            "  public static class Foo {",
            "  }",
            "}");
    waitForAutoBuild();
    Object actual = evaluateSingleMethod(typeDeclaration, "root()");
    assertThat(actual).isNotNull();
  }

  public void test_creation_innerNotStatic() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "class Test {",
            "  public Object root() {",
            "    return new Foo();",
            "  }",
            "  public class Foo {",
            "  }",
            "}");
    waitForAutoBuild();
    Object actual = evaluateSingleMethod(typeDeclaration, "root()");
    assertThat(actual).isNull();
  }

  public void test_creation_varArgsDouble() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration("test", "Test.java", getSource(getDoubleQuotes(new String[]{
            "package test;",
            "public class Test {",
            "  double result = 0;",
            "  public Test(int value, double... parameters) {",
            "    for (double p : parameters) {",
            "      result += p;",
            "    }",
            "  }",
            "  public static Object callMe() {",
            "    return new Test(5, 1.0, 2.0, 3.0);",
            "  }",
            "}"})));
    waitForAutoBuild();
    Object actual = evaluateSingleMethod(typeDeclaration, "callMe()");
    assertEquals(1.0 + 2.0 + 3.0, ReflectionUtils.getFieldObject(actual, "result"));
  }

  public void test_creation_varArgsEmpty() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration("test", "Test.java", getSource(getDoubleQuotes(new String[]{
            "package test;",
            "public class Test {",
            "  String result = '';",
            "  public Test(String... parameters) {",
            "    for (String p : parameters) {",
            "      result += p;",
            "    }",
            "  }",
            "  public static Object callMe() {",
            "    return new Test();",
            "  }",
            "}"})));
    waitForAutoBuild();
    Object actual = evaluateSingleMethod(typeDeclaration, "callMe()");
    assertEquals("", ReflectionUtils.getFieldObject(actual, "result"));
  }

  public void test_creation_genericType() throws Exception {
    setFileContentSrc(
        "test/MyObject.java",
        getSourceDQ(
            "package test;",
            "public class MyObject<E> {",
            "  public MyObject(E e, int s) {",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    String source = getSourceDQ("new MyObject<String>('', 0)");
    Object object = evaluateExpression(source, "Object");
    assertNotNull(object);
  }

  public void test_creation_genericType_array() throws Exception {
    setFileContentSrc(
        "test/MyObject.java",
        getSourceDQ(
            "package test;",
            "public class MyObject<E> {",
            "  public MyObject(E[] e, int s) {",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    String source = getSourceDQ("new MyObject<String>(new String[]{}, 0)");
    Object object = evaluateExpression(source, "Object");
    assertNotNull(object);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation: AnonymousClassDeclaration
  //
  ////////////////////////////////////////////////////////////////////////////
  public static class TestAnonymousInvocationEvaluatorInterceptor
      extends
        InvocationEvaluatorInterceptor {
    @Override
    public Object evaluateAnonymous(EvaluationContext context,
        ClassInstanceCreation expression,
        ITypeBinding typeBinding,
        ITypeBinding typeBindingConcrete,
        IMethodBinding methodBinding,
        Object[] arguments) throws Exception {
      if (AstNodeUtils.getFullyQualifiedName(typeBindingConcrete, false).equals("test.MyClass")) {
        return new Object();
      }
      return AstEvaluationEngine.UNKNOWN;
    }
  }

  /**
   * During evaluation of {@link ClassInstanceCreation} with {@link AnonymousClassDeclaration} we
   * use {@link InvocationEvaluatorInterceptor}.
   */
  public void test_anonymous_InvocationEvaluatorInterceptor_evaluateAnonymous() throws Exception {
    setFileContentSrc(
        "test/MyClass.java",
        getSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "package test;",
            "public class MyClass {",
            "}"));
    waitForAutoBuild();
    //
    TestBundle testBundle = new TestBundle();
    try {
      Class<?> interceptorClass = TestAnonymousInvocationEvaluatorInterceptor.class;
      testBundle.addClass(interceptorClass);
      testBundle.addExtension(
          "org.eclipse.wb.core.invocationEvaluatorInterceptors",
          "<interceptor class='" + interceptorClass.getName() + "'/>");
      testBundle.install();
      // work with Bundle
      TypeDeclaration typeDeclaration =
          createTypeDeclaration(
              "test",
              "Test.java",
              getSource(
                  "// filler filler filler filler filler",
                  "package test;",
                  "public class Test {",
                  "  public Object foo() {",
                  "    return new MyClass() {",
                  "    };",
                  "  }",
                  "}"));
      // evaluate, should be no exception
      Object result = evaluateSingleMethod(typeDeclaration, "foo()");
      assertSame(Object.class, result.getClass());
    } finally {
      testBundle.dispose();
    }
  }

  /**
   * During evaluation of {@link ClassInstanceCreation} with {@link AnonymousClassDeclaration} we
   * use {@link InvocationEvaluatorInterceptor}. "concrete" type should be not abstract.
   */
  public void test_anonymous_InvocationEvaluatorInterceptor_evaluateAnonymousAbstract()
      throws Exception {
    setFileContentSrc(
        "test/MyClass.java",
        getSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "package test;",
            "public class MyClass {",
            "}"));
    setFileContentSrc(
        "test/AbstractSubClass.java",
        getSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "package test;",
            "public abstract class AbstractSubClass extends MyClass {",
            "}"));
    waitForAutoBuild();
    //
    TestBundle testBundle = new TestBundle();
    try {
      Class<?> interceptorClass = TestAnonymousInvocationEvaluatorInterceptor.class;
      testBundle.addClass(interceptorClass);
      testBundle.addExtension(
          "org.eclipse.wb.core.invocationEvaluatorInterceptors",
          "<interceptor class='" + interceptorClass.getName() + "'/>");
      testBundle.install();
      // work with Bundle
      TypeDeclaration typeDeclaration =
          createTypeDeclaration(
              "test",
              "Test.java",
              getSource(
                  "// filler filler filler filler filler",
                  "package test;",
                  "public class Test {",
                  "  public Object foo() {",
                  "    return new AbstractSubClass() {",
                  "    };",
                  "  }",
                  "}"));
      // evaluate, should be no exception
      Object result = evaluateSingleMethod(typeDeclaration, "foo()");
      assertSame(Object.class, result.getClass());
    } finally {
      testBundle.dispose();
    }
  }

  /**
   * We don't evaluate {@link ClassInstanceCreation} with {@link AnonymousClassDeclaration}.
   */
  public void test_anonymous_AnonymousEvaluationError() throws Exception {
    setFileContentSrc(
        "test/MyClass.java",
        getSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "package test;",
            "public class MyClass {",
            "}"));
    waitForAutoBuild();
    //
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "// filler filler filler filler filler",
            "public class Test {",
            "  public Object foo() {",
            "    return new MyClass() {",
            "    };",
            "  }",
            "}");
    try {
      evaluateSingleMethod(typeDeclaration, "foo()");
      fail();
    } catch (Throwable e) {
      assertTrue(AnonymousEvaluationError.is(e));
    }
  }

  /**
   * We still should evaluate anonymous listeners or handlers.
   */
  public void test_anonymous_evaluateListener() throws Exception {
    setFileContentSrc(
        "test/MyListener.java",
        getSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "package test;",
            "public class MyListener {",
            "}"));
    waitForAutoBuild();
    //
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "// filler filler filler filler filler",
            "public class Test {",
            "  public Object foo() {",
            "    return new MyListener() {",
            "    };",
            "  }",
            "}");
    //
    Object o = evaluateSingleMethod(typeDeclaration, "foo()");
    assertTrue(ReflectionUtils.isSuccessorOf(o, "test.MyListener"));
  }

  /**
   * We still should evaluate anonymous listeners or handlers.
   */
  public void test_anonymous_evaluateHandler() throws Exception {
    setFileContentSrc(
        "test/MyHandler.java",
        getSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "package test;",
            "public class MyHandler {",
            "}"));
    waitForAutoBuild();
    //
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "// filler filler filler filler filler",
            "public class Test {",
            "  public Object foo() {",
            "    return new MyHandler() {",
            "    };",
            "  }",
            "}");
    //
    Object o = evaluateSingleMethod(typeDeclaration, "foo()");
    assertTrue(ReflectionUtils.isSuccessorOf(o, "test.MyHandler"));
  }

  /**
   * We still should evaluate anonymous listeners or handlers.
   */
  public void test_anonymous_evaluateListenerAdapter() throws Exception {
    setFileContentSrc(
        "test/MyListener.java",
        getSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "package test;",
            "public interface MyListener {",
            "}"));
    setFileContentSrc(
        "test/MyListenerAdapter.java",
        getSource(
            "// filler filler filler filler filler",
            "package test;",
            "public class MyListenerAdapter implements MyListener {",
            "}"));
    waitForAutoBuild();
    //
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "// filler filler filler filler filler",
            "public class Test {",
            "  public Object foo() {",
            "    return new MyListenerAdapter() {",
            "    };",
            "  }",
            "}");
    //
    Object o = evaluateSingleMethod(typeDeclaration, "foo()");
    assertTrue(ReflectionUtils.isSuccessorOf(o, "test.MyListener"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ClassInstanceCreation
  //
  ////////////////////////////////////////////////////////////////////////////
  public static class TestConstructorInvocationEvaluatorInterceptor
      extends
        InvocationEvaluatorInterceptor {
    @Override
    public Object evaluate(EvaluationContext context,
        ClassInstanceCreation expression,
        ITypeBinding typeBinding,
        Class<?> clazz,
        Constructor<?> actualConstructor,
        Object[] arguments) throws Exception {
      if (AstNodeUtils.isSuccessorOf(typeBinding, "test.MyObject")) {
        return clazz.newInstance();
      }
      return AstEvaluationEngine.UNKNOWN;
    }
  }

  /**
   * Test that during {@link ClassInstanceCreation} evaluation
   * {@link InvocationEvaluatorInterceptor} is used.
   * <p>
   * Default constructor is OK.
   */
  public void test_creation_InvocationEvaluatorInterceptor_goodResult() throws Exception {
    setFileContentSrc(
        "test/MyObject.java",
        getSourceDQ(
            "package test;",
            "public class MyObject {",
            "  public MyObject() {",
            "  }",
            "  public MyObject(int value) {",
            "    throw new IllegalStateException();",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    TestBundle testBundle = new TestBundle();
    try {
      Class<?> interceptorClass = TestConstructorInvocationEvaluatorInterceptor.class;
      testBundle.addClass(interceptorClass);
      testBundle.addExtension(
          "org.eclipse.wb.core.invocationEvaluatorInterceptors",
          "<interceptor class='" + interceptorClass.getName() + "'/>");
      testBundle.install();
      try {
        TypeDeclaration typeDeclaration =
            createTypeDeclaration_Test(
                "public class Test {",
                "  public Object foo() {",
                "    return new MyObject(0);",
                "  }",
                "}");
        // evaluate, should be no exception
        evaluateSingleMethod(typeDeclaration, "foo()");
      } finally {
        testBundle.uninstall();
      }
    } finally {
      testBundle.dispose();
    }
  }

  /**
   * Test that during {@link ClassInstanceCreation} evaluation
   * {@link InvocationEvaluatorInterceptor} is used.
   * <p>
   * Default constructor is throws {@link Exception}.
   */
  public void test_creation_InvocationEvaluatorInterceptor_badResult() throws Exception {
    setFileContentSrc(
        "test/MyObject.java",
        getSourceDQ(
            "package test;",
            "public class MyObject {",
            "  public MyObject() {",
            "    throw new IllegalStateException();",
            "  }",
            "  public MyObject(int value) {",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    TestBundle testBundle = new TestBundle();
    try {
      Class<?> interceptorClass = TestConstructorInvocationEvaluatorInterceptor.class;
      testBundle.addClass(interceptorClass);
      testBundle.addExtension(
          "org.eclipse.wb.core.invocationEvaluatorInterceptors",
          "<interceptor class='" + interceptorClass.getName() + "'/>");
      testBundle.install();
      try {
        TypeDeclaration typeDeclaration =
            createTypeDeclaration_Test(
                "public class Test {",
                "  public Object foo() {",
                "    return new MyObject(0);",
                "  }",
                "}");
        // evaluate, but default constructor (used in interceptor) throws exception
        try {
          evaluateSingleMethod(typeDeclaration, "foo()");
          fail();
        } catch (Throwable e_) {
          Throwable e = DesignerExceptionUtils.getRootCause(e_);
          assertThat(e).isInstanceOf(IllegalStateException.class);
        }
      } finally {
        testBundle.uninstall();
      }
    } finally {
      testBundle.dispose();
    }
  }

  /**
   * If {@link ClassInstanceCreation} uses {@link ThisExpression} and superclass is not compatible
   * with parameter type, we use <code>null</code> as value.
   * <p>
   * Here {@link ThisExpression} value is compatible.
   */
  public void test_creation_useThisExpression_compatible() throws Exception {
    String[] myObjectLines =
        new String[]{
            "package test;",
            "public class MyObject {",
            "  private Object m_field;",
            "  public MyObject(Object p) {",
            "    m_field = p;",
            "  }",
            "}"};
    Object myObject = check_creation_replaceThisExpression_withNull(myObjectLines);
    assertNotSame(null, ReflectionUtils.getFieldObject(myObject, "m_field"));
  }

  /**
   * If {@link ClassInstanceCreation} uses {@link ThisExpression} and superclass is not compatible
   * with parameter type, we use <code>null</code> as value.
   * <p>
   * Here {@link ThisExpression} value is NOT compatible.
   */
  public void test_creation_useThisExpression_notCompatible() throws Exception {
    String[] myObjectLines =
        new String[]{
            "package test;",
            "public class MyObject {",
            "  private Test m_field;",
            "  public MyObject(Test p) {",
            "    m_field = p;",
            "  }",
            "}"};
    Object myObject = check_creation_replaceThisExpression_withNull(myObjectLines);
    assertSame(null, ReflectionUtils.getFieldObject(myObject, "m_field"));
  }

  private Object check_creation_replaceThisExpression_withNull(String[] myObjectLines)
      throws Exception {
    m_ignoreModelCompileProblems = true;
    setFileContentSrc("test/MyObject.java", getSource(myObjectLines));
    setFileContentSrc(
        "test/Test.java",
        getSourceDQ(
            "package test;",
            "public class Test {",
            "  public Object foo() {",
            "    return new MyObject(this);",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    ICompilationUnit testUnit = m_testProject.getCompilationUnit("test.Test");
    TypeDeclaration typeDeclaration =
        (TypeDeclaration) createASTCompilationUnit(testUnit).types().get(0);
    MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
    ReturnStatement returnStatement = (ReturnStatement) m_lastEditor.getEnclosingNode("return ");
    Expression expression = returnStatement.getExpression();
    // prepare context
    EvaluationContext context;
    {
      ClassLoader projectClassLoader =
          CodeUtils.getProjectClassLoader(m_lastEditor.getModelUnit().getJavaProject());
      ExecutionFlowDescription flowDescription = new ExecutionFlowDescription(methodDeclaration);
      context = new EvaluationContext(projectClassLoader, flowDescription) {
        @Override
        public Object evaluate(Expression e) throws Exception {
          if (e instanceof ThisExpression) {
            return new Object();
          }
          return super.evaluate(e);
        }
      };
    }
    // evaluate
    return AstEvaluationEngine.evaluate(context, expression);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // SuperMethodInvocation
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_SuperMethodInvocation() throws Exception {
    setFileContentSrc(
        "test/Base.java",
        getSourceDQ(
            "package test;",
            "public class Base {",
            "  public int getSize() {",
            "    return 5;",
            "  }",
            "}"));
    waitForAutoBuild();
    // validate
    Object evaluate = test_SuperMethodInvocation2();
    assertEquals(5, evaluate);
  }

  public void test_SuperMethodInvocation_withException() throws Exception {
    setFileContentSrc(
        "test/Base.java",
        getSourceDQ(
            "package test;",
            "public class Base {",
            "  public int getSize() {",
            "    throw new NullPointerException();",
            "  }",
            "}"));
    waitForAutoBuild();
    // validate
    try {
      m_ignoreModelCompileProblems = true;
      test_SuperMethodInvocation2();
      fail();
    } catch (Throwable e_) {
      DesignerException e = DesignerExceptionUtils.getDesignerException(e_);
      assertEquals(ICoreExceptionConstants.EVAL_SUPER_METHOD, e.getCode());
    }
  }

  private Object test_SuperMethodInvocation2() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test extends Base {",
            "  public int getSize() {",
            "    return super.getSize();",
            "  }",
            "}");
    MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
    ReturnStatement returnStatement = (ReturnStatement) m_lastEditor.getEnclosingNode("return ");
    Expression expression = returnStatement.getExpression();
    // prepare context
    EvaluationContext context;
    {
      ClassLoader projectClassLoader =
          CodeUtils.getProjectClassLoader(m_lastEditor.getModelUnit().getJavaProject());
      final Object baseInstance = projectClassLoader.loadClass("test.Base").newInstance();
      ExecutionFlowDescription flowDescription = new ExecutionFlowDescription(methodDeclaration);
      context = new EvaluationContext(projectClassLoader, flowDescription) {
        @Override
        public Object evaluate(Expression e) throws Exception {
          if (e == null) {
            return baseInstance;
          }
          return super.evaluate(e);
        }
      };
    }
    // evaluate
    Object evaluate = AstEvaluationEngine.evaluate(context, expression);
    return evaluate;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Project disposing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void test_tearDown() throws Exception {
    //System.exit(0);
    do_projectDispose();
  }
}
