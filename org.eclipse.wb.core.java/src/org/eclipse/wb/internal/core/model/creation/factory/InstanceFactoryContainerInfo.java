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
package org.eclipse.wb.internal.core.model.creation.factory;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.presentation.DefaultObjectPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;

import org.eclipse.swt.graphics.Image;

import java.util.List;

/**
 * Container for {@link InstanceFactoryInfo}, direct child of root {@link JavaInfo}.
 *
 * @author scheglov_ke
 * @coverage core.model.creation
 */
public final class InstanceFactoryContainerInfo extends ObjectInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "{instance factory container}";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the list of {@link InstanceFactoryInfo} children.
   */
  public List<InstanceFactoryInfo> getChildrenFactory() {
    return getChildren(InstanceFactoryInfo.class);
  }

  /**
   * @return the existing or new {@link InstanceFactoryContainerInfo} for given root.
   */
  public static InstanceFactoryContainerInfo get(JavaInfo root) throws Exception {
    // try to find existing container
    for (ObjectInfo child : root.getChildren()) {
      if (child instanceof InstanceFactoryContainerInfo) {
        return (InstanceFactoryContainerInfo) child;
      }
    }
    // add new container
    InstanceFactoryContainerInfo container = new InstanceFactoryContainerInfo();
    root.addChild(container);
    return container;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public IObjectPresentation getPresentation() {
    return new DefaultObjectPresentation(this) {
      public String getText() throws Exception {
        return "(instance factories)";
      }

      @Override
      public Image getIcon() throws Exception {
        return DesignerPlugin.getImage("components/factory_container.gif");
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean canDelete() {
    return false;
  }

  @Override
  public void delete() throws Exception {
  }
}
