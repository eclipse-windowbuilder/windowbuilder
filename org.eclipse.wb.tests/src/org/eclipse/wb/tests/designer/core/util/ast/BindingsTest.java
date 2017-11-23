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
package org.eclipse.wb.tests.designer.core.util.ast;

import static org.eclipse.wb.internal.core.utils.ast.AstNodeUtils.getFullyQualifiedName;
import static org.eclipse.wb.internal.core.utils.ast.AstNodeUtils.getMethodBinding;
import static org.eclipse.wb.internal.core.utils.ast.AstNodeUtils.getMethodBySignature;
import static org.eclipse.wb.internal.core.utils.ast.AstNodeUtils.getMethodDeclarationSignature;
import static org.eclipse.wb.internal.core.utils.ast.AstNodeUtils.getMethodGenericSignature;
import static org.eclipse.wb.internal.core.utils.ast.AstNodeUtils.getMethodSignature;
import static org.eclipse.wb.internal.core.utils.ast.AstNodeUtils.getTypeBinding;

import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.binding.BindingContext;
import org.eclipse.wb.internal.core.utils.ast.binding.DesignerMethodBinding;
import org.eclipse.wb.internal.core.utils.ast.binding.DesignerTypeBinding;
import org.eclipse.wb.tests.designer.core.AbstractJavaTest;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang.ArrayUtils;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Test for Designer*Binding - our lightweight implementations of bindings.
 * 
 * @author scheglov_ke
 */
public class BindingsTest extends AbstractJavaTest {
  private static final Object NULL_ARG[] = {null};

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
  // Exit zone :-) XXX
  //
  ////////////////////////////////////////////////////////////////////////////
  public void _test_exit() throws Exception {
    System.exit(0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // DesignerTypeBinding
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Object type.
   */
  public void test_DesignerTypeBinding_1() throws Exception {
    String code = "private java.util.List foo() {return null;}";
    check_DesignerTypeBinding(code);
  }

  /**
   * Primitive type.
   */
  public void test_DesignerTypeBinding_2() throws Exception {
    String code = "private int foo() {return 0;}";
    check_DesignerTypeBinding(code);
  }

  /**
   * Array type.
   */
  public void test_DesignerTypeBinding_3() throws Exception {
    String code = "private int[] foo() {return null;}";
    check_DesignerTypeBinding(code);
  }

  /**
   * Inner type.
   */
  public void test_DesignerTypeBinding_4() throws Exception {
    String code = "class Foo {} private Foo foo() {return null;}";
    check_DesignerTypeBinding(code);
  }

  /**
   * Our copy of {@link ITypeBinding} should implement {@link ITypeBinding#getDeclaredMethods()}.
   */
  public void test_DesignerTypeBinding_getDeclaredMethods() throws Exception {
    createTypeDeclaration_TestC(getSourceDQ(
        "  // filler filler filler filler filler",
        "  private java.util.List foo() {",
        "    return null;",
        "  }"));
    // prepare ITypeBinding copy
    ITypeBinding sourceBinding = getNode("java.util.List", Type.class).resolveBinding();
    ITypeBinding ourBinding = m_lastEditor.getBindingContext().get(sourceBinding);
    // has expected name
    assertEquals("java.util.List", getFullyQualifiedName(ourBinding, false));
    // has expected method
    assertNotNull(getMethodBySignature(ourBinding, "get(int)"));
  }

  /**
   * Our copy of {@link ITypeBinding} should implement {@link ITypeBinding#getTypeBounds()}.
   */
  public void test_DesignerTypeBinding_getTypeBounds() throws Exception {
    createTypeDeclaration_TestC(getSourceDQ(
        "  // filler filler filler filler filler",
        "  private <T extends java.util.List> T foo() {",
        "    return null;",
        "  }"));
    // prepare ITypeBinding copy
    ITypeBinding sourceBinding = getNode("T foo()", Type.class).resolveBinding();
    ITypeBinding ourBinding = m_lastEditor.getBindingContext().get(sourceBinding);
    // has expected name
    assertEquals("T", getFullyQualifiedName(ourBinding, false));
    // has expected type bounds
    assertEquals("java.util.List", getFullyQualifiedName(ourBinding.getTypeBounds()[0], false));
  }

  /**
   * Our copy of {@link ITypeBinding} should implement {@link ITypeBinding#getTypeArguments()},
   * {@link ITypeBinding#getTypeDeclaration()} and {@link ITypeBinding#getTypeParameters()}.
   */
  public void test_DesignerTypeBinding_getTypeDeclaration() throws Exception {
    createTypeDeclaration_TestC(getSourceDQ(
        "  // filler filler filler filler filler",
        "  // filler filler filler filler filler",
        "  private void foo() {",
        "    new java.util.ArrayList<Double>();",
        "  }"));
    // prepare ITypeBinding copy
    ITypeBinding ourBinding;
    {
      ClassInstanceCreation node = getNode("new java.util.ArrayList", ClassInstanceCreation.class);
      ITypeBinding sourceBinding = node.resolveTypeBinding();
      ourBinding = m_lastEditor.getBindingContext().get(sourceBinding);
    }
    // check "concrete" ITypeBinding
    {
      assertEquals("java.util.ArrayList", getFullyQualifiedName(ourBinding, false));
      assertFalse(ourBinding.isGenericType());
      // type arguments
      ITypeBinding[] typeArguments = ourBinding.getTypeArguments();
      assertThat(typeArguments).hasSize(1);
      // single "type argument"
      {
        ITypeBinding typeArgument = typeArguments[0];
        assertEquals("java.lang.Double", getFullyQualifiedName(typeArgument, false));
      }
    }
    // check "declaration" ITypeBinding
    {
      ITypeBinding declarationBinding = ourBinding.getTypeDeclaration();
      assertEquals("java.util.ArrayList", getFullyQualifiedName(declarationBinding, false));
      assertTrue(declarationBinding.isGenericType());
      // type parameters
      ITypeBinding[] typeParameters = declarationBinding.getTypeParameters();
      assertThat(typeParameters).hasSize(1);
      // single "type parameter"
      {
        ITypeBinding typeParameter = typeParameters[0];
        assertTrue(typeParameter.isTypeVariable());
        assertEquals("E", getFullyQualifiedName(typeParameter, false));
        assertEquals(
            "java.lang.Object",
            getFullyQualifiedName(typeParameter.getTypeBounds()[0], false));
      }
    }
  }

  /**
   * Checks {@link ITypeBinding} of first {@link MethodDeclaration} in given source.
   */
  private void check_DesignerTypeBinding(String code) throws Exception {
    TypeDeclaration typeDeclaration = createTypeDeclaration_TestC(code);
    ITypeBinding originalBinding = typeDeclaration.getMethods()[0].resolveBinding().getReturnType();
    ITypeBinding ourBinding = m_lastEditor.getBindingContext().get(originalBinding);
    assert_sameTypeBindings(originalBinding, ourBinding);
    assert_methodFails(ourBinding, "isEqualTo", NULL_ARG);
    assert_methodFails(ourBinding, "isAssignmentCompatible", NULL_ARG);
    assert_methodFails(ourBinding, "isCastCompatible", NULL_ARG);
    assert_methodFails(ourBinding, "isSubTypeCompatible", NULL_ARG);
    assert_methodFails(ourBinding, "createArrayType", new Object[]{0});
  }

  /**
   * Check different {@link DesignerTypeBinding} in {@link BindingContext} for anonymous classes.
   */
  public void test_DesignerTypeBinding_anonymous() throws Exception {
    createTypeDeclaration_TestC(getSourceDQ(
        "  // filler filler filler filler filler",
        "  private Object field_1 = new Object() { // marker_1",
        "  };",
        "  private Object field_2 = new Object() { // marker_2",
        "  };"));
    // prepare ITypeBinding originals
    ITypeBinding binding_1 = getNode("marker_1", ClassInstanceCreation.class).resolveTypeBinding();
    ITypeBinding binding_2 = getNode("marker_2", ClassInstanceCreation.class).resolveTypeBinding();
    // prepare ITypeBinding copies
    BindingContext context = new BindingContext();
    ITypeBinding copy_1 = context.get(binding_1);
    ITypeBinding copy_2 = context.get(binding_2);
    // check
    assertThat(copy_1).isNotSameAs(copy_2);
  }

  /**
   * Check {@link DesignerTypeBinding} (arguments & etc.) in {@link BindingContext} for generic
   * instance classes.
   */
  public void test_DesignerTypeBinding_generics() throws Exception {
    createModelType(
        "test",
        "G.java",
        getSourceDQ(
            "package test;",
            "public class G<N> {",
            "  private N value;",
            "  public G(N value){",
            "    this.value = value;",
            "  }",
            "}"));
    waitForAutoBuild();
    createTypeDeclaration_TestC(getSourceDQ(
        "  // filler filler filler filler filler",
        "  private G field_0 = new/*marker_0*/ G(new Long(1));",
        "  private G field_1 = new/*marker_1*/ G<java.lang.Double>(1.5);",
        "  private G field_2 = new/*marker_2*/ G<java.lang.Integer>(1);",
        "  private G field_3 = new/*marker_3*/ G<java.util.List<java.lang.String>>(null);"));
    // prepare ITypeBinding originals
    ITypeBinding binding_0 = getNode("marker_0", ClassInstanceCreation.class).resolveTypeBinding();
    ITypeBinding binding_1 = getNode("marker_1", ClassInstanceCreation.class).resolveTypeBinding();
    ITypeBinding binding_2 = getNode("marker_2", ClassInstanceCreation.class).resolveTypeBinding();
    ITypeBinding binding_3 = getNode("marker_3", ClassInstanceCreation.class).resolveTypeBinding();
    // prepare ITypeBinding copies
    BindingContext context = new BindingContext();
    ITypeBinding copy_0 = context.get(binding_0, true);
    ITypeBinding copy_1 = context.get(binding_1, true);
    ITypeBinding copy_2 = context.get(binding_2, true);
    ITypeBinding copy_3 = context.get(binding_3, true);
    // check bindings
    assertThat(copy_1).isNotSameAs(copy_0);
    assertThat(copy_2).isNotSameAs(copy_0);
    assertThat(copy_2).isNotSameAs(copy_1);
    assert_TypeBinding_names(copy_0, "test.G", "test.G");
    assert_TypeBinding_names(copy_1, "test.G", "test.G<java.lang.Double>");
    assert_TypeBinding_names(copy_2, "test.G", "test.G<java.lang.Integer>");
    assert_TypeBinding_names(copy_3, "test.G", "test.G<java.util.List<java.lang.String>>");
  }

  private static void assert_TypeBinding_names(ITypeBinding binding,
      String expectedRaw,
      String expectedGeneric) {
    String rawName = AstNodeUtils.getFullyQualifiedName(binding, false, false);
    String genericName = AstNodeUtils.getFullyQualifiedName(binding, false, true);
    assertEquals(expectedRaw, rawName);
    assertEquals(expectedGeneric, genericName);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // DesignerPackageBinding
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_DesignerPackageBinding() throws Exception {
    TypeDeclaration typeDeclaration = createTypeDeclaration_TestC("");
    IPackageBinding originalBinding = typeDeclaration.resolveBinding().getPackage();
    IPackageBinding ourBinding = m_lastEditor.getBindingContext().get(originalBinding);
    assert_sameProperties(IPackageBinding.class, originalBinding, ourBinding, new String[]{
        "getName",
        "isUnnamed"});
    assert_methodFails(ourBinding, "isEqualTo", NULL_ARG);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // DesignerMethodBinding
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Basic test for {@link DesignerMethodBinding}.
   */
  public void test_DesignerMethodBinding_1() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_TestC(getSourceDQ("  private int foo() {", "    return 0;", "  }"));
    MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
    IMethodBinding originalBinding = methodDeclaration.resolveBinding();
    IMethodBinding ourBinding = m_lastEditor.getBindingContext().get(originalBinding);
    assert_sameProperties(IMethodBinding.class, originalBinding, ourBinding, new String[]{
        "getDeclaringClass",
        "getName",
        "getReturnType",
        "getParameterTypes",
        "getExceptionTypes",
        "getMethodDeclaration",
        "isConstructor",
        "getModifiers",
        "isVarargs"});
    assert_methodFails(ourBinding, "getParameterAnnotations", new Object[]{0});
    assert_methodFails(ourBinding, "isSubsignature", NULL_ARG);
    assert_methodFails(ourBinding, "overrides", NULL_ARG);
    assert_methodFails(ourBinding, "isEqualTo", NULL_ARG);
  }

  /**
   * Basic test for {@link DesignerMethodBinding#getMethodDeclaration()}.
   */
  public void test_DesignerMethodBinding_getMethodDeclaration() throws Exception {
    createTypeDeclaration_TestC(getSourceDQ(
        "// filler filler filler filler filler",
        "  private void root() {",
        "    System.currentTimeMillis();",
        "    java.util.Arrays.asList('a', 'b', 'c');",
        "  }"));
    // not generic, same "declaration"
    {
      MethodInvocation invocation = getNode("System.", MethodInvocation.class);
      IMethodBinding originalBinding = invocation.resolveMethodBinding();
      IMethodBinding ourBinding = m_lastEditor.getBindingContext().get(originalBinding);
      //
      IMethodBinding declarationBinding = ourBinding.getMethodDeclaration();
      assertSame(ourBinding, declarationBinding);
    }
    // generic, special "declaration"
    {
      MethodInvocation invocation = getNode("Arrays.", MethodInvocation.class);
      IMethodBinding originalBinding = invocation.resolveMethodBinding();
      IMethodBinding ourBinding = m_lastEditor.getBindingContext().get(originalBinding);
      //
      IMethodBinding declarationBinding = ourBinding.getMethodDeclaration();
      assertNotSame(ourBinding, declarationBinding);
      assertEquals("asList(T[])", getMethodSignature(declarationBinding));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // DesignerMethodBinding#removeParameterType(int)
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link IMethodBinding} with parameters and
   * {@link DesignerMethodBinding#removeParameterType(int)}.
   */
  public void test_DesignerMethodBinding_removeParameterType() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_TestC(getSourceDQ(
            "  private int foo(int a, float b, double c) {",
            "    return 0;",
            "  }"));
    MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
    IMethodBinding originalBinding = methodDeclaration.resolveBinding();
    DesignerMethodBinding ourBinding = m_lastEditor.getBindingContext().get(originalBinding);
    //
    assertEquals("foo(int,float,double)", getMethodSignature(ourBinding));
    ourBinding.removeParameterType(1);
    assertEquals("foo(int,double)", getMethodSignature(ourBinding));
  }

  /**
   * Test for {@link DesignerMethodBinding#removeParameterType(int)}.
   * <p>
   * When we remove parameter from generic {@link IMethodBinding} we should also update its
   * {@link IMethodBinding#getMethodDeclaration()}.
   */
  public void test_DesignerMethodBinding_removeParameterType_whenGenerics() throws Exception {
    createTypeDeclaration_TestC(getSourceDQ(
        "  // filler filler filler filler filler",
        "  public <T extends java.util.List> void foo(int a, T b, double c) {",
        "  }",
        "  void bar() {",
        "    foo(0, new java.util.ArrayList(), 0.0);",
        "  }"));
    MethodInvocation invocation = getNode("foo(0", MethodInvocation.class);
    IMethodBinding originalBinding = getMethodBinding(invocation);
    DesignerMethodBinding ourBinding = m_lastEditor.getBindingContext().get(originalBinding);
    // original signatures
    assertEquals("foo(int,java.util.ArrayList,double)", getMethodSignature(ourBinding));
    assertEquals("foo(int,T,double)", getMethodGenericSignature(ourBinding));
    assertEquals("foo(int,java.util.List,double)", getMethodDeclarationSignature(ourBinding));
    // do remove
    ourBinding.removeParameterType(1);
    assertEquals("foo(int,double)", getMethodSignature(ourBinding));
    assertEquals("foo(int,double)", getMethodGenericSignature(ourBinding));
    assertEquals("foo(int,double)", getMethodDeclarationSignature(ourBinding));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // DesignerVariableBinding
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Basic test for {@link DesignerVariableBinding}.
   */
  public void test_DesignerVariableBinding_1() throws Exception {
    TypeDeclaration typeDeclaration = createTypeDeclaration_TestC("private int m_value;");
    FieldDeclaration fieldDeclaration = typeDeclaration.getFields()[0];
    IVariableBinding originalBinding =
        ((VariableDeclarationFragment) fieldDeclaration.fragments().get(0)).resolveBinding();
    IVariableBinding ourBinding = m_lastEditor.getBindingContext().get(originalBinding);
    assert_sameProperties(IVariableBinding.class, originalBinding, ourBinding, new String[]{
        "getName",
        "getDeclaringClass",
        "getType",
        "isField",
        "getModifiers"});
    assert_methodFails(ourBinding, "isEqualTo", NULL_ARG);
  }

  /**
   * Test for "manual" constructor of {@link DesignerVariableBinding}.
   */
  public void test_DesignerVariableBinding_2() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_TestC("private java.util.List m_value;");
    ITypeBinding typeBinding = typeDeclaration.resolveBinding();
    FieldDeclaration fieldDeclaration = typeDeclaration.getFields()[0];
    ITypeBinding fieldTypeBinding = fieldDeclaration.getType().resolveBinding();
    //
    IVariableBinding ourVariable =
        m_lastEditor.getBindingContext().get(
            "myField",
            typeBinding,
            fieldTypeBinding,
            true,
            Modifier.PUBLIC);
    assertEquals("myField", ourVariable.getName());
    assertEquals(
        getFullyQualifiedName(fieldTypeBinding, false),
        getFullyQualifiedName(ourVariable.getType(), false));
    assertEquals(
        getFullyQualifiedName(typeBinding, false),
        getFullyQualifiedName(ourVariable.getDeclaringClass(), false));
    assertEquals(Modifier.PUBLIC, ourVariable.getModifiers());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getCopy()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link BindingContext#getCopy(ITypeBinding)}.
   */
  public void test_getCopy() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test extends javax.swing.JFrame {",
            "  public Test() {",
            "  }",
            "}");
    ITypeBinding typeBinding = getTypeBinding(typeDeclaration);
    ITypeBinding typeBinding2 = m_lastEditor.getBindingContext().getCopy(typeBinding);
    // new ITypeBinding
    assertNotSame(typeBinding, typeBinding2);
    // ...but with same properties
    assert_sameTypeBindings(typeBinding, typeBinding2);
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

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Asserts that two {@link ITypeBinding}'s have same values for properties.
   */
  private static void assert_sameTypeBindings(ITypeBinding expectedBinding,
      ITypeBinding actualBinding) throws Exception {
    assert_sameProperties(ITypeBinding.class, expectedBinding, actualBinding, new String[]{
        "isPrimitive",
        "isNullType",
        "isArray",
        "getElementType",
        "getDimensions",
        "isClass",
        "isInterface",
        "isEnum",
        "getName",
        "getKey",
        "getPackage",
        "getDeclaringClass",
        "getSuperclass",
        "getInterfaces",
        "isTopLevel",
        "isNested",
        "isMember",
        "isLocal",
        "isAnonymous",
        "isGenericType",
        "isParameterizedType",
        "isTypeVariable",
        "getTypeArguments",
        "getTypeDeclaration",
        "getTypeParameters",
        "getTypeBounds",
        "getDeclaredMethods",
        "getModifiers",
        "getDeclaredModifiers"});
  }

  /**
   * Checks that method with given name and arguments fails.
   */
  private static void assert_methodFails(Object actualObject, String methodName, Object... args)
      throws Exception {
    Method[] allMethods = actualObject.getClass().getMethods();
    for (Method method : allMethods) {
      method.setAccessible(true);
      if (method.getName().equals(methodName)) {
        try {
          method.invoke(actualObject, args);
          fail("Method " + method + " should throw exception.");
        } catch (InvocationTargetException e) {
          assertInstanceOf(IllegalArgumentException.class, e.getCause());
        }
      }
    }
  }

  /**
   * Checks that methods with given names returns same values and other (without parameters) methods
   * fail.
   */
  private static void assert_sameProperties(Class<?> clazz,
      Object expectedObject,
      Object actualObject,
      String methodNames[]) throws Exception {
    Method[] allMethods = clazz.getMethods();
    for (int i = 0; i < allMethods.length; i++) {
      Method method = allMethods[i];
      if (ArrayUtils.indexOf(methodNames, method.getName()) != -1) {
        Object expectedValue = method.invoke(expectedObject);
        Object actualValue = method.invoke(actualObject);
        assert_equals(method, expectedValue, actualValue);
      } else if (method.getParameterTypes().length == 0) {
        assert_methodFails(actualObject, method.getName());
      }
    }
  }

  private static void assert_equals(Method method, Object expectedValue, Object actualValue) {
    String message = "For method " + method;
    if (expectedValue == null) {
      assertNull(message, actualValue);
    } else if (expectedValue.getClass().isArray()) {
      assertTrue(message, actualValue.getClass().isArray());
      int length = Array.getLength(expectedValue);
      for (int i = 0; i < length; i++) {
        Object expectedElement = Array.get(expectedValue, i);
        Object actualElement = Array.get(actualValue, i);
        assert_equals(method, expectedElement, actualElement);
      }
    } else if (expectedValue instanceof IMethodBinding) {
      assertEquals(
          getMethodSignature((IMethodBinding) expectedValue),
          getMethodSignature((IMethodBinding) actualValue));
    } else if (expectedValue instanceof ITypeBinding) {
      assertEquals(
          getFullyQualifiedName((ITypeBinding) expectedValue, false),
          getFullyQualifiedName((ITypeBinding) actualValue, false));
    } else if (expectedValue instanceof IPackageBinding) {
      IPackageBinding expectedPackage = (IPackageBinding) expectedValue;
      IPackageBinding actualPackage = (IPackageBinding) actualValue;
      assertEquals(message, expectedPackage.getName(), actualPackage.getName());
      assertEquals(message, expectedPackage.isUnnamed(), actualPackage.isUnnamed());
    } else {
      assertEquals(message, expectedValue, actualValue);
    }
  }
}
