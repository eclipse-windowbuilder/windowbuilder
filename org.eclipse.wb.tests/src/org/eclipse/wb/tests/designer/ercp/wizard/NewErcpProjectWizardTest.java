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
import org.eclipse.wb.internal.ercp.wizards.project.rcp.NewProjectCreationOperation;
import org.eclipse.wb.internal.ercp.wizards.project.rcp.NewProjectWizard;
import org.eclipse.wb.tests.designer.core.model.parser.AbstractJavaInfoTest;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * Test for {@link NewProjectWizard} and {@link NewProjectCreationOperation}.
 * 
 * @author scheglov_ke
 */
public class NewErcpProjectWizardTest extends AbstractJavaInfoTest {
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
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for creating empty eRCP project.
   */
  public void test_empty() throws Exception {
    String projectName = "com.project.my";
    try {
      // create eRCP project
      {
        NewProjectCreationOperation operation = new NewProjectCreationOperation(projectName, false);
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
      assertTrue(project.getFolder(new Path("src/" + projectName.replace('.', '/'))).exists());
      assertTrue(project.getFile(
          new Path("src/" + projectName.replace('.', '/') + "/Activator.java")).exists());
      assertTrue(project.getFolder("META-INF").exists());
      assertTrue(project.getFile(new Path("META-INF/MANIFEST.MF")).exists());
      assertTrue(project.getFile("plugin.xml").exists());
      // check IJavaProject
      {
        assertTrue(javaProject.findPackageFragmentRoot(new Path("/" + projectName + "/src")).exists());
        assertNotNull(javaProject.findType(projectName + ".Activator"));
        // check that eRCP types can be found, so MANIFEST.MF has required plugins
        assertNotNull(javaProject.findType("org.eclipse.swt.widgets.Button"));
        // in theory we should check also eRCP classes,
        // but this required "target platform" configuration, skip for now
        if (Math.sqrt(4.0) == 3.0) {
          assertNotNull(javaProject.findType("org.eclipse.ercp.swt.mobile.Command"));
          assertNotNull(javaProject.findType("org.eclipse.ercp.eworkbench.eWorkbench"));
        }
        // no sample types expected
        assertNull(javaProject.findType(projectName + ".views.MyViewPart"));
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
        NewProjectCreationOperation operation = new NewProjectCreationOperation(projectName, true);
        operation.run(new NullProgressMonitor());
      }
      // prepare created project
      IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
      IJavaProject javaProject = JavaCore.create(project);
      // check IJavaProject
      {
        assertNotNull(javaProject.findType(projectName + ".views.MyViewPart"));
        assertNotNull(javaProject.findType(projectName + ".preferences.PreferencePage_1"));
        assertNotNull(javaProject.findType(projectName + ".preferences.PreferencePage_2"));
      }
    } finally {
      IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
      project.delete(true, null);
    }
  }
}
