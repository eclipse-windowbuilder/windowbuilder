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
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.association.Associations;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectClipboardCopy;
import org.eclipse.wb.internal.core.xml.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.xml.model.clipboard.XmlObjectMemento;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;

import org.eclipse.swt.widgets.Tree;

import java.util.List;

/**
 * Model for {@link Tree}.
 *
 * @author scheglov_ke
 * @coverage XWT.model.widgets
 */
public class TreeInfo extends CompositeInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TreeInfo(EditorContext context,
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
        // copy TreeColumn's
        if (object == TreeInfo.this) {
          for (TreeColumnInfo column : getColumns()) {
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
  private static int BORDER_WIDTH = 1;

  @Override
  protected void refresh_fetch() throws Exception {
    super.refresh_fetch();
    // prepare metrics
    int headerHeight;
    {
      Object tree = getObject();
      headerHeight = (Integer) ReflectionUtils.invokeMethod(tree, "getHeaderHeight()");
    }
    // prepare columns bounds
    int x = BORDER_WIDTH;
    int y = BORDER_WIDTH;
    for (TreeColumnInfo column : getColumns()) {
      int columnWidth = (Integer) ReflectionUtils.invokeMethod(column.getObject(), "getWidth()");
      Rectangle bounds = new Rectangle(x, y, columnWidth, headerHeight);
      column.setModelBounds(bounds);
      column.setBounds(bounds);
      x += columnWidth;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Children
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link TreeColumnInfo} children.
   */
  public List<TreeColumnInfo> getColumns() {
    return getChildren(TreeColumnInfo.class);
  }

  /**
   * @return the {@link TreeItemInfo} children.
   */
  public final List<TreeItemInfo> getItems() {
    return getChildren(TreeItemInfo.class);
  }
}