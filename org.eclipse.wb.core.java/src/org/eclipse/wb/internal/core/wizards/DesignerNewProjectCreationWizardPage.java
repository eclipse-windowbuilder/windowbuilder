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
package org.eclipse.wb.internal.core.wizards;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.core.util.ISourceAttribute;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.wizards.JavaCapabilityConfigurationPage;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * As addition to the JavaCapabilityConfigurationPage, the wizard does an early project creation (so
 * that linked folders can be defined) and, if an existing external location was specified, offers
 * to do a classpath detection.
 *
 * @author lobas_av
 * @coverage core.wizards.ui
 */
public class DesignerNewProjectCreationWizardPage extends JavaCapabilityConfigurationPage {
  private final WizardNewProjectCreationPage fMainPage;
  private IPath fCurrProjectLocation;
  protected IProject fCurrProject;
  protected boolean fCanRemoveContent;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DesignerNewProjectCreationWizardPage(WizardNewProjectCreationPage mainPage) {
    super();
    fMainPage = mainPage;
    fCurrProjectLocation = null;
    fCurrProject = null;
    fCanRemoveContent = false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // NewProjectCreationWizardPage
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setVisible(boolean visible) {
    if (visible) {
      changeToNewProject();
    } else {
      removeProject();
    }
    super.setVisible(visible);
  }

  private void changeToNewProject() {
    IProject newProjectHandle = fMainPage.getProjectHandle();
    IPath newProjectLocation = fMainPage.getLocationPath();
    if (fMainPage.useDefaults()) {
      fCanRemoveContent = !newProjectLocation.append(fMainPage.getProjectName()).toFile().exists();
    } else {
      fCanRemoveContent = !newProjectLocation.toFile().exists();
    }
    final boolean initialize =
        !(newProjectHandle.equals(fCurrProject) && newProjectLocation.equals(fCurrProjectLocation));
    IRunnableWithProgress op = new IRunnableWithProgress() {
      public void run(IProgressMonitor monitor) throws InvocationTargetException,
          InterruptedException {
        try {
          updateProject(initialize, monitor);
        } catch (CoreException e) {
          throw new InvocationTargetException(e);
        }
      }
    };
    try {
      getContainer().run(false, true, op);
    } catch (InvocationTargetException e) {
      String title = Messages.DesignerNewProjectCreationWizardPage_title;
      String message = Messages.DesignerNewProjectCreationWizardPage_message;
      DesignerNewElementWizard.ExceptionHandler.perform(e, getShell(), title, message);
    } catch (InterruptedException e) {
      // cancel pressed
    }
  }

  protected void updateProject(boolean initialize, IProgressMonitor monitor) throws CoreException,
      InterruptedException {
    fCurrProject = fMainPage.getProjectHandle();
    fCurrProjectLocation = fMainPage.getLocationPath();
    boolean noProgressMonitor = !initialize && fCanRemoveContent;
    if (monitor == null || noProgressMonitor) {
      monitor = new NullProgressMonitor();
    }
    try {
      monitor.beginTask("Creating project and examining existing resources...", 2); //$NON-NLS-1$
      createProject(fCurrProject, fCurrProjectLocation, new SubProgressMonitor(monitor, 1));
      if (initialize) {
        IClasspathEntry[] entries = null;
        IPath outputLocation = null;
        if (fCurrProjectLocation.toFile().exists()
            && !Platform.getLocation().equals(fCurrProjectLocation)) {
          // detect classpath
          if (!fCurrProject.getFile(".classpath").exists()) { //$NON-NLS-1$
            // if .classpath exists noneed to look for files
            ClassPathDetector detector = new ClassPathDetector(fCurrProject);
            entries = detector.getClasspath();
            outputLocation = detector.getOutputLocation();
          }
        }
        init(JavaCore.create(fCurrProject), outputLocation, entries, false);
      }
      monitor.worked(1);
    } finally {
      monitor.done();
    }
  }

  /**
   * Called from the wizard on finish.
   */
  public void performFinish(IProgressMonitor monitor) throws CoreException, InterruptedException {
    try {
      monitor.beginTask("Creating project...", 3); //$NON-NLS-1$
      if (fCurrProject == null) {
        updateProject(true, new SubProgressMonitor(monitor, 1));
      }
      configureJavaProject(new SubProgressMonitor(monitor, 2));
    } finally {
      monitor.done();
      fCurrProject = null;
    }
  }

  private void removeProject() {
    if (fCurrProject == null || !fCurrProject.exists()) {
      return;
    }
    IRunnableWithProgress op = new IRunnableWithProgress() {
      public void run(IProgressMonitor monitor) throws InvocationTargetException,
          InterruptedException {
        boolean noProgressMonitor = Platform.getLocation().equals(fCurrProjectLocation);
        if (monitor == null || noProgressMonitor) {
          monitor = new NullProgressMonitor();
        }
        monitor.beginTask(Messages.DesignerNewProjectCreationWizardPage_removeProgress, 3);
        try {
          fCurrProject.delete(fCanRemoveContent, false, monitor);
        } catch (CoreException e) {
          throw new InvocationTargetException(e);
        } finally {
          monitor.done();
          fCurrProject = null;
          fCanRemoveContent = false;
        }
      }
    };
    try {
      getContainer().run(false, true, op);
    } catch (InvocationTargetException e) {
      String title = Messages.DesignerNewProjectCreationWizardPage_removeErrorTitle;
      String message = Messages.DesignerNewProjectCreationWizardPage_removeErrorMessage;
      DesignerNewElementWizard.ExceptionHandler.perform(e, getShell(), title, message);
    } catch (InterruptedException e) {
      // cancel pressed
    }
  }

  /**
   * Called from the wizard on cancel.
   */
  public void performCancel() {
    removeProject();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class ClassPathDetector implements IResourceProxyVisitor {
    private final Map<IPath, List<IPath>> fSourceFolders = new HashMap<IPath, List<IPath>>();
    private final List<IFile> fClassFiles = new ArrayList<IFile>();
    private final Set<IPath> fJARFiles = new HashSet<IPath>();
    private final IProject fProject;
    private IPath fResultOutputFolder;
    private IClasspathEntry[] fResultClasspath;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public ClassPathDetector(IProject project) throws CoreException {
      fProject = project;
      project.accept(this, IResource.NONE);
      fResultClasspath = null;
      fResultOutputFolder = null;
      detectClasspath();
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // ClassPathDetector
    //
    ////////////////////////////////////////////////////////////////////////////
    private boolean isNested(IPath path, Iterator<IPath> iter) {
      while (iter.hasNext()) {
        IPath other = iter.next();
        if (other.isPrefixOf(path)) {
          return true;
        }
      }
      return false;
    }

    /**
     * Method detectClasspath.
     */
    private void detectClasspath() {
      List<IClasspathEntry> cpEntries = new ArrayList<IClasspathEntry>();
      detectSourceFolders(cpEntries);
      IPath outputLocation = detectOutputFolder(cpEntries);
      detectLibraries(cpEntries, outputLocation);
      if (cpEntries.isEmpty() && fClassFiles.isEmpty()) {
        return;
      }
      IClasspathEntry[] jreEntries = PreferenceConstants.getDefaultJRELibrary();
      for (int i = 0; i < jreEntries.length; i++) {
        cpEntries.add(jreEntries[i]);
      }
      IClasspathEntry[] entries = cpEntries.toArray(new IClasspathEntry[cpEntries.size()]);
      if (!JavaConventions.validateClasspath(JavaCore.create(fProject), entries, outputLocation).isOK()) {
        return;
      }
      fResultClasspath = entries;
      fResultOutputFolder = outputLocation;
    }

    private IPath findInSourceFolders(IPath path) {
      Iterator<IPath> iter = fSourceFolders.keySet().iterator();
      while (iter.hasNext()) {
        IPath key = iter.next();
        List<IPath> cus = fSourceFolders.get(key);
        if (cus.contains(path)) {
          return key;
        }
      }
      return null;
    }

    private IPath detectOutputFolder(List<IClasspathEntry> entries) {
      Set<IPath> classFolders = new HashSet<IPath>();
      for (Iterator<IFile> iter = fClassFiles.iterator(); iter.hasNext();) {
        IFile file = iter.next();
        IPath location = file.getLocation();
        if (location == null) {
          continue;
        }
        IClassFileReader reader =
            ToolFactory.createDefaultClassFileReader(
                location.toOSString(),
                IClassFileReader.CLASSFILE_ATTRIBUTES);
        if (reader == null) {
          continue; // problematic class file
        }
        char[] className = reader.getClassName();
        ISourceAttribute sourceAttribute = reader.getSourceFileAttribute();
        if (className != null
            && sourceAttribute != null
            && sourceAttribute.getSourceFileName() != null) {
          IPath packPath = file.getParent().getFullPath();
          int idx = CharOperation.lastIndexOf('/', className) + 1;
          IPath relPath = new Path(new String(className, 0, idx));
          IPath cuPath = relPath.append(new String(sourceAttribute.getSourceFileName()));
          IPath resPath = null;
          if (idx == 0) {
            resPath = packPath;
          } else {
            IPath folderPath = getFolderPath(packPath, relPath);
            if (folderPath != null) {
              resPath = folderPath;
            }
          }
          if (resPath != null) {
            IPath path = findInSourceFolders(cuPath);
            if (path != null) {
              return resPath;
            } else {
              classFolders.add(resPath);
            }
          }
        }
      }
      IPath projPath = fProject.getFullPath();
      if (fSourceFolders.size() == 1
          && classFolders.isEmpty()
          && fSourceFolders.get(projPath) != null) {
        return projPath;
      } else {
        IPath path =
            projPath.append(PreferenceConstants.getPreferenceStore().getString(
                PreferenceConstants.SRCBIN_BINNAME));
        while (classFolders.contains(path)) {
          path = new Path(path.toString() + '1');
        }
        return path;
      }
    }

    private void detectLibraries(List<IClasspathEntry> cpEntries, IPath outputLocation) {
      Set<IPath> sourceFolderSet = fSourceFolders.keySet();
      for (Iterator<IPath> iter = fJARFiles.iterator(); iter.hasNext();) {
        IPath path = iter.next();
        if (isNested(path, sourceFolderSet.iterator())) {
          continue;
        }
        if (outputLocation != null && outputLocation.isPrefixOf(path)) {
          continue;
        }
        IClasspathEntry entry = JavaCore.newLibraryEntry(path, null, null);
        cpEntries.add(entry);
      }
    }

    private void detectSourceFolders(List<IClasspathEntry> resEntries) {
      Set<IPath> sourceFolderSet = fSourceFolders.keySet();
      for (Iterator<IPath> iter = sourceFolderSet.iterator(); iter.hasNext();) {
        IPath path = iter.next();
        List<IPath> excluded = new ArrayList<IPath>();
        for (Iterator<IPath> inner = sourceFolderSet.iterator(); inner.hasNext();) {
          IPath other = inner.next();
          if (!path.equals(other) && path.isPrefixOf(other)) {
            IPath pathToExclude =
                other.removeFirstSegments(path.segmentCount()).addTrailingSeparator();
            excluded.add(pathToExclude);
          }
        }
        IPath[] excludedPaths = excluded.toArray(new IPath[excluded.size()]);
        IClasspathEntry entry = JavaCore.newSourceEntry(path, excludedPaths);
        resEntries.add(entry);
      }
    }

    private void visitCompilationUnit(IFile file) throws JavaModelException {
      ICompilationUnit cu = JavaCore.createCompilationUnitFrom(file);
      if (cu != null) {
        ICompilationUnit workingCopy = null;
        try {
          workingCopy = (ICompilationUnit) cu.getWorkingCopy();
          synchronized (workingCopy) {
            workingCopy.reconcile();
          }
          IPath packPath = file.getParent().getFullPath();
          IPackageDeclaration[] decls = workingCopy.getPackageDeclarations();
          String cuName = file.getName();
          if (decls.length == 0) {
            addToMap(fSourceFolders, packPath, new Path(cuName));
          } else {
            IPath relpath = new Path(decls[0].getElementName().replace('.', '/'));
            IPath folderPath = getFolderPath(packPath, relpath);
            if (folderPath != null) {
              addToMap(fSourceFolders, folderPath, relpath.append(cuName));
            }
          }
        } finally {
          if (workingCopy != null) {
            workingCopy.destroy();
          }
        }
      }
    }

    private void addToMap(Map<IPath, List<IPath>> map, IPath folderPath, IPath relPath) {
      List<IPath> list = map.get(folderPath);
      if (list == null) {
        list = new ArrayList<IPath>(50);
        map.put(folderPath, list);
      }
      list.add(relPath);
    }

    private IPath getFolderPath(IPath packPath, IPath relpath) {
      int remainingSegments = packPath.segmentCount() - relpath.segmentCount();
      if (remainingSegments >= 0) {
        IPath common = packPath.removeFirstSegments(remainingSegments);
        if (common.equals(relpath)) {
          return packPath.uptoSegment(remainingSegments);
        }
      }
      return null;
    }

    private boolean hasExtension(String name, String ext) {
      return name.endsWith(ext) && ext.length() != name.length();
    }

    private boolean isValidCUName(String name) {
      return !JavaConventions.validateCompilationUnitName(name).matches(IStatus.ERROR);
    }

    public boolean visit(IResourceProxy proxy) throws CoreException {
      if (proxy.getType() == IResource.FILE) {
        String name = proxy.getName();
        if (hasExtension(name, ".java") && isValidCUName(name)) { //$NON-NLS-1$
          visitCompilationUnit((IFile) proxy.requestResource());
        } else if (hasExtension(name, ".class")) { //$NON-NLS-1$
          fClassFiles.add((IFile) proxy.requestResource());
        } else if (hasExtension(name, ".jar")) { //$NON-NLS-1$
          fJARFiles.add(proxy.requestFullPath());
        }
        return false;
      }
      return true;
    }

    public IPath getOutputLocation() {
      return fResultOutputFolder;
    }

    public IClasspathEntry[] getClasspath() {
      return fResultClasspath;
    }
  }
}