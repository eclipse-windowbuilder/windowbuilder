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
package org.eclipse.wb.internal.ercp.wizards.project;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.ide.IDE;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.text.StrSubstitutor;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Abstract implementation of {@link WorkspaceModifyOperation} to create {@link IJavaProject}.
 * 
 * @author scheglov_ke
 * @coverage ercp.wizards
 */
public abstract class AbstractProjectCreationOperation extends WorkspaceModifyOperation {
  protected final String m_projectName;
  protected IProject project;
  protected IJavaProject javaProject;
  protected IPackageFragmentRoot m_packageFragmentRoot;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractProjectCreationOperation(String projectName) {
    m_projectName = projectName;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // WorkspaceModifyOperation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
      InterruptedException {
    createProject(monitor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Java project creation
  //
  ////////////////////////////////////////////////////////////////////////////
  private void createProject(IProgressMonitor monitor) throws CoreException {
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    project = root.getProject(m_projectName);
    // create project
    {
      project.create(monitor);
      project.open(monitor);
      javaProject = JavaCore.create(project);
    }
    // add Java nature
    addNature(JavaCore.NATURE_ID, monitor);
    // set empty class path
    javaProject.setRawClasspath(new IClasspathEntry[0], null);
    // create src/bin folders
    createSourceFolder(monitor);
    createBinFolder(monitor);
    // add Java libraries
    addClassPathEntry(JavaRuntime.getDefaultJREContainerEntry(), monitor);
  }

  private void createSourceFolder(IProgressMonitor monitor) throws CoreException {
    IFolder folder = project.getFolder("src");
    folder.create(false, true, monitor);
    m_packageFragmentRoot = javaProject.getPackageFragmentRoot(folder);
    // add "src" to class path
    addClassPathEntry(JavaCore.newSourceEntry(m_packageFragmentRoot.getPath()), monitor);
  }

  private void createBinFolder(IProgressMonitor monitor) throws CoreException {
    IFolder binFolder = project.getFolder("bin");
    binFolder.create(false, true, monitor);
    // set output
    IPath outputLocation = binFolder.getFullPath();
    javaProject.setOutputLocation(outputLocation, monitor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Project utilities
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds given {@link IClasspathEntry} to the {@link IJavaProject} class path.
   */
  protected final void addClassPathEntry(IClasspathEntry entry, IProgressMonitor monitor)
      throws JavaModelException {
    IClasspathEntry[] entries = javaProject.getRawClasspath();
    entries = (IClasspathEntry[]) ArrayUtils.add(entries, entry);
    javaProject.setRawClasspath(entries, monitor);
  }

  /**
   * Adds nature with given id to the {@link IProject}.
   */
  protected final void addNature(String natureId, IProgressMonitor monitor) throws CoreException {
    IProjectDescription description = project.getDescription();
    String[] natureIds = description.getNatureIds();
    natureIds = (String[]) ArrayUtils.add(natureIds, natureId);
    description.setNatureIds(natureIds);
    project.setDescription(description, monitor);
  }

  /**
   * Schedules opening given {@link IFile}.
   */
  protected final void scheduleOpen(final IFile file) {
    DesignerPlugin.getStandardDisplay().asyncExec(new Runnable() {
      public void run() {
        ExecutionUtils.runLog(new RunnableEx() {
          public void run() throws Exception {
            IDE.openEditor(DesignerPlugin.getActivePage(), file);
          }
        });
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Template operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates new {@link IFile} using template.
   * 
   * @param templatePath
   *          the path to the template, relative to this {@link Class}.
   * @param folderPath
   *          the path of {@link IFolder}.
   * @param fileName
   *          the name of file in {@link IFolder}.
   * 
   * @return the create {@link IFile}.
   */
  protected final IFile createTemplateFile(String templatePath,
      Map<String, String> valueMap,
      String folderPath,
      String fileName,
      IProgressMonitor monitor) throws Exception {
    IFile file;
    if (folderPath != null) {
      project.getFolder(new Path(folderPath)).create(true, true, monitor);
      file = project.getFile(new Path(folderPath + "/" + fileName));
    } else {
      file = project.getFile(new Path(fileName));
    }
    // create
    String template = IOUtils.toString(getClass().getResourceAsStream(templatePath));
    String contents = StrSubstitutor.replace(template, valueMap);
    IOUtils2.setFileContents(file, new ByteArrayInputStream(contents.getBytes()));
    //
    return file;
  }

  /**
   * Creates new {@link ICompilationUnit} using template.
   * 
   * @param templatePath
   *          the path to the template, relative to this {@link Class}.
   * @param packageName
   *          the path of {@link IFolder}.
   * @param unitName
   *          the name of file in {@link IFolder}.
   * @return the created {@link ICompilationUnit}.
   */
  protected final ICompilationUnit createTemplateUnit(String templatePath,
      Map<String, String> valueMap,
      String packageName,
      String unitName,
      IProgressMonitor monitor) throws Exception {
    String template = IOUtils.toString(getClass().getResourceAsStream(templatePath));
    String contents = StrSubstitutor.replace(template, valueMap);
    // create
    IPackageFragment packageFragment =
        m_packageFragmentRoot.createPackageFragment(packageName, true, monitor);
    return packageFragment.createCompilationUnit(unitName, contents, true, monitor);
  }
}
