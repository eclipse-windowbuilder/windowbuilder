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

import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.AstReflectionUtils;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.tests.designer.core.AbstractJavaTest;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Test for {@link AstReflectionUtils}.
 * 
 * @author sablin_aa
 * @author scheglov_ke
 */
public class AstReflectionUtilsTest extends AbstractJavaTest {
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
  // getClass()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstReflectionUtils#getClass(ClassLoader, ITypeBinding)}.
   */
  public void test_getClass() throws Exception {
    createTypeDeclaration_Test(
        "// filler filler filler filler filler",
        "import javax.swing.JPanel;",
        "public class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    ITypeBinding typeBinding;
    {
      SimpleName name = getNode("JPanel");
      typeBinding = name.resolveTypeBinding();
    }
    // load Class
    {
      ClassLoader classLoader = CodeUtils.getProjectClassLoader(m_javaProject);
      Class<?> clazz = AstReflectionUtils.getClass(classLoader, typeBinding);
      assertEquals("javax.swing.JPanel", clazz.getCanonicalName());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // updateForVarArgs()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstReflectionUtils#updateForVarArgs(ClassLoader, IMethodBinding, Object[])}.
   */
  public void test_updateForVarArgs_hasVarArgs() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getSourceDQ(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "package test;",
            "public class MyPanel {",
            "  public void hasVarArgs(int value, String... names) {",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    createTypeDeclaration_Test(
        "import javax.swing.JButton;",
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    hasVarArgs(1, 'a', 'b', 'c');",
        "  }",
        "}");
    ClassLoader classLoader = CodeUtils.getProjectClassLoader(m_javaProject);
    MethodInvocation invocation = getNode("hasVarArgs(", MethodInvocation.class);
    IMethodBinding methodBinding = invocation.resolveMethodBinding();
    // use sequence
    {
      Object[] rawArguments = new Object[]{1, "a", "b", "c"};
      Object[] updatedArguments =
          AstReflectionUtils.updateForVarArgs(classLoader, methodBinding, rawArguments);
      assertThat(updatedArguments).isEqualTo(new Object[]{1, new String[]{"a", "b", "c"}});
    }
    // use array
    {
      Object[] rawArguments = new Object[]{1, new String[]{"a", "b", "c"}};
      Object[] updatedArguments =
          AstReflectionUtils.updateForVarArgs(classLoader, methodBinding, rawArguments);
      assertThat(updatedArguments).isEqualTo(new Object[]{1, new String[]{"a", "b", "c"}});
    }
    // single element
    {
      Object[] rawArguments = new Object[]{1, "a"};
      Object[] updatedArguments =
          AstReflectionUtils.updateForVarArgs(classLoader, methodBinding, rawArguments);
      assertThat(updatedArguments).isEqualTo(new Object[]{1, new String[]{"a"}});
    }
    // no elements
    {
      Object[] rawArguments = new Object[]{1};
      Object[] updatedArguments =
          AstReflectionUtils.updateForVarArgs(classLoader, methodBinding, rawArguments);
      assertThat(updatedArguments).isEqualTo(new Object[]{1, new String[]{}});
    }
  }

  /**
   * Test for {@link AstReflectionUtils#updateForVarArgs(ClassLoader, IMethodBinding, Object[])}.
   */
  public void test_updateForVarArgs_noVarArgs() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getSourceDQ(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "package test;",
            "public class MyPanel {",
            "  public void noVarArgs(int value, String name) {",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    createTypeDeclaration_Test(
        "import javax.swing.JButton;",
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    noVarArgs(1, 'str');",
        "  }",
        "}");
    ClassLoader classLoader = CodeUtils.getProjectClassLoader(m_javaProject);
    MethodInvocation invocation = getNode("noVarArgs(", MethodInvocation.class);
    IMethodBinding methodBinding = invocation.resolveMethodBinding();
    // no "varArgs"
    Object[] rawArguments = new Object[]{1, "str"};
    Object[] updatedArguments =
        AstReflectionUtils.updateForVarArgs(classLoader, methodBinding, rawArguments);
    assertThat(updatedArguments).isEqualTo(new Object[]{1, "str"});
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getConstructor()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstReflectionUtils#getConstructor(Class, SuperConstructorInvocation)}
   */
  public void test_getConstructor_SuperConstructorInvocation() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getSourceDQ(
            "package test;",
            "import javax.swing.JComponent;",
            "import javax.swing.JPanel;",
            "public class MyPanel extends JPanel {",
            "  public <T extends JComponent> MyPanel(String string, T value) {",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    createTypeDeclaration_Test(
        "import javax.swing.JButton;",
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    super('test', new JButton());",
        "  }",
        "}");
    SuperConstructorInvocation superInvocation = getNode("super(");
    // prepare "MyPanel" class
    Class<?> clazz;
    {
      ClassLoader classLoader = CodeUtils.getProjectClassLoader(m_javaProject);
      clazz = classLoader.loadClass("test.MyPanel");
    }
    // can not find "constructor" using "simple" signature
    {
      String signature = AstNodeUtils.getSuperSignature(superInvocation);
      Constructor<?> constructor = ReflectionUtils.getConstructorBySignature(clazz, signature);
      assertNull(constructor);
    }
    // can find "constructor" using "generic" signature
    {
      Constructor<?> constructor = AstReflectionUtils.getConstructor(clazz, superInvocation);
      assertNotNull(constructor);
    }
  }

  /**
   * Test for {@link AstReflectionUtils#getConstructor(Class, ClassInstanceCreation)}
   */
  public void test_getConstructor_ClassInstanceCreation() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getSourceDQ(
            "package test;",
            "import javax.swing.*;",
            "public class MyPanel extends JPanel {",
            "  public <T extends JComponent> MyPanel(String string, T value) {",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    createTypeDeclaration_Test(
        "import javax.swing.*;",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    add(new MyPanel('test', new JButton()));",
        "  }",
        "}");
    ClassInstanceCreation classInstanceCreation = getNode("new MyPanel(");
    // prepare "MyPanel" class
    Class<?> clazz;
    {
      ClassLoader classLoader = CodeUtils.getProjectClassLoader(m_javaProject);
      clazz = classLoader.loadClass("test.MyPanel");
    }
    // can not find "constructor" using "simple" signature
    {
      String signature = AstNodeUtils.getCreationSignature(classInstanceCreation);
      Constructor<?> constructor = ReflectionUtils.getConstructorBySignature(clazz, signature);
      assertNull(constructor);
    }
    // can find "constructor" using "generic" signature
    {
      Constructor<?> constructor = AstReflectionUtils.getConstructor(clazz, classInstanceCreation);
      assertNotNull(constructor);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getMethod()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstReflectionUtils#getMethod(Class, MethodInvocation)}.
   */
  public void test_getMethod_MethodInvocation() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getSourceDQ(
            "package test;",
            "import javax.swing.JComponent;",
            "import javax.swing.JPanel;",
            "public class MyPanel extends JPanel {",
            "  public <T extends JComponent> T componentFactory(String string, T component) {",
            "    return component;",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    createTypeDeclaration_Test(
        "import javax.swing.JButton;",
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    componentFactory('test', new JButton());",
        "  }",
        "}");
    MethodInvocation invocation =
        AstNodeUtils.getEnclosingNode(getNode("componentFactory("), MethodInvocation.class);
    // prepare "MyPanel" class
    Class<?> clazz;
    {
      ClassLoader projectClassLoader = CodeUtils.getProjectClassLoader(m_javaProject);
      clazz = projectClassLoader.loadClass("test.MyPanel");
    }
    // can not find "method" using "simple" signature
    {
      String signature = AstNodeUtils.getMethodSignature(invocation);
      Method method = ReflectionUtils.getMethodBySignature(clazz, signature);
      assertNull(method);
    }
    // can not find "method" using "simple" signature, even if ask "generic"
    {
      String signature = AstNodeUtils.getMethodSignature(invocation);
      Method method = ReflectionUtils.getMethodByGenericSignature(clazz, signature);
      assertNull(method);
    }
    // can find "method" using "generic" signature
    {
      Method method = AstReflectionUtils.getMethod(clazz, invocation);
      assertNotNull(method);
    }
  }
}
