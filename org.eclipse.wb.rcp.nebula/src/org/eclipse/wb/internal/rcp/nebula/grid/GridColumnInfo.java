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

/**
 * Model {@link GridColumn}.
 *
 * @author sablin_aa
 * @coverage nebula.model
 */
public final class GridColumnInfo extends ItemInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GridColumnInfo(AstEditor editor,
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
      Object swtBounds = ReflectionUtils.invokeMethod(getObject(), "getBounds()");
      Rectangle bounds = RectangleSupport.getRectangle(swtBounds);
      if (getParent() instanceof GridColumnGroupInfo) {
        GridColumnGroupInfo parent = (GridColumnGroupInfo) getParent();
        Object swtParentBounds = ReflectionUtils.invokeMethod(parent.getObject(), "getBounds()");
        Rectangle parentBounds = RectangleSupport.getRectangle(swtParentBounds);
        bounds.performTranslate(-parentBounds.x, -parentBounds.y);
      }
      setModelBounds(bounds);
    }
    // continue in super()
    super.refresh_fetch();
  }
}
