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

/**
 * Model {@link CContainerColumn}.
 *
 * @author sablin_aa
 * @coverage nebula.model
 */
public final class CContainerColumnInfo extends ItemInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CContainerColumnInfo(AstEditor editor,
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
      int left = (Integer) ReflectionUtils.invokeMethod(getObject(), "getLeft()");
      int right = (Integer) ReflectionUtils.invokeMethod(getObject(), "getRight()");
      Rectangle bounds = new Rectangle(left, 0, right - left, getHeaderHeight());
      setModelBounds(bounds);
    }
    // continue in super()
    super.refresh_fetch();
  }

  private int getHeaderHeight() throws Exception {
    Object container = ReflectionUtils.getFieldObject(getObject(), "container");
    return (Integer) ReflectionUtils.invokeMethod(container, "getHeaderHeight()");
  }
}
