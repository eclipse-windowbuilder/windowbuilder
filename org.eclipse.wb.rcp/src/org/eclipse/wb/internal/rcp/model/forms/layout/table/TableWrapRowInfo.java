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
 * Model for row in {@link ITableWrapLayout_Info<C>}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.forms
 */
public final class TableWrapRowInfo<C extends IControlInfo> extends TableWrapDimensionInfo<C> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TableWrapRowInfo(ITableWrapLayoutInfo<C> layout) {
    super(layout);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public int getIndex() {
    return m_layout.getRows().indexOf(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Grab
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean getGrab(ITableWrapDataInfo layoutData) {
    return layoutData.getVerticalGrab();
  }

  @Override
  protected void setGrab(ITableWrapDataInfo layoutData, boolean grab) throws Exception {
    layoutData.setVerticalGrab(grab);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Alignment
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getAlignmentTitle(int alignment) {
    if (alignment == TableWrapData.TOP) {
      return "top";
    } else if (alignment == TableWrapData.MIDDLE) {
      return "middle";
    } else if (alignment == TableWrapData.BOTTOM) {
      return "bottom";
    } else {
      return "fill";
    }
  }

  @Override
  protected int getAlignment(ITableWrapDataInfo layoutData) {
    return layoutData.getVerticalAlignment();
  }

  @Override
  protected void setAlignment(ITableWrapDataInfo layoutData, int alignment) throws Exception {
    layoutData.setVerticalAlignment(alignment);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Processing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean shouldProcessThisControl(ITableWrapDataInfo layoutData) {
    return layoutData.getY() == getIndex();
  }
}
