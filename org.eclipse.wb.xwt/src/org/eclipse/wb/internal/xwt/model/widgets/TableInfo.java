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
package org.eclipse.wb.internal.xwt.model.widgets;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.association.Associations;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectClipboardCopy;
import org.eclipse.wb.internal.core.xml.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.xml.model.clipboard.XmlObjectMemento;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;
import org.eclipse.wb.internal.swt.support.TableSupport;

import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import java.util.List;

/**
 * Model for {@link Table}.
 *
 * @author scheglov_ke
 * @coverage XWT.model.widgets
 */
public final class TableInfo extends CompositeInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TableInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(context, description, creationSupport);
    contributeToClipboardCopy();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Broadcasts
  //
  ////////////////////////////////////////////////////////////////////////////
  private void contributeToClipboardCopy() {
    addBroadcastListener(new XmlObjectClipboardCopy() {
      public void invoke(XmlObjectInfo object, List<ClipboardCommand> commands) throws Exception {
        if (object == TableInfo.this) {
          for (TableColumnInfo column : getColumns()) {
            final XmlObjectMemento columnMemento = XmlObjectMemento.createMemento(column);
            commands.add(new ClipboardCommand() {
              private static final long serialVersionUID = 0L;

              @Override
              public void execute(XmlObjectInfo object) throws Exception {
                XmlObjectInfo column = columnMemento.create(object);
                XmlObjectUtils.add(column, Associations.direct(), object, null);
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
        int columnWidth = ((TableColumn) column.getObject()).getWidth();
        int y = 0;
        if (EnvironmentUtils.IS_MAC) {
          // HACK:
          // SWT Cocoa excludes column headers from client area, so insets.top is header height.
          // workaround is to adjust y to header height.
          y -= headerHeight;
        }
        column.setModelBounds(new Rectangle(x, y, columnWidth, headerHeight));
        x += columnWidth;
      }
    }
    // prepare items bounds
    {
      int y = headerHeight;
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
