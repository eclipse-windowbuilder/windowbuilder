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
package org.eclipse.wb.internal.core.utils.pde;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.pde.core.plugin.IFragmentModel;
import org.eclipse.pde.core.plugin.IPluginLibrary;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.core.plugin.TargetPlatform;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Public copy for internal PDE ClasspathUtilCore.
 * 
 * @author lobas_av
 * @coverage core.util.pde
 */
public class ClasspathUtilCore {
  ////////////////////////////////////////////////////////////////////////////
  //
  // 
  //
  ////////////////////////////////////////////////////////////////////////////
  public static void addLibraries(IPluginModelBase model, List<IClasspathEntry> result)
      throws CoreException {
    if (new File(model.getInstallLocation()).isFile()) {
      addJARdPlugin(model, result);
    } else {
      IPluginLibrary[] libraries = model.getPluginBase().getLibraries();
      for (int i = 0; i < libraries.length; i++) {
        if (IPluginLibrary.RESOURCE.equals(libraries[i].getType())) {
          continue;
        }
        IClasspathEntry entry = createLibraryEntry(libraries[i]);
        if (entry != null && !result.contains(entry)) {
          result.add(entry);
        }
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // 
  //
  ////////////////////////////////////////////////////////////////////////////
  private static void addJARdPlugin(IPluginModelBase model, List<IClasspathEntry> result)
      throws CoreException {
    IPath sourcePath = getSourceAnnotation(model, ".");
    if (sourcePath == null) {
      sourcePath = new Path(model.getInstallLocation());
    }
    IClasspathEntry entry =
        JavaCore.newLibraryEntry(new Path(model.getInstallLocation()), sourcePath, null, false);
    if (entry != null && !result.contains(entry)) {
      result.add(entry);
    }
  }

  private static IClasspathEntry createLibraryEntry(IPluginLibrary library) {
    IClasspathEntry entry = null;
    try {
      String name = library.getName();
      String expandedName = expandLibraryName(name);
      IPluginModelBase model = library.getPluginModel();
      IPath path = getPath(model, expandedName);
      if (path == null) {
        if (model.isFragmentModel() || !containsVariables(name)) {
          return null;
        }
        model = resolveLibraryInFragments(library, expandedName);
        if (model == null) {
          return null;
        }
        path = getPath(model, expandedName);
      }
      // classpath must not contain entries referencing external folders
      if (model.getUnderlyingResource() == null && path.toFile().isDirectory()) {
        return null;
      }
      entry = JavaCore.newLibraryEntry(path, getSourceAnnotation(model, expandedName), null, false);
    } catch (CoreException e) {
    }
    return entry;
  }

  private static boolean containsVariables(String name) {
    return name.indexOf("$os$") != -1
        || name.indexOf("$ws$") != -1
        || name.indexOf("$nl$") != -1
        || name.indexOf("$arch$") != -1;
  }

  private static String expandLibraryName(String source) {
    if (source == null || source.length() == 0) {
      return "";
    }
    if (source.indexOf("$ws$") != -1) {
      source = source.replaceAll("\\$ws\\$", "ws" + IPath.SEPARATOR + TargetPlatform.getWS());
    }
    if (source.indexOf("$os$") != -1) {
      source = source.replaceAll("\\$os\\$", "os" + IPath.SEPARATOR + TargetPlatform.getOS());
    }
    if (source.indexOf("$nl$") != -1) {
      source = source.replaceAll("\\$nl\\$", "nl" + IPath.SEPARATOR + TargetPlatform.getNL());
    }
    if (source.indexOf("$arch$") != -1) {
      source =
          source.replaceAll("\\$arch\\$", "arch" + IPath.SEPARATOR + TargetPlatform.getOSArch());
    }
    return source;
  }

  private static IPath getSourceAnnotation(IPluginModelBase model, String libraryName)
      throws CoreException {
    String zipName = getSourceZipName(libraryName);
    IPath path = getPath(model, zipName);
    if (path == null) {
      org.eclipse.pde.internal.core.SourceLocationManager manager =
          org.eclipse.pde.internal.core.PDECore.getDefault().getSourceLocationManager();
      path = manager.findSourcePath(model.getPluginBase(), new Path(zipName));
    }
    return path;
  }

  private static String getSourceZipName(String libraryName) {
    int dot = libraryName.lastIndexOf('.');
    return dot != -1 ? libraryName.substring(0, dot) + "src.zip" : libraryName;
  }

  private static IPluginModelBase resolveLibraryInFragments(IPluginLibrary library,
      String libraryName) {
    IFragmentModel[] fragments = findFragmentsFor(library.getPluginModel());
    for (int i = 0; i < fragments.length; i++) {
      IPath path = getPath(fragments[i], libraryName);
      if (path != null) {
        return fragments[i];
      }
    }
    return null;
  }

  private static IPath getPath(IPluginModelBase model, String libraryName) {
    IResource resource = model.getUnderlyingResource();
    if (resource != null) {
      IResource jarFile = resource.getProject().findMember(libraryName);
      if (jarFile != null) {
        return jarFile.getFullPath();
      }
    } else {
      File file = new File(model.getInstallLocation(), libraryName);
      if (file.exists()) {
        return new Path(file.getAbsolutePath());
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // 
  //
  ////////////////////////////////////////////////////////////////////////////
  private static IFragmentModel[] findFragmentsFor(IPluginModelBase model) {
    List<IFragmentModel> result = new ArrayList<IFragmentModel>();
    BundleDescription desc = getBundleDescription(model);
    if (desc != null) {
      BundleDescription[] fragments = desc.getFragments();
      for (int i = 0; i < fragments.length; i++) {
        IPluginModelBase candidate = PluginRegistry.findModel(fragments[i]);
        if (candidate instanceof IFragmentModel) {
          result.add((IFragmentModel) candidate);
        }
      }
    }
    return result.toArray(new IFragmentModel[result.size()]);
  }

  private static BundleDescription getBundleDescription(IPluginModelBase model) {
    BundleDescription desc = model.getBundleDescription();
    if (desc == null && model.getUnderlyingResource() != null) {
      // the model may be an editor model. 
      // editor models don't carry a bundle description
      // get the core model counterpart.
      IProject project = model.getUnderlyingResource().getProject();
      IPluginModelBase coreModel = PluginRegistry.findModel(project);
      if (coreModel != null) {
        desc = coreModel.getBundleDescription();
      }
    }
    return desc;
  }
}