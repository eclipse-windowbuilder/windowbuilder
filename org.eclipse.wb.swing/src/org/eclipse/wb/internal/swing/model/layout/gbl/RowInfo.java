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

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import java.util.EnumSet;
import java.util.List;

/**
 * Model for row in {@link AbstractGridBagLayoutInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.model.layout
 */
public final class RowInfo extends DimensionInfo {
  public enum Alignment {
    UNKNOWN, TOP, CENTER, BOTTOM, FILL, BASELINE, BASELINE_ABOVE, BASELINE_BELOW
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RowInfo(AbstractGridBagLayoutInfo layout, DimensionOperations<?> operations) {
    super(layout, operations);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the alignment of this {@link ColumnInfo}. {@link ColumnInfo} has alignment only if all
   *         {@link ComponentInfo}'s have same alignment. In other case {@link Alignment#UNKNOWN}
   *         will be returned.
   */
  public Alignment getAlignment() {
    final EnumSet<Alignment> alignments = EnumSet.noneOf(Alignment.class);
    visit(new IComponentVisitor() {
      public void visit(ComponentInfo component, AbstractGridBagConstraintsInfo constraints)
          throws Exception {
        alignments.add(constraints.getVerticalAlignment());
      }
    });
    return alignments.size() == 1 ? alignments.iterator().next() : Alignment.UNKNOWN;
  }

  /**
   * Sets alignment for all {@link ComponentInfo}'s in this {@link RowInfo}.
   */
  public void setAlignment(final RowInfo.Alignment alignment) throws Exception {
    visit(new IComponentVisitor() {
      public void visit(ComponentInfo component, AbstractGridBagConstraintsInfo constraints)
          throws Exception {
        constraints.setVerticalAlignment(alignment);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected int initialize_getMinimumSize(int index) {
    Object layoutObject = m_layout.getObject();
    int[] widths = (int[]) ReflectionUtils.getFieldObject(layoutObject, "rowHeights");
    if (widths != null && widths.length > index) {
      return widths[index];
    } else {
      return 0;
    }
  }

  @Override
  protected double initialize_getWeight(int index) {
    Object layoutObject = m_layout.getObject();
    double[] weights = (double[]) ReflectionUtils.getFieldObject(layoutObject, "rowWeights");
    if (weights != null && weights.length > index) {
      return weights[index];
    } else {
      return 0;
    }
  }

  @Override
  protected List<? extends DimensionInfo> getDimensions() {
    return m_layout.getRows();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visiting
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean isDimensionComponent(AbstractGridBagConstraintsInfo constraints) {
    return constraints.y == getIndex();
  }
}
