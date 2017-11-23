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
package org.eclipse.wb.tests.designer.core.util.jdt.core;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;

import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.tests.designer.core.AbstractJavaTest;
import org.eclipse.wb.tests.designer.core.PdeProjectConversionUtils;
import org.eclipse.wb.tests.designer.core.TestProject;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Tests for {@link CodeUtils}.
 * 
 * @author scheglov_ke
 */
public class CodeUtilsTest extends AbstractJavaTest {
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
  // Class/package
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getShortClass() {
    assertEquals("boolean", CodeUtils.getShortClass("boolean"));
    assertEquals("List", CodeUtils.getShortClass("java.util.List"));
    assertEquals("Sub", CodeUtils.getShortClass("test.MyPanel$Sub"));
  }

  public void test_getPackage() {
    assertEquals("", CodeUtils.getPackage("SimpleName"));
    assertEquals("java.util", CodeUtils.getPackage("java.util.List"));
  }

  public void test_isSamePackage() {
    assertTrue(CodeUtils.isSamePackage("java.util.List", "java.util.Set"));
    assertFalse(CodeUtils.isSamePackage("java.util.List", "java.lang.Object"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // join
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_join_1() {
    assertTrue(ArrayUtils.isEquals(new String[]{"aaa"}, CodeUtils.join(null, "aaa")));
    assertTrue(ArrayUtils.isEquals(
        new String[]{"aaa", "bbb", "ccc"},
        CodeUtils.join(new String[]{"aaa", "bbb"}, "ccc")));
  }

  public void test_join_2() {
    assertTrue(ArrayUtils.isEquals(
        new String[]{"aaa", "bbb"},
        CodeUtils.join(null, new String[]{"aaa", "bbb"})));
    assertTrue(ArrayUtils.isEquals(
        new String[]{"aaa", "bbb"},
        CodeUtils.join(new String[]{"aaa", "bbb"}, (String[]) null)));
    assertTrue(ArrayUtils.isEquals(
        new String[]{"aaa", "bbb", "ccc"},
        CodeUtils.join(new String[]{"aaa", "bbb"}, new String[]{"ccc"})));
  }

  public void test_join_3() {
    String[] a = {"a", "aa"};
    String[] b = {"b", "bb"};
    String[] c = {"c", "cc"};
    String[] result = CodeUtils.join(a, b, c);
    assertThat(result).isEqualTo(new String[]{"a", "aa", "b", "bb", "c", "cc"});
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getSource()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link CodeUtils#getSource(String...)}.
   */
  public void test_getSource() throws Exception {
    assertEquals("aaa\nbbb\nccc", CodeUtils.getSource("aaa", "bbb", "ccc"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // generateUniqueName
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link CodeUtils#generateUniqueName(String, com.google.common.base.Predicate)}.
   */
  public void test_generateUniqueName() throws Exception {
    assertSame("base", CodeUtils.generateUniqueName("base", Predicates.<String>alwaysTrue()));
    assertEquals("base_3", CodeUtils.generateUniqueName("base", new Predicate<String>() {
      public boolean apply(String name) {
        return !name.equals("base") && !name.equals("base_1") && !name.equals("base_2");
      }
    }));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // clearHiddenCode
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link FieldDeclaration} is hidden, so no fields expected.
   */
  public void test_clearHiddenCode_parsing() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "// filler filler filler",
            "public class Test {",
            "  int field; //$hide$",
            "}");
    assertEquals(0, typeDeclaration.getFields().length);
  }

  public void test_clearHiddenCode_block() throws Exception {
    String[] lines_1 = new String[]{"000", "1//$hide>>$1", "222", "333", "4//$hide<<$4", "555"};
    String[] lines_2 = new String[]{"000", "1//         ", "   ", "   ", "            ", "555"};
    check_clearHiddenCode(lines_1, lines_2);
  }

  /**
   * It seems that after formatting "//$hide" tags may be converted into "// $hide". So, we should
   * ignore these spaces.
   */
  public void test_clearHiddenCode_withSpace_line() throws Exception {
    String[] lines_1 = new String[]{"000", "111// $hide$", "222"};
    String[] lines_2 = new String[]{"000", "            ", "222"};
    check_clearHiddenCode(lines_1, lines_2);
  }

  /**
   * It seems that after formatting "//$hide" tags may be converted into "// $hide". So, we should
   * ignore these spaces.
   */
  public void test_clearHiddenCode_withSpace_block() throws Exception {
    String[] lines_1 = new String[]{"000", "1// $hide>>$11", "222", "3// $hide<<$33", "444"};
    String[] lines_2 = new String[]{"000", "1//           ", "   ", "              ", "444"};
    check_clearHiddenCode(lines_1, lines_2);
  }

  /**
   * No "block hide begin" tag, should fail.
   */
  public void test_clearHiddenCode_blockNoBegin() throws Exception {
    String[] lines_1 = new String[]{"000", "222", "333", "//$hide<<$", "444"};
    try {
      check_clearHiddenCode(lines_1, ArrayUtils.EMPTY_STRING_ARRAY);
      fail();
    } catch (IllegalStateException e) {
    }
  }

  /**
   * No "block hide end" tag, should fail.
   */
  public void test_clearHiddenCode_blockNoEnd() throws Exception {
    String[] lines_1 = new String[]{"000", "//$hide>>$", "222", "333", "444"};
    try {
      check_clearHiddenCode(lines_1, ArrayUtils.EMPTY_STRING_ARRAY);
      fail();
    } catch (IllegalStateException e) {
    }
  }

  /**
   * No "block hide begin" after "block hide end" tag, should fail.
   */
  public void test_clearHiddenCode_blockWrongSequence() throws Exception {
    String[] lines_1 = new String[]{"000", "//$hide<<$", "222", "333", "//$hide>>$", "444"};
    try {
      check_clearHiddenCode(lines_1, ArrayUtils.EMPTY_STRING_ARRAY);
      fail();
    } catch (IllegalStateException e) {
    }
  }

  public void test_clearHiddenCode_line() throws Exception {
    String[] lines_1 = new String[]{"000", "222 //$hide$", "333"};
    String[] lines_2 = new String[]{"000", "            ", "333"};
    check_clearHiddenCode(lines_1, lines_2);
  }

  private void check_clearHiddenCode(String[] lines, String[] expectedLines) throws Exception {
    String source = StringUtils.join(lines, "\n");
    String expectedSource = StringUtils.join(expectedLines, "\n");
    try {
      String clearedSource =
          (String) ReflectionUtils.invokeMethod(
              CodeUtils.class,
              "clearHiddenCode(java.lang.String)",
              source);
      assertEquals(expectedSource, clearedSource);
    } catch (InvocationTargetException e) {
      throw (Exception) e.getCause();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getProjectClassLoader
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getProjectClassLoader() throws Exception {
    setFileContentSrc("test/Test.java", getSourceDQ("package test;", "public class Test {", "}"));
    waitForAutoBuild();
    // check ClassLoader
    ClassLoader projectClassLoader =
        CodeUtils.getProjectClassLoader(m_testProject.getJavaProject());
    projectClassLoader.loadClass("test.Test");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getSourceContainers
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * If {@link IJavaProject} does not exist, it does not have source containers.
   */
  @DisposeProjectAfter
  public void test_getSourceContainers_notJavaProject() throws Exception {
    ProjectUtils.removeNature(m_project, JavaCore.NATURE_ID);
    List<IContainer> sourceContainers = CodeUtils.getSourceContainers(m_javaProject, true);
    assertThat(sourceContainers).isEmpty();
  }

  /**
   * Single project with "src" folder.
   */
  public void test_getSourceContainers_1() throws Exception {
    IJavaProject javaProject = m_testProject.getJavaProject();
    // by default we create "src" source folder
    List<IContainer> sourceContainers = CodeUtils.getSourceContainers(javaProject, true);
    assertThat(sourceContainers).hasSize(1);
    assertEquals("/TestProject/src", sourceContainers.get(0).getFullPath().toString());
  }

  /**
   * Single project without "src" folder, so project itself is source container.
   */
  @DisposeProjectAfter
  public void test_getSourceContainers_2() throws Exception {
    // remove "src"
    {
      IClasspathEntry[] rawClasspath = m_javaProject.getRawClasspath();
      rawClasspath = (IClasspathEntry[]) ArrayUtils.remove(rawClasspath, rawClasspath.length - 1);
      m_javaProject.setRawClasspath(rawClasspath, new NullProgressMonitor());
    }
    // add ""
    m_testProject.addSourceFolder("/TestProject");
    // assert that source container is project itself
    List<IContainer> sourceContainers = CodeUtils.getSourceContainers(m_javaProject, true);
    assertThat(sourceContainers).hasSize(1);
    assertEquals("/TestProject", sourceContainers.get(0).getFullPath().toString());
  }

  /**
   * Test for adding source folder of required project.
   */
  @DisposeProjectAfter
  public void test_getSourceContainers_3() throws Exception {
    // create new project "myProject"
    TestProject myProject = new TestProject("myProject");
    IJavaProject myJavaProject = myProject.getJavaProject();
    // reference "myProject" from "TestProject"
    try {
      ProjectUtils.requireProject(m_javaProject, myJavaProject);
      // assert that both "TestProject" and "myProject" source folders returned
      List<IContainer> sourceContainers = CodeUtils.getSourceContainers(m_javaProject, true);
      assertThat(sourceContainers).hasSize(2);
      assertEquals("/TestProject/src", sourceContainers.get(0).getFullPath().toString());
      assertEquals("/myProject/src", sourceContainers.get(1).getFullPath().toString());
    } finally {
      myProject.dispose();
    }
  }

  /**
   * "TestProject" requires "myProject", but also "myProject" requires "TestProject".<br>
   * So, we test circular dependency problem.
   */
  @DisposeProjectAfter
  public void test_getSourceContainers_4() throws Exception {
    // create new project "myProject"
    TestProject myProject = new TestProject("myProject");
    IJavaProject myJavaProject = myProject.getJavaProject();
    // reference "myProject" from "TestProject"
    try {
      // create circular dependency
      ProjectUtils.requireProject(m_javaProject, myJavaProject);
      ProjectUtils.requireProject(myJavaProject, m_javaProject);
      // still two source folders
      List<IContainer> sourceContainers = CodeUtils.getSourceContainers(m_javaProject, true);
      assertThat(sourceContainers).hasSize(2);
      assertEquals("/TestProject/src", sourceContainers.get(0).getFullPath().toString());
      assertEquals("/myProject/src", sourceContainers.get(1).getFullPath().toString());
    } finally {
      myProject.dispose();
    }
  }

  @DisposeProjectAfter
  public void test_geSourceContainers_notExistingSourceFolder() throws Exception {
    // add "src2"
    m_testProject.addSourceFolder("/TestProject/src2");
    // ...but "src2" does not exist, so it is not returned
    List<IContainer> sourceContainers = CodeUtils.getSourceContainers(m_javaProject, true);
    assertThat(sourceContainers).hasSize(1);
    assertEquals("/TestProject/src", sourceContainers.get(0).getFullPath().toString());
  }

  /**
   * Test for using plugin fragments.
   */
  @DisposeProjectAfter
  public void test_getSourceContainers_PDE() throws Exception {
    PdeProjectConversionUtils.convertToPDE(m_testProject.getProject(), null);
    // create fragment
    TestProject fragmentProject = new TestProject("TestProject_ru");
    try {
      PdeProjectConversionUtils.convertToPDE(fragmentProject.getProject(), "TestProject");
      waitForAutoBuild();
      // check that we have containers for project itself and its fragment
      List<IContainer> sourceContainers =
          CodeUtils.getSourceContainers(m_testProject.getJavaProject(), true);
      assertThat(sourceContainers).hasSize(2);
      assertEquals("/TestProject/src", sourceContainers.get(0).getFullPath().toString());
      assertEquals("/TestProject_ru/src", sourceContainers.get(1).getFullPath().toString());
    } finally {
      fragmentProject.dispose();
    }
  }

  /**
   * Test for fragment that is not Java project. Not sure if this makes sense, but one user has such
   * project.
   */
  @DisposeProjectAfter
  public void test_getSourceContainers_PDE_noJavaNature() throws Exception {
    PdeProjectConversionUtils.convertToPDE(m_project, null);
    // create fragment
    TestProject fragmentProject = new TestProject("TestProject_ru");
    try {
      PdeProjectConversionUtils.convertToPDE(fragmentProject.getProject(), "TestProject");
      ProjectUtils.removeNature(fragmentProject.getProject(), JavaCore.NATURE_ID);
      waitForAutoBuild();
      // fragment is not Java project, so we have container only for project itself
      List<IContainer> sourceContainers = CodeUtils.getSourceContainers(m_javaProject, true);
      assertThat(sourceContainers).hasSize(1);
      assertEquals("/TestProject/src", sourceContainers.get(0).getFullPath().toString());
    } finally {
      fragmentProject.dispose();
    }
  }

  /**
   * Plug-in is in a classic format. We don't support its fragments, but should not throw
   * {@link NullPointerException} too.
   */
  @DisposeProjectAfter
  public void test_getSourceContainers_PDE_oldNotOSGi() throws Exception {
    setFileContent(
        "plugin.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<plugin name='foo' id='some.id'>",
            "</plugin>"));
    waitForAutoBuild();
    // without fragment
    List<IContainer> sourceContainers =
        CodeUtils.getSourceContainers(m_testProject.getJavaProject(), true);
    assertThat(sourceContainers).hasSize(1);
    assertEquals("/TestProject/src", sourceContainers.get(0).getFullPath().toString());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getPackageFragmentRoot()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Normal cases, {@link IJavaElement} is child of {@link IPackageFragmentRoot}.
   */
  public void test_getPackageFragmentRoot_1() throws Exception {
    IType type =
        createModelType(
            "test",
            "Test.java",
            getSourceDQ("package test;", "public class Test {", "  public Test() {", "  }", "}"));
    String expectedPath = "/TestProject/src";
    // "null" as element
    {
      IJavaElement element = null;
      IPackageFragmentRoot packageFragmentRoot = CodeUtils.getPackageFragmentRoot(element);
      assertNull(packageFragmentRoot);
    }
    // IType as element
    {
      IJavaElement element = type;
      IPackageFragmentRoot packageFragmentRoot = CodeUtils.getPackageFragmentRoot(element);
      assertPackageFragmentRootPath(expectedPath, packageFragmentRoot);
    }
    // ICompilationUnit as element
    {
      IJavaElement element = type.getCompilationUnit();
      IPackageFragmentRoot packageFragmentRoot = CodeUtils.getPackageFragmentRoot(element);
      assertPackageFragmentRootPath(expectedPath, packageFragmentRoot);
    }
    // IMethod as element
    {
      IJavaElement element = type.getMethods()[0];
      IPackageFragmentRoot packageFragmentRoot = CodeUtils.getPackageFragmentRoot(element);
      assertPackageFragmentRootPath(expectedPath, packageFragmentRoot);
    }
  }

  /**
   * {@link IJavaProject} as element, so first source folder should be used.
   */
  public void test_getPackageFragmentRoot_2() throws Exception {
    try {
      IJavaProject javaProject = m_testProject.getJavaProject();
      // IJavaProject as element
      {
        IJavaElement element = javaProject;
        IPackageFragmentRoot packageFragmentRoot = CodeUtils.getPackageFragmentRoot(element);
        assertPackageFragmentRootPath("/TestProject/src", packageFragmentRoot);
      }
    } finally {
      do_projectDispose();
      do_projectCreate();
    }
  }

  /**
   * {@link IJavaProject} as element, but no separate source folders, so {@link IJavaProject} itself
   * is source folder.
   */
  public void test_getPackageFragmentRoot_3() throws Exception {
    try {
      IJavaProject javaProject = m_testProject.getJavaProject();
      // remove "src"
      {
        IClasspathEntry[] rawClasspath = javaProject.getRawClasspath();
        rawClasspath = (IClasspathEntry[]) ArrayUtils.remove(rawClasspath, rawClasspath.length - 1);
        javaProject.setRawClasspath(rawClasspath, new NullProgressMonitor());
      }
      // add ""
      {
        IClasspathEntry[] rawClasspath = javaProject.getRawClasspath();
        rawClasspath =
            (IClasspathEntry[]) ArrayUtils.add(
                rawClasspath,
                JavaCore.newSourceEntry(new Path("/TestProject")));
        javaProject.setRawClasspath(rawClasspath, new NullProgressMonitor());
      }
      // IJavaProject as element
      {
        IJavaElement element = javaProject;
        IPackageFragmentRoot packageFragmentRoot = CodeUtils.getPackageFragmentRoot(element);
        assertPackageFragmentRootPath("/TestProject", packageFragmentRoot);
      }
    } finally {
      do_projectDispose();
      do_projectCreate();
    }
  }

  /**
   * Asserts that given {@link IPackageFragmentRoot} has same path in workspace as expected.
   */
  private static void assertPackageFragmentRootPath(String expectedPath,
      IPackageFragmentRoot packageFragmentRoot) throws Exception {
    assertEquals(expectedPath, packageFragmentRoot.getUnderlyingResource().getFullPath().toString());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // findPrimaryType
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_findPrimaryType_ok() throws Exception {
    ICompilationUnit compilationUnit =
        createModelCompilationUnit(
            "test",
            "Test.java",
            getSourceDQ(
                "package test;",
                "class test_private_class {",
                "}",
                "public class Test {",
                "}"));
    IType primaryType = CodeUtils.findPrimaryType(compilationUnit);
    assertNotNull(primaryType);
    assertTrue(primaryType.getElementName().equals("Test"));
  }

  public void test_findPrimaryType_no() throws Exception {
    ICompilationUnit compilationUnit =
        createModelCompilationUnit(
            "test",
            "Test.java",
            getSourceDQ(
                "package test;",
                "class test_private_class {",
                "}",
                "class test_private_class2 {",
                "}"));
    IType primaryType = CodeUtils.findPrimaryType(compilationUnit);
    assertNull(primaryType);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // searchReferences()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link CodeUtils#searchReferences(IType)}.
   */
  public void test_searchReferences_IType_IField() throws Exception {
    IType targetType =
        createModelCompilationUnit(
            "test",
            "Target.java",
            getSourceDQ(
                "// filler filler filler filler filler",
                "// filler filler filler filler filler",
                "package test;",
                "public class Target {",
                "}")).getTypes()[0];
    createModelCompilationUnit(
        "test",
        "Test.java",
        getSourceDQ(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "package test;",
            "public class Test {",
            "  private Target myTarget;",
            "}"));
    // search
    List<IJavaElement> references = CodeUtils.searchReferences(targetType);
    assertThat(references).hasSize(1);
    // check IField
    IField fieldElement = (IField) references.get(0);
    assertEquals("myTarget", fieldElement.getElementName());
  }

  /**
   * Test for {@link CodeUtils#searchReferences(IType)}.
   */
  public void test_searchReferences_IType_IAnnotation() throws Exception {
    IType targetType =
        createModelCompilationUnit(
            "test",
            "MyAnnotation.java",
            getSourceDQ(
                "// filler filler filler filler filler",
                "// filler filler filler filler filler",
                "package test;",
                "public @interface MyAnnotation {",
                "}")).getTypes()[0];
    createModelCompilationUnit(
        "test",
        "Test.java",
        getSourceDQ(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "package test;",
            "@MyAnnotation",
            "public class Test {",
            "}"));
    // search
    List<IJavaElement> references = CodeUtils.searchReferences(targetType);
    assertThat(references).hasSize(1);
    // check IField
    IAnnotation annotation = (IAnnotation) references.get(0);
    assertEquals("MyAnnotation", annotation.getElementName());
  }

  /**
   * Test for {@link CodeUtils#searchReferences(IField)}.
   */
  public void test_searchReferences_IField() throws Exception {
    IField targetField =
        createModelCompilationUnit(
            "test",
            "Target.java",
            getSourceDQ(
                "// filler filler filler filler filler",
                "// filler filler filler filler filler",
                "package test;",
                "public class Target {",
                "  int myField;",
                "}")).getTypes()[0].getFields()[0];
    IType type =
        createModelCompilationUnit(
            "test",
            "Test.java",
            getSourceDQ(
                "// filler filler filler filler filler",
                "// filler filler filler filler filler",
                "package test;",
                "public class Test {",
                "  void foo(Target t) {",
                "    t.myField = 1;",
                "  }",
                "}")).getTypes()[0];
    // search
    List<IJavaElement> references = CodeUtils.searchReferences(targetField);
    assertThat(references).hasSize(1);
    // check IMethod
    {
      IMethod methodElement = (IMethod) references.get(0);
      assertEquals(type.getMethods()[0], methodElement);
      assertEquals("foo", methodElement.getElementName());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getType()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link CodeUtils#getType(IJavaElement)}.
   */
  public void test_getType() throws Exception {
    IJavaProject javaProject = m_testProject.getJavaProject();
    IType listType = javaProject.findType("java.util.List");
    // IType as input element
    {
      IJavaElement element = listType;
      assertEquals("java.util.List", CodeUtils.getType(element).getFullyQualifiedName());
    }
    // IMethod as input element
    {
      IJavaElement element = listType.getMethods()[0];
      assertEquals("java.util.List", CodeUtils.getType(element).getFullyQualifiedName());
    }
    // IClassFile as input element
    {
      IJavaElement element = listType.getClassFile();
      assertEquals("java.util.List", CodeUtils.getType(element).getFullyQualifiedName());
    }
    // ICompilationUnit as input element
    {
      IType type =
          createModelType(
              "test",
              "Test.java",
              getSourceDQ(
                  "package test;",
                  "public class Test {",
                  "}",
                  "",
                  "class NonMainType {",
                  "}"));
      IJavaElement element = type.getCompilationUnit();
      assertEquals("test.Test", CodeUtils.getType(element).getFullyQualifiedName());
    }
    // "null" as input
    {
      IJavaElement element = null;
      assertNull(CodeUtils.getType(element));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getResolvedTypeName
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getResolvedTypeName() throws Exception {
    ICompilationUnit unit =
        createModelCompilationUnit(
            "test",
            "Test.java",
            getSourceDQ("package test;", "import java.util.*;", "public class Test {", "}"));
    IType type = unit.getTypes()[0];
    assertEquals("int", CodeUtils.getResolvedTypeName(type, "I"));
    assertEquals("long", CodeUtils.getResolvedTypeName(type, "J"));
    assertEquals("int[]", CodeUtils.getResolvedTypeName(type, "[I"));
    assertEquals("int[][]", CodeUtils.getResolvedTypeName(type, "[[I"));
    assertEquals("java.util.List", CodeUtils.getResolvedTypeName(type, "QList;"));
    assertNull(CodeUtils.getResolvedTypeName(type, "QNoSuchName;"));
  }

  /**
   * Test for {@link CodeUtils#getResolvedTypeName(IType, String)}.
   */
  public void test_getResolvedTypeName_forTypeVariable_hasBounds() throws Exception {
    IType type =
        createModelType(
            "test",
            "Test.java",
            getSource(
                "package test;",
                "public class Test<E extends java.awt.Component> {",
                "  // filler",
                "}"));
    assertEquals("java.awt.Component", CodeUtils.getResolvedTypeName(type, "TE;"));
    assertEquals("java.awt.Component", CodeUtils.getResolvedTypeName(type, "QE;"));
  }

  /**
   * Test for {@link CodeUtils#getResolvedTypeName(IType, String)}.
   */
  public void test_getResolvedTypeName_forTypeVariable_noBounds() throws Exception {
    IType type =
        createModelType(
            "test",
            "Test.java",
            getSource("package test;", "public class Test<E> {", "  // filler filler filler", "}"));
    assertEquals("java.lang.Object", CodeUtils.getResolvedTypeName(type, "TE;"));
    assertEquals("java.lang.Object", CodeUtils.getResolvedTypeName(type, "QE;"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getMethodSignature
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getMethodSignature() throws Exception {
    ICompilationUnit unit =
        createModelCompilationUnit(
            "test",
            "Test.java",
            getSourceDQ(
                "package test;",
                "import java.util.ArrayList;",
                "class Test {",
                "  void foo() {",
                "  }",
                "  void foo(int p1) {",
                "  }",
                "  void foo(int p1, float p2) {",
                "  }",
                "  void foo(int[][] p1) {",
                "  }",
                "  void foo(java.util.List list) {",
                "  }",
                "  void foo(ArrayList list) {",
                "  }",
                "  void foo(ArrayList [] list[]) {",
                "  }",
                "  Test(int a) {",
                "  }",
                "}"));
    IMethod[] methods = unit.getTypes()[0].getMethods();
    assertEquals("foo()", CodeUtils.getMethodSignature(methods[0]));
    assertEquals("foo(int)", CodeUtils.getMethodSignature(methods[1]));
    assertEquals("foo(int,float)", CodeUtils.getMethodSignature(methods[2]));
    assertEquals("foo(int[][])", CodeUtils.getMethodSignature(methods[3]));
    assertEquals("foo(java.util.List)", CodeUtils.getMethodSignature(methods[4]));
    assertEquals("foo(java.util.ArrayList)", CodeUtils.getMethodSignature(methods[5]));
    assertEquals("foo(java.util.ArrayList[][])", CodeUtils.getMethodSignature(methods[6]));
    assertEquals("<init>(int)", CodeUtils.getMethodSignature(methods[7]));
  }

  /**
   * Test for {@link CodeUtils#getMethodSignature(IMethod)}.
   * <p>
   * Signature of {@link IMethod} with type parameters should use bounds in place of parameters.
   */
  public void test_getMethodSignature_withGenerics() throws Exception {
    IType type =
        createModelType(
            "test",
            "Test.java",
            getSourceDQ(
                "package test;",
                "import java.awt.Component;",
                "public class Test<T extends Component> {",
                "  void foo(T component) {",
                "  }",
                "}"));
    IMethod method = type.getMethods()[0];
    assertEquals("foo(java.awt.Component)", CodeUtils.getMethodSignature(method));
  }

  /**
   * Test for {@link CodeUtils#getMethodSignature(IMethod)}.
   * <p>
   * Signature of {@link IMethod} with type parameters should use bounds in place of parameters.
   */
  public void test_getMethodSignature_withGenerics_array() throws Exception {
    IType type = m_javaProject.findType("javax.swing.JComboBox");
    IMethod method = type.getMethod("JComboBox", new String[]{"[TE;"});
    assertEquals("<init>(java.lang.Object[])", CodeUtils.getMethodSignature(method));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // findMethod
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_findMethod_withType() throws Exception {
    IType aType =
        createModelCompilationUnit(
            "test",
            "A.java",
            getSourceDQ("package test;", "public class A {", "  public void foo() {", "  }", "}")).getTypes()[0];
    IType bType =
        createModelCompilationUnit(
            "test",
            "B.java",
            getSourceDQ(
                "package test;",
                "public class B extends A {",
                "  public void bar() {",
                "  }",
                "}")).getTypes()[0];
    // ask method from "A" 
    {
      IMethod method = CodeUtils.findMethod(aType, "foo()");
      assertSame(aType.getMethods()[0], method);
    }
    // ask method from "A" using type "B"
    {
      IMethod method = CodeUtils.findMethod(bType, "foo()");
      assertSame(aType.getMethods()[0], method);
    }
    // ask method from "B"
    {
      IMethod method = CodeUtils.findMethod(bType, "bar()");
      assertSame(bType.getMethods()[0], method);
    }
    // "null" as IType
    {
      IMethod method = CodeUtils.findMethod(null, "noMatter()");
      assertNull(method);
    }
    // no such method
    {
      IMethod method = CodeUtils.findMethod(bType, "noSuchMethod()");
      assertNull(method);
    }
  }

  /**
   * Test for {@link CodeUtils#findMethod(IJavaProject, String, String)}.
   */
  public void test_findMethod() throws Exception {
    ICompilationUnit aUnit =
        createModelCompilationUnit(
            "test",
            "A.java",
            getSourceDQ(
                "package test;",
                "class A {",
                "  A() {",
                "  }",
                "  void foo() {",
                "  }",
                "  void bar() {",
                "  }",
                "}"));
    createModelCompilationUnit(
        "test",
        "B.java",
        getSourceDQ(
            "// filler filler filler filler filler",
            "package test;",
            "class B extends A {",
            "  void foo() {",
            "  }",
            "}"));
    TypeDeclaration type =
        createTypeDeclaration_Test(
            "class Test {",
            "  void test() {",
            "    (new A()).foo();",
            "    (new B()).foo();",
            "  }",
            "}");
    IJavaProject project = m_lastEditor.getJavaProject();
    // test for constructor
    {
      IMethod method = CodeUtils.findMethod(project, "test.A", "<init>()");
      assertSame(aUnit.getTypes()[0].getMethods()[0], method);
    }
    //
    {
      IMethod method = CodeUtils.findMethod(project, "test.B", "bar()");
      assertSame(aUnit.getTypes()[0].getMethods()[2], method);
    }
    //
    {
      IMethodBinding[] methodBindings = getInvocationBindings(type, 0);
      {
        IMethodBinding methodBinding = methodBindings[0];
        IMethod method = CodeUtils.findMethod(project, methodBinding);
        assertNotNull(method);
        assertEquals("A", method.getDeclaringType().getElementName());
      }
      {
        IMethodBinding methodBinding = methodBindings[1];
        IMethod method = CodeUtils.findMethod(project, methodBinding);
        assertNotNull(method);
        assertEquals("B", method.getDeclaringType().getElementName());
      }
    }
  }

  /**
   * Test for {@link CodeUtils#findMethod(IType, String)}.
   * <p>
   * We should be able to find {@link IMethod} with type parameters using signature with bounds
   * type.
   */
  public void test_findMethod_withGenerics() throws Exception {
    IType type =
        createModelType(
            "test",
            "Test.java",
            getSourceDQ(
                "package test;",
                "import java.awt.Component;",
                "public class Test<T extends Component> {",
                "  void foo(T component) {",
                "  }",
                "}"));
    IMethod method = CodeUtils.findMethod(type, "foo(java.awt.Component)");
    assertNotNull(method);
    assertTrue(method.exists());
    assertEquals("foo", method.getElementName());
  }

  /**
   * Test for {@link CodeUtils#findMethod(IJavaProject, String, String)}.
   */
  public void test_findMethod_noSuchType() throws Exception {
    assertNull(CodeUtils.findMethod(m_testProject.getJavaProject(), "no.such.Type", "foo()"));
  }

  /**
   * Test for {@link CodeUtils#findMethods(IType, String[])}.
   */
  public void test_findMethods() throws Exception {
    ICompilationUnit aUnit =
        createModelCompilationUnit(
            "test",
            "A.java",
            getSourceDQ(
                "package test;",
                "class A {",
                "  void foo() {",
                "  }",
                "  void bar() {",
                "  }",
                "}"));
    IType aType = aUnit.getTypes()[0];
    //
    IMethod[] methods = CodeUtils.findMethods(aType, new String[]{"foo()", "bar()", "baz()"});
    assertEquals("foo", methods[0].getElementName());
    assertEquals("bar", methods[1].getElementName());
    assertNull(methods[2]);
  }

  /**
   * Test for {@link CodeUtils#findMethods(IType, List)}.
   */
  public void test_findMethods_List() throws Exception {
    ICompilationUnit aUnit =
        createModelCompilationUnit(
            "test",
            "A.java",
            getSourceDQ(
                "package test;",
                "class A {",
                "  void foo() {",
                "  }",
                "  void bar() {",
                "  }",
                "}"));
    IType aType = aUnit.getTypes()[0];
    //
    List<IMethod> methods =
        CodeUtils.findMethods(aType, ImmutableList.of("foo()", "bar()", "baz()"));
    assertThat(methods).hasSize(3);
    assertEquals("foo", methods.get(0).getElementName());
    assertEquals("bar", methods.get(1).getElementName());
    assertSame(null, methods.get(2));
  }

  /**
   * Test for {@link CodeUtils#findMethodSingleType(IType, String)} for parameter with generic.
   */
  public void test_findMethodWithGeneric() throws Exception {
    ICompilationUnit aUnit =
        createModelCompilationUnit(
            "test",
            "A.java",
            getSourceDQ(
                "package test;",
                "class A {",
                "  void foo(java.util.List<Integer> a) {",
                "  }",
                "}"));
    IType aType = aUnit.getTypes()[0];
    //
    IMethod method = CodeUtils.findMethodSingleType(aType, "foo(java.util.List)");
    assertNotNull(method);
    assertEquals("foo", method.getElementName());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // isSuccessorOf
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link CodeUtils#isSuccessorOf(IType, IType)}.
   */
  public void test_isSuccessorOf_1() throws Exception {
    IJavaProject javaProject = m_testProject.getJavaProject();
    IType typeList = javaProject.findType("java.util.List");
    IType typeMap = javaProject.findType("java.util.Map");
    IType typeAbstractList = javaProject.findType("java.util.AbstractList");
    //
    assertTrue(CodeUtils.isSuccessorOf(javaProject.findType("java.util.ArrayList"), typeList));
    assertTrue(CodeUtils.isSuccessorOf(javaProject.findType("java.util.LinkedList"), typeList));
    //
    assertTrue(CodeUtils.isSuccessorOf(
        javaProject.findType("java.util.ArrayList"),
        typeAbstractList));
    assertTrue(CodeUtils.isSuccessorOf(
        javaProject.findType("java.util.LinkedList"),
        typeAbstractList));
    //
    assertFalse(CodeUtils.isSuccessorOf(javaProject.findType("java.util.ArrayList"), typeMap));
    assertFalse(CodeUtils.isSuccessorOf(javaProject.findType("java.util.LinkedList"), typeMap));
  }

  /**
   * Test for {@link CodeUtils#isSuccessorOf(IType, String)}.
   */
  public void test_isSuccessorOf_2() throws Exception {
    IJavaProject javaProject = m_testProject.getJavaProject();
    String typeList = "java.util.List";
    String typeMap = "java.util.Map";
    String typeAbstractList = "java.util.AbstractList";
    //
    assertFalse(CodeUtils.isSuccessorOf(javaProject.findType("java.util.ArrayList"), "no.such.Type"));
    //
    assertTrue(CodeUtils.isSuccessorOf(javaProject.findType("java.util.ArrayList"), typeList));
    assertTrue(CodeUtils.isSuccessorOf(javaProject.findType("java.util.LinkedList"), typeList));
    //
    assertTrue(CodeUtils.isSuccessorOf(
        javaProject.findType("java.util.ArrayList"),
        typeAbstractList));
    assertTrue(CodeUtils.isSuccessorOf(
        javaProject.findType("java.util.LinkedList"),
        typeAbstractList));
    //
    assertFalse(CodeUtils.isSuccessorOf(javaProject.findType("java.util.ArrayList"), typeMap));
    assertFalse(CodeUtils.isSuccessorOf(javaProject.findType("java.util.LinkedList"), typeMap));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // findSuperMethod
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_findSuperMethod() throws Exception {
    ICompilationUnit aUnit =
        createModelCompilationUnit(
            "test",
            "A.java",
            getSourceDQ(
                "package test;",
                "class A {",
                "  void foo() {",
                "  }",
                "  void baz() {",
                "  }",
                "}"));
    createModelCompilationUnit(
        "test",
        "B.java",
        getSourceDQ("package test;", "class B extends A {", "}"));
    ICompilationUnit cUnit =
        createModelCompilationUnit(
            "test",
            "C.java",
            getSourceDQ(
                "package test;",
                "class C extends B {",
                "  void foo() {",
                "  }",
                "  void bar() {",
                "  }",
                "}"));
    //
    IMethod[] aMethods = aUnit.getTypes()[0].getMethods();
    IMethod[] cMethods = cUnit.getTypes()[0].getMethods();
    //
    assertSame(aMethods[0], CodeUtils.findSuperMethod(cMethods[0]));
    assertNull(CodeUtils.findSuperMethod(cMethods[1]));
    assertNull(CodeUtils.findSuperMethod(aMethods[1]));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // findField()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link IField} that is declared directly in given {@link IType}.
   */
  public void test_findField_1() throws Exception {
    IJavaProject javaProject = m_testProject.getJavaProject();
    setFileContentSrc(
        "test/PrefConstants.java",
        getSourceDQ("package test;", "public interface PrefConstants {", "  int field = 0;", "}"));
    assertNull(CodeUtils.findField(javaProject, "no.such.Type", "noMatter"));
    assertNull(CodeUtils.findField(javaProject, "test.PrefConstants", "noSuchField"));
    {
      IField field = CodeUtils.findField(javaProject, "test.PrefConstants", "field");
      assertNotNull(field);
      assertEquals("field", field.getElementName());
    }
  }

  /**
   * {@link IField} that is declared in super-class.
   */
  public void test_findField_2() throws Exception {
    IJavaProject javaProject = m_testProject.getJavaProject();
    createModelType(
        "test",
        "A.java",
        getSourceDQ("package test;", "public class A {", "  int field;", "}"));
    createModelType(
        "test",
        "B.java",
        getSourceDQ("package test;", "public class B extends A {", "  int otherField;", "}"));
    //
    assertNull(CodeUtils.findField(javaProject, "test.B", "noSuchField"));
    {
      IField field = CodeUtils.findField(javaProject, "test.B", "field");
      assertNotNull(field);
      assertEquals("field", field.getElementName());
      assertEquals("test.A", field.getDeclaringType().getFullyQualifiedName());
    }
    {
      IField field = CodeUtils.findField(javaProject, "test.B", "otherField");
      assertNotNull(field);
      assertEquals("otherField", field.getElementName());
      assertEquals("test.B", field.getDeclaringType().getFullyQualifiedName());
    }
  }

  /**
   * {@link IField}'s that are declared in interfaces.
   */
  public void test_findField_3() throws Exception {
    IJavaProject javaProject = m_testProject.getJavaProject();
    createModelType(
        "test",
        "IA.java",
        getSourceDQ("package test;", "public interface IA {", "  int fieldA = 0;", "}"));
    createModelType(
        "test",
        "IB.java",
        getSourceDQ("package test;", "public interface IB extends IA {", "  int fieldB = 0;", "}"));
    createModelType(
        "test",
        "C.java",
        getSourceDQ("package test;", "public class C implements IB {", "}"));
    //
    assertNull(CodeUtils.findField(javaProject, "test.C", "noSuchField"));
    {
      IField field = CodeUtils.findField(javaProject, "test.C", "fieldA");
      assertNotNull(field);
      assertEquals("fieldA", field.getElementName());
      assertEquals("test.IA", field.getDeclaringType().getFullyQualifiedName());
    }
    {
      IField field = CodeUtils.findField(javaProject, "test.C", "fieldB");
      assertNotNull(field);
      assertEquals("fieldB", field.getElementName());
      assertEquals("test.IB", field.getDeclaringType().getFullyQualifiedName());
    }
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
   * @return the array of {@link IMethodBinding} for statements in method with given index.
   */
  static IMethodBinding[] getInvocationBindings(TypeDeclaration typeDeclaration, int index) {
    IMethodBinding[] methodBindings;
    MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[index];
    List<Statement> statements = DomGenerics.statements(methodDeclaration.getBody());
    methodBindings = new IMethodBinding[statements.size()];
    for (int i = 0; i < statements.size(); i++) {
      ExpressionStatement statement = (ExpressionStatement) statements.get(i);
      MethodInvocation invocation = (MethodInvocation) statement.getExpression();
      methodBindings[i] = AstNodeUtils.getMethodBinding(invocation);
    }
    return methodBindings;
  }
}
