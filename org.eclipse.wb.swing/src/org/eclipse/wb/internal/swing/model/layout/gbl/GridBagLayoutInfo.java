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

import org.eclipse.wb.core.model.association.EmptyAssociation;
import org.eclipse.wb.draw2d.geometry.Interval;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.IExceptionConstants;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import org.apache.commons.lang.ArrayUtils;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.util.List;

/**
 * Model for {@link GridBagLayout}.
 * 
 * @author scheglov_ke
 * @author sablin_aa
 * @coverage swing.model.layout
 */
public final class GridBagLayoutInfo extends AbstractGridBagLayoutInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GridBagLayoutInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GridBagConstraintsInfo access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link AbstractGridBagConstraintsInfo} for given {@link ComponentInfo}.
   */
  @Override
  public AbstractGridBagConstraintsInfo getConstraints(final ComponentInfo component) {
    return getConstraintsFor(component);
  }

  public static GridBagConstraintsInfo getConstraintsFor(final ComponentInfo component) {
    return ExecutionUtils.runObject(new RunnableObjectEx<GridBagConstraintsInfo>() {
      public GridBagConstraintsInfo runObject() throws Exception {
        // prepare constraints
        GridBagConstraintsInfo constraints;
        {
          List<GridBagConstraintsInfo> constraintsList =
              component.getChildren(GridBagConstraintsInfo.class);
          if (constraintsList.size() > 1) {
            throw new DesignerException(IExceptionConstants.MORE_THAN_ONE_CONSTRAINTS,
                component.toString(),
                constraintsList.toString(),
                component.getVariableSupport().getComponentName());
          }
          if (constraintsList.size() == 1) {
            constraints = constraintsList.get(0);
          } else {
            constraints =
                (GridBagConstraintsInfo) JavaInfoUtils.createJavaInfo(
                    component.getEditor(),
                    GridBagConstraints.class,
                    new VirtualConstraintsCreationSupport(component));
            constraints.setVariableSupport(new VirtualConstraintsVariableSupport(constraints));
            constraints.setAssociation(new EmptyAssociation());
            component.addChild(constraints);
          }
        }
        // initialize and return
        constraints.init();
        return constraints;
      }
    });
  }

  @Override
  public Object getConstraintsObject(final java.awt.Component component) throws Exception {
    GridBagConstraints constraints;
    if (component == null) {
      // no component instance, we probably add new component, so use just some GridBagConstraints 
      constraints = new GridBagConstraints();
    } else {
      // component is bound to parent, get constraints from layout
      GridBagLayout layout = (GridBagLayout) getObject();
      constraints = layout.getConstraints(component);
    }
    return constraints;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Set
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void onSet() throws Exception {
    addFieldAssignment("rowWeights", "new double[]{Double.MIN_VALUE}");
    addFieldAssignment("columnWeights", "new double[]{Double.MIN_VALUE}");
    addFieldAssignment("rowHeights", "new int[]{0}");
    addFieldAssignment("columnWidths", "new int[]{0}");
    super.onSet();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void beforeRefreshFilled() throws Exception {
    Object layout = getObject();
    // size arrays should have elements for all columns/rows
    ensureObjectFieldArrayLength(layout, "columnWidths", getColumns().size());
    ensureObjectFieldArrayLength(layout, "rowHeights", getRows().size());
    super.beforeRefreshFilled();
  }

  @Override
  protected boolean afterRefreshFilled() throws Exception {
    boolean shouldPerformLayout = super.afterRefreshFilled();
    GridBagLayout layout = (GridBagLayout) getObject();
    int columnCount = getColumns().size();
    int rowCount = getRows().size();
    int[][] dimensions = getLayoutDimensions();
    // set minimum size for empty columns
    int minWidth = EMPTY_DIM + EMPTY_GAP;
    for (int column = 0; column < columnCount; column++) {
      if (!m_refreshFilledColumns.contains(column) && dimensions[0][column] < minWidth) {
        layout.columnWidths[column] = minWidth;
        shouldPerformLayout = true;
      }
    }
    // set minimum size for empty rows
    int minHeight = EMPTY_DIM + EMPTY_GAP;
    for (int row = 0; row < rowCount; row++) {
      if (!m_refreshFilledRows.contains(row) && dimensions[1][row] < minHeight) {
        layout.rowHeights[row] = minHeight;
        shouldPerformLayout = true;
      }
    }
    return shouldPerformLayout;
  }

  /**
   * Ensures that <code>int[]</code> field of given object is not <code>null</code> and has at least
   * required length.
   */
  private static void ensureObjectFieldArrayLength(Object o, String fieldName, int requiredLength)
      throws Exception {
    int[] array = (int[]) ReflectionUtils.getFieldObject(o, fieldName);
    if (array == null) {
      ReflectionUtils.setField(o, fieldName, new int[requiredLength]);
    } else if (array.length < requiredLength) {
      int[] newArray = new int[requiredLength];
      System.arraycopy(array, 0, newArray, 0, array.length);
      ReflectionUtils.setField(o, fieldName, newArray);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dimensions
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected int[][] getLayoutDimensions() throws Exception {
    GridBagLayout layout = (GridBagLayout) getObject();
    return layout.getLayoutDimensions();
  }

  @Override
  protected Point getLayoutOrigin() throws Exception {
    GridBagLayout layout = (GridBagLayout) getObject();
    return layout.getLayoutOrigin();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IGridInfo support
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Interval[] checkColumnIntervals(Interval[] columnIntervals) {
    GridBagLayout layout = (GridBagLayout) getObject();
    final int lastIndex = columnIntervals.length - 1;
    if (columnIntervals.length > 0
        && layout.columnWeights != null
        && layout.columnWeights.length >= columnIntervals.length
        && layout.columnWeights[lastIndex] > 0.0
        && layout.columnWeights[lastIndex] < 0.001
        && getColumnOperations().isEmpty(lastIndex)) {
      return (Interval[]) ArrayUtils.remove(columnIntervals, lastIndex);
    }
    return columnIntervals;
  }

  @Override
  protected Interval[] checkRowIntervals(Interval[] rowIntervals) {
    GridBagLayout layout = (GridBagLayout) getObject();
    int lastIndex = rowIntervals.length - 1;
    if (rowIntervals.length > 0
        && layout.rowWeights != null
        && layout.rowWeights.length >= rowIntervals.length
        && layout.rowWeights[lastIndex] > 0.0
        && layout.rowWeights[lastIndex] < 0.001
        && getRowOperations().isEmpty(lastIndex)) {
      return (Interval[]) ArrayUtils.remove(rowIntervals, lastIndex);
    }
    return rowIntervals;
  }
}
