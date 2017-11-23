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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import static org.eclipse.wb.internal.core.utils.ast.AstNodeUtils.getMethodDeclarationSignature;
import static org.eclipse.wb.internal.core.utils.ast.AstNodeUtils.getMethodGenericSignature;
import static org.eclipse.wb.internal.core.utils.ast.AstNodeUtils.getMethodSignature;

import org.eclipse.wb.internal.core.utils.ast.AnonymousTypeDeclaration;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.binding.BindingContext;
import org.eclipse.wb.internal.core.utils.check.AssertionFailedException;
import org.eclipse.wb.tests.designer.core.AbstractJavaTest;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang.ArrayUtils;
import org.assertj.core.api.Assertions;

import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Test for {@link AstNodeUtils}.
 *
 * @author scheglov_ke
 */
public class AstNodeUtilsTest extends AbstractJavaTest {
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
  // getFullyQualifiedName()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * For <code>null</code> binding {@link AstNodeUtils#NO_TYPE_BINDING_NAME} returned.
   */
  public void test_getFullyQualifiedName_null() {
    String qualifiedName = AstNodeUtils.getFullyQualifiedName((ITypeBinding) null, false);
    assertSame(AstNodeUtils.NO_TYPE_BINDING_NAME, qualifiedName);
  }

  public void test_getFullyQualifiedName_type_expression() throws Exception {
    TypeDeclaration typeDeclaration;
    {
      String code = "private String m_string = \"12345\";";
      typeDeclaration = createTypeDeclaration_TestC(code);
    }
    {
      FieldDeclaration field = typeDeclaration.getFields()[0];
      assertEquals("java.lang.String", AstNodeUtils.getFullyQualifiedName(field.getType(), false));
      VariableDeclarationFragment vdf = (VariableDeclarationFragment) field.fragments().get(0);
      assertEquals(
          "java.lang.String",
          AstNodeUtils.getFullyQualifiedName(vdf.getInitializer(), false));
    }
  }

  public void test_getFullyQualifiedName() throws Exception {
    TypeDeclaration typeDeclaration;
    {
      String code =
          "private int m_int;"
              + "private int[] m_int_array;"
              + "private String m_string;"
              + "private String[] m_array_1;"
              + "private String[][] m_array_2;"
              + "private Inner m_inner;"
              + "private Inner[] m_inner_array;"
              + "private Class m_class = getClass();"
              + "private class Inner{}";
      typeDeclaration = createTypeDeclaration_TestC(code);
    }
    //
    check_FieldDeclarationQualifiedNames(typeDeclaration, 0, "int", "int");
    check_FieldDeclarationQualifiedNames(typeDeclaration, 1, "int[]", "int[]");
    check_FieldDeclarationQualifiedNames(typeDeclaration, 2, "java.lang.String", "java.lang.String");
    check_FieldDeclarationQualifiedNames(
        typeDeclaration,
        3,
        "java.lang.String[]",
        "java.lang.String[]");
    check_FieldDeclarationQualifiedNames(
        typeDeclaration,
        4,
        "java.lang.String[][]",
        "java.lang.String[][]");
    check_FieldDeclarationQualifiedNames(typeDeclaration, 5, "test.Test.Inner", "test.Test$Inner");
    check_FieldDeclarationQualifiedNames(
        typeDeclaration,
        6,
        "test.Test.Inner[]",
        "test.Test$Inner[]");
    check_FieldDeclarationQualifiedNames(typeDeclaration, 7, "java.lang.Class", "java.lang.Class");
    // check for java.lang.Class (it is generic)
    {
      FieldDeclaration field = typeDeclaration.getFields()[7];
      VariableDeclaration variableDeclaration = (VariableDeclaration) field.fragments().get(0);
      assertEquals(
          "java.lang.Class",
          AstNodeUtils.getFullyQualifiedName(variableDeclaration.getInitializer(), false));
    }
  }

  /**
   * Test for {@link AstNodeUtils#getFullyQualifiedName(Expression, boolean)} for
   * {@link AnonymousClassDeclaration}.
   */
  public void test_getFullyQualifiedName_anon() throws Exception {
    TypeDeclaration type_0 =
        createTypeDeclaration_Test(
            "public class Test {",
            "  Object o_1 = new Object() {",
            "  };",
            "  Object o_2 = new Object() {",
            "  };",
            "  Object o_3 = new Object() {",
            "    Object o_4 = new Object() {;",
            "    };",
            "    Object o_5 = new Object() {;",
            "      Object o_6 = new Object() {;",
            "      };",
            "    };",
            "    Object o_7 = new Object() {;",
            "      Object o_8 = new Object() {;",
            "      };",
            "    };",
            "  };",
            "}");
    {
      FieldDeclaration field_1 = type_0.getFields()[0];
      assert_getFullyQualifiedName_anon("test.Test$1", field_1);
    }
    {
      FieldDeclaration field_2 = type_0.getFields()[1];
      assert_getFullyQualifiedName_anon("test.Test$2", field_2);
    }
    {
      FieldDeclaration field_3 = type_0.getFields()[2];
      assert_getFullyQualifiedName_anon("test.Test$3", field_3);
      {
        TypeDeclaration type_3 =
            AnonymousTypeDeclaration.create(((ClassInstanceCreation) DomGenerics.fragments(
                field_3).get(0).getInitializer()).getAnonymousClassDeclaration());
        {
          FieldDeclaration field_4 = type_3.getFields()[0];
          assert_getFullyQualifiedName_anon("test.Test$3$1", field_4);
        }
        {
          FieldDeclaration field_5 = type_3.getFields()[1];
          assert_getFullyQualifiedName_anon("test.Test$3$2", field_5);
          {
            TypeDeclaration type_5 =
                AnonymousTypeDeclaration.create(((ClassInstanceCreation) DomGenerics.fragments(
                    field_5).get(0).getInitializer()).getAnonymousClassDeclaration());
            {
              FieldDeclaration field_6 = type_5.getFields()[0];
              assert_getFullyQualifiedName_anon("test.Test$3$2$1", field_6);
            }
          }
        }
        {
          FieldDeclaration field_7 = type_3.getFields()[2];
          assert_getFullyQualifiedName_anon("test.Test$3$3", field_7);
          {
            TypeDeclaration type_7 =
                AnonymousTypeDeclaration.create(((ClassInstanceCreation) DomGenerics.fragments(
                    field_7).get(0).getInitializer()).getAnonymousClassDeclaration());
            {
              FieldDeclaration field_8 = type_7.getFields()[0];
              assert_getFullyQualifiedName_anon("test.Test$3$3$1", field_8);
            }
          }
        }
      }
    }
  }

  /**
   * Asserts that given {@link FieldDeclaration} with single fragment has expected fully qualified
   * name for initializer.
   */
  private static void assert_getFullyQualifiedName_anon(String expectedName, FieldDeclaration field) {
    Expression initializer = DomGenerics.fragments(field).get(0).getInitializer();
    assertEquals(expectedName, AstNodeUtils.getFullyQualifiedName(initializer, true));
  }

  /**
   * Test for {@link AstNodeUtils#getFullyQualifiedName(Expression, boolean)} for
   * {@link ITypeBinding} which represents {@link TypeVariable}.
   */
  public void test_getFullyQualifiedName_generic_TypeVariable() throws Exception {
    setFileContentSrc(
        "test/MyObject.java",
        getSourceDQ(
            "package test;",
            "public class MyObject<E> {",
            "  public MyObject(E[] elements) {",
            "  }",
            "}"));
    createTypeDeclaration_Test(
        "public class Test {",
        "  public Test() {",
        "    new MyObject<String>(new String[]{'a', 'b', 'c'});",
        "  }",
        "}");
    ClassInstanceCreation creation = getNode("new MyObject");
    // "actual" binding
    IMethodBinding binding = AstNodeUtils.getCreationBinding(creation);
    {
      ITypeBinding parameterType = binding.getParameterTypes()[0];
      assertEquals("java.lang.String[]", AstNodeUtils.getFullyQualifiedName(parameterType, false));
    }
    // "generic" or "declaration" binding
    {
      IMethodBinding genericBinding = binding.getMethodDeclaration();
      ITypeBinding parameterType = genericBinding.getParameterTypes()[0];
      assertEquals("E[]", AstNodeUtils.getFullyQualifiedName(parameterType, false));
    }
  }

  /**
   * Test for {@link AstNodeUtils#getFullyQualifiedName(ITypeBinding, boolean, boolean)} for generic
   * instance classes.
   */
  public void test_getFullyQualifiedName_generic() throws Exception {
    createModelType(
        "test",
        "G.java",
        getSourceDQ(
            "package test;",
            "public class G<N extends java.lang.Number> {",
            "  private N value;",
            "  public G(N value){",
            "    this.value = value;",
            "  }",
            "}"));
    waitForAutoBuild();
    createTypeDeclaration_TestC(getSourceDQ(
        "  // filler filler filler filler filler",
        "  private G field_1 = new/*marker_1*/ G(new Long(1));",
        "  private G field_2 = new/*marker_2*/ G<java.lang.Double>(1.5);",
        "  private G field_3 = new/*marker_3*/ G<Integer>(2);"));
    // prepare ITypeBinding originals
    ITypeBinding binding_1 = getNode("marker_1", ClassInstanceCreation.class).resolveTypeBinding();
    ITypeBinding binding_2 = getNode("marker_2", ClassInstanceCreation.class).resolveTypeBinding();
    ITypeBinding binding_3 = getNode("marker_3", ClassInstanceCreation.class).resolveTypeBinding();
    assertThat(binding_1).isNotSameAs(binding_2);
    // check base class names
    {
      String name_1 = AstNodeUtils.getFullyQualifiedName(binding_1, false);
      String name_2 = AstNodeUtils.getFullyQualifiedName(binding_2, false);
      assertThat(name_1).isEqualTo("test.G");
      assertThat(name_2).isEqualTo("test.G");
      assertThat(name_1).isEqualTo(name_2);
    }
    // check class names with generics
    {
      String name_1 = AstNodeUtils.getFullyQualifiedName(binding_1, false, true);
      String name_2 = AstNodeUtils.getFullyQualifiedName(binding_2, false, true);
      String name_3 = AstNodeUtils.getFullyQualifiedName(binding_3, false, true);
      assertThat(name_1).isEqualTo("test.G");
      assertThat(name_2).isEqualTo("test.G<java.lang.Double>");
      assertThat(name_3).isEqualTo("test.G<java.lang.Integer>");
      assertThat(name_1).isNotEqualTo(name_2);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // TypeBinding
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_typeBindings_defaultPackage() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration(
            "",
            "Test.java",
            getSourceDQ(
                "public class Test {",
                "  private Inner m_inner;",
                "  private class Inner {",
                "  }",
                "}"));
    check_FieldDeclarationQualifiedNames(typeDeclaration, 0, "Test.Inner", "Test$Inner");
  }

  public void test_getTypeBinding() throws Exception {
    TypeDeclaration typeDeclaration;
    {
      String code = "private String m_string = \"123\";";
      typeDeclaration = createTypeDeclaration_TestC(code);
    }
    //
    FieldDeclaration fieldDeclaration = typeDeclaration.getFields()[0];
    VariableDeclaration variableDeclaration =
        (VariableDeclaration) fieldDeclaration.fragments().get(0);
    ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(variableDeclaration.getInitializer());
    assertEquals("java.lang.String", AstNodeUtils.getFullyQualifiedName(typeBinding, false));
  }

  /**
   * Test for {@link AstNodeUtils#getTypeByQualifiedName(CompilationUnit, String)}.
   */
  public void test_getTypeByQualifiedName() throws Exception {
    CompilationUnit compilationUnit =
        createASTCompilationUnit(
            "test",
            "Test.java",
            getSourceDQ(
                "package test;",
                "public class Test {",
                "  Object o_1 = new Object() {",
                "  };",
                "  Object o_2 = new Object() {",
                "  };",
                "}"));
    {
      TypeDeclaration typeDeclaration =
          AstNodeUtils.getTypeByQualifiedName(compilationUnit, "test.Test");
      assertSame(DomGenerics.types(compilationUnit).get(0), typeDeclaration);
    }
    {
      TypeDeclaration typeDeclaration =
          AstNodeUtils.getTypeByQualifiedName(compilationUnit, "test.Test$1");
      AnonymousClassDeclaration acd = (AnonymousClassDeclaration) typeDeclaration.getParent();
      ClassInstanceCreation creation = (ClassInstanceCreation) acd.getParent();
      VariableDeclaration variableDeclaration = (VariableDeclaration) creation.getParent();
      assertEquals("o_1", variableDeclaration.getName().getIdentifier());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getTypeBinding(Type)
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstNodeUtils#getTypeBinding(Type)}.
   */
  public void test_getTypeBinding_Type_goodType() throws Exception {
    createASTCompilationUnit(
        "test",
        "Test.java",
        getSourceDQ(
            "// filler filler filler filler filler",
            "package test;",
            "public class Test {",
            "  String str;",
            "}"));
    Type type = (Type) m_lastEditor.getEnclosingNode("String ").getParent();
    //
    ITypeBinding binding = AstNodeUtils.getTypeBinding(type);
    assertNotNull(binding);
    assertEquals("java.lang", binding.getPackage().getName());
    assertEquals("String", binding.getName());
  }

  /**
   * Test for {@link AstNodeUtils#getTypeBinding(Type)}.
   */
  public void test_getTypeBinding_Type_badType() throws Exception {
    m_ignoreModelCompileProblems = true;
    createASTCompilationUnit(
        "test",
        "Test.java",
        getSourceDQ(
            "// filler filler filler filler filler",
            "package test;",
            "public class Test {",
            "  Foo foo;",
            "}"));
    Type type = (Type) m_lastEditor.getEnclosingNode("Foo ").getParent();
    // unknown type, so no binding
    ITypeBinding binding = AstNodeUtils.getTypeBinding(type);
    assertNull(binding);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getTypeBinding(TypeDeclaration)
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstNodeUtils#getTypeBinding(TypeDeclaration)}.
   */
  public void test_getTypeBinding_TypeDeclaration_goodType() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test extends javax.swing.JPanel {",
            "  // filler",
            "}");
    //
    ITypeBinding binding = AstNodeUtils.getTypeBinding(typeDeclaration);
    assertNotNull(binding);
    assertEquals("test", binding.getPackage().getName());
    assertEquals("Test", binding.getName());
    assertEquals("JPanel", binding.getSuperclass().getName());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getTypeBindingArgument(ITypeBinding, int) & getTypeBindingArgument(ITypeBinding, String, int)
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getTypeBindingArgument() throws Exception {
    setFileContentSrc(
        "test/Wrapper.java",
        getSourceDQ(
            "package test;",
            "public class Wrapper<N extends java.lang.Number> {",
            "  private N value;",
            "  public Wrapper(N value){",
            "    this.value = value;",
            "  }",
            "}"));
    setFileContentSrc(
        "test/WrapperSub.java",
        getSourceDQ(
            "package test;",
            "public class WrapperSub<T, S extends java.lang.Number> extends Wrapper<S> {",
            "  public WrapperSub(S value){",
            "    super(value);",
            "  }",
            "}"));
    waitForAutoBuild();
    createTypeDeclaration_TestC(getSourceDQ(
        "  // filler filler filler filler filler",
        "  private Wrapper field_1 = new/*marker_1*/ Wrapper(new Long(1));",
        "  private Wrapper field_2 = new/*marker_2*/ Wrapper<Double>(1.5);",
        "  private WrapperSub field_3 = new/*marker_3*/ WrapperSub<String, Float>(3.5f);"));
    // prepare ITypeBinding originals
    ITypeBinding binding_1 = getNode("marker_1", ClassInstanceCreation.class).resolveTypeBinding();
    ITypeBinding binding_2 = getNode("marker_2", ClassInstanceCreation.class).resolveTypeBinding();
    ITypeBinding binding_3 = getNode("marker_3", ClassInstanceCreation.class).resolveTypeBinding();
    // no type argument
    {
      ITypeBinding argument = AstNodeUtils.getTypeBindingArgument(binding_1, 0);
      assertThat(argument).isNotNull();
      String argumentName = AstNodeUtils.getFullyQualifiedName(argument, false);
      assertThat(argumentName).isEqualTo("java.lang.Number");
    }
    // Double as type argument
    {
      ITypeBinding argument = AstNodeUtils.getTypeBindingArgument(binding_2, 0);
      assertThat(argument).isNotNull();
      String argumentName = AstNodeUtils.getFullyQualifiedName(argument, false);
      assertThat(argumentName).isEqualTo("java.lang.Double");
    }
    // ask WrapperSub
    {
      // ask 0-th type argument directly
      {
        ITypeBinding argument = AstNodeUtils.getTypeBindingArgument(binding_3, 0);
        assertThat(argument).isNotNull();
        String argumentName = AstNodeUtils.getFullyQualifiedName(argument, false);
        assertThat(argumentName).isEqualTo("java.lang.String");
      }
      // ask 0-th type argument of Wrapper
      {
        ITypeBinding argument = AstNodeUtils.getTypeBindingArgument(binding_3, "test.Wrapper", 0);
        assertThat(argument).isNotNull();
        String argumentName = AstNodeUtils.getFullyQualifiedName(argument, false);
        assertThat(argumentName).isEqualTo("java.lang.Float");
      }
      // no such base class
      try {
        AstNodeUtils.getTypeBindingArgument(binding_3, "no.such.Class", 0);
        fail();
      } catch (IllegalArgumentException e) {
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getVariableBinding()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getVariableBinding_notVariable() throws Exception {
    createASTCompilationUnit(
        "test",
        "Test.java",
        getSourceDQ(
            "// filler filler filler filler filler",
            "package test;",
            "public class Test {",
            "  // filler",
            "}"));
    // check bad SimpleName
    SimpleName name = (SimpleName) m_lastEditor.getEnclosingNode("Test");
    assertNull(AstNodeUtils.getVariableBinding(name));
  }

  public void test_getVariableBinding_SimpleName() throws Exception {
    createASTCompilationUnit(
        "test",
        "Test.java",
        getSourceDQ(
            "// filler filler filler filler filler",
            "package test;",
            "public class Test {",
            "  private int m_value = 123;",
            "}"));
    SimpleName variable = (SimpleName) m_lastEditor.getEnclosingNode("m_value");
    IVariableBinding variableBinding = AstNodeUtils.getVariableBinding(variable);
    //
    assertNotNull(variableBinding);
    assertEquals("m_value", variableBinding.getName());
    assertEquals(true, variableBinding.isField());
    // check variable methods
    assertFalse(AstNodeUtils.isVariable(null));
    assertTrue(AstNodeUtils.isVariable(variable));
    assertEquals("m_value", AstNodeUtils.getVariableName(variable));
    assertSame(variable, AstNodeUtils.getVariableSimpleName(variable));
  }

  /**
   * Test for {@link AstNodeUtils#isVariable(ASTNode)}, etc - with {@link FieldAccess}.
   */
  public void test_getVariableBinding_FieldAccess() throws Exception {
    createASTCompilationUnit(
        "test",
        "Test.java",
        getSourceDQ(
            "package test;",
            "public class Test {",
            "  private int m_value;",
            "  void foo() {",
            "    this.m_value = 123;",
            "  }",
            "}"));
    FieldAccess variable = (FieldAccess) m_lastEditor.getEnclosingNode(".m_value");
    // getVariableBinding()
    {
      IVariableBinding variableBinding = AstNodeUtils.getVariableBinding(variable);
      assertNotNull(variableBinding);
      assertEquals("m_value", variableBinding.getName());
      assertEquals(true, variableBinding.isField());
    }
    // check variable methods
    assertFalse(AstNodeUtils.isVariable(null));
    assertTrue(AstNodeUtils.isVariable(variable));
    assertFalse(AstNodeUtils.isVariable(variable.getName()));
    assertEquals("m_value", AstNodeUtils.getVariableName(variable));
    assertSame(variable.getName(), AstNodeUtils.getVariableSimpleName(variable));
  }

  /**
   * Test for {@link AstNodeUtils#isVariable(ASTNode)}
   * <p>
   * Use unknown type.
   */
  public void test_getVariableBinding_unknownType() throws Exception {
    m_ignoreModelCompileProblems = true;
    createASTCompilationUnit(
        "test",
        "Test.java",
        getSourceDQ("package test;", "public class Test {", "  private Foo m_value = 123;", "}"));
    SimpleName variable = (SimpleName) m_lastEditor.getEnclosingNode("m_value");
    // not IVariableBinding
    {
      IVariableBinding variableBinding = AstNodeUtils.getVariableBinding(variable);
      assertNull(variableBinding);
    }
    // unknown type, so don't consider as variable
    assertFalse(AstNodeUtils.isVariable(variable));
    // unknown type, but we still know SimpleName
    assertSame(variable, AstNodeUtils.getVariableSimpleName(variable));
    // unknown type, so we still know its name
    assertEquals("m_value", AstNodeUtils.getVariableName(variable));
  }

  private void check_FieldDeclarationQualifiedNames(TypeDeclaration typeDeclaration,
      int index,
      String sourceName,
      String runtimeName) {
    FieldDeclaration fieldDeclaration =
        (FieldDeclaration) typeDeclaration.bodyDeclarations().get(index);
    // check type of field
    {
      ITypeBinding typeBinding = fieldDeclaration.getType().resolveBinding();
      check_typeBindingNames(typeBinding, sourceName, runtimeName);
    }
    // check type of variable
    {
      assertEquals(1, fieldDeclaration.fragments().size());
      VariableDeclaration variableDeclaration =
          (VariableDeclaration) fieldDeclaration.fragments().get(0);
      ITypeBinding typeBinding = variableDeclaration.resolveBinding().getType();
      check_typeBindingNames(typeBinding, sourceName, runtimeName);
    }
  }

  private void check_typeBindingNames(ITypeBinding typeBinding,
      String sourceName,
      String runtimeName) {
    assertEquals(sourceName, AstNodeUtils.getFullyQualifiedName(typeBinding, false));
    assertEquals(runtimeName, AstNodeUtils.getFullyQualifiedName(typeBinding, true));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getTypeBinding(SingleVariableDeclaration)
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstNodeUtils#getTypeBinding(SingleVariableDeclaration)}.
   */
  public void test_getTypeBinding_SingleVariableDeclaration() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  private void foo(int a, String b, String[] c) {",
            "  }",
            "}");
    MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
    List<SingleVariableDeclaration> parameters = DomGenerics.parameters(methodDeclaration);
    // int
    {
      SingleVariableDeclaration parameter = parameters.get(0);
      ITypeBinding binding = AstNodeUtils.getTypeBinding(parameter);
      assertEquals("int", AstNodeUtils.getFullyQualifiedName(binding, false));
    }
    // java.lang.String
    {
      SingleVariableDeclaration parameter = parameters.get(1);
      ITypeBinding binding = AstNodeUtils.getTypeBinding(parameter);
      assertEquals("java.lang.String", AstNodeUtils.getFullyQualifiedName(binding, false));
    }
    // java.lang.String[]
    {
      SingleVariableDeclaration parameter = parameters.get(2);
      ITypeBinding binding = AstNodeUtils.getTypeBinding(parameter);
      assertEquals("java.lang.String[]", AstNodeUtils.getFullyQualifiedName(binding, false));
    }
  }

  /**
   * Test for {@link AstNodeUtils#getTypeBinding(SingleVariableDeclaration)}.
   */
  public void test_getTypeBinding_SingleVariableDeclaration_noType() throws Exception {
    m_ignoreModelCompileProblems = true;
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  private void foo(NoSuchType a) {",
            "  }",
            "}");
    MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
    List<SingleVariableDeclaration> parameters = DomGenerics.parameters(methodDeclaration);
    //
    SingleVariableDeclaration parameter = parameters.get(0);
    ITypeBinding binding = AstNodeUtils.getTypeBinding(parameter);
    assertNull(binding);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getTypeBinding(VariableDeclaration)
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstNodeUtils#getTypeBinding(VariableDeclaration)}.
   */
  public void test_getTypeBinding_VariableDeclaration() throws Exception {
    createTypeDeclaration_Test(
        "// filler filler filler filler filler",
        "public class Test {",
        "  private short d;",
        "  private void foo(long a) {",
        "    String b;",
        "    for (int c = 0; c < 10; c++) {",
        "    }",
        "  }",
        "}");
    // "a"
    {
      VariableDeclaration var = getNode("a)", VariableDeclaration.class);
      ITypeBinding binding = AstNodeUtils.getTypeBinding(var);
      assertEquals("long", AstNodeUtils.getFullyQualifiedName(binding, false));
    }
    // "b"
    {
      VariableDeclaration var = getNode("b;", VariableDeclaration.class);
      ITypeBinding binding = AstNodeUtils.getTypeBinding(var);
      assertEquals("java.lang.String", AstNodeUtils.getFullyQualifiedName(binding, false));
    }
    // "c"
    {
      VariableDeclaration var = getNode("c =", VariableDeclaration.class);
      ITypeBinding binding = AstNodeUtils.getTypeBinding(var);
      assertEquals("int", AstNodeUtils.getFullyQualifiedName(binding, false));
    }
    // "d"
    {
      VariableDeclaration var = getNode("d;", VariableDeclaration.class);
      ITypeBinding binding = AstNodeUtils.getTypeBinding(var);
      assertEquals("short", AstNodeUtils.getFullyQualifiedName(binding, false));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getFullyQualifiedName(SingleVariableDeclaration)
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstNodeUtils#getFullyQualifiedName(SingleVariableDeclaration, boolean)}.
   */
  public void test_getFullyQualifiedName_SingleVariableDeclaration() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  private void foo(int a, String b, String[] c) {",
            "  }",
            "}");
    MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
    List<SingleVariableDeclaration> parameters = DomGenerics.parameters(methodDeclaration);
    // int
    {
      SingleVariableDeclaration parameter = parameters.get(0);
      assertEquals("int", AstNodeUtils.getFullyQualifiedName(parameter, false));
    }
    // java.lang.String
    {
      SingleVariableDeclaration parameter = parameters.get(1);
      assertEquals("java.lang.String", AstNodeUtils.getFullyQualifiedName(parameter, false));
    }
    // java.lang.String[]
    {
      SingleVariableDeclaration parameter = parameters.get(2);
      assertEquals("java.lang.String[]", AstNodeUtils.getFullyQualifiedName(parameter, false));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // isSuccessorOf
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstNodeUtils#isSuccessorOf(ITypeBinding, String)}.
   */
  public void test_isSuccessorOf_ITypeBinding_1() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  private String m_string = '123';",
            "  private String[] m_string_array = {};",
            "}");
    //
    {
      FieldDeclaration fieldDeclaration = typeDeclaration.getFields()[0];
      ITypeBinding typeBinding = fieldDeclaration.getType().resolveBinding();
      assertTrue(AstNodeUtils.isSuccessorOf(typeBinding, "java.lang.String"));
      assertTrue(AstNodeUtils.isSuccessorOf(typeBinding, String.class));
      assertTrue(AstNodeUtils.isSuccessorOf(typeBinding, "java.lang.Object"));
      assertFalse(AstNodeUtils.isSuccessorOf(typeBinding, "java.lang.Integer"));
    }
    //
    {
      FieldDeclaration fieldDeclaration = typeDeclaration.getFields()[1];
      ITypeBinding typeBinding = fieldDeclaration.getType().resolveBinding();
      assertFalse(AstNodeUtils.isSuccessorOf(typeBinding, "java.lang.String"));
      assertTrue(AstNodeUtils.isSuccessorOf(typeBinding, "java.lang.String[]"));
      assertTrue(AstNodeUtils.isSuccessorOf(typeBinding, "java.lang.Object"));
    }
  }

  /**
   * Test for {@link AstNodeUtils#isSuccessorOf(ITypeBinding, String...)}.
   */
  public void test_isSuccessorOf_ITypeBinding_2() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "// filler filler filler",
            "public class Test extends javax.swing.JPanel {",
            "}");
    ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(typeDeclaration);
    assertFalse(AstNodeUtils.isSuccessorOf(typeBinding, "java.util.List", "java.util.Map"));
    assertTrue(AstNodeUtils.isSuccessorOf(typeBinding, "java.util.List", "java.awt.Container"));
  }

  /**
   * Test for {@link AstNodeUtils#isSuccessorOf(ITypeBinding, String)}.
   */
  public void test_isSuccessorOf_ITypeBinding_3() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "// filler filler filler",
            "public class Test {",
            "  private String m_string = '123';",
            "}");
    //
    FieldDeclaration fieldDeclaration = typeDeclaration.getFields()[0];
    VariableDeclaration declaration = DomGenerics.fragments(fieldDeclaration).get(0);
    Expression initializer = declaration.getInitializer();
    assertTrue(AstNodeUtils.isSuccessorOf(initializer, "java.lang.String"));
    assertTrue(AstNodeUtils.isSuccessorOf(initializer, String.class));
    assertTrue(AstNodeUtils.isSuccessorOf(initializer, "java.lang.Object"));
    assertFalse(AstNodeUtils.isSuccessorOf(initializer, "java.lang.Integer"));
  }

  /**
   * Test for {@link AstNodeUtils#isSuccessorOf(ITypeBinding, ITypeBinding)}.
   */
  public void test_isSuccessorOf_ITypeBinding_ITypeBinding() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  private java.util.List m_List = null;",
            "  private java.util.ArrayList m_ArrayList = null;",
            "  private java.util.Set m_Set = null;",
            "}");
    // prepare bindings
    FieldDeclaration[] fields = typeDeclaration.getFields();
    ITypeBinding bindingList = AstNodeUtils.getTypeBinding(fields[0].getType());
    ITypeBinding bindingArrayList = AstNodeUtils.getTypeBinding(fields[1].getType());
    ITypeBinding bindingSet = AstNodeUtils.getTypeBinding(fields[2].getType());
    // check bindings
    assertTrue(AstNodeUtils.isSuccessorOf(bindingArrayList, bindingList));
    assertFalse(AstNodeUtils.isSuccessorOf(bindingArrayList, bindingSet));
  }

  /**
   * Test for {@link AstNodeUtils#isSuccessorOf(ITypeBinding, ITypeBinding)}.
   */
  public void test_isSuccessorOf_Expression_ITypeBinding() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  private Object m_ArrayList = new java.util.ArrayList();",
            "  private java.util.List m_List = null;",
            "  private java.util.Set m_Set = null;",
            "}");
    // prepare bindings
    FieldDeclaration[] fields = typeDeclaration.getFields();
    Expression expressionArrayList = DomGenerics.fragments(fields[0]).get(0).getInitializer();
    ITypeBinding bindingList = AstNodeUtils.getTypeBinding(fields[1].getType());
    ITypeBinding bindingSet = AstNodeUtils.getTypeBinding(fields[2].getType());
    // check bindings
    assertTrue(AstNodeUtils.isSuccessorOf(expressionArrayList, bindingList));
    assertFalse(AstNodeUtils.isSuccessorOf(expressionArrayList, bindingSet));
  }

  /**
   * Test for {@link AstNodeUtils#getTypeBinding(SingleVariableDeclaration)}.
   */
  public void test_isSuccessorOf_SingleVariableDeclaration_ITypeBinding() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  private void foo(int a, String b, java.util.ArrayList c) {",
            "  }",
            "}");
    MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
    List<SingleVariableDeclaration> parameters = DomGenerics.parameters(methodDeclaration);
    // int
    {
      SingleVariableDeclaration parameter = parameters.get(0);
      assertTrue(AstNodeUtils.isSuccessorOf(parameter, "int"));
    }
    // java.lang.String
    {
      SingleVariableDeclaration parameter = parameters.get(1);
      assertTrue(AstNodeUtils.isSuccessorOf(parameter, "java.lang.String"));
      assertTrue(AstNodeUtils.isSuccessorOf(parameter, "java.lang.Object"));
    }
    // java.util.ArrayList
    {
      SingleVariableDeclaration parameter = parameters.get(2);
      assertTrue(AstNodeUtils.isSuccessorOf(parameter, "java.util.List"));
      assertFalse(AstNodeUtils.isSuccessorOf(parameter, "java.util.Map"));
    }
  }

  /**
   * Test for {@link AstNodeUtils#isSuccessorOf(Type, String)}.
   */
  public void test_isSuccessorOf_Type() throws Exception {
    createASTCompilationUnit(
        "test",
        "Test.java",
        getSourceDQ(
            "// filler filler filler filler filler",
            "package test;",
            "public class Test {",
            "  String str;",
            "}"));
    Type type = getNode("String ", Type.class);
    //
    assertFalse(AstNodeUtils.isSuccessorOf(type, "java.util.List"));
    assertTrue(AstNodeUtils.isSuccessorOf(type, "java.lang.String"));
    assertTrue(AstNodeUtils.isSuccessorOf(type, "java.lang.Object"));
  }

  /**
   * Test for {@link AstNodeUtils#isSuccessorOf(TypeDeclaration, String)}.
   */
  public void test_isSuccessorOf_TypeDeclaration() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration(
            "test",
            "Test.java",
            getSourceDQ(
                "// filler filler filler filler filler",
                "// filler filler filler filler filler",
                "package test;",
                "import java.util.ArrayList;",
                "public class Test extends ArrayList {",
                "}"));
    //
    assertTrue(AstNodeUtils.isSuccessorOf(typeDeclaration, "java.lang.Object"));
    assertTrue(AstNodeUtils.isSuccessorOf(typeDeclaration, "java.util.List"));
    assertTrue(AstNodeUtils.isSuccessorOf(typeDeclaration, "java.util.ArrayList"));
    assertFalse(AstNodeUtils.isSuccessorOf(typeDeclaration, "java.util.Map"));
  }

  /**
   * Test for {@link AstNodeUtils#isSuccessorOf(AnonymousClassDeclaration, String)}.
   */
  public void test_isSuccessorOf_Anonymous() throws Exception {
    createASTCompilationUnit(
        "test",
        "Test.java",
        getSourceDQ(
            "// filler filler filler filler filler",
            "package test;",
            "import java.util.ArrayList;",
            "public class Test {",
            "  ArrayList object = new ArrayList() {",
            "    // marker",
            "  };",
            "}"));
    AnonymousClassDeclaration declaration = getNode("marker", AnonymousClassDeclaration.class);
    //
    assertTrue(AstNodeUtils.isSuccessorOf(declaration, "java.lang.Object"));
    assertTrue(AstNodeUtils.isSuccessorOf(declaration, "java.util.List"));
    assertTrue(AstNodeUtils.isSuccessorOf(declaration, "java.util.ArrayList"));
    assertFalse(AstNodeUtils.isSuccessorOf(declaration, "java.util.Map"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Modifiers
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstNodeUtils#hasVisibility(int, int)}.
   */
  public void test_hasVisibility() throws Exception {
    // exact match
    assertTrue(AstNodeUtils.hasVisibility(Modifier.PUBLIC, Modifier.PUBLIC));
    assertTrue(AstNodeUtils.hasVisibility(Modifier.PROTECTED, Modifier.PROTECTED));
    assertTrue(AstNodeUtils.hasVisibility(Modifier.PRIVATE, Modifier.PRIVATE));
    // ignore additional modifiers
    assertTrue(AstNodeUtils.hasVisibility(Modifier.PROTECTED | Modifier.FINAL, Modifier.PROTECTED));
    // we need exact match
    assertFalse(AstNodeUtils.hasVisibility(Modifier.PUBLIC, Modifier.PROTECTED));
    assertFalse(AstNodeUtils.hasVisibility(Modifier.PROTECTED, Modifier.PUBLIC));
    // we require "public" or "protected"
    assertFalse(AstNodeUtils.hasVisibility(Modifier.PRIVATE, Modifier.PUBLIC | Modifier.PROTECTED));
    assertTrue(AstNodeUtils.hasVisibility(Modifier.PUBLIC, Modifier.PUBLIC | Modifier.PROTECTED));
    assertTrue(AstNodeUtils.hasVisibility(Modifier.PROTECTED, Modifier.PUBLIC | Modifier.PROTECTED));
  }

  /**
   * Test for {@link AstNodeUtils#isStatic(BodyDeclaration)}.
   */
  public void test_isStatic_forBodyDeclaration() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  private int m_foo;",
            "  private static int m_bar;",
            "  public void foo() {",
            "  }",
            "  public static void bar() {",
            "  }",
            "}");
    {
      FieldDeclaration fieldDeclaration = typeDeclaration.getFields()[0];
      assertFalse(AstNodeUtils.isStatic(fieldDeclaration));
    }
    {
      FieldDeclaration fieldDeclaration = typeDeclaration.getFields()[1];
      assertTrue(AstNodeUtils.isStatic(fieldDeclaration));
    }
    {
      MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
      assertFalse(AstNodeUtils.isStatic(methodDeclaration));
    }
    {
      MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[1];
      assertTrue(AstNodeUtils.isStatic(methodDeclaration));
    }
  }

  /**
   * Test for {@link AstNodeUtils#isStatic(IMethodBinding)}.
   */
  public void test_isStatic_IMethodBinding() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  public void foo() {",
            "  }",
            "  public static void bar() {",
            "  }",
            "}");
    {
      MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
      IMethodBinding methodBinding = AstNodeUtils.getMethodBinding(methodDeclaration);
      assertFalse(AstNodeUtils.isStatic(methodBinding));
    }
    {
      MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[1];
      IMethodBinding methodBinding = AstNodeUtils.getMethodBinding(methodDeclaration);
      assertTrue(AstNodeUtils.isStatic(methodBinding));
    }
  }

  /**
   * Test for {@link AstNodeUtils#isStatic(ITypeBinding)}.
   */
  public void test_isStatic_ITypeBinding() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  public class Foo {",
            "  }",
            "  public static class Bar {",
            "  }",
            "}");
    {
      TypeDeclaration fooType = typeDeclaration.getTypes()[0];
      ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(fooType);
      assertFalse(AstNodeUtils.isStatic(typeBinding));
    }
    {
      TypeDeclaration barType = typeDeclaration.getTypes()[1];
      ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(barType);
      assertTrue(AstNodeUtils.isStatic(typeBinding));
    }
  }

  /**
   * Test for {@link AstNodeUtils#isAbstract(IMethodBinding)}.
   */
  public void test_isAbstract_IMethodBinding() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public abstract class Test {",
            "  public void foo() {",
            "  }",
            "  public abstract void bar();",
            "}");
    {
      MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
      IMethodBinding methodBinding = AstNodeUtils.getMethodBinding(methodDeclaration);
      assertFalse(AstNodeUtils.isAbstract(methodBinding));
    }
    {
      MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[1];
      IMethodBinding methodBinding = AstNodeUtils.getMethodBinding(methodDeclaration);
      assertTrue(AstNodeUtils.isAbstract(methodBinding));
    }
  }

  /**
   * Test for {@link AstNodeUtils#isAbstract(IMethodBinding)}.
   */
  public void test_isAbstract_MethodDeclaration() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public abstract class Test {",
            "  public void foo() {",
            "  }",
            "  public abstract void bar();",
            "}");
    {
      MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
      assertFalse(AstNodeUtils.isAbstract(methodDeclaration));
    }
    {
      MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[1];
      assertTrue(AstNodeUtils.isAbstract(methodDeclaration));
    }
  }

  /**
   * Test for {@link AstNodeUtils#isAbstract(ITypeBinding)}.
   */
  public void test_isAbstract_ITypeBinding() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  public class Foo {",
            "  }",
            "  public abstract class Bar {",
            "  }",
            "}");
    {
      TypeDeclaration fooType = typeDeclaration.getTypes()[0];
      ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(fooType);
      assertFalse(AstNodeUtils.isAbstract(typeBinding));
    }
    {
      TypeDeclaration barType = typeDeclaration.getTypes()[1];
      ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(barType);
      assertTrue(AstNodeUtils.isAbstract(typeBinding));
    }
  }

  /**
   * Test for {@link ITypeBinding#isMember()}.
   */
  public void test_isMember() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  public Object foo() {",
            "    return new Object() {};",
            "  }",
            "  public class Bar {",
            "  }",
            "}");
    // anonymous is not "inner"
    {
      ClassInstanceCreation creation =
          (ClassInstanceCreation) m_lastEditor.getEnclosingNode("new Object");
      ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(creation);
      assertFalse(typeBinding.isMember());
    }
    // Bar is "inner"
    {
      TypeDeclaration barType = typeDeclaration.getTypes()[0];
      ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(barType);
      assertTrue(typeBinding.isMember());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Searching in bindings
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstNodeUtils#getMethodBindings(ITypeBinding, int)}.
   */
  public void test_getMethodBindings_bindingsWithVisibility() throws Exception {
    createModelType(
        "test",
        "A.java",
        getSourceDQ(
            "package test;",
            "public class A {",
            "  private void aPrivate() {}",
            "  protected void aProtected() {}",
            "  public void aPublic() {}",
            "}"));
    waitForAutoBuild();
    TypeDeclaration typeDeclaration =
        createTypeDeclaration(
            "test",
            "B.java",
            getSourceDQ(
                "package test;",
                "public class B extends A {",
                "  private void bPrivate() {}",
                "  protected void bProtected() {}",
                "  public void bPublic() {}",
                "}"));
    ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(typeDeclaration);
    // "public" methods
    {
      List<IMethodBinding> methods = AstNodeUtils.getMethodBindings(typeBinding, Modifier.PUBLIC);
      assertHasMethods(methods, new String[]{"aPublic()", "bPublic()"}, new String[]{
          "aPrivate()",
          "aProtected()",
          "bPrivate()",
          "bProtected()"});
    }
    // "public" or "protected" methods
    {
      List<IMethodBinding> methods =
          AstNodeUtils.getMethodBindings(typeBinding, Modifier.PUBLIC | Modifier.PROTECTED);
      assertHasMethods(methods, new String[]{
          "aPublic()",
          "aProtected()",
          "bPublic()",
          "bProtected()"}, new String[]{"aPrivate()", "bPrivate()"});
    }
    // "private"
    {
      List<IMethodBinding> methods = AstNodeUtils.getMethodBindings(typeBinding, Modifier.PRIVATE);
      assertHasMethods(methods, new String[]{"bPrivate()"}, new String[]{
          "aPrivate()",
          "aProtected()",
          "aPublic()",
          "bProtected()",
          "bPublic()"});
    }
  }

  /**
   * Asserts that given {@link IMethodBinding}'s have methods with all expected signatures (but may
   * be not only them).
   */
  private static void assertHasMethods(List<IMethodBinding> methods,
      String[] expectedSignatures,
      String[] unExpectedSignatures) {
    Set<String> actualSignatures = Sets.newTreeSet();
    for (IMethodBinding methodBinding : methods) {
      actualSignatures.add(getMethodSignature(methodBinding));
    }
    // expected
    assertThat(actualSignatures).contains(expectedSignatures);
    // un-expected
    for (String unExpectedSignature : unExpectedSignatures) {
      assertFalse("" + actualSignatures, actualSignatures.contains(unExpectedSignature));
    }
  }

  /**
   * Test for {@link AstNodeUtils#getFieldBindings(ITypeBinding, int)}.
   */
  public void test_getFieldBindings_bindingsWithVisibility() throws Exception {
    createModelType(
        "test",
        "A.java",
        getSourceDQ(
            "package test;",
            "public class A {",
            "  private int aPrivate;",
            "  protected int aProtected;",
            "  public int aPublic;",
            "}"));
    waitForAutoBuild();
    TypeDeclaration typeDeclaration =
        createTypeDeclaration(
            "test",
            "B.java",
            getSourceDQ(
                "package test;",
                "public class B extends A {",
                "  private int bPrivate;",
                "  protected int bProtected;",
                "  public int bPublic;",
                "}"));
    ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(typeDeclaration);
    // "public"
    {
      List<IVariableBinding> fields = AstNodeUtils.getFieldBindings(typeBinding, Modifier.PUBLIC);
      assertHasFields(fields, new String[]{"aPublic", "bPublic"}, new String[]{
          "aPrivate",
          "aProtected",
          "bPrivate",
          "bProtected"});
    }
    // "public" or "protected"
    {
      List<IVariableBinding> fields =
          AstNodeUtils.getFieldBindings(typeBinding, Modifier.PUBLIC | Modifier.PROTECTED);
      assertHasFields(
          fields,
          new String[]{"aPublic", "aProtected", "bPublic", "bProtected"},
          new String[]{"aPrivate", "bPrivate"});
    }
    // "private"
    {
      List<IVariableBinding> fields = AstNodeUtils.getFieldBindings(typeBinding, Modifier.PRIVATE);
      assertHasFields(fields, new String[]{"bPrivate"}, new String[]{
          "aPrivate",
          "aProtected",
          "aPublic",
          "bProtected",
          "bPublic"});
    }
  }

  /**
   * Asserts that given {@link IVariableBinding}'s have fields with all expected names (but may be
   * not only them).
   */
  private static void assertHasFields(List<IVariableBinding> actualFields,
      String[] expectedNames,
      String[] unExpectedNames) {
    Set<String> actualNames = Sets.newTreeSet();
    for (IVariableBinding field : actualFields) {
      actualNames.add(field.getName());
    }
    // expected
    assertThat(actualNames).contains(expectedNames);
    // un-expected
    for (String unExpectedSignature : unExpectedNames) {
      assertFalse("" + actualNames, actualNames.contains(unExpectedSignature));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Field utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstNodeUtils#getFieldAssignment(ASTNode)}.
   */
  public void test_getFieldAssignment_QualifiedName() throws Exception {
    setFileContentSrc(
        "test/MyObject.java",
        getSourceDQ("package test;", "public class MyObject {", "  public int m_value;", "}"));
    createTypeDeclaration_Test(
        "public class Test {",
        "  public Test() {",
        "    MyObject foo = new MyObject();",
        "    foo.m_value = 1;",
        "  }",
        "}");
    // foo = new MyObject(), invalid
    {
      int index = m_lastEditor.getSource().indexOf("foo =");
      Expression fooNode = (Expression) m_lastEditor.getEnclosingNode(index);
      Expression fieldAccess = AstNodeUtils.getFieldAssignment(fooNode);
      assertThat(fieldAccess).isNull();
      assertThat(AstNodeUtils.getFieldAccessName(fooNode)).isNull();
      assertThat(AstNodeUtils.getFieldAccessQualifier(fooNode)).isNull();
    }
    // foo.m_value = 1, valid
    {
      int index = m_lastEditor.getSource().indexOf("oo.");
      ASTNode fooNode = m_lastEditor.getEnclosingNode(index);
      Expression fieldAccess = AstNodeUtils.getFieldAssignment(fooNode);
      assertThat(fieldAccess).isNotNull().isInstanceOf(QualifiedName.class);
      assertThat(fieldAccess.getParent()).isInstanceOf(Assignment.class);
      assertEquals("m_value", AstNodeUtils.getFieldAccessName(fieldAccess).getIdentifier());
      {
        Expression qualifier = AstNodeUtils.getFieldAccessQualifier(fieldAccess);
        assertThat(qualifier).isInstanceOf(SimpleName.class);
        assertEquals("foo", m_lastEditor.getSource(qualifier));
      }
    }
  }

  /**
   * Test for {@link AstNodeUtils#getFieldAssignment(ASTNode)}.
   */
  public void test_getFieldAssignment_FieldAccess() throws Exception {
    setFileContentSrc(
        "test/MyObject.java",
        getSourceDQ("package test;", "public class MyObject {", "  public int m_value;", "}"));
    createTypeDeclaration_Test(
        "public class Test {",
        "  public Test() {",
        "    new MyObject().m_value = 1;",
        "  }",
        "}");
    // new MyObject().m_value = 1, valid
    {
      int index = m_lastEditor.getSource().indexOf("().");
      ASTNode objectNode = m_lastEditor.getEnclosingNode(index);
      Expression fieldAccess = AstNodeUtils.getFieldAssignment(objectNode);
      assertThat(fieldAccess).isNotNull().isInstanceOf(FieldAccess.class);
      assertThat(fieldAccess.getParent()).isInstanceOf(Assignment.class);
      assertEquals("m_value", AstNodeUtils.getFieldAccessName(fieldAccess).getIdentifier());
      {
        Expression qualifier = AstNodeUtils.getFieldAccessQualifier(fieldAccess);
        assertThat(qualifier).isInstanceOf(ClassInstanceCreation.class);
        assertEquals("new MyObject()", m_lastEditor.getSource(qualifier));
      }
    }
  }

  /**
   * Test for {@link AstNodeUtils#getFieldFragmentByName(TypeDeclaration, String)}.
   */
  public void test_getFieldFragmentByName() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  private int m_value;",
            "  public String m_name = '123';",
            "}");
    //
    VariableDeclarationFragment fragment0 =
        AstNodeUtils.getFieldFragmentByName(typeDeclaration, "m_value");
    assertNotNull(fragment0);
    assertEquals("m_value", fragment0.getName().getIdentifier());
    //
    VariableDeclarationFragment fragment1 =
        AstNodeUtils.getFieldFragmentByName(typeDeclaration, "m_name");
    assertNotNull(fragment1);
    assertEquals("m_name", fragment1.getName().getIdentifier());
    //
    assertNull(AstNodeUtils.getFieldFragmentByName(typeDeclaration, "m_key"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getEnclosingXXX
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getEnclosingStatement() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_TestC("void foo(){System.out.println();}");
    MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
    ExpressionStatement statement =
        (ExpressionStatement) methodDeclaration.getBody().statements().get(0);
    assertSame(statement, AstNodeUtils.getEnclosingStatement(statement));
    assertSame(statement, AstNodeUtils.getEnclosingStatement(statement.getExpression()));
    assertNull(AstNodeUtils.getEnclosingStatement(typeDeclaration));
  }

  public void test_getEnclosingBlock() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_TestC("void foo(){System.out.println();}");
    MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
    Block body = methodDeclaration.getBody();
    //
    ExpressionStatement statement = (ExpressionStatement) body.statements().get(0);
    assertSame(body, AstNodeUtils.getEnclosingBlock(body));
    assertSame(body, AstNodeUtils.getEnclosingBlock(statement));
    assertNull(AstNodeUtils.getEnclosingBlock(typeDeclaration));
  }

  public void test_getEnclosingFieldDeclaration() throws Exception {
    TypeDeclaration typeDeclaration = createTypeDeclaration_TestC("int m_field;");
    FieldDeclaration fieldDeclaration = typeDeclaration.getFields()[0];
    assertSame(fieldDeclaration, AstNodeUtils.getEnclosingFieldDeclaration(fieldDeclaration));
    assertSame(
        fieldDeclaration,
        AstNodeUtils.getEnclosingFieldDeclaration(fieldDeclaration.getType()));
    assertNull(AstNodeUtils.getEnclosingFieldDeclaration(typeDeclaration));
  }

  /**
   * Test for {@link AstNodeUtils#getEnclosingMethod(ASTNode)}.
   */
  public void test_getEnclosingMethod() throws Exception {
    TypeDeclaration typeDeclaration = createTypeDeclaration_TestC("void foo(){}");
    MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
    assertSame(methodDeclaration, AstNodeUtils.getEnclosingMethod(methodDeclaration));
    assertSame(methodDeclaration, AstNodeUtils.getEnclosingMethod(methodDeclaration.getBody()));
    assertNull(AstNodeUtils.getEnclosingMethod(typeDeclaration));
  }

  /**
   * Test for {@link AstNodeUtils#getEnclosingMethod(TypeDeclaration, ASTNode)}.
   */
  public void test_getEnclosingMethod_inTypeDeclaration() throws Exception {
    TypeDeclaration testType =
        createTypeDeclaration_Test(
            "public class Test {",
            "  private int markA;",
            "  public void methodA() {",
            "    class Inner {",
            "      public void methodB() {",
            "        int markB;",
            "      }",
            "    }",
            "  }",
            "  public void methodC() {",
            "    int markC;",
            "  }",
            "}");
    ASTNode markA = getNode("markA");
    ASTNode markB = getNode("markB");
    ASTNode markC = getNode("markC");
    MethodDeclaration methodA = getNode("methodA", MethodDeclaration.class);
    MethodDeclaration methodC = getNode("methodC", MethodDeclaration.class);
    // no method for "markA"
    assertSame(null, AstNodeUtils.getEnclosingMethod(testType, markA));
    // "methodA" for "markB"
    assertSame(methodA, AstNodeUtils.getEnclosingMethod(testType, markB));
    // "methodC" for "markC"
    assertSame(methodC, AstNodeUtils.getEnclosingMethod(testType, markC));
  }

  public void test_getEnclosingType() throws Exception {
    TypeDeclaration typeDeclaration = createTypeDeclaration_TestC("String m_string;");
    assertSame(typeDeclaration, AstNodeUtils.getEnclosingType(typeDeclaration));
    assertSame(typeDeclaration, AstNodeUtils.getEnclosingType(typeDeclaration.getFields()[0]));
    assertNull(AstNodeUtils.getEnclosingType(typeDeclaration.getParent()));
  }

  public void test_getEnclosingTypeTop() throws Exception {
    TypeDeclaration testType =
        createTypeDeclaration_Test(
            "public class Test {",
            "  public class Inner {",
            "    public void foo() {",
            "    }",
            "  }",
            "  public void bar() {",
            "  }",
            "}");
    TypeDeclaration innerType = testType.getTypes()[0];
    MethodDeclaration fooMethod = innerType.getMethods()[0];
    MethodDeclaration barMethod = testType.getMethods()[0];
    assertSame(testType, AstNodeUtils.getEnclosingTypeTop(fooMethod));
    assertSame(testType, AstNodeUtils.getEnclosingTypeTop(barMethod));
    assertSame(testType, AstNodeUtils.getEnclosingTypeTop(innerType));
    assertSame(testType, AstNodeUtils.getEnclosingTypeTop(testType));
  }

  public void test_getParentType() throws Exception {
    TypeDeclaration testType =
        createTypeDeclaration_Test(
            "public class Test {",
            "  public void foo() {",
            "    Object object = new Object(){",
            "      public String toString() {",
            "          return 'test';",
            "        }",
            "      };",
            "  }",
            "}");
    MethodDeclaration fooMethod = testType.getMethods()[0];
    assertSame(AstNodeUtils.getParentType(fooMethod), testType);
    MethodDeclaration toStringMethod =
        AstNodeUtils.getEnclosingMethod(this.<ASTNode>getNode("toString"));
    assertThat(toStringMethod.getParent()).isInstanceOf(AnonymousClassDeclaration.class);
    assertThat(AstNodeUtils.getEnclosingType(toStringMethod)).isSameAs(testType);
    assertThat(AstNodeUtils.getParentType(toStringMethod)).isInstanceOf(TypeDeclaration.class);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getEnclosingNode
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getEnclosingNode_1() throws Exception {
    TypeDeclaration typeDeclaration = createTypeDeclaration_TestC("void foo(){}");
    MethodDeclaration method = typeDeclaration.getMethods()[0];
    {
      ASTNode node = typeDeclaration.getName();
      assertSame(node, AstNodeUtils.getEnclosingNode(typeDeclaration, node.getStartPosition() + 1));
    }
    {
      ASTNode node = method.getName();
      assertSame(
          method,
          AstNodeUtils.getEnclosingNode(typeDeclaration, AstNodeUtils.getSourceEnd(node)));
    }
  }

  public void test_getEnclosingNode_2() throws Exception {
    TypeDeclaration typeDeclaration = createTypeDeclaration_TestC("void foo(){   int a;   }");
    MethodDeclaration method = typeDeclaration.getMethods()[0];
    {
      Block body = method.getBody();
      assertSame(body, AstNodeUtils.getEnclosingNode(typeDeclaration, body.getStartPosition() + 1));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getCommonParent()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstNodeUtils#getCommonParent(ASTNode, ASTNode)}.
   */
  public void test_getCommonParent() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  public Test() {",
            "    int a;",
            "    int b;",
            "  }",
            "  public void foo() {",
            "    int c;",
            "  }",
            "}");
    ASTNode nodeA = getNode("a;");
    ASTNode nodeB = getNode("b;");
    ASTNode nodeC = getNode("c;");
    Block blockTest = AstNodeUtils.getEnclosingBlock(nodeA);
    // "a" and "b"
    {
      ASTNode commonParent = AstNodeUtils.getCommonParent(nodeA, nodeB);
      assertSame(blockTest, commonParent);
    }
    // "a" and "a"
    {
      ASTNode parentA = nodeA.getParent();
      ASTNode commonParent = AstNodeUtils.getCommonParent(nodeA, nodeA);
      assertSame(parentA, commonParent);
    }
    // "a" and "c"
    {
      ASTNode commonParent = AstNodeUtils.getCommonParent(nodeA, nodeC);
      assertSame(typeDeclaration, commonParent);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getCommonBlock()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstNodeUtils#getCommonBlock(ASTNode, ASTNode)}.
   */
  public void test_getCommonBlock() throws Exception {
    createTypeDeclaration_Test(
        "public class Test {",
        "  public Test() {",
        "    int a;",
        "    int b;",
        "  }",
        "  public void foo() {",
        "    int c;",
        "  }",
        "}");
    ASTNode nodeA = getNode("a;");
    ASTNode nodeB = getNode("b;");
    ASTNode nodeC = getNode("c;");
    Block blockTest = AstNodeUtils.getEnclosingBlock(nodeA);
    // "a" and "b"
    {
      Block commonBlock = AstNodeUtils.getCommonBlock(nodeA, nodeB);
      assertSame(blockTest, commonBlock);
    }
    // "a" and "a"
    {
      Block commonBlock = AstNodeUtils.getCommonBlock(nodeA, nodeA);
      assertSame(blockTest, commonBlock);
    }
    // "a" and "c"
    {
      Block commonBlock = AstNodeUtils.getCommonBlock(nodeA, nodeC);
      assertSame(null, commonBlock);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getStatementWithinBlock()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstNodeUtils#getStatementWithinBlock(Block, ASTNode)}.
   */
  public void test_getStatementWithinBlock() throws Exception {
    createTypeDeclaration_Test(
        "public class Test {",
        "  public Test() {",
        "    int a;",
        "    {",
        "      int b;",
        "    }",
        "  }",
        "  public void foo() {",
        "    int c;",
        "  }",
        "}");
    ASTNode nodeA = getNode("a;");
    ASTNode nodeB = getNode("b;");
    ASTNode nodeC = getNode("c;");
    Block blockA = AstNodeUtils.getEnclosingBlock(nodeA);
    Block blockB = AstNodeUtils.getEnclosingBlock(nodeB);
    // "a" in "blockA"
    {
      Statement statementA = AstNodeUtils.getEnclosingStatement(nodeA);
      Statement statement = AstNodeUtils.getStatementWithinBlock(blockA, nodeA);
      assertSame(statementA, statement);
    }
    // "b" in "blockB"
    {
      Statement statementB = AstNodeUtils.getEnclosingStatement(nodeB);
      Statement statement = AstNodeUtils.getStatementWithinBlock(blockB, nodeB);
      assertSame(statementB, statement);
    }
    // "b" in "blockA"
    {
      Statement statement = AstNodeUtils.getStatementWithinBlock(blockA, nodeB);
      assertSame(blockB, statement);
    }
    // "c" in "blockA"
    {
      Statement statement = AstNodeUtils.getStatementWithinBlock(blockA, nodeC);
      assertSame(null, statement);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Statement
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getNextStatement() throws Exception {
    createASTCompilationUnit(
        "test",
        "Test.java",
        getSourceDQ(
            "package test;",
            "public class Test {",
            "  public Test() {",
            "    int a;",
            "    int b;",
            "    int c;",
            "  }",
            "}"));
    {
      int index = m_lastEditor.getSource().indexOf("int b;");
      Statement statement = m_lastEditor.getEnclosingStatement(index);
      Statement nextStatement = AstNodeUtils.getNextStatement(statement);
      assertEquals("int c;", m_lastEditor.getSource(nextStatement));
    }
    {
      int index = m_lastEditor.getSource().indexOf("int c;");
      Statement statement = m_lastEditor.getEnclosingStatement(index);
      Statement nextStatement = AstNodeUtils.getNextStatement(statement);
      assertNull(nextStatement);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getVariableDeclarationsAll
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates type in package "test" and requests all {@link VariableDeclaration}'s.
   */
  private void check_getVariableDeclarationsAll(String[] lines, String[] expectedNames)
      throws Exception {
    String source = getSource(lines);
    source = "package test;\n" + source;
    TypeDeclaration typeDeclaration = createTypeDeclaration("test", "Test.java", source);
    // check declarations
    List<VariableDeclaration> declarations =
        AstNodeUtils.getVariableDeclarationsAll(typeDeclaration);
    assertEquals("Wrong count of variables.", expectedNames.length, declarations.size());
    for (int i = 0; i < declarations.size(); i++) {
      VariableDeclaration declaration = declarations.get(i);
      assertEquals(declaration.getName().getIdentifier(), expectedNames[i]);
    }
  }

  /**
   * {@link AstNodeUtils#getVariableDeclarationsAll(ASTNode)} reports only
   * {@link VariableDeclaration} 's, but ignores other {@link SimpleName}'s, such as methods and
   * types.
   */
  public void test_getVariableDeclarationsAll() throws Exception {
    check_getVariableDeclarationsAll(new String[]{
        "public class Test {",
        "  private int a;",
        "  public void foo(int b) {",
        "    int c;",
        "  }",
        "}"}, new String[]{"a", "b", "c"});
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getVariableDeclarationsAfter
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates type in package "test" and requests potential shadowing {@link VariableDeclaration}'s
   * at position of given piece of source.
   */
  private void check_getVariableDeclarationsAfter(String[] lines,
      String positionSource,
      String[] expectedNames) throws Exception {
    String source = getSource(lines);
    source = "package test;\n" + source;
    TypeDeclaration typeDeclaration = createTypeDeclaration("test", "Test.java", source);
    // prepare position
    int position = source.indexOf(positionSource);
    assertTrue(position != -1);
    // prepare shadowing declarations
    List<VariableDeclaration> declarations =
        AstNodeUtils.getVariableDeclarationsAfter(typeDeclaration, position);
    assertEquals("Wrong count of shadowing variables.", expectedNames.length, declarations.size());
    for (int i = 0; i < declarations.size(); i++) {
      VariableDeclaration declaration = declarations.get(i);
      assertEquals(declaration.getName().getIdentifier(), expectedNames[i]);
    }
  }

  /**
   * When target is in {@link TypeDeclaration}, for example field, it can be shadowed by any
   * {@link VariableDeclaration} in {@link BodyDeclaration}'s of this {@link TypeDeclaration}.
   * <p>
   * Note, that {@link FieldDeclaration}'s of this {@link TypeDeclaration} are not considered
   * because they are not shadows, they are conflicts.
   */
  public void test_getVariableDeclarationsAfter_type() throws Exception {
    check_getVariableDeclarationsAfter(new String[]{
        "public class Test {",
        "  // marker",
        "  int a;",
        "  void foo(int b) {",
        "    int c;",
        "  }",
        "}"}, "// marker", new String[]{"b", "c"});
  }

  /**
   * Position between two variables.
   */
  public void test_getVariableDeclarationsAfter_statement_1() throws Exception {
    check_getVariableDeclarationsAfter(new String[]{
        "public class Test {",
        "  int a;",
        "  void foo() {",
        "    int b;",
        "    System.gc();",
        "    int c;",
        "  }",
        "}"}, "System.gc()", new String[]{"c"});
  }

  /**
   * Position on comment, so target node is {@link Block}.
   */
  public void test_getVariableDeclarationsAfter_statement_2() throws Exception {
    check_getVariableDeclarationsAfter(new String[]{
        "public class Test {",
        "  void foo() {",
        "    int a;",
        "    // marker",
        "    int b;",
        "  }",
        "}"}, "// marker", new String[]{"b"});
  }

  /**
   * Position on beginning of variable declaration.
   */
  public void test_getVariableDeclarationsAfter_statement_3() throws Exception {
    check_getVariableDeclarationsAfter(new String[]{
        "public class Test {",
        "  void foo() {",
        "    int a;",
        "    int b;",
        "  }",
        "}"}, "int b", new String[]{"b"});
  }

  /**
   * Test that variables in inner {@link Block}'s are also processed.
   */
  public void test_getVariableDeclarationsAfter_statement_4() throws Exception {
    check_getVariableDeclarationsAfter(new String[]{
        "public class Test {",
        "  void foo() {",
        "    int a;",
        "    System.gc();",
        "    int b;",
        "    {",
        "      int c;",
        "    }",
        "  }",
        "}"}, "System.gc()", new String[]{"b", "c"});
  }

  /**
   * Test that variables in outer {@link Block}'s are ignored.
   */
  public void test_getVariableDeclarationsAfter_statement_5() throws Exception {
    check_getVariableDeclarationsAfter(new String[]{
        "public class Test {",
        "  void foo() {",
        "    {",
        "      System.gc();",
        "    }",
        "    int a;",
        "    {",
        "      int b;",
        "    }",
        "  }",
        "}"}, "System.gc()", new String[]{});
  }

  /**
   * Test that variables in inner classes are also processed.
   */
  public void test_getVariableDeclarationsAfter_statement_6() throws Exception {
    check_getVariableDeclarationsAfter(new String[]{
        "public class Test {",
        "  void foo() {",
        "    System.gc();",
        "    class Inner {int a;}",
        "    Runtime.getRuntime().addShutdownHook(new Thread() {int b;});",
        "  }",
        "}"}, "System.gc()", new String[]{"a", "b"});
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getVariableDeclarationsVisibleAt
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getVariableDeclarationsVisibleAt_1() throws Exception {
    String code = "void foo(){int a = 1; int b;}";
    check_getVariableDeclarationsVisibleAt(code, new int[]{0}, 0, new String[]{});
  }

  public void test_getVariableDeclarationsVisibleAt_2() throws Exception {
    String code = "void foo(){int a = 1;  int b;}";
    check_getVariableDeclarationsVisibleAt(code, new int[]{1}, 0, new String[]{"a = 1"});
  }

  public void test_getVariableDeclarationsVisibleAt_3() throws Exception {
    String code = "void foo(){int a = 1;  int b;}";
    check_getVariableDeclarationsVisibleAt(code, new int[]{1}, -1, new String[]{"a = 1"});
  }

  public void test_getVariableDeclarationsVisibleAt_4() throws Exception {
    String code = "void foo(){int a = 1;  int b;}";
    check_getVariableDeclarationsVisibleAt(code, new int[]{1}, -4, new String[]{});
  }

  public void test_getVariableDeclarationsVisibleAt_5() throws Exception {
    String code = "int a = 1; void foo(int b){int c;}";
    check_getVariableDeclarationsVisibleAt(code, new int[]{0}, 0, new String[]{"int b", "a = 1"});
  }

  public void check_getVariableDeclarationsVisibleAt(String code,
      int indexes[],
      int offset,
      String[] expectedSources) throws Exception {
    TypeDeclaration typeDeclaration = createTypeDeclaration_TestC(code);
    MethodDeclaration method = typeDeclaration.getMethods()[0];
    //
    Statement statement = getStatement(method.getBody(), indexes);
    int position = statement.getStartPosition() + offset;
    List<VariableDeclaration> declarations =
        AstNodeUtils.getVariableDeclarationsVisibleAt(typeDeclaration, position);
    //
    assertEquals(expectedSources.length, declarations.size());
    for (int i = 0; i < declarations.size(); i++) {
      VariableDeclaration declaration = declarations.get(i);
      String actualSource = m_lastEditor.getSource(declaration);
      String expectedSource = expectedSources[i];
      assertEquals(expectedSource, actualSource);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Variables
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_isVariable() throws Exception {
    createTypeDeclaration_Test(
        "",
        "public class Test {",
        "  int field = 0;",
        "  public void foo() {",
        "    int local = 0;",
        "    System.out.println(field);",
        "    System.out.println(this.field);",
        "    System.out.println(local);",
        "    System.out.println(java.util.Collections.EMPTY_LIST);",
        "  }",
        "}");
    // field: is variable
    {
      SimpleName argument = (SimpleName) m_lastEditor.getEnclosingNode("field)");
      assertTrue(AstNodeUtils.isVariable(argument));
    }
    // local: is variable
    {
      SimpleName argument = (SimpleName) m_lastEditor.getEnclosingNode("local)");
      assertTrue(AstNodeUtils.isVariable(argument));
    }
    // this.field: is variable
    {
      FieldAccess argument = (FieldAccess) m_lastEditor.getEnclosingNode(".field)");
      assertTrue(AstNodeUtils.isVariable(argument));
      assertFalse(AstNodeUtils.isVariable(argument.getName()));
    }
    // VariableDeclarationFragment "local = 0":  is not variable
    {
      VariableDeclarationFragment argument =
          (VariableDeclarationFragment) m_lastEditor.getEnclosingNode("local = 0").getParent();
      assertFalse(AstNodeUtils.isVariable(argument));
    }
    // qualified name: is not variable
    {
      QualifiedName argument =
          (QualifiedName) m_lastEditor.getEnclosingNode("EMPTY_LIST)").getParent();
      assertFalse(AstNodeUtils.isVariable(argument));
      assertFalse(AstNodeUtils.isVariable(argument.getName()));
      assertFalse(AstNodeUtils.isVariable(argument.getQualifier()));
    }
  }

  public void test_getActualVariableExpression() throws Exception {
    createTypeDeclaration_Test(
        "public class Test {",
        "  Object field1 = new Object();",
        "  Object field2 = null;",
        "  public void foo() {",
        "    field2 = new Double(0);",
        "    Object local1 = null;",
        "    Object local2 = null;",
        "    System.out.println(field1/*marker1*/);",
        "    System.out.println(this.field2/*marker2*/);",
        "    local1 = new Integer(7);",
        "    System.out.println(local1/*marker3*/);",
        "    System.out.println(local2/*marker4*/);",
        "  }",
        "}");
    // field: is variable
    assert_variable("marker1", "new Object()");
    assert_variable("marker2", "new Double(0)");
    assert_variable("marker3", "new Integer(7)");
    assert_variable("marker4", "null");
  }

  private void assert_variable(String marker, String actualExpression) {
    MethodInvocation invocation = getNode(marker);
    Expression argument = DomGenerics.arguments(invocation).get(0);
    assertTrue(AstNodeUtils.isVariable(argument));
    Expression expression = AstNodeUtils.getActualVariableExpression(argument);
    assertThat(expression.toString()).isEqualTo(actualExpression);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getMethodInvocations()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getMethodInvocations_1() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  void foo() {",
            "    bar(1);",
            "    bar(2);",
            "  }",
            "  void bar(int i) {",
            "  }",
            "}");
    MethodDeclaration[] methods = typeDeclaration.getMethods();
    //
    List<MethodInvocation> invocations = AstNodeUtils.getMethodInvocations(methods[1]);
    assertThat(invocations).hasSize(2);
    {
      List<Statement> statementList = DomGenerics.statements(methods[0].getBody());
      Statement statements[] = statementList.toArray(new Statement[statementList.size()]);
      assertTrue(invocations.contains(((ExpressionStatement) statements[0]).getExpression()));
      assertTrue(invocations.contains(((ExpressionStatement) statements[1]).getExpression()));
    }
  }

  public void test_getMethodInvocations_sameSignature_differentTypes() throws Exception {
    setFileContentSrc(
        "test/MyObject.java",
        getSourceDQ(
            "package test;",
            "public class MyObject {",
            "  public void bar(int i) {",
            "  }",
            "}"));
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  private MyObject myObject = new MyObject();",
            "  void foo() {",
            "    myObject.bar(1);",
            "    bar(2);",
            "  }",
            "  void bar(int i) {",
            "  }",
            "}");
    MethodDeclaration[] methods = typeDeclaration.getMethods();
    //
    List<MethodInvocation> invocations = AstNodeUtils.getMethodInvocations(methods[1]);
    assertThat(invocations).hasSize(1);
    {
      List<Statement> statementList = DomGenerics.statements(methods[0].getBody());
      Statement statements[] = statementList.toArray(new Statement[statementList.size()]);
      assertTrue(invocations.contains(((ExpressionStatement) statements[1]).getExpression()));
    }
  }

  public void test_getMethodInvocations_noType_noBinding() throws Exception {
    m_ignoreModelCompileProblems = true;
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  void root() {",
            "    toInvoke(null);",
            "  }",
            "  void toInvoke(NoSuchType o) {",
            "  }",
            "}");
    // method toInvoke() has no binding
    MethodDeclaration method = typeDeclaration.getMethods()[1];
    assertNull(AstNodeUtils.getMethodBinding(method));
    // ...so, we can not find its invocations
    List<MethodInvocation> invocations = AstNodeUtils.getMethodInvocations(method);
    assertThat(invocations).isEmpty();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getLocalMethodDeclaration()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstNodeUtils#getLocalMethodDeclaration(MethodInvocation)}.<br>
   * Several variants with local and non-local methods.
   */
  public void test_getLocalMethodDeclaration_1() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  void foo() {",
            "    bar();",
            "    this.bar();",
            "    new Test().bar();",
            "    System.gc();",
            "  }",
            "  void bar() {",
            "  }",
            "}");
    List<Statement> statements = DomGenerics.statements(typeDeclaration.getMethods()[0].getBody());
    // bar()
    {
      MethodInvocation invocation =
          (MethodInvocation) ((ExpressionStatement) statements.get(0)).getExpression();
      assertSame(
          typeDeclaration.getMethods()[1],
          AstNodeUtils.getLocalMethodDeclaration(invocation));
    }
    // this.bar()
    {
      MethodInvocation invocation =
          (MethodInvocation) ((ExpressionStatement) statements.get(1)).getExpression();
      assertSame(
          typeDeclaration.getMethods()[1],
          AstNodeUtils.getLocalMethodDeclaration(invocation));
    }
    // new Test().bar()
    {
      MethodInvocation invocation =
          (MethodInvocation) ((ExpressionStatement) statements.get(2)).getExpression();
      assertSame(
          typeDeclaration.getMethods()[1],
          AstNodeUtils.getLocalMethodDeclaration(invocation));
    }
    // System.gc()
    {
      MethodInvocation invocation =
          (MethodInvocation) ((ExpressionStatement) statements.get(3)).getExpression();
      assertNull(AstNodeUtils.getLocalMethodDeclaration(invocation));
    }
  }

  /**
   * Test for {@link AstNodeUtils#getLocalMethodDeclaration(MethodInvocation)}.<br>
   * No qualifier for invocation, so probably local method, but invalid binding/signature.
   */
  public void test_getLocalMethodDeclaration_2() throws Exception {
    m_ignoreModelCompileProblems = true;
    CompilationUnit compilationUnit =
        createASTCompilationUnit(
            "test",
            "Test.java",
            getSourceDQ(
                "package test;",
                "public class Test {",
                "  void foo() {",
                "    baZ();",
                "  }",
                "  void bar() {",
                "  }",
                "}"));
    TypeDeclaration typeDeclaration = (TypeDeclaration) compilationUnit.types().get(0);
    List<Statement> statements = DomGenerics.statements(typeDeclaration.getMethods()[0].getBody());
    // baZ()
    {
      MethodInvocation invocation =
          (MethodInvocation) ((ExpressionStatement) statements.get(0)).getExpression();
      assertNull(AstNodeUtils.getLocalMethodDeclaration(invocation));
    }
  }

  /**
   * Test for {@link AstNodeUtils#getLocalMethodDeclaration(MethodInvocation)}.<br>
   * There was problem that after removing {@link MethodDeclaration} and adding same
   * {@link MethodDeclaration} we return used old cached {@link MethodDeclaration}.
   */
  public void test_getLocalMethodDeclaration_cachingBug() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  void foo() {",
            "    bar();",
            "  }",
            "  void bar() {",
            "  }",
            "}");
    MethodInvocation invocation =
        (MethodInvocation) m_lastEditor.getEnclosingNode("bar()").getParent();
    MethodDeclaration oldMethod = typeDeclaration.getMethods()[1];
    // initially old method
    assertSame(oldMethod, AstNodeUtils.getLocalMethodDeclaration(invocation));
    // remove old method
    m_lastEditor.removeBodyDeclaration(oldMethod);
    MethodDeclaration newMethod =
        m_lastEditor.addMethodDeclaration(
            "void bar()",
            ImmutableList.<String>of(),
            new BodyDeclarationTarget(typeDeclaration, false));
    // now new method
    assertSame(newMethod, AstNodeUtils.getLocalMethodDeclaration(invocation));
    // second time to test visually (using coverage) that caching works
    assertSame(newMethod, AstNodeUtils.getLocalMethodDeclaration(invocation));
  }

  public void test_getLocalConstructorDeclaration() throws Exception {
    CompilationUnit compilationUnit =
        createASTCompilationUnit(
            "test",
            "Test.java",
            getSourceDQ(
                "package test;",
                "public class Test {",
                "  void foo() {",
                "    new Test(0);",
                "    new Integer(0);",
                "  }",
                "  public Test(int value) {",
                "  }",
                "}"));
    TypeDeclaration typeDeclaration = (TypeDeclaration) compilationUnit.types().get(0);
    List<Statement> statements = DomGenerics.statements(typeDeclaration.getMethods()[0].getBody());
    {
      ClassInstanceCreation creation =
          (ClassInstanceCreation) ((ExpressionStatement) statements.get(0)).getExpression();
      assertSame(
          typeDeclaration.getMethods()[1],
          AstNodeUtils.getLocalConstructorDeclaration(creation));
    }
    {
      ClassInstanceCreation creation =
          (ClassInstanceCreation) ((ExpressionStatement) statements.get(1)).getExpression();
      assertNull(AstNodeUtils.getLocalConstructorDeclaration(creation));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getConstructorInvocations()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getConstructorInvocations_1() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  Test() {",
            "    this(1);",
            "  }",
            "  public Test(int i) {",
            "  }",
            "}");
    MethodDeclaration[] methods = typeDeclaration.getMethods();
    //
    List<ConstructorInvocation> invocations = AstNodeUtils.getConstructorInvocations(methods[1]);
    assertThat(invocations).hasSize(1);
    {
      ConstructorInvocation expected =
          (ConstructorInvocation) m_lastEditor.getEnclosingNode("this(1)");
      assertSame(expected, invocations.get(0));
    }
  }

  public void test_getConstructorInvocations_noType_noBinding() throws Exception {
    m_ignoreModelCompileProblems = true;
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  Test() {",
            "    this(null);",
            "  }",
            "  Test(NoSuchType o) {",
            "  }",
            "}");
    // constructor Test(NoSuchType) has no binding
    MethodDeclaration method = typeDeclaration.getMethods()[1];
    assertNull(AstNodeUtils.getMethodBinding(method));
    // ...so, we can not find its invocations
    List<ConstructorInvocation> invocations = AstNodeUtils.getConstructorInvocations(method);
    assertThat(invocations).isEmpty();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getClassInstanceCreations()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstNodeUtils#getClassInstanceCreations(MethodDeclaration)}.
   */
  public void test_getClassInstanceCreations_0() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "// filler filler filler",
            "public class Test {",
            "  public Test(int value) {",
            "  }",
            "}");
    MethodDeclaration[] methods = typeDeclaration.getMethods();
    //
    List<ClassInstanceCreation> invocations = AstNodeUtils.getClassInstanceCreations(methods[0]);
    assertThat(invocations).isEmpty();
  }

  /**
   * Test for {@link AstNodeUtils#getClassInstanceCreations(MethodDeclaration)}.
   */
  public void test_getClassInstanceCreations_2() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  public Test(int value) {",
            "  }",
            "  public static void main(String[] args) {",
            "    new Test(1);",
            "    new Test(2);",
            "  }",
            "}");
    MethodDeclaration[] methods = typeDeclaration.getMethods();
    //
    List<ClassInstanceCreation> invocations = AstNodeUtils.getClassInstanceCreations(methods[0]);
    assertThat(invocations).hasSize(2);
    Assertions.<ASTNode>assertThat(invocations).containsOnly(
        m_lastEditor.getEnclosingNode("new Test(1)"),
        m_lastEditor.getEnclosingNode("new Test(2)"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getConstructors()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstNodeUtils#getConstructors(TypeDeclaration)}.
   */
  public void test_getConstructors() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  public Test(int value) {",
            "  }",
            "  public void foo() {",
            "  }",
            "  public Test(boolean value) {",
            "  }",
            "}");
    MethodDeclaration[] methods = typeDeclaration.getMethods();
    List<MethodDeclaration> constructors = AstNodeUtils.getConstructors(typeDeclaration);
    assertThat(constructors).containsOnly(methods[0], methods[2]);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getClassInstanceCreations(TypeDeclaration)
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstNodeUtils#getClassInstanceCreations(TypeDeclaration)}.
   */
  public void test_getClassInstanceCreations_TypeDeclaration_0() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "// filler filler filler",
            "public class Test {",
            "  public Test(int value) {",
            "  }",
            "  private class Foo {",
            "  }",
            "}");
    TypeDeclaration foo = typeDeclaration.getTypes()[0];
    //
    List<ClassInstanceCreation> invocations = AstNodeUtils.getClassInstanceCreations(foo);
    assertThat(invocations).isEmpty();
  }

  /**
   * Test for {@link AstNodeUtils#getClassInstanceCreations(TypeDeclaration)}.
   */
  public void test_getClassInstanceCreations_TypeDeclaration_2() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  public Test(int value) {",
            "    new Foo(1);",
            "    new Foo(2);",
            "  }",
            "  private class Foo {",
            "    Foo(int value) {}",
            "  }",
            "}");
    TypeDeclaration foo = typeDeclaration.getTypes()[0];
    //
    List<ClassInstanceCreation> invocations = AstNodeUtils.getClassInstanceCreations(foo);
    assertThat(invocations).hasSize(2);
    Assertions.<ASTNode>assertThat(invocations).containsExactly(getNode("new Foo(1)"), getNode("new Foo(2)"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // JavaDoc
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstNodeUtils#getJavaDocTag(BodyDeclaration, String)}.
   */
  public void test_getJavaDocTag() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  /**",
            "  * @my.tag",
            "  */",
            "  private int m_foo;",
            "  /**",
            "  * Not a tag.",
            "  */",
            "  private int m_bar;",
            "  /**",
            "  * @second.tag aaa bbb",
            "  */",
            "  private int m_baz;",
            "  private int m_goo;",
            "}");
    {
      FieldDeclaration fooField = typeDeclaration.getFields()[0];
      assertNull(AstNodeUtils.getJavaDocTag(fooField, "@noSuchTag"));
      TagElement tagElement = AstNodeUtils.getJavaDocTag(fooField, "@my.tag");
      assertNotNull(tagElement);
      assertEquals("@my.tag", m_lastEditor.getSource(tagElement));
    }
    {
      FieldDeclaration barField = typeDeclaration.getFields()[1];
      assertNull(AstNodeUtils.getJavaDocTag(barField, "@noAnyTag"));
    }
    {
      FieldDeclaration bazField = typeDeclaration.getFields()[2];
      TagElement tagElement = AstNodeUtils.getJavaDocTag(bazField, "@second.tag");
      assertNotNull(tagElement);
      assertEquals("@second.tag aaa bbb", m_lastEditor.getSource(tagElement));
    }
    {
      FieldDeclaration gooField = typeDeclaration.getFields()[3];
      assertFalse(AstNodeUtils.hasJavaDocTag(gooField, "@noAnyTag"));
    }
  }

  /**
   * Test for {@link AstNodeUtils#hasJavaDocTag(BodyDeclaration, String)}.
   */
  public void test_hasJavaDocTag() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  /**",
            "  * @my.tag",
            "  */",
            "  private int m_foo;",
            "  /**",
            "  * Not a tag.",
            "  */",
            "  private int m_bar;",
            "  private int m_goo;",
            "}");
    {
      FieldDeclaration fooField = typeDeclaration.getFields()[0];
      assertTrue(AstNodeUtils.hasJavaDocTag(fooField, "@my.tag"));
      assertFalse(AstNodeUtils.hasJavaDocTag(fooField, "@noSuchTag"));
    }
    {
      FieldDeclaration barField = typeDeclaration.getFields()[1];
      assertFalse(AstNodeUtils.hasJavaDocTag(barField, "@noAnyTag"));
    }
    {
      FieldDeclaration gooField = typeDeclaration.getFields()[2];
      assertFalse(AstNodeUtils.hasJavaDocTag(gooField, "@noAnyTag"));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Matching
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstNodeUtils#isMethodInvocation(ASTNode, String)}.
   */
  public void test_isMethodInvocation_onlySignature() throws Exception {
    createTypeDeclaration_Test(
        "public class Test {",
        "  private void root() {",
        "    System.exit(0);",
        "    exit(1);",
        "  }",
        "  private void exit(int code) {",
        "  }",
        "}");
    // System.exit(int)
    {
      ASTNode node = m_lastEditor.getEnclosingNode("(0)");
      assertTrue(AstNodeUtils.isMethodInvocation(node, "exit(int)"));
      assertFalse(AstNodeUtils.isMethodInvocation(node, "incorrectSignature()"));
    }
    // local exit(int)
    {
      ASTNode node = m_lastEditor.getEnclosingNode("(1)");
      assertTrue(AstNodeUtils.isMethodInvocation(node, "exit(int)"));
    }
    // not a MethodInvocation
    {
      ASTNode node = m_lastEditor.getEnclosingNode("Test");
      assertFalse(AstNodeUtils.isMethodInvocation(node, "notMethodInvocation()"));
    }
  }

  /**
   * Test for {@link AstNodeUtils#isMethodInvocation(ASTNode, String, String)}.
   */
  public void test_isMethodInvocation_1() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  Object m_1 = java.util.Arrays.toString(new int[] {});",
            "  Object m_2 = null;",
            "  int m_3 = hashCode();",
            "}");
    // correct and incorrect type/method
    {
      Expression initializer = getFieldFragment(typeDeclaration, 0).getInitializer();
      assertTrue(AstNodeUtils.isMethodInvocation(initializer, "java.util.Arrays", "toString(int[])"));
      assertFalse(AstNodeUtils.isMethodInvocation(
          initializer,
          "java.util.Arrays",
          "toString(long[])"));
      assertFalse(AstNodeUtils.isMethodInvocation(
          initializer,
          "java.util.Collections",
          "no-such-signature"));
    }
    // not a MethodInvocation
    {
      Expression initializer = getFieldFragment(typeDeclaration, 1).getInitializer();
      assertFalse(AstNodeUtils.isMethodInvocation(initializer, "not-a-class", "no-such-signature"));
    }
    // local ("null" expression) invocation
    {
      Expression initializer = getFieldFragment(typeDeclaration, 2).getInitializer();
      assertTrue(AstNodeUtils.isMethodInvocation(initializer, null, "hashCode()"));
    }
  }

  /**
   * Test for {@link AstNodeUtils#isMethodInvocation(ASTNode, String, String)}.
   * <p>
   * Test that methods of subclasses are also accepted.
   */
  public void test_isMethodInvocation_2() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  int m_1 = new java.util.ArrayList().size();",
            "}");
    Expression initializer = getFieldFragment(typeDeclaration, 0).getInitializer();
    // ask as method of superclass
    assertTrue(AstNodeUtils.isMethodInvocation(initializer, "java.util.List", "size()"));
  }

  /**
   * Test for {@link AstNodeUtils#isMethodInvocation(ASTNode, String, String)}.
   */
  public void test_isMethodInvocation_SuperMethodInvocation() throws Exception {
    createTypeDeclaration_Test(
        "public class Test {",
        "  public Test() {",
        "    int value = 123;",
        "    super.hashCode();",
        "  }",
        "}");
    // correct and incorrect type/method
    {
      Expression expression = getNode(".hashCode", SuperMethodInvocation.class);
      assertTrue(AstNodeUtils.isMethodInvocation(expression, "java.lang.Object", "hashCode()"));
      assertFalse(AstNodeUtils.isMethodInvocation(expression, "wrong.Class", "hashCode()"));
      assertFalse(AstNodeUtils.isMethodInvocation(
          expression,
          "java.lang.Object",
          "no-such-signature"));
    }
    // not a SuperMethodInvocation
    {
      Expression expression = getNode("123");
      assertFalse(AstNodeUtils.isMethodInvocation(expression, "not-a-class", "no-such-signature"));
    }
  }

  /**
   * Test for {@link AstNodeUtils#isMethodInvocation(ASTNode, String, String[])}.
   */
  public void test_isMethodInvocation_signatures_1() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  Object m_1 = java.util.Arrays.toString(new int[] {});",
            "  Object m_2 = null;",
            "  int m_3 = hashCode();",
            "}");
    // correct and incorrect type/method
    {
      Expression initializer = getFieldFragment(typeDeclaration, 0).getInitializer();
      // matches
      assertTrue(AstNodeUtils.isMethodInvocation(
          initializer,
          "java.util.Arrays",
          new String[]{"toString(int[])"}));
      // different signature
      assertFalse(AstNodeUtils.isMethodInvocation(
          initializer,
          "java.util.Arrays",
          new String[]{"toString(long[])"}));
      // invalid signature
      assertFalse(AstNodeUtils.isMethodInvocation(
          initializer,
          "java.util.Collections",
          new String[]{"no-such-signature"}));
      // invalid signature, but second signature matches
      assertFalse(AstNodeUtils.isMethodInvocation(
          initializer,
          "java.util.Collections",
          new String[]{"no-such-signature", "toString(int[])"}));
    }
    // not a MethodInvocation
    {
      Expression initializer = getFieldFragment(typeDeclaration, 1).getInitializer();
      assertFalse(AstNodeUtils.isMethodInvocation(
          initializer,
          "not-a-class",
          new String[]{"no-such-signature"}));
    }
    // local ("null" expression) invocation
    {
      Expression initializer = getFieldFragment(typeDeclaration, 2).getInitializer();
      assertTrue(AstNodeUtils.isMethodInvocation(initializer, null, new String[]{"hashCode()"}));
      assertFalse(AstNodeUtils.isMethodInvocation(
          initializer,
          "java.util.Collection",
          new String[]{"hashCode()"}));
    }
  }

  /**
   * Test for {@link AstNodeUtils#isMethodInvocation(ASTNode, String, String[])}.<br>
   * Test that methods of subclasses are also accepted.
   */
  public void test_isMethodInvocation_signatures_2() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  int m_1 = new java.util.ArrayList().size();",
            "}");
    Expression initializer = getFieldFragment(typeDeclaration, 0).getInitializer();
    // ask as method of superclass
    assertTrue(AstNodeUtils.isMethodInvocation(
        initializer,
        "java.util.List",
        new String[]{"size()"}));
  }

  public void test_isCreation() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  Object m_1 = new java.util.ArrayList(0);",
            "  Object m_2 = null;",
            "}");
    // correct and incorrect type/signature
    {
      Expression initializer = getFieldFragment(typeDeclaration, 0).getInitializer();
      assertTrue(AstNodeUtils.isCreation(initializer, "java.util.ArrayList", "<init>(int)"));
      assertFalse(AstNodeUtils.isCreation(initializer, "java.util.List", "<init>(int)"));
      assertFalse(AstNodeUtils.isCreation(initializer, "java.util.ArrayList", "invalid-signature"));
    }
    // several signatures
    {
      Expression initializer = getFieldFragment(typeDeclaration, 0).getInitializer();
      assertTrue(AstNodeUtils.isCreation(initializer, "java.util.ArrayList", new String[]{
          "<init>(int)",
          "<init>()"}));
      assertFalse(AstNodeUtils.isCreation(initializer, "java.util.ArrayList", new String[]{
          "<init>()",
          "<init>()"}));
    }
    // not a ClassInstanceCreation
    {
      Expression initializer = getFieldFragment(typeDeclaration, 1).getInitializer();
      assertFalse(AstNodeUtils.isCreation(initializer, "not-a-class", "no-such-signature"));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getMethodBySignature()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstNodeUtils#getMethodBySignature(ITypeBinding, String)}.
   */
  public void test_getMethodBySignature() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "import java.util.ArrayList;",
            "public class Test extends ArrayList {",
            "  public void foo() {",
            "  }",
            "}");
    ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(typeDeclaration);
    // test.Test.foo()
    {
      IMethodBinding method = AstNodeUtils.getMethodBySignature(typeBinding, "foo()");
      String declaringType = AstNodeUtils.getFullyQualifiedName(method.getDeclaringClass(), false);
      assertEquals("test.Test", declaringType);
    }
    // java.util.ArrayList.get(int)
    {
      IMethodBinding method = AstNodeUtils.getMethodBySignature(typeBinding, "get(int)");
      String declaringType = AstNodeUtils.getFullyQualifiedName(method.getDeclaringClass(), false);
      assertEquals("java.util.ArrayList", declaringType);
    }
    // no such method
    {
      assertNull(AstNodeUtils.getMethodBySignature(typeBinding, "noSuchMethod()"));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getMethodSignature(MethodInvocation)
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getMethodSignature_forMethodInvocation() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_TestC("void foo(){bar(1);} void bar(int i){}");
    MethodDeclaration[] methods = typeDeclaration.getMethods();
    //
    List<MethodInvocation> invocations = AstNodeUtils.getMethodInvocations(methods[1]);
    assertEquals(1, invocations.size());
    //
    MethodInvocation invocation = invocations.get(0);
    assertEquals("bar(int)", getMethodSignature(invocation));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getMainType
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getMainType_1() throws Exception {
    CompilationUnit compilationUnit =
        createASTCompilationUnit("test", "Test.java", "public class Test{} class Foo{}");
    TypeDeclaration mainType = AstNodeUtils.getTypeByName(compilationUnit, "Test");
    assertNotNull(mainType);
    assertEquals("Test", mainType.getName().getIdentifier());
  }

  public void test_getMainType_2() throws Exception {
    CompilationUnit compilationUnit =
        createASTCompilationUnit("test", "Test.java", "class Foo{} public class Test{}");
    TypeDeclaration mainType = AstNodeUtils.getTypeByName(compilationUnit, "Test");
    assertNotNull(mainType);
    assertEquals("Test", mainType.getName().getIdentifier());
  }

  public void test_getMainType_3() throws Exception {
    CompilationUnit compilationUnit =
        createASTCompilationUnit("test", "Test.java", "class Foo{} class Test{}");
    TypeDeclaration mainType = AstNodeUtils.getTypeByName(compilationUnit, "Test");
    assertNotNull(mainType);
    assertEquals("Test", mainType.getName().getIdentifier());
  }

  public void test_getMainType_4() throws Exception {
    CompilationUnit compilationUnit =
        createASTCompilationUnit("test", "Test.java", "class Foo{} class Bar{}");
    TypeDeclaration mainType = AstNodeUtils.getTypeByName(compilationUnit, "Test");
    assertNull(mainType);
  }

  /**
   * There was problem in JDT when using unknown generics caused absence for {@link ITypeBinding}s
   * not only for this type, but globally in {@link CompilationUnit}. So, we were not able to find
   * required {@link TypeDeclaration}. However in Eclipse 3.6.1 this problem was fixed.
   */
  public void test_getMainType_noTypeBinding() throws Exception {
    m_ignoreModelCompileProblems = true;
    TypeDeclaration typeDeclaration =
        createTypeDeclaration(
            "test",
            "Test.java",
            getSource(
                "// filler filler filler",
                "package test;",
                "public class Test {",
                "  public EntityCombo<ArticleFormat> foo;",
                "  public Test() {",
                "    foo.bar();",
                "  }",
                "}"));
    // has binding
    assertNotNull(typeDeclaration.resolveBinding());
    // ...so, can find TypeDeclaration
    CompilationUnit compilationUnit = (CompilationUnit) typeDeclaration.getParent();
    TypeDeclaration actual = AstNodeUtils.getTypeByName(compilationUnit, "Test");
    assertSame(typeDeclaration, actual);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getTypeDeclaration
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getFullyQualifiedName_TypeDeclaration() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "// filler filler filler",
            "class Test {",
            "  private class A {}",
            "}");
    assertEquals("test.Test", AstNodeUtils.getFullyQualifiedName(typeDeclaration, false));
    assertEquals(
        "test.Test.A",
        AstNodeUtils.getFullyQualifiedName(typeDeclaration.getTypes()[0], false));
  }

  /**
   * Test for {@link AstNodeUtils#getTypeDeclaration(ClassInstanceCreation)}.
   */
  public void test_getTypeDeclaration_inner() throws Exception {
    createTypeDeclaration_Test(
        "class Test {",
        "  private class A {}",
        "  void foo() {",
        "    new A();",
        "    new Integer(0);",
        "  }",
        "}");
    // new A()
    {
      ClassInstanceCreation creation = getNode("new A(");
      TypeDeclaration typeDeclaration = AstNodeUtils.getTypeDeclaration(creation);
      assertNotNull(typeDeclaration);
    }
    // new Integer(0)
    {
      ClassInstanceCreation creation = getNode("new Integer(");
      TypeDeclaration typeDeclaration = AstNodeUtils.getTypeDeclaration(creation);
      assertNull(typeDeclaration);
    }
  }

  /**
   * Test for {@link AstNodeUtils#getTypeDeclaration(ClassInstanceCreation)}.
   */
  public void test_getTypeDeclaration_local() throws Exception {
    createTypeDeclaration_Test(
        "class Test {",
        "  void foo() {",
        "    class A {}",
        "    new A();",
        "  }",
        "}");
    // new A()
    {
      ClassInstanceCreation creation = getNode("new A(");
      TypeDeclaration typeDeclaration = AstNodeUtils.getTypeDeclaration(creation);
      assertNotNull(typeDeclaration);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getMethodSignature(MethodDeclaration)/getMethodBySignature
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When {@link IMethodBinding} is <code>null</code>, the
   * {@link AstNodeUtils#NO_METHOD_BINDING_SIGNATURE} is returned.
   */
  public void test_getMethodSignature_null() throws Exception {
    assertSame(AstNodeUtils.NO_METHOD_BINDING_SIGNATURE, getMethodSignature((IMethodBinding) null));
  }

  public void test_getMethodSignature_constructor() throws Exception {
    TypeDeclaration typeDeclaration;
    {
      String code = "Test() {} Test(int i, String s) {}";
      typeDeclaration = createTypeDeclaration_TestC(code);
    }
    MethodDeclaration[] methods = typeDeclaration.getMethods();
    //
    assertEquals("<init>()", getMethodSignature(methods[0]));
    assertEquals("<init>(int,java.lang.String)", getMethodSignature(methods[1]));
  }

  public void test_getMethodSignature() throws Exception {
    TypeDeclaration typeDeclaration;
    {
      String code =
          "void m_0() {}"
              + "void m_1(int p1) {}"
              + "void m_2(String p1) {}"
              + "int m_3(String p1, java.util.ArrayList p2) {return 0;}"
              + "java.util.Map m_4(String p1) {return null;}";
      typeDeclaration = createTypeDeclaration_TestC(code);
    }
    MethodDeclaration[] methods = typeDeclaration.getMethods();
    // getMethodSignature
    {
      assertEquals("m_0()", getMethodSignature(methods[0]));
      assertEquals("m_1(int)", getMethodSignature(methods[1]));
      assertEquals("m_2(java.lang.String)", getMethodSignature(methods[2]));
      assertEquals("m_3(java.lang.String,java.util.ArrayList)", getMethodSignature(methods[3]));
      assertEquals("m_4(java.lang.String)", getMethodSignature(methods[4]));
      //
      while (true) {
        try {
          getMethodSignature((MethodDeclaration) null);
        } catch (AssertionFailedException e) {
          break;
        }
        fail();
      }
    }
    // getMethodBySignature
    {
      assertSame(methods[0], AstNodeUtils.getMethodBySignature(typeDeclaration, "m_0()"));
      assertSame(methods[1], AstNodeUtils.getMethodBySignature(typeDeclaration, "m_1(int)"));
      assertSame(
          methods[2],
          AstNodeUtils.getMethodBySignature(typeDeclaration, "m_2(java.lang.String)"));
      assertSame(methods[3], AstNodeUtils.getMethodBySignature(
          typeDeclaration,
          "m_3(java.lang.String,java.util.ArrayList)"));
      assertSame(
          methods[4],
          AstNodeUtils.getMethodBySignature(typeDeclaration, "m_4(java.lang.String)"));
      assertNull(AstNodeUtils.getMethodBySignature(typeDeclaration, ""));
      assertNull(AstNodeUtils.getMethodBySignature(typeDeclaration, "m_0(int)"));
      assertNull(AstNodeUtils.getMethodBySignature(typeDeclaration, "m_X(int)"));
      //
      while (true) {
        try {
          AstNodeUtils.getMethodBySignature((TypeDeclaration) null, "");
        } catch (AssertionFailedException e) {
          break;
        }
        fail();
      }
      while (true) {
        try {
          AstNodeUtils.getMethodBySignature(typeDeclaration, null);
        } catch (AssertionFailedException e) {
          break;
        }
        fail();
      }
    }
  }

  /**
   * Test for {@link AstNodeUtils#getMethodGenericSignature(IMethodBinding)} and
   * {@link AstNodeUtils#getMethodDeclarationSignature(IMethodBinding)}
   */
  public void test_getMethodGenericSignature_noBounds() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getSourceDQ(
            "package test;",
            "import javax.swing.*;",
            "public class MyPanel extends JPanel {",
            "  public <T> void foo(int v, T value) {",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    createTypeDeclaration_Test(
        "import javax.swing.*;",
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    foo(0, new JButton());",
        "  }",
        "}");
    MethodInvocation invocation = getNode("foo(", MethodInvocation.class);
    IMethodBinding binding = AstNodeUtils.getMethodBinding(invocation);
    // check signatures
    for (int i = 0; i < 2; i++) {
      if (i != 0) {
        binding = new BindingContext().get(binding);
      }
      assertEquals("foo(int,javax.swing.JButton)", getMethodSignature(binding));
      assertEquals("foo(int,T)", getMethodGenericSignature(binding));
      assertEquals("foo(int,java.lang.Object)", getMethodDeclarationSignature(binding));
    }
  }

  /**
   * Test for {@link AstNodeUtils#getMethodGenericSignature(IMethodBinding)} and
   * {@link AstNodeUtils#getMethodDeclarationSignature(IMethodBinding)}
   */
  public void test_getMethodGenericSignature_extendsClass() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getSourceDQ(
            "package test;",
            "import javax.swing.*;",
            "public class MyPanel extends JPanel {",
            "  public <T extends JButton> void foo(int v, T value) {",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    createTypeDeclaration_Test(
        "import javax.swing.*;",
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    foo(0, new JButton());",
        "  }",
        "}");
    MethodInvocation invocation = getNode("foo(", MethodInvocation.class);
    IMethodBinding binding = AstNodeUtils.getMethodBinding(invocation);
    // check signatures
    for (int i = 0; i < 2; i++) {
      if (i != 0) {
        binding = new BindingContext().get(binding);
      }
      assertEquals("foo(int,javax.swing.JButton)", getMethodSignature(binding));
      assertEquals("foo(int,T)", getMethodGenericSignature(binding));
      assertEquals("foo(int,javax.swing.JButton)", getMethodDeclarationSignature(binding));
    }
  }

  /**
   * Test for {@link AstNodeUtils#getMethodGenericSignature(IMethodBinding)} and
   * {@link AstNodeUtils#getMethodDeclarationSignature(IMethodBinding)}
   */
  public void test_getMethodGenericSignature_extendsInterface() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getSourceDQ(
            "package test;",
            "import javax.swing.*;",
            "public class MyPanel extends JPanel {",
            "  public <T extends java.util.List> void foo(int v, T value) {",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    createTypeDeclaration_Test(
        "import javax.swing.*;",
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    foo(0, new java.util.ArrayList());",
        "  }",
        "}");
    MethodInvocation invocation = getNode("foo(", MethodInvocation.class);
    IMethodBinding binding = AstNodeUtils.getMethodBinding(invocation);
    // check signatures
    for (int i = 0; i < 2; i++) {
      if (i != 0) {
        binding = new BindingContext().get(binding);
      }
      assertEquals("foo(int,java.util.ArrayList)", getMethodSignature(binding));
      assertEquals("foo(int,T)", getMethodGenericSignature(binding));
      assertEquals("foo(int,java.util.List)", getMethodDeclarationSignature(binding));
    }
  }

  /**
   * Test for {@link AstNodeUtils#getMethodSignatures(List)}.
   */
  public void test_getMethodSignatures() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  public void foo() {",
            "  }",
            "  public void bar(int a, String b) {",
            "  }",
            "}");
    MethodDeclaration[] methods = typeDeclaration.getMethods();
    //
    List<String> signatures =
        AstNodeUtils.getMethodSignatures(ImmutableList.of(methods[0], methods[1]));
    assertThat(signatures).hasSize(2).isEqualTo(
        ImmutableList.of("foo()", "bar(int,java.lang.String)"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getMethodByName()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstNodeUtils#getMethodByName(TypeDeclaration, String)}.
   */
  public void test_getMethodByName() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "import java.util.ArrayList;",
            "public class Test extends ArrayList {",
            "  public void foo(int a) {",
            "  }",
            "}");
    {
      MethodDeclaration actual = AstNodeUtils.getMethodByName(typeDeclaration, "foo");
      assertSame(typeDeclaration.getMethods()[0], actual);
    }
    {
      MethodDeclaration actual = AstNodeUtils.getMethodByName(typeDeclaration, "noSuchMethod");
      assertSame(null, actual);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getMethodBinding
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getMethodBinding_2_declaration() throws Exception {
    TypeDeclaration typeDeclaration;
    typeDeclaration = createTypeDeclaration_TestC("int foo(String s) {return 0;}");
    MethodDeclaration[] methods = typeDeclaration.getMethods();
    //
    IMethodBinding binding = AstNodeUtils.getMethodBinding(methods[0]);
    assertEquals("foo(java.lang.String)", getMethodSignature(binding));
  }

  /**
   * Test for {@link AstNodeUtils#getMethodBinding(MethodDeclaration)}.
   */
  public void test_getMethodBinding_MethodDeclaration_noBinding() throws Exception {
    m_ignoreModelCompileProblems = true;
    TypeDeclaration typeDeclaration =
        createTypeDeclaration(
            "test",
            "Test.java",
            getSource(
                "package test;",
                "public class Test {",
                "  private badMethod(Foo foo) {",
                "  }",
                "}"));
    MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
    //
    assertSame(null, AstNodeUtils.getMethodBinding(methodDeclaration));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ConstructorInvocation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstNodeUtils#getSignature(ConstructorInvocation)} and
   * {@link AstNodeUtils#getConstructor(ConstructorInvocation)}.
   */
  public void test_ConstructorInvocation() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  public Test() {",
            "    this(false, 2);",
            "  }",
            "  public Test(boolean b, int i) {",
            "  }",
            "}");
    ConstructorInvocation invocation =
        (ConstructorInvocation) m_lastEditor.getEnclosingNode("this(");
    assertEquals("<init>(boolean,int)", AstNodeUtils.getSignature(invocation));
    assertSame(typeDeclaration.getMethods()[1], AstNodeUtils.getConstructor(invocation));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getMethodBinding() for SuperMethodInvocation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstNodeUtils#getMethodBinding(SuperMethodInvocation)}.
   */
  public void test_getMethodBinding_SuperMethodInvocation() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  public String toString() {",
            "    super.toString();",
            "    return null;",
            "  }",
            "}");
    MethodDeclaration toStringMethod = typeDeclaration.getMethods()[0];
    ExpressionStatement firstStatement =
        (ExpressionStatement) toStringMethod.getBody().statements().get(0);
    SuperMethodInvocation superInvocation = (SuperMethodInvocation) firstStatement.getExpression();
    // check signature
    IMethodBinding binding = AstNodeUtils.getMethodBinding(superInvocation);
    assertEquals("toString()", getMethodSignature(binding));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getCreationSignature
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstNodeUtils#getCreationSignature(ClassInstanceCreation)}.<br>
   * Case when bindings are from JDT.
   */
  public void test_getCreationSignature_1() throws Exception {
    TypeDeclaration typeDeclaration;
    {
      String code =
          "javax.swing.JButton button_1 = new javax.swing.JButton();\n"
              + "javax.swing.JButton button_2 = new javax.swing.JButton(\"abc\");\n"
              + "javax.swing.JButton button_3 = new javax.swing.JButton(\"abc\", null);\n";
      typeDeclaration = createTypeDeclaration_TestC(code);
    }
    // getCreationSignature
    {
      check_getCreationSignature(typeDeclaration, 0, "<init>()");
      check_getCreationSignature(typeDeclaration, 1, "<init>(java.lang.String)");
      check_getCreationSignature(typeDeclaration, 2, "<init>(java.lang.String,javax.swing.Icon)");
    }
  }

  /**
   * Test for {@link AstNodeUtils#getCreationSignature(ClassInstanceCreation)}.<br>
   * Case when bindings are from just parsed {@link ClassInstanceCreation}.
   */
  public void test_getCreationSignature_2() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "// filler filler filler",
            "// filler filler filler",
            "public class Test {",
            "}");
    // add new field
    m_lastEditor.addFieldDeclaration(
        "private Integer value = new Integer(5);",
        new BodyDeclarationTarget(typeDeclaration, true));
    assertEditor(
        getSourceDQ(
            "package test;",
            "// filler filler filler",
            "// filler filler filler",
            "public class Test {",
            "  private Integer value = new Integer(5);",
            "}"),
        m_lastEditor);
    // getCreationSignature()
    check_getCreationSignature(typeDeclaration, 0, "<init>(int)");
  }

  private static void check_getCreationSignature(TypeDeclaration typeDeclaration,
      int fieldIndex,
      String expectedSignature) throws Exception {
    FieldDeclaration[] fields = typeDeclaration.getFields();
    VariableDeclarationFragment fragment =
        (VariableDeclarationFragment) fields[fieldIndex].fragments().get(0);
    ClassInstanceCreation initializer = (ClassInstanceCreation) fragment.getInitializer();
    assertEquals(expectedSignature, AstNodeUtils.getCreationSignature(initializer));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // SuperConstructorInvocation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Tests for {@link AstNodeUtils#getSuperSignature(SuperConstructorInvocation)} and
   * {@link AstNodeUtils#getSuperBinding(SuperConstructorInvocation)}.
   */
  public void test_SuperConstructorInvocation() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "import java.awt.*;",
            "import javax.swing.*;",
            "public class Test extends JPanel {",
            "  public Test() {",
            "    super(new BorderLayout());",
            "  }",
            "}");
    // prepare SuperConstructorInvocation
    SuperConstructorInvocation invocation;
    {
      MethodDeclaration constructor = typeDeclaration.getMethods()[0];
      List<Statement> statements = DomGenerics.statements(constructor.getBody());
      invocation = (SuperConstructorInvocation) statements.get(0);
    }
    // getSuperSignature()
    {
      String signature = AstNodeUtils.getSuperSignature(invocation);
      assertEquals("<init>(java.awt.LayoutManager)", signature);
    }
    // getSuperBinding()
    {
      IMethodBinding binding = AstNodeUtils.getSuperBinding(invocation);
      String signature = getMethodSignature(binding);
      assertEquals("<init>(java.awt.LayoutManager)", signature);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Source range test
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getSourceX() throws Exception {
    TypeDeclaration typeDeclaration = createTypeDeclaration_TestC("int m_value;");
    FieldDeclaration fieldDeclaration = typeDeclaration.getFields()[0];
    assertEquals(fieldDeclaration.getStartPosition(), AstNodeUtils.getSourceBegin(fieldDeclaration));
    assertEquals(
        fieldDeclaration.getStartPosition() + fieldDeclaration.getLength(),
        AstNodeUtils.getSourceEnd(fieldDeclaration));
  }

  /**
   * Test for {@link AstNodeUtils#setSourceBegin(ASTNode, int)}.
   */
  public void test_setSourceBegin() throws Exception {
    TypeDeclaration typeDeclaration = createTypeDeclaration_TestC("int m_value;");
    FieldDeclaration fieldDeclaration = typeDeclaration.getFields()[0];
    //
    int newBegin = 555;
    int oldLength = fieldDeclaration.getLength();
    AstNodeUtils.setSourceBegin(fieldDeclaration, newBegin);
    assertEquals(newBegin, fieldDeclaration.getStartPosition());
    assertEquals(oldLength, fieldDeclaration.getLength());
  }

  /**
   * Test for {@link AstNodeUtils#setSourceBegin_keepEnd(ASTNode, int)}.
   */
  public void test_setSourceBegin_keepEnd() throws Exception {
    TypeDeclaration typeDeclaration = createTypeDeclaration_TestC("");
    //
    SimpleName node = typeDeclaration.getAST().newSimpleName("node");
    node.setSourceRange(10, 20);
    AstNodeUtils.setSourceBegin_keepEnd(node, 15);
    assertEquals(15, node.getStartPosition());
    assertEquals(15, node.getLength());
  }

  /**
   * Test for {@link AstNodeUtils#setSourceEnd(ASTNode, ASTNode)}.
   */
  public void test_setSourceEnd() throws Exception {
    TypeDeclaration typeDeclaration = createTypeDeclaration_TestC("int m_value;");
    SimpleName sourceNode = getNode("m_value");
    // just fake source range, only for test
    ASTNode targetNode = typeDeclaration.getAST().newSimpleName("foo");
    targetNode.setSourceRange(10, 0);
    AstNodeUtils.setSourceEnd(targetNode, sourceNode);
    assertEquals(10, targetNode.getStartPosition());
    assertEquals(AstNodeUtils.getSourceEnd(sourceNode), AstNodeUtils.getSourceEnd(targetNode));
  }

  /**
   * Test for {@link AstNodeUtils#setSourceLength(ASTNode, int)}.
   */
  public void test_setSourceLength() throws Exception {
    TypeDeclaration typeDeclaration = createTypeDeclaration_TestC("int m_value;");
    FieldDeclaration fieldDeclaration = typeDeclaration.getFields()[0];
    //
    int newLength = 50;
    int oldPosition = fieldDeclaration.getStartPosition();
    AstNodeUtils.setSourceLength(fieldDeclaration, newLength);
    assertEquals(oldPosition, fieldDeclaration.getStartPosition());
    assertEquals(newLength, fieldDeclaration.getLength());
  }

  public void test_copySourceRange() throws Exception {
    TypeDeclaration typeDeclaration = createTypeDeclaration_TestC("int m_value;");
    FieldDeclaration fieldDeclaration = typeDeclaration.getFields()[0];
    //
    ASTNode targetNode = typeDeclaration.getAST().newSimpleName("foo");
    AstNodeUtils.copySourceRange(targetNode, fieldDeclaration);
    assertEquals(fieldDeclaration.getStartPosition(), targetNode.getStartPosition());
    assertEquals(fieldDeclaration.getLength(), targetNode.getLength());
  }

  public void test_setSourceRange_1() throws Exception {
    TypeDeclaration typeDeclaration = createTypeDeclaration_TestC("int m_value;");
    FieldDeclaration fieldDeclaration = typeDeclaration.getFields()[0];
    VariableDeclarationFragment fragment =
        (VariableDeclarationFragment) fieldDeclaration.fragments().get(0);
    // just fake source range, only for test
    ASTNode targetNode = typeDeclaration.getAST().newSimpleName("foo");
    AstNodeUtils.setSourceRange(targetNode, fieldDeclaration.getType(), fragment.getName());
    assertEquals(fieldDeclaration.getStartPosition(), targetNode.getStartPosition());
    assertEquals(fieldDeclaration.getLength() - ";".length(), targetNode.getLength());
  }

  public void test_setSourceRange_2() throws Exception {
    TypeDeclaration typeDeclaration = createTypeDeclaration_TestC("int m_value;");
    FieldDeclaration fieldDeclaration = typeDeclaration.getFields()[0];
    VariableDeclarationFragment fragment =
        (VariableDeclarationFragment) fieldDeclaration.fragments().get(0);
    // just fake source range, only for test
    ASTNode targetNode = typeDeclaration.getAST().newSimpleName("foo");
    AstNodeUtils.setSourceRange(targetNode, fieldDeclaration.getType(), fragment.getName(), 1);
    assertEquals(fieldDeclaration.getStartPosition(), targetNode.getStartPosition());
    assertEquals(fieldDeclaration.getLength(), targetNode.getLength());
  }

  public void test_setSourceRange_3() throws Exception {
    TypeDeclaration typeDeclaration = createTypeDeclaration_TestC("int m_value;");
    FieldDeclaration fieldDeclaration = typeDeclaration.getFields()[0];
    // just fake source range, only for test
    ASTNode targetNode = typeDeclaration.getAST().newSimpleName("foo");
    AstNodeUtils.setSourceRange(targetNode, fieldDeclaration, 1);
    assertEquals(fieldDeclaration.getStartPosition(), targetNode.getStartPosition());
    assertEquals(fieldDeclaration.getLength() + 1, targetNode.getLength());
  }

  public void test_moveNode() throws Exception {
    TypeDeclaration typeDeclaration = createTypeDeclaration_TestC("int m_value;");
    FieldDeclaration fieldDeclaration = typeDeclaration.getFields()[0];
    // remember ranges
    final LinkedList<Integer> ranges = Lists.newLinkedList();
    fieldDeclaration.accept(new ASTVisitor() {
      @Override
      public void preVisit(ASTNode node) {
        ranges.add(node.getStartPosition());
        ranges.add(node.getLength());
      }
    });
    // do move
    int targetPosition = 1;
    final int delta = targetPosition - fieldDeclaration.getStartPosition();
    AstNodeUtils.moveNode(fieldDeclaration, targetPosition);
    // compare ranges
    fieldDeclaration.accept(new ASTVisitor() {
      @Override
      public void preVisit(ASTNode node) {
        assertEquals(ranges.removeFirst(), Integer.valueOf(node.getStartPosition() - delta));
        assertEquals(ranges.removeFirst(), Integer.valueOf(node.getLength()));
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // SORT_BY_POSITION, SORT_BY_REVERSE_POSITION
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_comparators() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_TestC("void foo(){int a; int b; int c;}");
    //
    Statement statement_1, statement_2, statement_3;
    {
      List<Statement> statements =
          DomGenerics.statements(typeDeclaration.getMethods()[0].getBody());
      statement_1 = statements.get(0);
      statement_2 = statements.get(1);
      statement_3 = statements.get(2);
    }
    //
    Statement[] statements = new Statement[]{statement_1, statement_3, statement_2};
    // forward comparator
    Arrays.sort(statements, AstNodeUtils.SORT_BY_POSITION);
    assertTrue(ArrayUtils.isEquals(
        new Statement[]{statement_1, statement_2, statement_3},
        statements));
    // reverse comparator
    Arrays.sort(statements, AstNodeUtils.SORT_BY_REVERSE_POSITION);
    assertTrue(ArrayUtils.isEquals(
        new Statement[]{statement_3, statement_2, statement_1},
        statements));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // isDangling
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_isDangling() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_TestC("void foo(){int a; int b; int c;}");
    MethodDeclaration method = typeDeclaration.getMethods()[0];
    Statement statement = (Statement) method.getBody().statements().get(0);
    assertFalse(AstNodeUtils.isDanglingNode(statement));
    m_lastEditor.removeStatement(statement);
    assertTrue(AstNodeUtils.isDanglingNode(statement));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // contains
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_contains() throws Exception {
    TypeDeclaration typeDeclaration = createTypeDeclaration_TestC("int a; int b;");
    FieldDeclaration field_1 = typeDeclaration.getFields()[0];
    FieldDeclaration field_2 = typeDeclaration.getFields()[1];
    assertTrue(AstNodeUtils.contains(field_1, field_1.getType()));
    assertFalse(AstNodeUtils.contains(field_1, field_2));
    assertFalse(AstNodeUtils.contains(field_1, field_1));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // removeDanglingNodes()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstNodeUtils#removeDanglingNodes(Iterable)}.
   */
  public void test_removeDanglingNodes() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration(
            "test",
            "Test.java",
            getSource(
                "package test;",
                "public class Test {",
                "  int field_1;",
                "  int field_2;",
                "}"));
    FieldDeclaration field_1 = typeDeclaration.getFields()[0];
    FieldDeclaration field_2 = typeDeclaration.getFields()[1];
    List<FieldDeclaration> fields = Lists.newArrayList(field_1, field_2);
    // initial state
    assertThat(fields).containsExactly(field_1, field_2);
    // remove dangling, nothing changed
    AstNodeUtils.removeDanglingNodes(fields);
    assertThat(fields).containsExactly(field_1, field_2);
    // remove "field_1", our fields are not changed
    m_lastEditor.removeBodyDeclaration(field_1);
    assertThat(fields).containsExactly(field_1, field_2);
    // remove dangling, so "field_1"
    AstNodeUtils.removeDanglingNodes(fields);
    assertThat(fields).containsExactly(field_2);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Literals
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstNodeUtils#isLiteral(Expression)}.
   */
  public void test_isLiteral_simple() throws Exception {
    createASTCompilationUnit("test", "Test.java", getSourceDQ(new String[]{
        "package test;",
        "public class Test {",
        "  public Test() {",
        "    int var = 1;",
        "    System.out.println(true);",
        "    System.out.println(123);",
        "    System.out.println('abc');",
        "    Object valueNull = null; // 1",
        "    Object valueCastNull = (Object) null; // 2",
        "    System.out.println(java.util.Collections.EMPTY_LIST);",
        "    System.out.println(var);",
        "  }",
        "}"}));
    BooleanLiteral booleanLiteral = (BooleanLiteral) m_lastEditor.getEnclosingNode("true");
    NumberLiteral numberLiteral = (NumberLiteral) m_lastEditor.getEnclosingNode("123");
    StringLiteral stringLiteral = (StringLiteral) m_lastEditor.getEnclosingNode("abc");
    NullLiteral nullLiteral = (NullLiteral) m_lastEditor.getEnclosingNode("null; // 1");
    CastExpression castNullNode =
        (CastExpression) m_lastEditor.getEnclosingNode("(Object) null; // 2");
    QualifiedName qualifiedName =
        (QualifiedName) m_lastEditor.getEnclosingNode("EMPTY_LIST").getParent();
    SimpleName varNode = (SimpleName) m_lastEditor.getEnclosingNode("var)");
    // single checks
    assertTrue(AstNodeUtils.isLiteral(booleanLiteral));
    assertTrue(AstNodeUtils.isLiteral(numberLiteral));
    assertTrue(AstNodeUtils.isLiteral(stringLiteral));
    assertTrue(AstNodeUtils.isLiteral(nullLiteral));
    assertTrue(AstNodeUtils.isLiteral(castNullNode));
    assertTrue(AstNodeUtils.isLiteral(qualifiedName));
    assertFalse(AstNodeUtils.isLiteral(varNode));
    // List checks
    assertTrue(AstNodeUtils.areLiterals(ImmutableList.<Expression>of()));
    assertTrue(AstNodeUtils.areLiterals(ImmutableList.of(booleanLiteral, numberLiteral)));
    assertFalse(AstNodeUtils.areLiterals(ImmutableList.of(booleanLiteral, varNode)));
  }

  /**
   * Test for {@link AstNodeUtils#isLiteral(Expression)}.
   */
  public void test_isLiteral_complex() throws Exception {
    createASTCompilationUnit("test", "Test.java", getSourceDQ(new String[]{
        "package test;",
        "public class Test {",
        "  public Test() {",
        "    int var = 1;",
        "    System.out.println(-123);",
        "    System.out.println(+123);",
        "    System.out.println(1 + 2 + 3);",
        "  }",
        "}"}));
    Expression negativeExpression = (Expression) m_lastEditor.getEnclosingNode("-123");
    Expression positiveExpression = (Expression) m_lastEditor.getEnclosingNode("+123");
    Expression sumExpression = (Expression) m_lastEditor.getEnclosingNode("+ 2");
    // single checks
    assertTrue(AstNodeUtils.isLiteral(negativeExpression));
    assertTrue(AstNodeUtils.isLiteral(positiveExpression));
    assertTrue(AstNodeUtils.isLiteral(sumExpression));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Package
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstNodeUtils#getPackageName(CompilationUnit)}.
   */
  public void test_getPackageName_defaultPackage() throws Exception {
    CompilationUnit unit =
        createASTCompilationUnit(
            "",
            "Test.java",
            getSourceDQ("public class Test {", "  // filler filler filler", "}"));
    assertEquals("", AstNodeUtils.getPackageName(unit));
  }

  /**
   * Test for {@link AstNodeUtils#getPackageName(CompilationUnit)}.
   */
  public void test_getPackageName_qualifiedPackage() throws Exception {
    CompilationUnit unit =
        createASTCompilationUnit(
            "test",
            "Test.java",
            getSourceDQ(
                "package com.foo.bar;",
                "public class Test {",
                "  // filler filler filler",
                "}"));
    assertEquals("com.foo.bar", AstNodeUtils.getPackageName(unit));
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
