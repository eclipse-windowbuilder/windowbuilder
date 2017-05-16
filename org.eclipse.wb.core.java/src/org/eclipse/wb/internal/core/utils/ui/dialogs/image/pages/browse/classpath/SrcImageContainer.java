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

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IImageContainer;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.swt.graphics.Image;

import java.util.List;

/**
 * Implementation od {@link IImageContainer} for {@link IPackageFragmentRoot}.
 *
 * @author scheglov_ke
 * @coverage core.ui
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
    List<SrcPackageImageContainer> packageContainers = Lists.newArrayList();
    {
      IJavaElement[] children = m_packageFragmentRoot.getChildren();
      for (IJavaElement child : children) {
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
        packageContainers.toArray(new SrcPackageImageContainer[packageContainers.size()]);
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
  public IImageContainer[] elements() {
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
    for (SrcPackageImageContainer container : m_packageContainers) {
      container.dispose();
    }
  }
}
