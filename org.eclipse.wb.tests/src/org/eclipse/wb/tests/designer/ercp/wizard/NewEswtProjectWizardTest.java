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
package org.eclipse.wb.tests.designer.ercp.wizard;

import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.ercp.wizards.project.swt.NewProjectCreationOperation;
import org.eclipse.wb.internal.ercp.wizards.project.swt.NewProjectWizard;
import org.eclipse.wb.tests.designer.Expectations;
import org.eclipse.wb.tests.designer.Expectations.StrValue;
import org.eclipse.wb.tests.designer.core.model.parser.AbstractJavaInfoTest;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import java.io.FileNotFoundException;

/**
 * Test for {@link NewProjectWizard} and {@link NewProjectCreationOperation}.
 * 
 * @author scheglov_ke
 */
public class NewEswtProjectWizardTest extends AbstractJavaInfoTest {
  private static final String ERCP_LOCATION = Expectations.get("", new StrValue[]{
      new StrValue("scheglov-win", "C:/Work/eRCP-v20090806-2330"),
      new StrValue("sablin-aa", "C:/Work/eRCP"),
      new StrValue("flanker-windows", "C:/Work/eRCP"),});

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void configureToolkits() {
    super.configureToolkits();
    if (EnvironmentUtils.IS_WINDOWS && !EnvironmentUtils.IS_64BIT_OS) {
      configureDefaults(org.eclipse.wb.internal.ercp.ToolkitProvider.DESCRIPTION);
    }
  }

  @Override
  protected void tearDown() throws Exception {
    waitEventLoop(0);
    super.tearDown();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getAbsoluteJars()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getAbsoluteJars() throws Exception {
    // no exception expected
    ReflectionUtils.invokeMethod(
        NewProjectCreationOperation.class,
        "getAbsoluteJars(java.lang.String)",
        ERCP_LOCATION);
  }

  public void test_getAbsoluteJars_invalid_noLocation() throws Exception {
    try {
      ReflectionUtils.invokeMethod(
          NewProjectCreationOperation.class,
          "getAbsoluteJars(java.lang.String)",
          "");
    } catch (FileNotFoundException e) {
    }
  }

  public void test_getAbsoluteJars_invalid_notDirectory() throws Exception {
    try {
      ReflectionUtils.invokeMethod(
          NewProjectCreationOperation.class,
          "getAbsoluteJars(java.lang.String)",
          "c:/no-such-file.txt");
    } catch (FileNotFoundException e) {
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for creating empty eSWT project.
   */
  public void test_empty() throws Exception {
    String projectName = "com.project.my";
    try {
      // create eRCP project
      {
        NewProjectCreationOperation operation =
            new NewProjectCreationOperation(projectName, ERCP_LOCATION, false);
        operation.run(new NullProgressMonitor());
      }
      // prepare created project
      IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
      IJavaProject javaProject = JavaCore.create(project);
      // check IProject
      assertEquals(projectName, project.getName());
      assertTrue(project.exists());
      assertTrue(project.getFolder("bin").exists());
      assertTrue(project.getFolder("src").exists());
      // check IJavaProject
      {
        assertTrue(javaProject.findPackageFragmentRoot(new Path("/" + projectName + "/src")).exists());
        // check that eRCP types can be found, so needed classes are added
        assertNotNull(javaProject.findType("org.eclipse.swt.widgets.Button"));
        assertNotNull(javaProject.findType("org.eclipse.ercp.swt.mobile.Command"));
        assertNotNull(javaProject.findType("org.eclipse.jface.viewers.TableViewer"));
        // no sample types expected
        assertNull(javaProject.findType("com.test.SampleApplication"));
      }
    } finally {
      IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
      project.delete(true, null);
    }
  }

  /**
   * Test for creating sample eRCP project.
   */
  public void test_sample() throws Exception {
    String projectName = "com.project.my";
    try {
      // create eRCP project
      {
        NewProjectCreationOperation operation =
            new NewProjectCreationOperation(projectName, ERCP_LOCATION, true);
        operation.run(new NullProgressMonitor());
      }
      // prepare created project
      IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
      IJavaProject javaProject = JavaCore.create(project);
      // check IJavaProject
      {
        assertNotNull(javaProject.findType("com.test.SampleApplication"));
      }
    } finally {
      IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
      project.delete(true, null);
    }
  }
}
