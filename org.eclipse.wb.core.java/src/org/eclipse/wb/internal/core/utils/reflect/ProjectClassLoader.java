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
package org.eclipse.wb.internal.core.utils.reflect;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.utils.pde.ReflectivePDE;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.objectweb.asm.ClassReader;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.cert.Certificate;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.UIDefaults;
import javax.swing.UIManager;

/**
 * Implementation of {@link URLClassLoader} for loading classes from {@link IJavaProject}.
 *
 * @author scheglov_ke
 * @coverage core.util
 */
public class ProjectClassLoader extends URLClassLoader {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link ProjectClassLoader} for given {@link IJavaProject}.
   */
  public static ProjectClassLoader create(ClassLoader parentClassLoader, IJavaProject javaProject)
      throws Exception {
    URL[] urls = getClasspathUrls(javaProject);
    return new ProjectClassLoader(urls, parentClassLoader, javaProject);
  }

  /**
   * @return {@link URL}s for each classpath entry of given {@link IJavaProject}.
   */
  public static URL[] getClasspathUrls(IJavaProject javaProject) throws Exception {
    List<String> entries = Lists.newArrayList();
    addRuntimeClassPathEntries(entries, javaProject, Sets.<IJavaProject>newHashSet(), true);
    return toURLs(entries);
  }

  /**
   * @return the {@link URL}s for given locations in file system.
   */
  public static URL[] toURLs(List<String> locations) throws Exception {
    URL urls[] = new URL[locations.size()];
    for (int i = 0; i < locations.size(); i++) {
      String location = locations.get(i);
      urls[i] = new File(location).toURI().toURL();
    }
    return urls;
  }

  /**
   * @return the {@link ProjectClassLoader} for given {@link URL}'s with given parent
   *         {@link ClassLoader}.
   */
  public static ProjectClassLoader create(ClassLoader parentClassLoader,
      URL[] urls,
      IJavaProject javaProject) throws Exception {
    return new ProjectClassLoader(urls, parentClassLoader, javaProject);
  }

  private static void addRuntimeClassPathEntries(List<String> entries,
      IJavaProject javaProject,
      Set<IJavaProject> visitedProjects,
      boolean fullClassPath) throws Exception {
    IProject project = javaProject.getProject();
    // not Java project
    if (!javaProject.exists()) {
      // add its location for resources
      if (project.exists()) {
        String path = project.getLocation().toPortableString();
        entries.add(path);
      }
      // done
      return;
    }
    // check for recursion
    if (visitedProjects.contains(javaProject)) {
      return;
    }
    visitedProjects.add(javaProject);
    // do add classpath entries
    if (fullClassPath) {
      CollectionUtils.addAll(entries, getClasspath(javaProject));
    } else {
      IPath outputLocation = javaProject.getOutputLocation();
      addAbsoluteLocation(entries, outputLocation);
    }
    // include fragments
    addFragments(entries, project, visitedProjects);
  }

  private static void addFragments(List<String> entries,
      IProject project,
      Set<IJavaProject> visitedProjects) throws Exception {
    IJavaProject javaProject = JavaCore.create(project);
    if (!javaProject.exists()) {
      return;
    }
    // add fragments of this project
    {
      Object model = ReflectivePDE.findModel(project);
      if (model != null) {
        org.eclipse.osgi.service.resolver.BundleDescription modelBundleDescription =
            ReflectivePDE.getPluginModelBundleDescription(model);
        if (modelBundleDescription != null) {
          org.eclipse.osgi.service.resolver.BundleDescription[] fragments =
              modelBundleDescription.getFragments();
          for (org.eclipse.osgi.service.resolver.BundleDescription fragment : fragments) {
            String fragmentProjectName = fragment.getSymbolicName();
            addFragment_runtimeClassPathEntries(entries, fragmentProjectName, visitedProjects);
          }
        }
      }
    }
    // add also fragments of required projects
    {
      String[] requiredProjectNames = javaProject.getRequiredProjectNames();
      for (String requiredProjectName : requiredProjectNames) {
        addFragment_runtimeClassPathEntries(entries, requiredProjectName, visitedProjects);
      }
    }
  }

  private static void addFragment_runtimeClassPathEntries(List<String> entries,
      String projectName,
      Set<IJavaProject> visitedProjects) throws Exception {
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    IProject project = root.getProject(projectName);
    IJavaProject javaProject = JavaCore.create(project);
    addRuntimeClassPathEntries(entries, javaProject, visitedProjects, false);
  }

  /**
   * @return the locations of classpath entries of given {@link IJavaProject}. It does not support
   *         advanced features of JDT/PDE such as "Import-Package" statement, and adding fragments.
   *         However if does <em>not</em> fail when there is reference on not existing required
   *         {@link IProject}. We need this to play safe in GWT.
   */
  public static String[] getClasspath(IJavaProject javaProject) throws Exception {
    List<String> locations = Lists.newArrayList();
    // prepare unresolved class path
    IRuntimeClasspathEntry[] unresolvedEntries =
        JavaRuntime.computeUnresolvedRuntimeClasspath(javaProject);
    // resolve each entry
    for (int unresolvedIndex = 0; unresolvedIndex < unresolvedEntries.length; unresolvedIndex++) {
      IRuntimeClasspathEntry enresolvedEntry = unresolvedEntries[unresolvedIndex];
      IRuntimeClasspathEntry[] resolvedEntries =
          JavaRuntime.resolveRuntimeClasspathEntry(enresolvedEntry, javaProject);
      for (int resolvedIndex = 0; resolvedIndex < resolvedEntries.length; resolvedIndex++) {
        IRuntimeClasspathEntry resolvedEntry = resolvedEntries[resolvedIndex];
        String location = resolvedEntry.getLocation();
        if (location != null) {
          location = location.replace('\\', '/');
          locations.add(location);
        }
      }
    }
    // convert into array
    return locations.toArray(new String[locations.size()]);
  }

  /**
   * Adds absolute locations (in file system) of output folders for given and required projects.
   */
  public static void addOutputLocations(Set<IProject> visitedProjects,
      List<String> locations,
      IProject project) throws Exception {
    // may be not exists
    if (!project.exists()) {
      return;
    }
    // check for recursion
    if (visitedProjects.contains(project)) {
      return;
    }
    visitedProjects.add(project);
    // add output folders for IJavaProject
    {
      IJavaProject javaProject = JavaCore.create(project);
      if (javaProject.exists()) {
        // default output location
        {
          IPath outputPath = javaProject.getOutputLocation();
          addAbsoluteLocation(locations, outputPath);
        }
        // source folder specific output locations
        for (IClasspathEntry entry : javaProject.getResolvedClasspath(true)) {
          if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
            IPath outputPath = entry.getOutputLocation();
            addAbsoluteLocation(locations, outputPath);
          }
        }
      }
    }
    // process required projects
    IProject[] referencedProjects = project.getReferencedProjects();
    for (IProject referencedProject : referencedProjects) {
      addOutputLocations(visitedProjects, locations, referencedProject);
    }
  }

  /**
   * Adds absolute locations (in file system) of source folders for given and required projects.
   */
  public static void addSourceLocations(Set<IProject> visitedProjects,
      List<String> locations,
      IProject project) throws Exception {
    // may be not exists
    if (!project.exists()) {
      return;
    }
    // check for recursion
    if (visitedProjects.contains(project)) {
      return;
    }
    visitedProjects.add(project);
    // add source folders for IJavaProject
    {
      IJavaProject javaProject = JavaCore.create(project);
      if (javaProject.exists()) {
        for (IClasspathEntry entry : javaProject.getResolvedClasspath(true)) {
          if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
            IPath entryPath = entry.getPath();
            addAbsoluteLocation(locations, entryPath);
          }
        }
      }
    }
    // process required projects
    IProject[] referencedProjects = project.getReferencedProjects();
    for (IProject referencedProject : referencedProjects) {
      addSourceLocations(visitedProjects, locations, referencedProject);
    }
  }

  /**
   * Resolves given {@link IPath} to absolute location in file system and adds to the {@link List}.
   */
  private static void addAbsoluteLocation(List<String> locations, IPath outputPath) {
    if (outputPath != null) {
      IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
      // prepare location
      IPath entryLocation;
      if (outputPath.segmentCount() == 1) {
        String projectName = outputPath.lastSegment();
        IProject project = workspaceRoot.getProject(projectName);
        entryLocation = project.getLocation();
      } else {
        IFolder entryFolder = workspaceRoot.getFolder(outputPath);
        entryLocation = entryFolder.getLocation();
      }
      // add location
      locations.add(entryLocation.toPortableString());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clean up
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Remove any static references for JIDE, see (Case 4713).
   */
  private static void cleanUpJIDE() {
    UIDefaults defaults = UIManager.getDefaults();
    for (Iterator<?> I = defaults.keySet().iterator(); I.hasNext();) {
      Object key = I.next();
      if (key.toString().toLowerCase().indexOf("jide") != -1) {
        I.remove();
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link List} of additional {@link IByteCodeProcessor}'s that should be applied to each loaded
   * class.
   */
  private final List<IByteCodeProcessor> m_processors = Lists.newArrayList();
  /**
   * {@link Set} of classes names that should be made non-abstract.
   */
  private final Set<String> m_nonAbstractClasses = Sets.newTreeSet();
  private final IJavaProject m_javaProject;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ProjectClassLoader(URL[] urls, ClassLoader parent, IJavaProject javaProject) {
    super(urls, parent);
    m_javaProject = javaProject;
    cleanUpJIDE();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds given {@link IByteCodeProcessor}.
   */
  public void add(IByteCodeProcessor processor) {
    m_processors.add(processor);
    processor.initialize(this);
  }

  /**
   * Adds the name of class that should be made non-abstract.
   */
  public void addNonAbstractClass(String className) {
    m_nonAbstractClasses.add(className);
  }

  @Override
  public void addURL(URL url) {
    // make addURL() method public
    super.addURL(url);
  }

  public IJavaProject getJavaProject() {
    return m_javaProject;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Loading
  //
  ////////////////////////////////////////////////////////////////////////////
  private static CodeSource m_fakeCodeSource;

  @Override
  protected Class<?> findClass(String className) throws ClassNotFoundException {
    String classResourceName = className.replace('.', '/') + ".class";
    InputStream input = getResourceAsStream(classResourceName);
    if (input == null) {
      throw new ClassNotFoundException(className);
    } else {
      try {
        // read class bytes
        byte[] bytes = IOUtils2.readBytes(input);
        // apply processors
        for (IByteCodeProcessor processor : m_processors) {
          bytes = processor.process(className, bytes);
        }
        // implement abstract methods (only for required classes)
        if (m_nonAbstractClasses.contains(className)) {
          ClassReader classReader = new ClassReader(bytes);
          AbstractMethodsImplementorVisitor rewriter =
              new AbstractMethodsImplementorVisitor(className);
          classReader.accept(rewriter, 0);
          bytes = rewriter.toByteArray();
        }
        // define package
        {
          String pkgName = StringUtils.substringBeforeLast(className, ".");
          if (getPackage(pkgName) == null) {
            definePackage(pkgName, null, null, null, null, null, null, null);
          }
        }
        // return (possibly modified) class
        ensureCodeSource();
        return defineClass(className, bytes, 0, bytes.length, m_fakeCodeSource);
      } catch (Throwable e) {
        throw new ClassNotFoundException("Error loading class " + className, e);
      }
    }
  }

  private static void ensureCodeSource() {
    if (m_fakeCodeSource == null) {
      try {
        m_fakeCodeSource = new CodeSource(new URL("file:/"), (Certificate[]) null);
      } catch (Throwable e) {
      }
    }
  }
}
