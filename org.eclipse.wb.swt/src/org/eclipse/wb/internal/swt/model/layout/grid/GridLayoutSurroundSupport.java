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
package org.eclipse.wb.internal.swt.model.layout.grid;

import org.eclipse.wb.internal.swt.model.util.surround.LayoutSurroundSupport;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

import java.util.List;

/**
 * Helper for surrounding {@link ControlInfo}'s with some {@link CompositeInfo}.
 *
 * @author scheglov_ke
 * @coverage swt.model.layout
 */
public final class GridLayoutSurroundSupport extends LayoutSurroundSupport {
  static final String CELLS_KEY = "SurroundSupport_CELLS";
  private final GridLayoutInfo m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GridLayoutSurroundSupport(GridLayoutInfo layout) {
    super(layout);
    m_layout = layout;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operation
  //
  ////////////////////////////////////////////////////////////////////////////
  private Rectangle m_enclosingCells;

  @Override
  protected boolean validateComponents(List<ControlInfo> components) throws Exception {
    if (!super.validateComponents(components)) {
      return false;
    }
    // prepare enclosing bounds
    {
      m_enclosingCells = getCells(components.get(0));
      for (ControlInfo component : components) {
        m_enclosingCells.union(getCells(component));
      }
    }
    // check that there are no other controls in enclosing bounds
    for (ControlInfo control : m_layout.getControls()) {
      if (!components.contains(control)
          && !m_layout.isFiller(control)
          && m_enclosingCells.intersects(getCells(control))) {
        return false;
      }
    }
    // OK
    return true;
  }

  @Override
  protected void addContainer(CompositeInfo container, List<ControlInfo> components)
      throws Exception {
    container.putArbitraryValue(CELLS_KEY, m_enclosingCells);
    // add container
    int targetRow = m_layout.getRows().size();
    m_layout.command_CREATE(container, 0, false, targetRow, false);
    m_layout.setRemoveEmptyColumnsRows(false);
  }

  @Override
  protected void moveDone(CompositeInfo container, List<ControlInfo> components) throws Exception {
    super.moveDone(container, components);
    m_layout.setRemoveEmptyColumnsRows(true);
    // move "container"
    m_layout.command_setCells(container, m_enclosingCells, true);
    {
      GridDataInfo gridData = GridLayoutInfo.getGridData(container);
      gridData.setHorizontalAlignment(SWT.FILL);
      gridData.setVerticalAlignment(SWT.FILL);
    }
    // "container" may be only component in some columns/rows, so no need for spanning
    m_layout.command_normalizeSpanning();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Rectangle} with grid bounds, i.e. column/row plus width/height span.
   */
  static Rectangle getCells(ControlInfo control) throws Exception {
    GridDataInfo gridData = GridLayoutInfo.getGridData(control);
    return new Rectangle(gridData.x, gridData.y, gridData.width, gridData.height);
  }
}
