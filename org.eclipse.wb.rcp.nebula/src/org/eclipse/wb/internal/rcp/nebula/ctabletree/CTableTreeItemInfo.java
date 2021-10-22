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
package org.eclipse.wb.internal.rcp.nebula.ctabletree;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.model.widgets.ItemInfo;
import org.eclipse.wb.internal.swt.support.RectangleSupport;

import java.util.List;

/**
 * Model {@link CTableTreeItem}.
 *
 * @author sablin_aa
 * @coverage nebula.model
 */
public final class CTableTreeItemInfo extends ItemInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CTableTreeItemInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_fetch() throws Exception {
    {
      Rectangle bounds = getComponentBounds();
      if (getParent() instanceof CTableTreeItemInfo) {
        CTableTreeItemInfo parent = (CTableTreeItemInfo) getParent();
        Rectangle parentBounds = parent.getComponentBounds();
        if (parent.getComponentExpanded()) {
          bounds.translate(-parentBounds.x, -parentBounds.y);
        } else {
          bounds.translate(0, parentBounds.height);
        }
      } else {
        bounds.translate(0, getHeaderHeight());
      }
      setModelBounds(bounds);
    }
    // continue in super()
    super.refresh_fetch();
  }

  private Rectangle getComponentBounds() throws Exception {
    Object swtBounds = ReflectionUtils.invokeMethod(getObject(), "getUnifiedBounds()");
    Rectangle bounds = RectangleSupport.getRectangle(swtBounds);
    if (getComponentVisible()) {
      List<CTableTreeItemInfo> childItems = getChildren(CTableTreeItemInfo.class);
      for (CTableTreeItemInfo childrenItem : childItems) {
        if (childrenItem.getComponentVisible()) {
          Rectangle childBounds = childrenItem.getComponentBounds();
          if (childBounds.height > 0 && childBounds.width > 0) {
            bounds.union(childBounds);
          }
        }
      }
    }
    return bounds;
  }

  private boolean getComponentVisible() throws Exception {
    return (Boolean) ReflectionUtils.invokeMethod(getObject(), "getVisible()");
  }

  private boolean getComponentExpanded() throws Exception {
    return (Boolean) ReflectionUtils.invokeMethod(getObject(), "getExpanded()");
  }

  private int getHeaderHeight() throws Exception {
    Object container = ReflectionUtils.getFieldObject(getObject(), "container");
    return (Boolean) ReflectionUtils.invokeMethod(container, "getHeaderVisible()")
        ? (Integer) ReflectionUtils.invokeMethod(container, "getHeaderHeight()")
        : 0;
  }
}
