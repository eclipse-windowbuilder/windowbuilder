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

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IImageContainer;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IImageElement;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.swt.graphics.Image;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation od {@link IImageContainer} for {@link IPackageFragmentRoot}.
 * 
 * @author scheglov_ke
 */
final class SrcImageContainer implements IImageContainer, IClasspathImageContainer {
  private final IPackageFragmentRoot m_packageFragmentRoot;
  private final SrcPackageImageContainer[] m_packageContainers;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SrcImageContainer(String id, IPackageFragmentRoot packageFragmentRoot) throws Exception {
    m_packageFragmentRoot = packageFragmentRoot;
    //
    List packageContainers = new ArrayList();
    {
      IJavaElement[] children = m_packageFragmentRoot.getChildren();
      for (int i = 0; i < children.length; i++) {
        IJavaElement child = children[i];
        if (child instanceof IPackageFragment) {
          IPackageFragment packageFragment = (IPackageFragment) child;
          SrcPackageImageContainer container =
              new SrcPackageImageContainer(id, packageFragmentRoot, packageFragment);
          if (!container.isEmpty()) {
            packageContainers.add(container);
          }
        }
      }
    }
    m_packageContainers =
        (SrcPackageImageContainer[]) packageContainers.toArray(new SrcPackageImageContainer[packageContainers.size()]);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IImageElement
  //
  ////////////////////////////////////////////////////////////////////////////
  public Image getImage() {
    return DesignerPlugin.getImage("folder_package.gif");
  }

  public String getName() {
    return m_packageFragmentRoot.getElementName();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IImageContainer
  //
  ////////////////////////////////////////////////////////////////////////////
  public IImageElement[] elements() {
    return m_packageContainers;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal access
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean isEmpty() {
    return m_packageContainers.length == 0;
  }

  public void dispose() {
    for (int i = 0; i < m_packageContainers.length; i++) {
      SrcPackageImageContainer container = m_packageContainers[i];
      container.dispose();
    }
  }
}
