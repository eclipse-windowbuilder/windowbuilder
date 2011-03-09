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
package org.eclipse.wb.internal.swt.model.widgets;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swt.support.TableSupport;

import java.util.List;

/**
 * Model for SWT table {@link org.eclipse.swt.widgets.Table}.
 * 
 * @author lobas_av
 * @coverage swt.model.widgets
 */
public class TableInfo extends CompositeInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TableInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initialize
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void initialize() throws Exception {
    super.initialize();
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void clipboardCopy(JavaInfo javaInfo, List<ClipboardCommand> commands)
          throws Exception {
        // copy TableColumn's
        if (javaInfo == TableInfo.this) {
          for (TableColumnInfo column : getColumns()) {
            final JavaInfoMemento columnMemento = JavaInfoMemento.createMemento(column);
            commands.add(new ClipboardCommand() {
              private static final long serialVersionUID = 0L;

              @Override
              public void execute(JavaInfo javaInfo) throws Exception {
                JavaInfo column = columnMemento.create(javaInfo);
                JavaInfoUtils.add(column, null, javaInfo, null);
                columnMemento.apply();
              }
            });
          }
        }
      }
    });
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
    Object table = getObject();
    int headerHeight = TableSupport.getHeaderHeight(table);
    int itemHeight = TableSupport.getItemHeight(table);
    // prepare columns bounds
    int x = 0;
    {
      for (TableColumnInfo column : getColumns()) {
        int columnWidth = TableSupport.getColumnWidth(column.getObject());
        int y = 0;
        if (!EnvironmentUtils.IS_WINDOWS) {
          // SWT Cocoa && Linux GTK excludes column headers from client area, so insets.top is header height.
          // workaround is to adjust y to header height.
          y -= headerHeight;
        }
        column.setModelBounds(new Rectangle(x, y, columnWidth, headerHeight));
        x += columnWidth;
      }
    }
    // prepare items bounds
    {
      // see comment above in column headers bounds section.
      int y = EnvironmentUtils.IS_WINDOWS ? headerHeight : 0;
      int width = getClientArea().width;
      for (TableItemInfo item : getItems()) {
        item.setModelBounds(new Rectangle(0, y, width, itemHeight));
        y += itemHeight;
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Children
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link TableColumnInfo} children.
   */
  public List<TableColumnInfo> getColumns() {
    return getChildren(TableColumnInfo.class);
  }

  /**
   * @return the {@link TableItemInfo} children.
   */
  public List<TableItemInfo> getItems() {
    return getChildren(TableItemInfo.class);
  }
}