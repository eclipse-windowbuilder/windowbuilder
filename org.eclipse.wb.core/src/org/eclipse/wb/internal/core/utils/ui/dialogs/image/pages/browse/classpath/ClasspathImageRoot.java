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
package org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.classpath;

import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IImageContainer;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IImageElement;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IImageRoot;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link IImageRoot} for browsing classpath.
 * 
 * @author scheglov_ke
 */
public final class ClasspathImageRoot implements IImageRoot {
  private final IJavaProject m_project;
  private final IImageContainer m_containers[];

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ClasspathImageRoot(String id, IJavaProject project) {
    m_project = project;
    //
    List containers = new ArrayList();
    try {
      IPackageFragmentRoot[] roots = m_project.getAllPackageFragmentRoots();
      for (int i = 0; i < roots.length; i++) {
        IPackageFragmentRoot root = roots[i];
        try {
          if (root.isArchive()) {
            JarImageContainer jarContainer = new JarImageContainer(id, root);
            if (!jarContainer.isEmpty()) {
              containers.add(jarContainer);
            }
          } else {
            SrcImageContainer srcContainer = new SrcImageContainer(id, root);
            if (!srcContainer.isEmpty()) {
              containers.add(srcContainer);
            }
          }
        } catch (Throwable e) {
        }
      }
    } catch (Throwable e) {
    }
    m_containers = (IImageContainer[]) containers.toArray(new IImageContainer[containers.size()]);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IImageRoot
  //
  ////////////////////////////////////////////////////////////////////////////
  public void dispose() {
    for (int i = 0; i < m_containers.length; i++) {
      IClasspathImageContainer container = (IClasspathImageContainer) m_containers[i];
      container.dispose();
    }
  }

  public IImageElement[] elements() {
    return m_containers;
  }

  public Object[] getSelectionPath(Object data) {
    if (data instanceof String) {
      // prepare path
      String path = (String) data;
      if (!path.startsWith("/")) {
        return null;
      }
      path = path.substring(1);
      // prepare package and resource names
      IPath pathObject = new Path(path);
      String packageName = pathObject.removeLastSegments(1).toPortableString().replace('/', '.');
      String resourceName = pathObject.lastSegment();
      // try each root container
      for (int i = 0; i < m_containers.length; i++) {
        IImageContainer rootContainer = m_containers[i];
        // try each package container
        IImageElement[] packageContainers = rootContainer.elements();
        for (int j = 0; j < packageContainers.length; j++) {
          IImageContainer packageContainer = (IImageContainer) packageContainers[j];
          if (packageContainer.getName().equals(packageName)) {
            // try each resource in package
            IImageElement[] elements = packageContainer.elements();
            for (int k = 0; k < elements.length; k++) {
              IImageElement element = elements[k];
              if (element.getName().equals(resourceName)) {
                return new Object[]{rootContainer, packageContainer, element};
              }
            }
          }
        }
      }
    }
    // don't know
    return null;
  }
}
