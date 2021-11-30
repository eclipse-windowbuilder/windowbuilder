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
package org.eclipse.wb.internal.xwt.model.jface;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.swt.support.TableSupport;
import org.eclipse.wb.internal.xwt.model.widgets.TableInfo;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import java.util.List;

/**
 * Model for {@link TableViewer}.
 *
 * @author scheglov_ke
 * @coverage XWT.model.jface
 */
public class TableViewerInfo extends ViewerInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TableViewerInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(context, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link TableInfo} model.
   */
  public TableInfo getTable() {
    return (TableInfo) getControl();
  }

  /**
   * @return the {@link TableViewerColumnInfo} children.
   */
  public List<TableViewerColumnInfo> getColumns() {
    return getChildren(TableViewerColumnInfo.class);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_fetch() throws Exception {
    super.refresh_fetch();
    // prepare metrics
    Table table = (Table) getControl().getObject();
    int headerHeight = TableSupport.getHeaderHeight(table);
    // prepare columns bounds
    int x = 0;
    {
      for (TableViewerColumnInfo column : getColumns()) {
        TableColumn columnObject = ((TableViewerColumn) column.getObject()).getColumn();
        int columnWidth = columnObject.getWidth();
        int y = 0;
        if (EnvironmentUtils.IS_MAC) {
          // HACK:
          // SWT Cocoa excludes column headers from client area, so insets.top is header height.
          // workaround is to adjust y to header height.
          y -= headerHeight;
        }
        column.setModelBounds(new Rectangle(x, y, columnWidth, headerHeight));
        column.getBounds().translate(getControl().getClientAreaInsets());
        x += columnWidth;
      }
    }
  }
}
