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
package org.eclipse.wb.internal.core.utils.dialogfields;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import java.util.ArrayList;
import java.util.List;

/**
 * A special composite to layout columns inside a table. The composite is needed since we have to
 * layout the columns "before" the actual table gets layouted. Hence we can't use a normal layout
 * manager.
 */
class TableLayoutComposite extends Composite {
  /**
   * The number of extra pixels taken as horizontal trim by the table column. To ensure there are N
   * pixels available for the content of the column, assign N+COLUMN_TRIM for the column width.
   */
  private static int COLUMN_TRIM = "carbon".equals(SWT.getPlatform()) ? 24 : 3;
  private final List columns = new ArrayList();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates a new <code>TableLayoutComposite</code>.
   */
  public TableLayoutComposite(Composite parent, int style) {
    super(parent, style);
    addControlListener(new ControlAdapter() {
      @Override
      public void controlResized(ControlEvent e) {
        Rectangle area = getClientArea();
        Table table = (Table) getChildren()[0];
        Point preferredSize = computeTableSize(table);
        int width = area.width - 2 * table.getBorderWidth();
        // subtract the scroll bar width from the total column width
        if (preferredSize.y > area.height) {
          Point vBarSize = table.getVerticalBar().getSize();
          width -= vBarSize.x;
        }
        // do layout
        layoutTable(table, width, area, table.getSize().x < area.width);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds {@link ColumnLayoutData} to this table layout.
   */
  public void addColumnData(ColumnLayoutData data) {
    columns.add(data);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Helpers
  //
  ////////////////////////////////////////////////////////////////////////////
  private Point computeTableSize(Table table) {
    Point result = table.computeSize(SWT.DEFAULT, SWT.DEFAULT);
    int width = 0;
    int size = columns.size();
    for (int i = 0; i < size; ++i) {
      ColumnLayoutData layoutData = (ColumnLayoutData) columns.get(i);
      if (layoutData instanceof ColumnPixelData) {
        ColumnPixelData col = (ColumnPixelData) layoutData;
        width += col.width;
        if (col.addTrim) {
          width += COLUMN_TRIM;
        }
      } else if (layoutData instanceof ColumnWeightData) {
        ColumnWeightData col = (ColumnWeightData) layoutData;
        width += col.minimumWidth;
      } else {
        Assert.isTrue(false, "Unknown column layout data");
      }
    }
    if (width > result.x) {
      result.x = width;
    }
    return result;
  }

  private void layoutTable(Table table, int width, Rectangle area, boolean increase) {
    // may be invalid first time on Linux
    if (width <= 1) {
      return;
    }
    // do layout
    TableColumn[] tableColumns = table.getColumns();
    int size = Math.min(columns.size(), tableColumns.length);
    int[] widths = new int[size];
    int fixedWidth = 0;
    int numberOfWeightColumns = 0;
    int totalWeight = 0;
    // first calculate space occupied by fixed columns
    for (int i = 0; i < size; i++) {
      ColumnLayoutData col = (ColumnLayoutData) columns.get(i);
      if (col instanceof ColumnPixelData) {
        ColumnPixelData cpd = (ColumnPixelData) col;
        int pixels = cpd.width;
        if (cpd.addTrim) {
          pixels += COLUMN_TRIM;
        }
        widths[i] = pixels;
        fixedWidth += pixels;
      } else if (col instanceof ColumnWeightData) {
        ColumnWeightData cw = (ColumnWeightData) col;
        numberOfWeightColumns++;
        int weight = cw.weight;
        totalWeight += weight;
      } else {
        Assert.isTrue(false, "Unknown column layout data");
      }
    }
    // do we have columns that have a weight?
    if (numberOfWeightColumns > 0) {
      // now distribute the rest to the columns with weight
      int rest = width - fixedWidth;
      int totalDistributed = 0;
      for (int i = 0; i < size; ++i) {
        ColumnLayoutData col = (ColumnLayoutData) columns.get(i);
        if (col instanceof ColumnWeightData) {
          ColumnWeightData cw = (ColumnWeightData) col;
          int weight = cw.weight;
          int pixels = totalWeight == 0 ? 0 : weight * rest / totalWeight;
          if (pixels < cw.minimumWidth) {
            pixels = cw.minimumWidth;
          }
          totalDistributed += pixels;
          widths[i] = pixels;
        }
      }
      // distribute any remaining pixels to columns with weight
      int diff = rest - totalDistributed;
      for (int i = 0; diff > 0; ++i) {
        if (i == size) {
          i = 0;
        }
        ColumnLayoutData col = (ColumnLayoutData) columns.get(i);
        if (col instanceof ColumnWeightData) {
          ++widths[i];
          --diff;
        }
      }
    }
    if (increase) {
      table.setSize(area.width, area.height);
    }
    for (int i = 0; i < size; i++) {
      tableColumns[i].setWidth(widths[i]);
    }
    if (!increase) {
      table.setSize(area.width, area.height);
    }
  }
}
