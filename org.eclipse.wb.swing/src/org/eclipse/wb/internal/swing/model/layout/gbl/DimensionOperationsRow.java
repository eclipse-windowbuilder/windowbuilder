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
 * Implementation of {@link DimensionOperations} for {@link RowInfo}'s.
 * 
 * @author scheglov_ke
 * @coverage swing.model.layout
 */
public final class DimensionOperationsRow extends DimensionOperations<RowInfo> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DimensionOperationsRow(AbstractGridBagLayoutInfo layout) {
    super(layout, "rowHeights", "rowWeights");
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
            int y = constraints.getY();
            int height = constraints.getHeight();
            filled[0] |= y <= index && index < y + height;
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
  public RowInfo insert(int index) throws Exception {
    m_layout.prepareCell(-1, false, index, true);
    m_layout.ensureGapInsets();
    return m_layout.getRows().get(index);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal dimensions
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected LinkedList<RowInfo> getDimensions() {
    return m_layout.getRows();
  }

  @Override
  protected RowInfo newDimension() {
    return new RowInfo(m_layout, this);
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
    m_layout.command_MOVE(component, constraints.x, false, location, false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal GridBagConstraintsInfo operations
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected int getLocation(AbstractGridBagConstraintsInfo constraints) {
    return constraints.y;
  }

  @Override
  protected int getSize(AbstractGridBagConstraintsInfo constraints) {
    return constraints.height;
  }

  @Override
  protected void setLocation(AbstractGridBagConstraintsInfo constraints, int location)
      throws Exception {
    constraints.setY(location);
  }

  @Override
  protected void setSize(AbstractGridBagConstraintsInfo constraints, int size) throws Exception {
    constraints.setHeight(size);
  }
}
