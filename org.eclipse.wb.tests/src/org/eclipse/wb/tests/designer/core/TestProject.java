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
package org.eclipse.wb.tests.designer.core;

import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.ClasspathUtilCore;
import org.eclipse.pde.internal.core.PDECore;

import org.apache.commons.lang.ArrayUtils;
import org.osgi.framework.Bundle;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * @author Erich Gamma
 * @author scheglov_ke
 */
public class TestProject {
  private final IProject m_project;
  private final IJavaProject m_javaProject;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TestProject() throws CoreException {
    this("TestProject");
  }

  public TestProject(String projectName) throws CoreException {
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    m_project = root.getProject(projectName);
    // delete project
    if (m_project.exists()) {
      m_project.delete(true, true, null);
    }
    // create project
    {
      m_project.create(null);
      m_project.open(null);
      m_javaProject = JavaCore.create(m_project);
    }
    //
    IFolder binFolder = createBinFolder();
    setJavaNature();
    m_javaProject.setRawClasspath(new IClasspathEntry[0], null);
    addSystemLibraries();
    //
    createOutputFolder(binFolder);
    createSourceFolder();
  }

  public TestProject(IProject project) {
    m_project = project;
    m_javaProject = JavaCore.create(m_project);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Project creation utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private IPackageFragmentRoot createSourceFolder() throws CoreException {
    IFolder folder = m_project.getFolder("src");
    folder.create(false, true, null);
    IPackageFragmentRoot root = m_javaProject.getPackageFragmentRoot(folder);
    //
    IClasspathEntry[] oldEntries = m_javaProject.getRawClasspath();
    IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
    System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
    newEntries[oldEntries.length] = JavaCore.newSourceEntry(root.getPath());
    m_javaProject.setRawClasspath(newEntries, null);
    return root;
  }

  private IFolder createBinFolder() throws CoreException {
    IFolder binFolder = m_project.getFolder("bin");
    binFolder.create(false, true, null);
    return binFolder;
  }

  private void setJavaNature() throws CoreException {
    IProjectDescription description = m_project.getDescription();
    description.setNatureIds(new String[]{JavaCore.NATURE_ID});
    m_project.setDescription(description, null);
  }

  private void createOutputFolder(IFolder binFolder) throws JavaModelException {
    IPath outputLocation = binFolder.getFullPath();
    m_javaProject.setOutputLocation(outputLocation, null);
  }

  private void addSystemLibraries() throws JavaModelException {
    addClassPathEntry(JavaRuntime.getDefaultJREContainerEntry());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Class path utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds "jar" with given location.
   */
  public void addExternalJar(File jar) throws JavaModelException {
    String location = jar.getAbsolutePath();
    addExternalJar(location);
  }

  /**
   * Adds "jar" with given location.
   */
  public void addExternalJar(String location) throws JavaModelException {
    addClassPathEntry(JavaCore.newLibraryEntry(new Path(location), null, null));
  }

  /**
   * Adds "jar" files in given folder.
   */
  public void addExternalJars(String folderLocation) throws JavaModelException {
    File folder = new File(folderLocation);
    Assert.isTrue(folder.exists(), "Does not exist: " + folderLocation);
    Assert.isTrue(folder.isDirectory(), "Is not directory: " + folderLocation);
    for (File file : folder.listFiles()) {
      String absolutePath = file.getAbsolutePath();
      if (absolutePath.endsWith(".jar")) {
        addExternalJar(absolutePath);
      }
    }
  }

  /**
   * Adds {@link IClasspathEntry}'s for plugin with given id.
   */
  public void addPlugin(String pluginId) throws CoreException {
    // prepare entries for plugin
    ArrayList<IClasspathEntry> entries = new ArrayList<>();
    {
      IPluginModelBase model = PDECore.getDefault().getModelManager().findModel(pluginId);
      ClasspathUtilCore.addLibraries(model, entries);
    }
    // add entries
    for (IClasspathEntry entry : entries) {
      addClassPathEntry(entry);
    }
  }

  public void addBundleJars(String bundleId) throws Exception {
    addBundleJars(bundleId, "/");
  }

  /**
   * @param path
   *          the path name in which to look. The path is always relative to the root of this bundle
   *          and may begin with "/". A path value of "/" indicates the root of this bundle.
   */
  public void addBundleJars(String bundleId, String path) throws Exception {
    Bundle bundle = Platform.getBundle(bundleId);
    Enumeration<URL> entries = bundle.findEntries(path, "*.jar", false);
    while (entries.hasMoreElements()) {
      URL jarURL = entries.nextElement();
      String jarPath = FileLocator.toFileURL(jarURL).getPath();
      addExternalJar(jarPath);
    }
  }

  /**
   * Adds new source folder.
   */
  public void addSourceFolder(String pathLocation) throws JavaModelException {
    IPath path = new Path(pathLocation);
    IClasspathEntry entry = JavaCore.newSourceEntry(path);
    addClassPathEntry(entry);
  }

  /**
   * Removes source folder.
   */
  public void removeSourceFolder(String pathLocation) throws JavaModelException {
    IClasspathEntry[] oldEntries = m_javaProject.getRawClasspath();
    for (int i = 0; i < oldEntries.length; i++) {
      IClasspathEntry entry = oldEntries[i];
      if (entry.getPath().toPortableString().equals(pathLocation)) {
        IClasspathEntry[] newEntries = (IClasspathEntry[]) ArrayUtils.remove(oldEntries, i);
        m_javaProject.setRawClasspath(newEntries, null);
      }
    }
  }

  /**
   * Adds new required/referenced project.
   */
  public void addRequiredProject(TestProject requiredProject) throws CoreException {
    addRequiredProject(requiredProject.getProject());
  }

  /**
   * Adds new required/referenced project.
   */
  public void addRequiredProject(IProject requiredProject) throws CoreException {
    IClasspathEntry projectEntry = JavaCore.newProjectEntry(requiredProject.getFullPath());
    addClassPathEntry(projectEntry);
  }

  private void addClassPathEntry(IClasspathEntry newEntry) throws JavaModelException {
    IClasspathEntry[] oldEntries = m_javaProject.getRawClasspath();
    // check, may be there is already same entry
    for (int i = 0; i < oldEntries.length; i++) {
      IClasspathEntry entry = oldEntries[i];
      if (entry.getPath().equals(newEntry.getPath())) {
        return;
      }
    }
    // do add
    IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
    System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
    newEntries[oldEntries.length] = newEntry;
    m_javaProject.setRawClasspath(newEntries, null);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public IProject getProject() {
    return m_project;
  }

  public IJavaProject getJavaProject() {
    return m_javaProject;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "destructor"
  //
  ////////////////////////////////////////////////////////////////////////////
  public void dispose() throws CoreException {
    clearReadOnlyFlag();
    m_project.delete(true, true, null);
    // may be fix for https://bugs.eclipse.org/bugs/show_bug.cgi?id=245008
    // leak of ProjectDescriptionReader
    try {
      Class<?> readerClass = getClass().getClassLoader().loadClass(
          "org.eclipse.core.internal.resources.ProjectDescriptionReader");
      ReflectionUtils.setField(readerClass, "singletonParser", null);
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  public IPackageFragmentRoot getSourceFolder() throws CoreException {
    IPackageFragmentRoot[] packageRoots = m_javaProject.getPackageFragmentRoots();
    for (IPackageFragmentRoot packageRoot : packageRoots) {
      if (packageRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
        return packageRoot;
      }
    }
    throw new IllegalStateException("Can not find source folder.");
  }

  /**
   * @return the {@link IPackageFragment} with given name, in default source folder. Creates it if
   *         does not exist.
   */
  public IPackageFragment getPackage(String name) throws CoreException {
    IPackageFragmentRoot sourceFolder = getSourceFolder();
    IPackageFragment packageFragment = sourceFolder.getPackageFragment(name);
    if (packageFragment == null || !packageFragment.exists()) {
      packageFragment = sourceFolder.createPackageFragment(name, false, null);
    }
    return packageFragment;
  }

  public ICompilationUnit createUnit(IPackageFragment pack, String cuName, String source)
      throws JavaModelException {
    return pack.createCompilationUnit(cuName, source, false, null);
  }

  /**
   * Creates {@link IFile} (non Java resource) in given package.
   */
  public IFile createFile(IPackageFragment pack, String fileName, String contents)
      throws Exception {
    IFolder folder = (IFolder) pack.getUnderlyingResource();
    IFile file = folder.getFile(fileName);
    file.create(new ByteArrayInputStream(contents.getBytes()), true, null);
    return file;
  }

  /**
   * @return the {@link ICompilationUnit} for {@link IType} with given name.
   */
  public ICompilationUnit getCompilationUnit(String typeName) throws JavaModelException {
    return getJavaProject().findType(typeName).getCompilationUnit();
  }

  private static long m_totalWaitForAutoBuild;

  /**
   * Wait for auto-build notification to occur, that is for the auto-build to finish.
   */
  public static void waitForAutoBuild() {
    long start = System.currentTimeMillis();
    while (true) {
      try {
        IJobManager jobManager = Job.getJobManager();
        jobManager.wakeUp(ResourcesPlugin.FAMILY_AUTO_BUILD);
        jobManager.wakeUp(ResourcesPlugin.FAMILY_AUTO_BUILD);
        jobManager.wakeUp(ResourcesPlugin.FAMILY_AUTO_BUILD);
        jobManager.join(ResourcesPlugin.FAMILY_AUTO_BUILD, new MyNullProgressMonitor());
        break;
      } catch (Throwable e) {
      }
    }
    {
      long delta = System.currentTimeMillis() - start;
      m_totalWaitForAutoBuild += delta;
      //System.out.println("waitForAutoBuild: " + delta + "  " + m_totalWaitForAutoBuild);
    }
  }

  /**
   * Read only flag can prevent file removing, so clear all of them.
   */
  private void clearReadOnlyFlag() throws CoreException {
    if (m_project.isOpen()) {
      m_project.accept(new IResourceVisitor() {
        @Override
        public boolean visit(IResource resource) throws CoreException {
          if (resource instanceof IFile) {
            IFile file = (IFile) resource;
            ResourceAttributes resourceAttributes = file.getResourceAttributes();
            if (resourceAttributes != null) {
              resourceAttributes.setReadOnly(false);
              file.setResourceAttributes(resourceAttributes);
            }
          }
          return true;
        }
      });
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IProgressMonitor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * No-op implementation of {@link IProgressMonitor}.
   */
  private static final class MyNullProgressMonitor implements IProgressMonitor {
    public void beginTask(String name, int totalWork) {
    }

    public void done() {
    }

    public void internalWorked(double work) {
    }

    public boolean isCanceled() {
      return false;
    }

    public void setCanceled(boolean value) {
    }

    public void setTaskName(String name) {
    }

    public void subTask(String name) {
    }

    public void worked(int work) {
    }
  }
}
