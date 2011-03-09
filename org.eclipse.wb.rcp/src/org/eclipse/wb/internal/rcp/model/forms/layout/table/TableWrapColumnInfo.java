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
package org.eclipse.wb.internal.rcp.model.forms.layout.table;

import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.ui.forms.widgets.TableWrapData;

/**
 * Model for column in {@link ITableWrapLayout_Info<C>}.
 * 
 * @author scheglov_ke
 * @coverage rcp.model.forms
 */
public final class TableWrapColumnInfo<C extends IControlInfo> extends TableWrapDimensionInfo<C> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TableWrapColumnInfo(ITableWrapLayoutInfo<C> layout) {
    super(layout);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public int getIndex() {
    return m_layout.getColumns().indexOf(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Grab
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean getGrab(ITableWrapDataInfo layoutData) {
    return layoutData.getHorizontalGrab();
  }

  @Override
  protected void setGrab(ITableWrapDataInfo layoutData, boolean grab) throws Exception {
    layoutData.setHorizontalGrab(grab);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Alignment
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getAlignmentTitle(int alignment) {
    if (alignment == TableWrapData.LEFT) {
      return "left";
    } else if (alignment == TableWrapData.CENTER) {
      return "center";
    } else if (alignment == TableWrapData.RIGHT) {
      return "right";
    } else {
      return "fill";
    }
  }

  @Override
  protected int getAlignment(ITableWrapDataInfo layoutData) {
    return layoutData.getHorizontalAlignment();
  }

  @Override
  protected void setAlignment(ITableWrapDataInfo layoutData, int alignment) throws Exception {
    layoutData.setHorizontalAlignment(alignment);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Processing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean shouldProcessThisControl(ITableWrapDataInfo layoutData) {
    return layoutData.getX() == getIndex();
  }
}
