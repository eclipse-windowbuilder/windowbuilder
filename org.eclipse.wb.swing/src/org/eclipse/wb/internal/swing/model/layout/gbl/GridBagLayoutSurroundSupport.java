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
package org.eclipse.wb.internal.swing.model.layout.gbl;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.util.grid.GridAlignmentHelper;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.util.surround.LayoutSurroundSupport;

import java.util.List;

/**
 * Helper for surrounding {@link ComponentInfo}'s with some {@link ContainerInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.model.layout
 */
public final class GridBagLayoutSurroundSupport extends LayoutSurroundSupport {
  static final String CELLS_KEY = "SurroundSupport_CELLS";
  private final GridBagLayoutInfo m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GridBagLayoutSurroundSupport(GridBagLayoutInfo layout) {
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
  protected boolean validateComponents(List<ComponentInfo> components) throws Exception {
    // prepare enclosing bounds
    {
      m_enclosingCells = getCells(components.get(0));
      for (ComponentInfo component : components) {
        m_enclosingCells.union(getCells(component));
      }
    }
    // check that there are no other controls in enclosing bounds
    for (ComponentInfo component : m_layout.getContainer().getChildrenComponents()) {
      if (!components.contains(component) && m_enclosingCells.intersects(getCells(component))) {
        return false;
      }
    }
    // continue
    return super.validateComponents(components);
  }

  @Override
  protected void addContainer(ContainerInfo container, List<ComponentInfo> components)
      throws Exception {
    container.putArbitraryValue(CELLS_KEY, m_enclosingCells);
    // don't grab
    {
      JavaInfoUtils.setParameter(container, GridAlignmentHelper.V_GRAB_HORIZONTAL, "false");
      JavaInfoUtils.setParameter(container, GridAlignmentHelper.V_GRAB_VERTICAL, "false");
    }
    // add container
    m_layout.command_CREATE(container, m_enclosingCells.x, false, m_enclosingCells.y, false);
    setCells(container, m_enclosingCells);
    // set FILL/FILL alignments
    {
      GridBagConstraintsInfo constraints = GridBagLayoutInfo.getConstraintsFor(container);
      constraints.setAlignment(ColumnInfo.Alignment.FILL, RowInfo.Alignment.FILL);
    }
  }

  @Override
  protected void moveDone(ContainerInfo container, List<ComponentInfo> components) throws Exception {
    super.moveDone(container, components);
    // "container" may be only component in some columns/rows, so no need for spanning
    m_layout.getColumnOperations().normalizeSpanning();
    m_layout.getRowOperations().normalizeSpanning();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the {@link Rectangle} with grid bounds, i.e. column/row plus width/height span.
   */
  void setCells(ComponentInfo component, Rectangle cells) throws Exception {
    GridBagConstraintsInfo constraints = GridBagLayoutInfo.getConstraintsFor(component);
    constraints.setX(cells.x);
    constraints.setY(cells.y);
    constraints.setWidth(cells.width);
    constraints.setHeight(cells.height);
  }

  /**
   * @return the {@link Rectangle} with grid bounds, i.e. column/row plus width/height span.
   */
  static Rectangle getCells(ComponentInfo component) throws Exception {
    GridBagConstraintsInfo constraints = GridBagLayoutInfo.getConstraintsFor(component);
    return new Rectangle(constraints.x, constraints.y, constraints.width, constraints.height);
  }
}
