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
package org.eclipse.wb.internal.core.utils.jdt.core;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.eclipse.wb.internal.core.BundleResourceProvider;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.NoOpProgressMonitor;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.core.utils.pde.ReflectivePDE;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.swt.SWT;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.osgi.framework.Bundle;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The class implements utility methods that operate on {@link IProject} and {@link IJavaProject}.
 *
 * @author scheglov_ke
 * @author lobas_av
 * @coverage core.util.jdt
 */
public final class ProjectUtils {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private ProjectUtils() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the Java version compliance level.
   */
  public static float getJavaVersion(IJavaProject javaProject) {
    String complianceString = getJavaVersionString(javaProject);
    return Float.parseFloat(complianceString);
  }

  /**
   * @return the {@link String} name of Java version.
   */
  public static String getJavaVersionString(IJavaProject javaProject) {
    return javaProject.getOption(JavaCore.COMPILER_COMPLIANCE, true);
  }

  /**
   * @return <code>true</code> if the project has JDK 1.5 compiler compliance turned on.
   */
  public static boolean isJDK15(IJavaProject project) {
    if (project != null) {
      String complianceString = getJavaVersionString(project);
      if (complianceString != null) {
        float compliance = Float.parseFloat(complianceString);
        return compliance >= 1.5;
      }
    }
    return false;
  }

  /**
   * @return the {@link IJavaProject} options {@link Map}.
   */
  @SuppressWarnings("unchecked")
  public static Map<String, String> getOptions(IJavaProject project) {
    return project.getOptions(true);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Building
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Wait for auto-build notification to occur, that is for the auto-build to finish.
   */
  public static void waitForAutoBuild() {
    while (true) {
      boolean success = ExecutionUtils.runIgnore(new RunnableEx() {
        public void run() throws Exception {
          IJobManager jobManager = Job.getJobManager();
          jobManager.wakeUp(ResourcesPlugin.FAMILY_AUTO_BUILD);
          jobManager.wakeUp(ResourcesPlugin.FAMILY_AUTO_BUILD);
          jobManager.wakeUp(ResourcesPlugin.FAMILY_AUTO_BUILD);
          jobManager.join(ResourcesPlugin.FAMILY_AUTO_BUILD, new NoOpProgressMonitor());
        }
      });
      if (success) {
        break;
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Resource
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Ensures that {@link IJavaProject} has type with given name.
   *
   * @param javaProject
   *          the target {@link IJavaProject} to add resource type to.
   * @param bundle
   *          then bundle {@link Bundle} to access to original resource.
   * @param typeName
   *          the name of required type.
   */
  public static void ensureResourceType(IJavaProject javaProject, Bundle bundle, String typeName)
      throws Exception {
    // prepare required contents
    String contents;
    {
      String versionString = isJDK15(javaProject) ? "1.5" : "1.4";
      String path = "/resources/" + versionString + "/" + typeName.replace('.', '/') + ".java";
      contents = BundleResourceProvider.get(bundle).getFileString(path);
    }
    // prepare type
    IType type = javaProject.findType(typeName);
    // if no type, create in given project
    if (type == null) {
      createCompilationUnitWithType(javaProject, typeName, contents);
      return;
    }
    // has type, check unit
    {
      ICompilationUnit compilationUnit = type.getCompilationUnit();
      // binary type, can not update
      if (compilationUnit == null) {
        return;
      }
      // update, if up to date
      if (!compilationUnit.getSource().equals(contents)) {
        updateCompilationUnitWithType(compilationUnit, contents);
      }
    }
  }

  private static void createCompilationUnitWithType(IJavaProject javaProject,
      String typeName,
      String contents) throws Exception {
    ICompilationUnit unit;
    {
      String packageName = CodeUtils.getPackage(typeName);
      String unitName = CodeUtils.getShortClass(typeName) + ".java";
      IContainer sourceContainer = CodeUtils.getSourceContainers(javaProject, false).get(0);
      IPackageFragmentRoot packageFragmentRoot =
          javaProject.getPackageFragmentRoot(sourceContainer);
      IPackageFragment packageFragment =
          packageFragmentRoot.createPackageFragment(packageName, true, null);
      unit = packageFragment.createCompilationUnit(unitName, contents, true, null);
    }
    saveCompilationUnitWithType(unit);
  }

  private static void updateCompilationUnitWithType(ICompilationUnit unit, String contents)
      throws Exception {
    // ignore if read-only
    if (unit.getResource().getResourceAttributes().isReadOnly()) {
      return;
    }
    // do update
    unit.getBuffer().setContents(contents);
    saveCompilationUnitWithType(unit);
  }

  private static void saveCompilationUnitWithType(ICompilationUnit unit) throws Exception {
    // save, required if type already exists and open in editor
    unit.getBuffer().save(null, true);
    // wait for auto-build, added type should be compiled
    waitForAutoBuild();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Classpath
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if {@link IJavaProject} has type with given name.
   */
  public static boolean hasType(final IJavaProject project, final String className) {
    return ExecutionUtils.runObjectIgnore(new RunnableObjectEx<Boolean>() {
      public Boolean runObject() throws Exception {
        return project.findType(className) != null;
      }
    }, false);
  }

  /**
   * Adds JAR and (optional) attached source to the {@link IJavaProject}.
   *
   * @param javaProject
   *          the {@link IJavaProject} to add JAR to.
   * @param bundle
   *          the {@link Bundle} to read JAR/source from.
   * @param jarName
   *          the path of JAR in {@link Bundle}.
   * @param srcName
   *          the path of source ZIP in {@link Bundle}, optional, may be <code>null</code>.
   */
  public static void addJar(IJavaProject javaProject, Bundle bundle, String jarPath, String srcPath)
      throws Exception {
    String jarName = copyFile(javaProject, bundle, jarPath);
    String srcName = copyFile(javaProject, bundle, srcPath);
    // OK, now JAR/source are in project, add classpath entry
    addClasspathEntry(javaProject, jarName, srcName);
  }

  /**
   * Copies file from {@link Bundle} into {@link IProject}.
   */
  public static String copyFile(IJavaProject javaProject, Bundle bundle, String path)
      throws Exception {
    if (path == null) {
      return null;
    }
    String name = FilenameUtils.getName(path);
    IFile file = javaProject.getProject().getFile(name);
    InputStream stream = bundle.getEntry(path).openStream();
    IOUtils2.setFileContents(file, stream);
    return name;
  }

  /**
   * Adds JAR and (optional) attached source to the {@link IJavaProject} using JAR file anywhere at
   * file system. The JAR file would be copied to project's folder if needed.
   *
   * @param javaProject
   *          the {@link IJavaProject} to add JAR to.
   * @param jarPathString
   *          the full absolute path to JAR file.
   * @param srcPathString
   *          the full absolute path to ZIP file of sources. May be <code>null</code>.
   */
  public static void addJar(IJavaProject javaProject, String jarPathString, String srcPathString)
      throws Exception {
    IProject project = javaProject.getProject();
    String jarFileName = FilenameUtils.getName(jarPathString);
    String srcFileName = FilenameUtils.getName(srcPathString);
    // copy JAR
    {
      IFile jarFile = project.getFile(jarFileName);
      if (!jarFile.exists()) {
        InputStream jarStream = new FileInputStream(jarPathString);
        IOUtils2.setFileContents(jarFile, jarStream);
      }
    }
    // copy source
    if (srcPathString != null) {
      IFile srcFile = project.getFile(srcFileName);
      if (!srcFile.exists()) {
        InputStream srcStream = new FileInputStream(srcPathString);
        IOUtils2.setFileContents(srcFile, srcStream);
      }
    }
    addClasspathEntry(javaProject, jarFileName, srcFileName);
  }

  /**
   * Adds given external JAR into classpath. Does not copy it into {@link IProject}.
   *
   * @param javaProject
   *          the {@link IJavaProject} to add JAR to.
   * @param jarPathString
   *          the full absolute path to JAR file.
   * @param srcPathString
   *          the full absolute path to ZIP file of sources. May be <code>null</code>.
   */
  public static void addExternalJar(IJavaProject javaProject,
      String jarPathString,
      String srcPathString) throws Exception {
    IPath jarPath = new Path(jarPathString);
    IPath srcPath = srcPathString != null ? new Path(srcPathString) : null;
    IClasspathEntry newEntry = JavaCore.newLibraryEntry(jarPath, srcPath, null);
    addClasspathEntry(javaProject, newEntry);
  }

  /**
   * Adds {@link IClasspathEntry} to the {@link IJavaProject}.
   *
   * @param javaProject
   *          the {@link IJavaProject} to add JAR to.
   * @param entry
   *          the {@link IClasspathEntry} to add.
   */
  public static void addClasspathEntry(IJavaProject javaProject, IClasspathEntry entry)
      throws CoreException {
    IClasspathEntry[] entries = javaProject.getRawClasspath();
    entries = (IClasspathEntry[]) ArrayUtils.add(entries, entry);
    javaProject.setRawClasspath(entries, null);
  }

  /**
   * Adds {@link IClasspathEntry} to the {@link IJavaProject}.
   *
   * @param javaProject
   *          the {@link IJavaProject} to add JAR to.
   * @param jarPathString
   *          the JAR file name relative to project.
   * @param srcPathString
   *          the path to ZIP file of sources, relative to project. May be <code>null</code>.
   */
  public static void addClasspathEntry(IJavaProject javaProject,
      String jarPathString,
      String srcPathString) throws Exception {
    // prepare entry
    IClasspathEntry entry;
    {
      IProject project = javaProject.getProject();
      IPath jarPath = project.getFullPath().append(jarPathString);
      IPath srcPath = srcPathString != null ? project.getFullPath().append(srcPathString) : null;
      entry = JavaCore.newLibraryEntry(jarPath, srcPath, null);
    }
    // add entry
    addClasspathEntry(javaProject, entry);
    // notify listeners
    {
      List<IProjectClasspathListener> listeners =
          ExternalFactoriesHelper.getElementsInstances(
              IProjectClasspathListener.class,
              "org.eclipse.wb.core.projectClasspathListeners",
              "listener");
      for (IProjectClasspathListener listener : listeners) {
        listener.addClasspathEntry(javaProject, jarPathString, srcPathString);
      }
    }
  }

  /**
   * Add libraries of given plugin to the classpath of {@link IJavaProject}.
   *
   * @param javaProject
   *          the {@link IJavaProject} to add libraries to.
   * @param pluginId
   *          the plugin id.
   */
  public static void addPluginLibraries(IJavaProject javaProject, String pluginId) throws Exception {
    List<IClasspathEntry> entries = Lists.newArrayList();
    // add existing entries
    CollectionUtils.addAll(entries, javaProject.getRawClasspath());
    // add plugin entries
    ReflectivePDE.addPluginLibraries(pluginId, entries);
    // set new entries
    setRawClasspath(javaProject, entries);
  }

  /**
   * Adds SWT plugin library to the classpath of {@link IJavaProject}.
   */
  public static void addSWTLibrary(IJavaProject javaProject) throws Exception {
    String pluginId = "org.eclipse.swt." + SWT.getPlatform() + "." + Platform.getOS();
    boolean isMacCocoa64 = EnvironmentUtils.IS_MAC_COCOA && EnvironmentUtils.IS_64BIT_OS;
    if (isMacCocoa64 || !EnvironmentUtils.IS_MAC) {
      pluginId += "." + Platform.getOSArch();
    }
    addPluginLibraries(javaProject, pluginId);
  }

  /**
   * Requires <code>requiredProject</code> in <code>project</code>.
   */
  public static void requireProject(IJavaProject project, IJavaProject requiredProject)
      throws JavaModelException {
    IClasspathEntry[] rawClasspath = project.getRawClasspath();
    rawClasspath =
        (IClasspathEntry[]) ArrayUtils.add(
            rawClasspath,
            JavaCore.newProjectEntry(requiredProject.getPath()));
    project.setRawClasspath(rawClasspath, null);
  }

  /**
   * Removes {@link IClasspathEntry} for which {@link Predicate} returns <code>true</code>.
   */
  public static void removeClasspathEntries(IJavaProject javaProject,
      Predicate<IClasspathEntry> predicate) throws CoreException {
    List<IClasspathEntry> newEntries = Lists.newArrayList();
    IClasspathEntry[] existingEntries = javaProject.getRawClasspath();
    for (IClasspathEntry entry : existingEntries) {
      if (!predicate.apply(entry)) {
        newEntries.add(entry);
      }
    }
    setRawClasspath(javaProject, newEntries);
  }

  /**
   * Calls {@link IJavaProject#setRawClasspath(IClasspathEntry[], IProgressMonitor)}.
   */
  private static void setRawClasspath(IJavaProject javaProject, List<IClasspathEntry> entries)
      throws JavaModelException {
    javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Nature
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if {@link IProject} has nature.
   */
  public static boolean hasNature(final IProject project, final String natureId) {
    return ExecutionUtils.runObjectIgnore(new RunnableObjectEx<Boolean>() {
      public Boolean runObject() throws Exception {
        return project.hasNature(natureId);
      }
    }, false);
  }

  /**
   * Adds nature to the {@link IProject}.
   */
  public static void addNature(IProject project, String natureId) throws Exception {
    IProjectDescription description = project.getDescription();
    //
    List<String> natureIds = Lists.newArrayList();
    CollectionUtils.addAll(natureIds, description.getNatureIds());
    natureIds.add(natureId);
    description.setNatureIds(natureIds.toArray(new String[natureIds.size()]));
    //
    project.setDescription(description, null);
  }

  /**
   * Removes nature to the {@link IProject}.
   */
  public static void removeNature(IProject project, String natureId) throws Exception {
    IProjectDescription description = project.getDescription();
    //
    List<String> natureIds = Lists.newArrayList();
    CollectionUtils.addAll(natureIds, description.getNatureIds());
    natureIds.remove(natureId);
    description.setNatureIds(natureIds.toArray(new String[natureIds.size()]));
    //
    project.setDescription(description, null);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Resources
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Resolves workspace relative {@link IPath} into absolute file system {@link IPath}.
   *
   * @return the resolved {@link IPath}, may be <code>null</code> if no such resource.
   */
  public static IPath getOSPath(IPath path) {
    IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
    IResource resource = workspaceRoot.findMember(path);
    return resource != null ? resource.getLocation() : null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Files
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Finds {@link IFile}'s with given path in {@link IJavaProject} and required {@link IJavaProject}
   * 's.
   */
  public static List<IFile> findFiles(IJavaProject javaProject, String filePath) throws Exception {
    List<IFile> files = Lists.newArrayList();
    findFiles(files, javaProject, filePath, Sets.<IJavaProject>newHashSet());
    return files;
  }

  /**
   * Finds {@link IFile}'s with given path in {@link IJavaProject} and required {@link IJavaProject}
   * 's.
   */
  private static void findFiles(List<IFile> files,
      IJavaProject javaProject,
      String filePath,
      Set<IJavaProject> visitedProjects) throws Exception {
    // may be not exists
    if (!javaProject.exists()) {
      return;
    }
    // may be already visited
    if (visitedProjects.contains(javaProject)) {
      return;
    }
    visitedProjects.add(javaProject);
    // check required projects
    IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
    for (String projectName : javaProject.getRequiredProjectNames()) {
      IJavaProject requiredJavaProject = JavaCore.create(workspaceRoot.getProject(projectName));
      findFiles(files, requiredJavaProject, filePath, visitedProjects);
    }
    // check current project
    {
      IProject project = javaProject.getProject();
      IFile file = project.getFile(new Path(filePath));
      if (file.exists()) {
        files.add(file);
      }
    }
  }
}