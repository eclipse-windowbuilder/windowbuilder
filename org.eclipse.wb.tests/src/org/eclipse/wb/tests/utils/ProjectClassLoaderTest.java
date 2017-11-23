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
package org.eclipse.wb.tests.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.core.utils.reflect.ProjectClassLoader;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.core.PdeProjectConversionUtils;
import org.eclipse.wb.tests.designer.core.TestProject;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.CompilationUnit;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * Test for {@link ProjectClassLoader}.
 * 
 * @author scheglov_ke
 */
public class ProjectClassLoaderTest extends SwingModelTest {
  private static IWorkspace workspace = ResourcesPlugin.getWorkspace();
  private static IWorkspaceRoot workspaceRoot = workspace.getRoot();
  private static final String workspaceLocation = workspaceRoot.getLocation().toPortableString();

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
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that {@link ProjectClassLoader} defines packages.
   */
  public void test_getPackage() throws Exception {
    setFileContentSrc(
        "test/SuperPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "abstract public class SuperPanel extends JPanel {",
            "}"));
    waitForAutoBuild();
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "abstract class Test extends SuperPanel {",
            "  public Test() {",
            "  }",
            "}");
    //
    Class<?> superPanelClass = panel.getDescription().getComponentClass();
    assertNotNull(superPanelClass.getPackage());
    assertEquals("test", superPanelClass.getPackage().getName());
  }

  /**
   * Test that we can inherit from abstract classes with declared abstract method.
   */
  public void test_inheritanceWithAbstractMethod_noInvocation() throws Exception {
    setFileContentSrc(
        "test/AbstractPanel.java",
        getTestSource(
            "abstract public class AbstractPanel extends JPanel {",
            "  protected abstract int myAbstractMethod();",
            "}"));
    waitForAutoBuild();
    //
    parseContainer(
        "// filler filler filler",
        "abstract class Test extends AbstractPanel {",
        "  public Test() {",
        "  }",
        "}");
  }

  /**
   * If abstract method is invoked from binary, but not implemented in AST, return default value.
   */
  public void test_inheritanceWithAbstractMethod_withInvocation() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public abstract class MyPanel extends JPanel {",
            "  private final String m_foo;",
            "  public MyPanel() {",
            "    m_foo = getFoo();",
            "  }",
            "  protected abstract String getFoo();",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public abstract class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
    assertEquals("<dynamic>", ReflectionUtils.getFieldObject(panel.getObject(), "m_foo"));
  }

  /**
   * If abstract void method is invoked, we ignore this, because we don't need its result.
   * <p>
   * Now we switch to AST interpretation it this case, if method is implemented in parsed
   * {@link CompilationUnit}.
   */
  public void test_inheritanceWithAbstractMethod_voidInvocation() throws Exception {
    setFileContentSrc(
        "test/AbstractPanel.java",
        getTestSource(
            "abstract public class AbstractPanel extends JPanel {",
            "  public AbstractPanel() {",
            "    myAbstractMethod();",
            "  }",
            "  protected abstract void myAbstractMethod();",
            "}"));
    waitForAutoBuild();
    //
    parseContainer(
        "// filler filler filler",
        "abstract class Test extends AbstractPanel {",
        "  public Test() {",
        "  }",
        "}");
  }

  /**
   * Test that we can inherit from abstract classes with declared, but not implemented interfaces.
   */
  public void test_inheritanceWithInterfaces() throws Exception {
    setFileContentSrc(
        "test/ValueProvider2.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public interface ValueProvider2 {",
            "  int getValue();",
            "}"));
    setFileContentSrc(
        "test/AbstractPanel.java",
        getTestSource(
            "abstract public class AbstractPanel extends JPanel implements ValueProvider2 {",
            "}"));
    waitForAutoBuild();
    //
    parseContainer(
        "// filler filler filler",
        "abstract class Test extends AbstractPanel {",
        "  public Test() {",
        "  }",
        "}");
  }

  /**
   * Test for case when we call re-implemented abstract method.
   */
  public void test_inheritanceImplementCall() throws Exception {
    setFileContentSrc(
        "test/ValueProvider.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public interface ValueProvider {",
            "  int getValue();",
            "}"));
    setFileContentSrc(
        "test/MyAbstractButton.java",
        getTestSource(
            "public abstract class MyAbstractButton extends JButton implements ValueProvider {",
            "  public void setHorizontalAlignment(int alignment) {",
            "    super.setHorizontalAlignment( getValue() );",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton extends MyAbstractButton {",
            "  public int getValue() {",
            "    return 0;",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    ContainerInfo panel =
        parseContainer(
            "abstract class Test extends JPanel {",
            "  public Test() {",
            "  MyButton button = new MyButton();",
            "    add( button );",
            "    button.setHorizontalAlignment(0);",
            "  }",
            "}");
    assertNoErrors(panel);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Import-Package
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for "Import-Package" statement support in manifest.
   * <p>
   * Note: right now only directly imported packages.
   */
  @DisposeProjectAfter
  public void test_importPackage() throws Exception {
    PdeProjectConversionUtils.convertToPDE(m_project, null);
    // create plugin with needed Class
    TestProject projectWithPackage = new TestProject("SomeProject");
    try {
      PdeProjectConversionUtils.convertToPDE(projectWithPackage.getProject(), null);
      setFileContentSrc(
          projectWithPackage.getProject(),
          "my/classes/MyClass.java",
          getSource(
              "// filler filler filler filler filler",
              "package my.classes;",
              "public class MyClass {",
              "}"));
      // export/import package
      {
        IFile manifestFile = getFile(projectWithPackage.getProject(), "META-INF/MANIFEST.MF");
        String manifest = getFileContent(manifestFile);
        manifest = manifest.trim() + "\nExport-Package: my.classes\n\n";
        setFileContent(manifestFile, manifest);
      }
      {
        IFile manifestFile = getFile(m_testProject.getProject(), "META-INF/MANIFEST.MF");
        String manifest = getFileContent(manifestFile);
        manifest = manifest.trim() + "\nImport-Package: my.classes\n\n";
        setFileContent(manifestFile, manifest);
      }
      waitForAutoBuild();
      // prepare ClassLoader
      ProjectClassLoader classLoader = ProjectClassLoader.create(null, m_javaProject);
      // no exception
      classLoader.loadClass("my.classes.MyClass");
    } finally {
      projectWithPackage.dispose();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Fragments
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Fragments should be included into {@link ClassLoader} of main plugin.
   */
  @DisposeProjectAfter
  public void test_fragments() throws Exception {
    PdeProjectConversionUtils.convertToPDE(m_testProject.getProject(), null);
    // create fragment
    TestProject fragmentProject = new TestProject("TestProject_ru");
    try {
      PdeProjectConversionUtils.convertToPDE(fragmentProject.getProject(), "TestProject");
      {
        setFileContent(
            m_testProject.getProject(),
            "src/com/test",
            "Application.properties",
            getSourceDQ("shell.text=Hello!"));
        setFileContent(
            fragmentProject.getProject(),
            "src/com/test",
            "Application_ru.properties",
            getSourceDQ("shell.text=Privet!"));
      }
      waitForAutoBuild();
      // prepare ClassLoader
      ProjectClassLoader classLoader =
          ProjectClassLoader.create(null, m_testProject.getJavaProject());
      assertNotNull(classLoader.getResource("com/test/Application.properties"));
      assertNotNull(classLoader.getResource("com/test/Application_ru.properties"));
    } finally {
      fragmentProject.dispose();
    }
  }

  /**
   * Fragments should be included into {@link ClassLoader} of main plugin.
   */
  @DisposeProjectAfter
  public void test_fragments_cycle() throws Exception {
    PdeProjectConversionUtils.convertToPDE(m_testProject.getProject(), null);
    // create fragment
    TestProject fragmentProject = new TestProject("TestProject_ru");
    try {
      PdeProjectConversionUtils.convertToPDE(fragmentProject.getProject(), "TestProject");
      {
        setFileContent(
            m_testProject.getProject(),
            "src/com/test",
            "Application.properties",
            getSourceDQ("shell.text=Hello!"));
        setFileContent(
            fragmentProject.getProject(),
            "src/com/test",
            "Application_ru.properties",
            getSourceDQ("shell.text=Privet!"));
      }
      waitForAutoBuild();
      // bad thing: add fragment into classpath of its host
      m_testProject.addRequiredProject(fragmentProject);
      // prepare ClassLoader
      ProjectClassLoader classLoader =
          ProjectClassLoader.create(null, m_testProject.getJavaProject());
      assertNotNull(classLoader.getResource("com/test/Application.properties"));
      assertNotNull(classLoader.getResource("com/test/Application_ru.properties"));
    } finally {
      fragmentProject.dispose();
    }
  }

  /**
   * Test for fragment that is not Java project. Not sure if this makes sense, but one user has such
   * project.
   */
  @DisposeProjectAfter
  public void test_fragments_notJavaFragment() throws Exception {
    PdeProjectConversionUtils.convertToPDE(m_testProject.getProject(), null);
    // create fragment
    TestProject fragmentProject = new TestProject("TestProject_ru");
    try {
      PdeProjectConversionUtils.convertToPDE(fragmentProject.getProject(), "TestProject");
      ProjectUtils.removeNature(fragmentProject.getProject(), JavaCore.NATURE_ID);
      {
        setFileContent(
            m_testProject.getProject(),
            "src/com/test",
            "Application.properties",
            getSourceDQ("shell.text=Hello!"));
        setFileContent(
            fragmentProject.getProject(),
            "com/test",
            "Application_ru.properties",
            getSourceDQ("shell.text=Privet!"));
      }
      waitForAutoBuild();
      // prepare ClassLoader
      ProjectClassLoader classLoader =
          ProjectClassLoader.create(null, m_testProject.getJavaProject());
      assertNotNull(classLoader.getResource("com/test/Application.properties"));
      assertNotNull(classLoader.getResource("com/test/Application_ru.properties"));
    } finally {
      fragmentProject.dispose();
    }
  }

  /**
   * Fragments should be included into {@link ClassLoader} of main plugin and required plugins.
   */
  @DisposeProjectAfter
  public void test_fragments_ofRequiredProject() throws Exception {
    PdeProjectConversionUtils.convertToPDE(m_testProject.getProject(), null);
    // create projects
    TestProject requiredProject = new TestProject("RequiredProject");
    TestProject fragmentProject = new TestProject("RequiredProject_ru");
    try {
      PdeProjectConversionUtils.convertToPDE(requiredProject.getProject(), null);
      PdeProjectConversionUtils.convertToPDE(fragmentProject.getProject(), "RequiredProject");
      {
        setFileContent(
            requiredProject.getProject(),
            "src/com/test",
            "Application.properties",
            getSourceDQ("shell.text=Hello!"));
        setFileContent(
            fragmentProject.getProject(),
            "src/com/test",
            "Application_ru.properties",
            getSourceDQ("shell.text=Privet!"));
      }
      m_testProject.addRequiredProject(requiredProject);
      waitForAutoBuild();
      // prepare ClassLoader
      ProjectClassLoader classLoader =
          ProjectClassLoader.create(null, m_testProject.getJavaProject());
      assertNotNull(classLoader.getResource("com/test/Application.properties"));
      assertNotNull(classLoader.getResource("com/test/Application_ru.properties"));
    } finally {
      fragmentProject.dispose();
      requiredProject.dispose();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // addSourceLocations()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_addSourceLocations_normalProject() throws Exception {
    List<String> locations = getSourceLocations();
    assertThat(locations).containsExactly(workspaceLocation + "/TestProject/src");
  }

  @DisposeProjectAfter
  public void test_addSourceLocations_noSuchProject() throws Exception {
    m_project.delete(true, null);
    // check locations
    List<String> locations = getSourceLocations();
    assertThat(locations).isEmpty();
  }

  @DisposeProjectAfter
  public void test_addSourceLocations_notJavaProject() throws Exception {
    ProjectUtils.removeNature(m_project, JavaCore.NATURE_ID);
    // check locations
    List<String> locations = getSourceLocations();
    assertThat(locations).isEmpty();
  }

  @DisposeProjectAfter
  public void test_addSourceLocations_projectNotInWorkspace() throws Exception {
    String newProjectLocation = moveProjectIntoWorkspaceSubFolder();
    // check locations
    List<String> locations = getSourceLocations();
    assertThat(locations).containsExactly(newProjectLocation + "/src");
  }

  @DisposeProjectAfter
  public void test_addSourceLocations_recursion() throws Exception {
    // create new project "myProject"
    TestProject myProject = new TestProject("myProject");
    IJavaProject myJavaProject = myProject.getJavaProject();
    // reference "myProject" from "TestProject"
    try {
      // create circular dependency
      ProjectUtils.requireProject(m_javaProject, myJavaProject);
      ProjectUtils.requireProject(myJavaProject, m_javaProject);
      // check locations
      List<String> locations = getSourceLocations();
      assertThat(locations).containsExactly(
          workspaceLocation + "/TestProject/src",
          workspaceLocation + "/myProject/src");
    } finally {
      myProject.dispose();
    }
  }

  /**
   * Old Java project style, when source and output is project itself.
   * <p>
   * https://groups.google.com/forum/#!topic/google-web-toolkit/r0Klxfkd7qA
   */
  @DisposeProjectAfter
  public void test_addSourceLocations_oldProjectStyle() throws Exception {
    setFileContent(
        ".classpath",
        getSourceDQ(
            "<classpath>",
            "  <classpathentry kind='con' path='org.eclipse.jdt.launching.JRE_CONTAINER'/>",
            "  <classpathentry kind='src' path=''/>",
            "  <classpathentry kind='output' path='bin'/>",
            "</classpath>"));
    // check locations
    List<String> locations = getSourceLocations();
    assertThat(locations).containsExactly(workspaceLocation + "/TestProject");
  }

  /**
   * Move existing {@link IProject} into "subFolder" in workspace.
   * 
   * @return the new absolute location of project.
   */
  public static String moveProjectIntoWorkspaceSubFolder() throws Exception {
    String newProjectLocation = workspaceLocation + "/subFolder/Test";
    // move project content
    FileUtils.moveDirectory(
        new File(m_project.getLocation().toPortableString()),
        new File(newProjectLocation));
    // delete old project
    m_project.delete(true, null);
    // create new project, in workspace sub-folder
    {
      IProjectDescription projectDescription = workspace.newProjectDescription("Test");
      projectDescription.setLocation(new Path(newProjectLocation));
      m_project = workspaceRoot.getProject("Test");
      m_project.create(projectDescription, null);
      m_project.open(null);
      // update Java project
      m_testProject = new TestProject(m_project);
      m_javaProject = m_testProject.getJavaProject();
    }
    return newProjectLocation;
  }

  /**
   * @return result of {@link ProjectClassLoader#addSourceLocations(Set, List, IProject)}.
   */
  private List<String> getSourceLocations() throws Exception {
    List<String> locations = Lists.newArrayList();
    ProjectClassLoader.addSourceLocations(Sets.<IProject>newHashSet(), locations, m_project);
    return locations;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // addOutputLocations()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_addOutputLocations_normalProject() throws Exception {
    List<String> locations = getOutputLocations();
    assertThat(locations).containsExactly(workspaceLocation + "/TestProject/bin");
  }

  @DisposeProjectAfter
  public void test_addOutputLocations_noSuchProject() throws Exception {
    m_project.delete(true, null);
    // check locations
    List<String> locations = getOutputLocations();
    assertThat(locations).isEmpty();
  }

  @DisposeProjectAfter
  public void test_addOutputLocations_notJavaProject() throws Exception {
    ProjectUtils.removeNature(m_project, JavaCore.NATURE_ID);
    // check locations
    List<String> locations = getOutputLocations();
    assertThat(locations).isEmpty();
  }

  @DisposeProjectAfter
  public void test_addOutputLocations_projectNotInWorkspace() throws Exception {
    String newProjectLocation = moveProjectIntoWorkspaceSubFolder();
    // check locations
    List<String> locations = getOutputLocations();
    assertThat(locations).containsExactly(newProjectLocation + "/bin");
  }

  @DisposeProjectAfter
  public void test_addOutputLocations_recursion() throws Exception {
    // create new project "myProject"
    TestProject myProject = new TestProject("myProject");
    IJavaProject myJavaProject = myProject.getJavaProject();
    // reference "myProject" from "TestProject"
    try {
      // create circular dependency
      ProjectUtils.requireProject(m_javaProject, myJavaProject);
      ProjectUtils.requireProject(myJavaProject, m_javaProject);
      // check locations
      List<String> locations = getOutputLocations();
      assertThat(locations).containsExactly(
          workspaceLocation + "/TestProject/bin",
          workspaceLocation + "/myProject/bin");
    } finally {
      myProject.dispose();
    }
  }

  /**
   * @return result of {@link ProjectClassLoader#addOutputLocations(Set, List, IProject)}.
   */
  private List<String> getOutputLocations() throws Exception {
    List<String> locations = Lists.newArrayList();
    ProjectClassLoader.addOutputLocations(Sets.<IProject>newHashSet(), locations, m_project);
    return locations;
  }
}
