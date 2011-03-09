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

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import java.util.LinkedList;

/**
 * Implementation of {@link DimensionOperations} for {@link ColumnInfo}'s.
 * 
 * @author scheglov_ke
 * @coverage swing.model.layout
 */
public final class DimensionOperationsColumn extends DimensionOperations<ColumnInfo> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DimensionOperationsColumn(AbstractGridBagLayoutInfo layout) {
    super(layout, "columnWidths", "columnWeights");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean isEmpty(final int index) {
    final boolean[] filled = new boolean[1];
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        m_layout.visitComponents(new IComponentVisitor() {
          public void visit(ComponentInfo component, AbstractGridBagConstraintsInfo constraints)
              throws Exception {
            int x = constraints.getX();
            int width = constraints.getWidth();
            filled[0] |= x <= index && index < x + width;
          }
        });
      }
    });
    return !filled[0];
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public ColumnInfo insert(int index) throws Exception {
    m_layout.prepareCell(index, true, -1, false);
    m_layout.ensureGapInsets();
    return m_layout.getColumns().get(index);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal dimensions
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected LinkedList<ColumnInfo> getDimensions() {
    return m_layout.getColumns();
  }

  @Override
  protected ColumnInfo newDimension() {
    return new ColumnInfo(m_layout, this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal component operations
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void moveComponent(ComponentInfo component,
      AbstractGridBagConstraintsInfo constraints,
      int location) throws Exception {
    m_layout.command_MOVE(component, location, false, constraints.y, false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal GridBagConstraintsInfo operations
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected int getLocation(AbstractGridBagConstraintsInfo constraints) {
    return constraints.x;
  }

  @Override
  protected int getSize(AbstractGridBagConstraintsInfo constraints) {
    return constraints.width;
  }

  @Override
  protected void setLocation(AbstractGridBagConstraintsInfo constraints, int location)
      throws Exception {
    constraints.setX(location);
  }

  @Override
  protected void setSize(AbstractGridBagConstraintsInfo constraints, int size) throws Exception {
    constraints.setWidth(size);
  }
}
