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

import java.awt.GridBagLayout;
import java.util.List;

/**
 * Model for virtual column/row in {@link AbstractGridBagLayoutInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.model.layout
 */
public abstract class DimensionInfo {
  protected final AbstractGridBagLayoutInfo m_layout;
  protected final DimensionOperations<?> m_operations;
  private int m_size;
  private double m_weight;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DimensionInfo(AbstractGridBagLayoutInfo layout, DimensionOperations<?> operations) {
    m_layout = layout;
    m_operations = operations;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the index of this {@link DimensionInfo} in all dimensions on
   *         {@link AbstractGridBagLayoutInfo}.
   */
  public final int getIndex() {
    return getDimensions().indexOf(this);
  }

  /**
   * @return the minimum size, from {@link GridBagLayout#columnWidths} or
   *         {@link GridBagLayout#rowHeights}.
   */
  public final int getSize() {
    return m_size;
  }

  /**
   * Sets the minimum size, from {@link GridBagLayout#columnWidths} or
   * {@link GridBagLayout#rowHeights}.
   */
  public void setSize(int size) throws Exception {
    m_size = size;
    m_operations.setSizeFieldElement(getIndex(), m_size);
  }

  /**
   * @return <code>true</code> if this {@link DimensionInfo} has non-zero weight.
   */
  public final boolean hasWeight() {
    return m_weight != 0.0;
  }

  /**
   * @return the weight, from {@link GridBagLayout#columnWeights} or
   *         {@link GridBagLayout#rowWeights}.
   */
  public final double getWeight() {
    return m_weight;
  }

  /**
   * Sets the weight, from {@link GridBagLayout#columnWeights} or {@link GridBagLayout#rowWeights}.
   */
  public void setWeight(double weight) throws Exception {
    m_weight = weight;
    m_operations.setWeightFieldElement(getIndex(), m_weight);
  }

  /**
   * {@link DimensionInfo} is "end of grid" filler, if it has very small (but not zero) weight.
   */
  public final boolean isFiller() {
    if (0.0 < m_weight && m_weight < 0.001) {
      int index = getIndex();
      return m_operations.isEmpty(index);
    }
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Initializes this {@link DimensionInfo} from existing {@link AbstractGridBagLayoutInfo} object. <br>
   * This operation should be performed only once, when we first time have fully initialized
   * {@link AbstractGridBagLayoutInfo} object.
   */
  void initialize() throws Exception {
    int index = getIndex();
    m_size = initialize_getMinimumSize(index);
    m_weight = initialize_getWeight(index);
  }

  /**
   * @return the minimum size, from "columnWidths" or "rowHeights" fields.
   */
  protected abstract int initialize_getMinimumSize(int index);

  /**
   * @return the weight of dimension, from "columnWeights" or "rowWeights".
   */
  protected abstract double initialize_getWeight(int index);

  /**
   * @return all {@link DimensionInfo}, siblings of this {@link DimensionInfo}.
   */
  protected abstract List<? extends DimensionInfo> getDimensions();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visiting
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Visits {@link ComponentInfo} that belongs to this {@link DimensionInfo}.
   */
  protected final void visit(final IComponentVisitor visitor) {
    ExecutionUtils.runRethrow(new RunnableEx() {
      public void run() throws Exception {
        m_layout.visitComponents(visitor, new IComponentPredicate() {
          public boolean apply(ComponentInfo component, AbstractGridBagConstraintsInfo constraints)
              throws Exception {
            return isDimensionComponent(constraints);
          }
        });
      }
    });
  }

  /**
   * @return <code>true</code> if {@link ComponentInfo} belongs to this {@link DimensionInfo}, so
   *         should be visited.
   */
  protected abstract boolean isDimensionComponent(AbstractGridBagConstraintsInfo constraints);
}
