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

import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.core.utils.reflect.ProjectClassLoader;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.tests.designer.Expectations;
import org.eclipse.wb.tests.designer.Expectations.StrValue;
import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.designer.core.AbstractJavaTest;
import org.eclipse.wb.tests.designer.core.PdeProjectConversionUtils;
import org.eclipse.wb.tests.designer.core.TestBundle;
import org.eclipse.wb.tests.designer.core.TestProject;
import org.eclipse.wb.tests.designer.core.ZipFileFactory;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;

import static org.assertj.core.api.Assertions.assertThat;

import org.osgi.framework.Bundle;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Tests for {@link ProjectUtils}.
 * 
 * @author scheglov_ke
 * @author mitin_aa
 */
public class ProjectUtilsTest extends AbstractJavaTest {
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
  // Nature
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ProjectUtils#hasNature(IProject, String)}.
   */
  public void test_hasNature() throws Exception {
    assertTrue(ProjectUtils.hasNature(m_project, JavaCore.NATURE_ID));
    assertFalse(ProjectUtils.hasNature(m_project, "no.such.nature"));
  }

  /**
   * Test for {@link ProjectUtils#addNature(IProject, String)}.
   */
  @DisposeProjectAfter
  public void test_addNature() throws Exception {
    TestUtils.addDynamicExtension2(
        "org.eclipse.core.resources.natures",
        "MyTestNature",
        getSourceDQ(
            "<extension point='%pointId%' id='%extensionId%' name='My Test nature'>",
            "  <runtime>",
            "    <run class='" + MyNatureClass.class.getName() + "'/>",
            "  </runtime>",
            "</extension>"));
    try {
      cleanUpNatureManager();
      ProjectUtils.addNature(m_testProject.getProject(), "org.eclipse.wb.tests.MyTestNature");
    } finally {
      TestUtils.removeDynamicExtension("org.eclipse.core.resources.natures", "MyTestNature");
      cleanUpNatureManager();
    }
  }

  /**
   * Test for {@link ProjectUtils#removeNature(IProject, String)}.
   */
  @DisposeProjectAfter
  public void test_removeNature() throws Exception {
    assertTrue(m_project.hasNature(JavaCore.NATURE_ID));
    ProjectUtils.removeNature(m_project, JavaCore.NATURE_ID);
    assertFalse(m_project.hasNature(JavaCore.NATURE_ID));
  }

  /**
   * No-op implementation of {@link IProjectNature}.
   * 
   * @author scheglov_ke
   */
  public static class MyNatureClass implements IProjectNature {
    public IProject getProject() {
      return null;
    }

    public void setProject(IProject project) {
    }

    public void configure() throws CoreException {
    }

    public void deconfigure() throws CoreException {
    }
  }

  /**
   * <code>org.eclipse.core.internal.resources.NatureManager</code> is not dynamic aware, so we can
   * not just contribute nature and use it, we have to "deinitialize" it before to force natures
   * reload.
   */
  private static void cleanUpNatureManager() throws Exception {
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    Object natureManager = ReflectionUtils.invokeMethod(workspace, "getNatureManager()");
    ReflectionUtils.setField(natureManager, "descriptors", null);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ProjectUtils#getJavaVersion(IJavaProject)}.
   */
  public void test_getJavaVersion() throws Exception {
    // initially has 1.7 compliance
    assertEquals(1.7, ProjectUtils.getJavaVersion(m_javaProject), 0.001);
    // set temporary 1.3 compliance
    {
      String oldCompliance = m_javaProject.getOption(JavaCore.COMPILER_COMPLIANCE, true);
      try {
        m_javaProject.setOption(JavaCore.COMPILER_COMPLIANCE, "1.3");
        assertEquals(1.3, ProjectUtils.getJavaVersion(m_javaProject), 0.001);
      } finally {
        m_javaProject.setOption(JavaCore.COMPILER_COMPLIANCE, oldCompliance);
      }
    }
    // set "null", so default compliance, we use Java 1.7
    {
      String oldCompliance = m_javaProject.getOption(JavaCore.COMPILER_COMPLIANCE, true);
      try {
        m_javaProject.setOption(JavaCore.COMPILER_COMPLIANCE, null);
        assertEquals(1.7, ProjectUtils.getJavaVersion(m_javaProject), 0.001);
      } finally {
        m_javaProject.setOption(JavaCore.COMPILER_COMPLIANCE, oldCompliance);
      }
    }
    // check that again 1.7 compliance
    assertEquals(1.7, ProjectUtils.getJavaVersion(m_javaProject), 0.001);
  }

  /**
   * Test for {@link ProjectUtils#isJDK15(IJavaProject)}.
   */
  public void test_isJDK15() throws Exception {
    // initially has 1.5 compliance
    assertTrue(ProjectUtils.isJDK15(m_javaProject));
    // set temporary 1.3 compliance
    {
      String oldCompliance = m_javaProject.getOption(JavaCore.COMPILER_COMPLIANCE, true);
      try {
        m_javaProject.setOption(JavaCore.COMPILER_COMPLIANCE, "1.3");
        assertFalse(ProjectUtils.isJDK15(m_javaProject));
      } finally {
        m_javaProject.setOption(JavaCore.COMPILER_COMPLIANCE, oldCompliance);
      }
    }
    // set "null", so default compliance, we use Java 6 (or may be 5), so 1.5 is default
    {
      String oldCompliance = m_javaProject.getOption(JavaCore.COMPILER_COMPLIANCE, true);
      try {
        m_javaProject.setOption(JavaCore.COMPILER_COMPLIANCE, null);
        assertTrue(ProjectUtils.isJDK15(m_javaProject));
      } finally {
        m_javaProject.setOption(JavaCore.COMPILER_COMPLIANCE, oldCompliance);
      }
    }
    // check that again 1.5 compliance
    assertTrue(ProjectUtils.isJDK15(m_javaProject));
    // invalid project
    assertFalse(ProjectUtils.isJDK15(null));
  }

  /**
   * Test for {@link ProjectUtils#getOptions(IJavaProject)}.
   */
  public void test_getOptions() throws Exception {
    Map<String, String> options = ProjectUtils.getOptions(m_testProject.getJavaProject());
    assertNotNull(options);
    // check one option
    assertEquals("error", options.get("org.eclipse.jdt.core.incompleteClasspath"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // waitForAutoBuild()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ProjectUtils#waitForAutoBuild()}.
   */
  public void test_waitForAutoBuild() throws Exception {
    ProjectClassLoader classLoader =
        ProjectClassLoader.create(null, m_testProject.getJavaProject());
    // create unit
    setFileContentSrc(
        "test/Test.java",
        getSource("// filler filler filler", "// filler filler filler", "public class Test {", "}"));
    // no Test.class expected
    try {
      classLoader.loadClass("test.Test");
      fail();
    } catch (ClassNotFoundException e) {
    }
    // wait for build
    ProjectUtils.waitForAutoBuild();
    // now Test.class can be loaded
    classLoader.loadClass("test.Test");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Resource
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ProjectUtils#ensureResourceType(IJavaProject, Bundle, String)}.
   */
  @DisposeProjectAfter
  public void test_ensureResourceType_14() throws Exception {
    String managerClassName = "pkg.MyManager";
    // use test Bundle
    TestBundle testBundle = new TestBundle();
    try {
      String managerSource =
          getSource(
              "package test;",
              "public class MyManager {",
              "  // 1.4",
              "  // filler filler filler",
              "}");
      testBundle.setFile("resources/1.4/pkg/MyManager.java", managerSource);
      testBundle.install();
      // IJavaProject has 1.4 compliance
      m_javaProject.setOption(JavaCore.COMPILER_COMPLIANCE, "1.4");
      assertFalse(ProjectUtils.isJDK15(m_javaProject));
      // no "manager" initially
      assertTrue(m_javaProject.findType(managerClassName) == null);
      // add "manager" from test bundle
      ProjectUtils.ensureResourceType(m_javaProject, testBundle.getBundle(), managerClassName);
      assertTrue(m_javaProject.findType(managerClassName) != null);
      assertEquals(managerSource, getFileContentSrc("pkg/MyManager.java"));
      // second "ensure" does not break anything
      ProjectUtils.ensureResourceType(m_javaProject, testBundle.getBundle(), managerClassName);
      assertTrue(m_javaProject.findType(managerClassName) != null);
    } finally {
      testBundle.dispose();
    }
  }

  /**
   * Test for {@link ProjectUtils#ensureResourceType(IJavaProject, Bundle, String)}.
   */
  @DisposeProjectAfter
  public void test_ensureResourceType_15() throws Exception {
    String managerClassName = "pkg.MyManager";
    // IJavaProject has 1.5 compliance
    assertTrue(ProjectUtils.isJDK15(m_javaProject));
    // no "manager" initially
    assertTrue(m_javaProject.findType(managerClassName) == null);
    // add "manager" from test bundle
    TestBundle testBundle = new TestBundle();
    try {
      String managerSource =
          getSource(
              "package test;",
              "public class MyManager {",
              "  // 1.5",
              "  // filler filler filler",
              "}");
      testBundle.setFile("resources/1.5/pkg/MyManager.java", managerSource);
      testBundle.install();
      // do ensure
      ProjectUtils.ensureResourceType(m_javaProject, testBundle.getBundle(), managerClassName);
      assertTrue(m_javaProject.findType(managerClassName) != null);
      assertEquals(managerSource, getFileContentSrc("pkg/MyManager.java"));
      // second "ensure" does not break anything
      ProjectUtils.ensureResourceType(m_javaProject, testBundle.getBundle(), managerClassName);
      assertTrue(m_javaProject.findType(managerClassName) != null);
    } finally {
      testBundle.dispose();
    }
  }

  /**
   * Test for {@link ProjectUtils#ensureResourceType(IJavaProject, Bundle, String)}.
   */
  @DisposeProjectAfter
  public void test_ensureResourceType_existsButNotUpToDate() throws Exception {
    String managerClassName = "pkg.MyManager";
    String managerPath = "pkg/MyManager.java";
    // set some other contents for file
    setFileContentSrc(
        managerPath,
        getSource(
            "package test;",
            "public class MyManager {",
            "  // old version",
            "  // filler filler filler",
            "}"));
    // "manager" exists
    assertNotNull(m_javaProject.findType(managerClassName));
    // add "manager" from test bundle
    TestBundle testBundle = new TestBundle();
    try {
      String managerSource =
          getSource(
              "package test;",
              "public class MyManager {",
              "  // recent version",
              "  // filler filler filler",
              "}");
      testBundle.setFile("resources/1.5/pkg/MyManager.java", managerSource);
      testBundle.install();
      // do ensure
      ProjectUtils.ensureResourceType(m_javaProject, testBundle.getBundle(), managerClassName);
      assertTrue(m_javaProject.findType(managerClassName) != null);
      assertEquals(managerSource, getFileContentSrc(managerPath));
      // remember "modification stamp"
      IFile managerFile = getFileSrc(managerPath);
      long managerStamp = managerFile.getModificationStamp();
      // second "ensure" does not break anything
      ProjectUtils.ensureResourceType(m_javaProject, testBundle.getBundle(), managerClassName);
      assertTrue(m_javaProject.findType(managerClassName) != null);
      // "modification stamp" should not be changed, because "manager" was up to date
      assertEquals(managerStamp, managerFile.getModificationStamp());
    } finally {
      testBundle.dispose();
    }
  }

  /**
   * Test for {@link ProjectUtils#ensureResourceType(IJavaProject, Bundle, String)}.
   * <p>
   * If type exists, but not in given {@link IJavaProject}, i.e. in required project, then we should
   * update it in required project; not generate new copy in given {@link IJavaProject}.
   */
  @DisposeProjectAfter
  public void test_ensureResourceType_existsInDifferentProject_butNotUpToDate() throws Exception {
    String managerClassName = "pkg.MyManager";
    String managerPath = "pkg/MyManager.java";
    // set some other contents for file
    TestProject myProjectHelper = new TestProject("myProject");
    IProject myProject = myProjectHelper.getProject();
    {
      m_testProject.addRequiredProject(myProject);
      setFileContentSrc(
          myProject,
          managerPath,
          getSource(
              "package pkg;",
              "public class MyManager {",
              "  // old version",
              "  // filler filler filler",
              "}"));
    }
    // "manager" exists, in "myProject"
    {
      IType type = m_javaProject.findType(managerClassName);
      assertNotNull(type);
      IPath fullPath = type.getUnderlyingResource().getFullPath();
      assertThat(fullPath.toPortableString()).contains("/myProject/src/");
    }
    // no "manager" in main project
    assertFalse(getFileSrc(managerPath).exists());
    // add "manager" from test bundle
    TestBundle testBundle = new TestBundle();
    try {
      String managerSource =
          getSource(
              "package pkg;",
              "public class MyManager {",
              "  // recent version",
              "  // filler filler filler",
              "}");
      testBundle.setFile("resources/1.5/pkg/MyManager.java", managerSource);
      testBundle.install();
      // do ensure
      ProjectUtils.ensureResourceType(m_javaProject, testBundle.getBundle(), managerClassName);
      assertNotNull(m_javaProject.findType(managerClassName));
      // no "manager" in main project
      assertFalse(getFileSrc(managerPath).exists());
      // updated in "myProject"
      assertEquals(managerSource, getFileContentSrc(myProject, managerPath));
    } finally {
      testBundle.dispose();
      myProjectHelper.dispose();
    }
  }

  /**
   * Test for {@link ProjectUtils#ensureResourceType(IJavaProject, Bundle, String)}.
   * <p>
   * We should ignore {@link IType} if it is declared in binary file.
   */
  @DisposeProjectAfter
  public void test_ensureResourceType_binary() throws Exception {
    String managerClassName = "pkg.MyManager";
    String managerPath = "pkg/MyManager.java";
    String managerSourceOld =
        getSource(
            "package pkg;",
            "public class MyManager {",
            "  // old version, in binary",
            "  // filler filler filler",
            "}");
    // set some other contents for file
    setFileContentSrc(managerPath, managerSourceOld);
    waitForAutoBuild();
    // wrap "manager" class into Jar
    File managerJar;
    {
      managerJar = File.createTempFile("Manager", ".jar");
      ZipFileFactory factory = new ZipFileFactory(new FileOutputStream(managerJar));
      factory.add("pkg/MyManager.class", getFile("bin/pkg/MyManager.class").getContents());
      factory.close();
    }
    // remove "manager" type
    {
      IType type = m_javaProject.findType(managerClassName);
      type.getCompilationUnit().delete(true, null);
      assertNull(m_javaProject.findType(managerClassName));
    }
    // add "manager" from test bundle
    TestBundle testBundle = new TestBundle();
    try {
      String managerSourceNew =
          getSource(
              "package test;",
              "public class MyManager {",
              "  // recent version",
              "  // filler filler filler",
              "  public int newField;",
              "}");
      testBundle.setFile("resources/1.5/pkg/MyManager.java", managerSourceNew);
      testBundle.install();
      // add "manager" IType using Jar
      m_testProject.addExternalJar(managerJar);
      {
        IType managerType = m_javaProject.findType(managerClassName);
        assertThat(managerType).isNotNull();
        assertThat(managerType.getFields()).isEmpty();
      }
      // no changes, because "manager" is in binary 
      ProjectUtils.ensureResourceType(m_javaProject, testBundle.getBundle(), managerClassName);
      {
        IType managerType = m_javaProject.findType(managerClassName);
        assertThat(managerType).isNotNull();
        assertThat(managerType.getFields()).isEmpty();
      }
    } finally {
      testBundle.dispose();
      managerJar.delete();
    }
  }

  /**
   * Test for {@link ProjectUtils#ensureResourceType(IJavaProject, Bundle, String)}.
   * <p>
   * We should not try to update {@link IType} if it is in "read-only" unit.
   */
  @DisposeProjectAfter
  public void test_ensureResourceType_readOnly() throws Exception {
    String managerClassName = "pkg.MyManager";
    String managerPath = "pkg/MyManager.java";
    // set some other contents for file
    String oldSource =
        getSource(
            "package pkg;",
            "public class MyManager {",
            "  // old version",
            "  // filler filler filler filler filler",
            "}");
    IFile managerFile = setFileContentSrc(managerPath, oldSource);
    // mark as read-only
    {
      ResourceAttributes resourceAttributes = managerFile.getResourceAttributes();
      resourceAttributes.setReadOnly(true);
      managerFile.setResourceAttributes(resourceAttributes);
    }
    // "manager" exists
    assertNotNull(m_javaProject.findType(managerClassName));
    // add "manager" from test bundle
    TestBundle testBundle = new TestBundle();
    try {
      String newSource =
          getSource(
              "package pkg;",
              "public class MyManager {",
              "  // recent version",
              "  // filler filler filler filler filler",
              "}");
      testBundle.setFile("resources/1.5/pkg/MyManager.java", newSource);
      testBundle.install();
      // do ensure, ignored because file is read-only
      ProjectUtils.ensureResourceType(m_javaProject, testBundle.getBundle(), managerClassName);
      assertTrue(m_javaProject.findType(managerClassName) != null);
      assertEquals(oldSource, getFileContentSrc(managerPath));
    } finally {
      testBundle.dispose();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Classpath
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ProjectUtils#hasType(IJavaProject, String)}.
   */
  public void test_hasType() throws Exception {
    assertTrue(ProjectUtils.hasType(m_javaProject, "java.lang.String"));
    // no such type yet
    assertFalse(ProjectUtils.hasType(m_javaProject, "aaa.bbb.ccc.Test"));
    // add type
    setFileContentSrc(
        "aaa/bbb/ccc/Test.java",
        getSource(
            "package aaa.bbb.ccc;",
            "import javax.swing.JFrame;",
            "public class Test extends JFrame {",
            "}"));
    waitForAutoBuild();
    // OK, now such type exists in project
    assertTrue(ProjectUtils.hasType(m_javaProject, "aaa.bbb.ccc.Test"));
  }

  /**
   * Test for {@link ProjectUtils#addJar(IJavaProject, String, String)}.
   */
  @DisposeProjectAfter
  public void test_addJar_usingPaths() throws Exception {
    TestBundle testBundle = new TestBundle();
    try {
      String className = ClassForBundle.class.getName();
      testBundle.addJar("resources/myClasses.jar").addClass(ClassForBundle.class).close();
      testBundle.addJar("resources/myClasses.zip").add("A.java", "AaA").close();
      testBundle.install();
      // no ClassForBundle initially
      assertFalse(ProjectUtils.hasType(m_javaProject, className));
      // add jar/zip with ClassForBundle
      Bundle bundle = testBundle.getBundle();
      String jarPath = FileLocator.toFileURL(bundle.getEntry("/resources/myClasses.jar")).getPath();
      String srcPath = FileLocator.toFileURL(bundle.getEntry("/resources/myClasses.zip")).getPath();
      ProjectUtils.addJar(m_javaProject, jarPath, srcPath);
      waitForAutoBuild();
      // OK, ClassForBundle now exists in project
      assertTrue(ProjectUtils.hasType(m_javaProject, className));
      assertFileExists("myClasses.zip");
    } finally {
      testBundle.dispose();
    }
  }

  /**
   * Test for {@link ProjectUtils#addJar(IJavaProject, Bundle, String, String)}.
   * <p>
   * Add jar and src from {@link Bundle}, from some directory.
   */
  @DisposeProjectAfter
  public void test_addJar_fromBundle() throws Exception {
    TestBundle testBundle = new TestBundle();
    try {
      String className = ClassForBundle.class.getName();
      testBundle.addJar("resources/myClasses.jar").addClass(ClassForBundle.class).close();
      testBundle.addJar("resources/myClasses.zip").add("A.java", "AaA").close();
      testBundle.install();
      // no ClassForBundle initially
      assertFalse(ProjectUtils.hasType(m_javaProject, className));
      // add jar/zip with ClassForBundle
      ProjectUtils.addJar(
          m_javaProject,
          testBundle.getBundle(),
          "resources/myClasses.jar",
          "resources/myClasses.zip");
      waitForAutoBuild();
      // OK, ClassForBundle now exists in project
      assertTrue(ProjectUtils.hasType(m_javaProject, className));
      assertFileExists("myClasses.zip");
    } finally {
      testBundle.dispose();
    }
  }

  /**
   * Test for {@link ProjectUtils#addJar(IJavaProject, Bundle, String, String)}.
   * <p>
   * Add jar from {@link Bundle}, without sources.
   */
  @DisposeProjectAfter
  public void test_addJar_fromBundle_noSrc() throws Exception {
    TestBundle testBundle = new TestBundle();
    try {
      String className = ClassForBundle.class.getName();
      testBundle.addJar("resources/myClasses.jar").addClass(ClassForBundle.class).close();
      testBundle.install();
      // no ClassForBundle initially
      assertFalse(ProjectUtils.hasType(m_javaProject, className));
      // add jar with ClassForBundle
      ProjectUtils.addJar(m_javaProject, testBundle.getBundle(), "resources/myClasses.jar", null);
      waitForAutoBuild();
      // OK, ClassForBundle now exists in project
      assertTrue(ProjectUtils.hasType(m_javaProject, className));
      assertFileNotExists("myClasses.zip");
    } finally {
      testBundle.dispose();
    }
  }

  /**
   * Test for {@link ProjectUtils#addJar(IJavaProject, Bundle, String, String)}.
   * <p>
   * Add jar from {@link Bundle}, from some directory.
   * <p>
   * Project is also PDE project, so manifest also should be updated.
   */
  @DisposeProjectAfter
  public void test_addJar_whenPDE() throws Exception {
    PdeProjectConversionUtils.convertToPDE(m_project, null);
    TestBundle testBundle = new TestBundle();
    try {
      String className = ClassForBundle.class.getName();
      testBundle.addJar("resources/myClasses.jar").addClass(ClassForBundle.class).close();
      testBundle.install();
      // no ClassForBundle initially
      assertFalse(ProjectUtils.hasType(m_javaProject, className));
      // add jar/zip with ClassForBundle
      ProjectUtils.addJar(m_javaProject, testBundle.getBundle(), "resources/myClasses.jar", null);
      waitForAutoBuild();
      // OK, ClassForBundle now exists in project
      assertTrue(ProjectUtils.hasType(m_javaProject, className));
      // PDE manifest also updated
      {
        String manifest = getFileContent("META-INF/MANIFEST.MF");
        assertThat(manifest).contains("Bundle-ClassPath: .,\n myClasses.jar\n");
      }
    } finally {
      testBundle.dispose();
    }
  }

  /**
   * Test for {@link ProjectUtils#addExternalJar(IJavaProject, String, String)}.
   */
  @DisposeProjectAfter
  public void test_addExternalJar() throws Exception {
    TestBundle testBundle = new TestBundle();
    try {
      String className = ClassForBundle.class.getName();
      testBundle.addJar("resources/myClasses.jar").addClass(ClassForBundle.class).close();
      testBundle.install();
      // no ClassForBundle initially
      assertFalse(ProjectUtils.hasType(m_javaProject, className));
      // add jar with ClassForBundle
      {
        Bundle bundle = testBundle.getBundle();
        URL jarEntry = bundle.getEntry("/resources/myClasses.jar");
        String jarPath = FileLocator.toFileURL(jarEntry).getPath();
        ProjectUtils.addExternalJar(m_javaProject, jarPath, null);
      }
      waitForAutoBuild();
      // OK, ClassForBundle now exists in project
      assertTrue(ProjectUtils.hasType(m_javaProject, className));
      // but JAR file was not copied
      assertFalse(m_project.getFile("myClasses.jar").exists());
    } finally {
      testBundle.dispose();
    }
  }

  /**
   * Test for {@link ProjectUtils#addClasspathEntry(IJavaProject, IClasspathEntry)}.
   */
  @DisposeProjectAfter
  public void test_addClasspathEntry() throws Exception {
    TestBundle testBundle = new TestBundle();
    try {
      String className = ClassForBundle.class.getName();
      testBundle.addJar("resources/myClasses.jar").addClass(ClassForBundle.class).close();
      testBundle.install();
      // no ClassForBundle initially
      assertFalse(ProjectUtils.hasType(m_javaProject, className));
      // add jar with ClassForBundle
      {
        Bundle bundle = testBundle.getBundle();
        URL jarEntry = bundle.getEntry("/resources/myClasses.jar");
        String jarPath = FileLocator.toFileURL(jarEntry).getPath();
        IClasspathEntry entry = JavaCore.newLibraryEntry(new Path(jarPath), null, null);
        ProjectUtils.addClasspathEntry(m_javaProject, entry);
      }
      waitForAutoBuild();
      // OK, ClassForBundle now exists in project
      assertTrue(ProjectUtils.hasType(m_javaProject, className));
    } finally {
      testBundle.dispose();
    }
  }

  /**
   * Test for {@link ProjectUtils#addPluginLibraries(IJavaProject, String)}.
   */
  @DisposeProjectAfter
  public void test_addPluginLibraries() throws Exception {
    // no org.eclipse.jdt.core.IType
    assertFalse(ProjectUtils.hasType(m_javaProject, "org.eclipse.jdt.core.IType"));
    // add org.eclipse.jdt.core plugin
    ProjectUtils.addPluginLibraries(m_javaProject, "org.eclipse.jdt.core");
    // OK, org.eclipse.jdt.core.IType now exists in project
    assertTrue(ProjectUtils.hasType(m_javaProject, "org.eclipse.jdt.core.IType"));
  }

  /**
   * Test for {@link ProjectUtils#addSWTLibrary(IJavaProject)}.
   */
  @DisposeProjectAfter
  public void test_addSWTLibrary() throws Exception {
    // no org.eclipse.swt.SWT
    assertFalse(ProjectUtils.hasType(m_javaProject, "org.eclipse.swt.SWT"));
    // add SWP plugin library
    ProjectUtils.addSWTLibrary(m_javaProject);
    // OK, org.eclipse.swt.SWT now exists in project
    assertTrue(ProjectUtils.hasType(m_javaProject, "org.eclipse.swt.SWT"));
  }

  /**
   * Test for {@link ProjectUtils#removeClasspathEntries(IJavaProject, Predicate)}.
   */
  @DisposeProjectAfter
  public void test_removeClasspathEntries() throws Exception {
    // initially has JRE_CONTAINER and "src"
    {
      IClasspathEntry[] rawClasspath = m_javaProject.getRawClasspath();
      assertThat(rawClasspath).hasSize(2);
      assertEquals(
          "org.eclipse.jdt.launching.JRE_CONTAINER",
          rawClasspath[0].getPath().toPortableString());
      assertEquals("/TestProject/src", rawClasspath[1].getPath().toPortableString());
    }
    // remove "src"
    ProjectUtils.removeClasspathEntries(m_javaProject, new Predicate<IClasspathEntry>() {
      @Override
      public boolean apply(IClasspathEntry entry) {
        String location = entry.getPath().toPortableString();
        return location.endsWith("/src");
      }
    });
    // has only JRE_CONTAINER
    {
      IClasspathEntry[] rawClasspath = m_javaProject.getRawClasspath();
      assertThat(rawClasspath).hasSize(1);
      assertEquals(
          "org.eclipse.jdt.launching.JRE_CONTAINER",
          rawClasspath[0].getPath().toPortableString());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Resources
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ProjectUtils#getOSPath(IPath)}.
   */
  public void test_getOSPath_noSuchPath() throws Exception {
    IPath workspacePath = new Path("/noSuchProject/andNoFolder");
    IPath osPath = ProjectUtils.getOSPath(workspacePath);
    assertNull(osPath);
  }

  /**
   * Test for {@link ProjectUtils#getOSPath(IPath)}.
   */
  public void test_getOSPath_forSourceFolder() throws Exception {
    assert_test_getOSPath("/TestProject/src", "TestProject/src");
  }

  /**
   * Test for {@link ProjectUtils#getOSPath(IPath)}.
   */
  public void test_getOSPath_forOutputFolder() throws Exception {
    assert_test_getOSPath("/TestProject/bin", "TestProject/bin");
  }

  /**
   * Test for {@link ProjectUtils#getOSPath(IPath)}.
   */
  public void test_getOSPath_forProjectItself() throws Exception {
    assert_test_getOSPath("/TestProject", "TestProject");
  }

  private void assert_test_getOSPath(String workspacePathString, String expectedLocation) {
    IPath workspacePath = new Path(workspacePathString);
    IPath osPath = ProjectUtils.getOSPath(workspacePath);
    String osLocation = osPath.toPortableString();
    String pathEnds =
        Expectations.get("junit-workspace/" + expectedLocation, new StrValue[]{
            new StrValue("scheglov-macpro", "/" + expectedLocation),
            new StrValue("sablin-aa", ".wbp-tt/Core/" + expectedLocation),
            new StrValue("flanker-windows", "-Core.TWS/" + expectedLocation),
            new StrValue("flanker-desktop", ".wbp-tt/Core/" + expectedLocation)});
    assertThat(osLocation).endsWith(pathEnds);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // findFiles()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ProjectUtils#findFiles(IJavaProject, String)}.
   */
  public void test_findFiles_oneProject() throws Exception {
    IFile file = setFileContent("folder/1.txt", "");
    List<IFile> files = ProjectUtils.findFiles(m_testProject.getJavaProject(), "folder/1.txt");
    assertThat(files).containsOnly(file);
  }

  /**
   * Test for {@link ProjectUtils#findFiles(IJavaProject, String)}.
   */
  @DisposeProjectAfter
  public void test_findFiles_twoProjects() throws Exception {
    // create new project "myProject"
    TestProject myProject = new TestProject("myProject");
    try {
      // reference "myProject" from "TestProject"
      ProjectUtils.requireProject(m_testProject.getJavaProject(), myProject.getJavaProject());
      // prepare files
      IFile file_1 = setFileContent(m_testProject.getProject(), "folder/1.txt", "");
      IFile file_2 = setFileContent(myProject.getProject(), "folder/1.txt", "");
      // assert files
      List<IFile> files = ProjectUtils.findFiles(m_testProject.getJavaProject(), "folder/1.txt");
      assertThat(files).containsOnly(file_1, file_2);
    } finally {
      myProject.dispose();
    }
  }

  /**
   * Test for {@link ProjectUtils#findFiles(IJavaProject, String)}.
   * <p>
   * Reference not existing {@link IProject}.
   */
  @DisposeProjectAfter
  public void test_findFiles_notExistingProject() throws Exception {
    // add "myProject"
    TestProject myProject = new TestProject("myProject");
    m_testProject.addRequiredProject(myProject);
    myProject.dispose();
    // prepare files
    IFile file = setFileContent(m_testProject.getProject(), "folder/1.txt", "");
    // assert files
    List<IFile> files = ProjectUtils.findFiles(m_testProject.getJavaProject(), "folder/1.txt");
    assertThat(files).containsOnly(file);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ClassForBundle
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * We use this class to put it into new {@link Bundle}.
   */
  public static class ClassForBundle {
  }
}
