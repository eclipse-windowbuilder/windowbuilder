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
package org.eclipse.wb.internal.rcp.nebula.grid;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.model.widgets.ItemInfo;
import org.eclipse.wb.internal.swt.support.RectangleSupport;

import java.util.List;

/**
 * Model {@link GridItem}.
 * 
 * @author sablin_aa
 * @coverage nebula.model
 */
public final class GridItemInfo extends ItemInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GridItemInfo(AstEditor editor,
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
      if (getParent() instanceof GridItemInfo) {
        GridItemInfo parent = (GridItemInfo) getParent();
        Rectangle parentBounds = parent.getComponentBounds();
        if (parent.getComponentExpanded()) {
          bounds.translate(-parentBounds.x, -parentBounds.y);
        } else {
          bounds = new Rectangle(parentBounds.x, parentBounds.height, parentBounds.width, 0);
        }
      }
      setModelBounds(bounds);
    }
    // continue in super()
    super.refresh_fetch();
  }

  private Rectangle getComponentBounds() throws Exception {
    Rectangle bounds = getComponentCellsBounds();
    if (getComponentExpanded()) {
      List<GridItemInfo> childItems = getChildren(GridItemInfo.class);
      for (GridItemInfo chilsItem : childItems) {
        bounds.union(chilsItem.getComponentBounds());
      }
    }
    return bounds;
  }

  private Rectangle getComponentCellsBounds() throws Exception {
    Object grid = ReflectionUtils.invokeMethod(getObject(), "getParent()");
    int columnCount = (Integer) ReflectionUtils.invokeMethod(grid, "getColumnCount()");
    Rectangle bounds = null;
    for (int i = 0; i < columnCount; i++) {
      Object swtBounds = ReflectionUtils.invokeMethod(getObject(), "getBounds(int)", i);
      if (bounds == null) {
        bounds = RectangleSupport.getRectangle(swtBounds);
      } else {
        bounds.union(RectangleSupport.getRectangle(swtBounds));
      }
    }
    return bounds;
  }

  private Boolean getComponentExpanded() throws Exception {
    return (Boolean) ReflectionUtils.invokeMethod(getObject(), "isExpanded()");
  }
}
