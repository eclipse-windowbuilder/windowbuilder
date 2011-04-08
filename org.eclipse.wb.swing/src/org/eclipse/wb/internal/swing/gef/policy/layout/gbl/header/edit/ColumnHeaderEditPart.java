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
package org.eclipse.wb.internal.swing.gef.policy.layout.gbl.header.edit;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.Graphics;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Interval;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.swing.gef.GefMessages;
import org.eclipse.wb.internal.swing.gef.policy.layout.gbl.header.actions.DimensionHeaderAction;
import org.eclipse.wb.internal.swing.gef.policy.layout.gbl.header.actions.SetAlignmentColumnAction;
import org.eclipse.wb.internal.swing.gef.policy.layout.gbl.header.actions.SetGrowAction;
import org.eclipse.wb.internal.swing.model.layout.gbl.AbstractGridBagLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.ColumnInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.ColumnInfo.Alignment;
import org.eclipse.wb.internal.swing.model.layout.gbl.ui.ColumnEditDialog;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.graphics.Image;

/**
 * {@link EditPart} for {@link ColumnInfo} header of {@link AbstractGridBagLayoutInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.gef.policy
 */
public final class ColumnHeaderEditPart extends DimensionHeaderEditPart<ColumnInfo> {
  private final ColumnInfo m_column;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ColumnHeaderEditPart(AbstractGridBagLayoutInfo layout,
      ColumnInfo column,
      Figure containerFigure) {
    super(layout, column, containerFigure);
    m_column = column;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Figure
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Figure createFigure() {
    Figure figure = new Figure() {
      @Override
      protected void paintClientArea(Graphics graphics) {
        Rectangle r = getClientArea();
        // ignore paint when Layout already replaced, but event loop happens
        if (!m_layout.isActive()) {
          return;
        }
        // draw rectangle
        graphics.setForegroundColor(IColorConstants.buttonDarker);
        graphics.drawLine(r.x, r.y, r.x, r.bottom());
        graphics.drawLine(r.right() - 1, r.y, r.right() - 1, r.bottom());
        // draw column index
        int titleLeft;
        int titleRight;
        {
          String title = "" + getIndex();
          Dimension textExtents = graphics.getTextExtent(title);
          if (r.width < 3 + textExtents.width + 3) {
            return;
          }
          // draw title
          titleLeft = r.x + (r.width - textExtents.width) / 2;
          titleRight = titleLeft + textExtents.width;
          int y = r.y + (r.height - textExtents.height) / 2;
          graphics.setForegroundColor(IColorConstants.black);
          graphics.drawText(title, titleLeft, y);
        }
        // draw alignment indicator
        if (titleLeft - r.x > 3 + 7 + 3) {
          Image image = null;
          Alignment alignment = m_column.getAlignment();
          if (alignment == ColumnInfo.Alignment.LEFT) {
            image = getImage("left.gif");
          } else if (alignment == ColumnInfo.Alignment.RIGHT) {
            image = getImage("right.gif");
          } else if (alignment == ColumnInfo.Alignment.CENTER) {
            image = getImage("center.gif");
          } else if (alignment == ColumnInfo.Alignment.FILL) {
            image = getImage("fill.gif");
          }
          if (image != null) {
            int x = r.x + 2;
            drawCentered(graphics, image, x);
          }
        }
        // draw grow indicator
        if (m_column.hasWeight()) {
          if (titleRight + 3 + 7 + 3 < r.right()) {
            Image image = getImage("grow.gif");
            drawCentered(graphics, image, r.right() - 3 - image.getBounds().width);
          }
        }
      }

      private Image getImage(String name) {
        return AbstractGridBagLayoutInfo.getImage("headers/h/alignment/" + name);
      }

      private void drawCentered(Graphics graphics, Image image, int x) {
        int y = (getBounds().height - image.getBounds().height) / 2;
        graphics.drawImage(image, x, y);
      }
    };
    //
    figure.setOpaque(true);
    figure.setBackground(COLOR_NORMAL);
    figure.setFont(DEFAULT_FONT);
    return figure;
  }

  @Override
  protected void refreshVisuals() {
    super.refreshVisuals();
    int index = getIndex();
    Interval interval = m_layout.getGridInfo().getColumnIntervals()[index];
    Rectangle bounds =
        new Rectangle(interval.begin,
            0,
            interval.length + 1,
            ((GraphicalEditPart) getParent()).getFigure().getSize().height);
    bounds.translate(getOffset().x, 0);
    getFigure().setBounds(bounds);
  }

  @Override
  public int getIndex() {
    return m_layout.getColumns().indexOf(m_column);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IHeaderMenuProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public void buildContextMenu(IMenuManager manager) {
    // operations
    {
      manager.add(new DimensionHeaderAction<ColumnInfo>(this,
          GefMessages.ColumnHeaderEditPart_insertColumn) {
        @Override
        protected void run(ColumnInfo column) throws Exception {
          m_layout.getColumnOperations().insert(column.getIndex());
        }
      });
      manager.add(new DimensionHeaderAction<ColumnInfo>(this,
          GefMessages.ColumnHeaderEditPart_appendColumn) {
        @Override
        protected void run(ColumnInfo column) throws Exception {
          m_layout.getColumnOperations().insert(column.getIndex() + 1);
        }
      });
      manager.add(new DimensionHeaderAction<ColumnInfo>(this,
          GefMessages.ColumnHeaderEditPart_deleteColumn,
          AbstractGridBagLayoutInfo.getImageDescriptor("headers/h/menu/delete.gif")) {
        @Override
        protected void run(ColumnInfo column) throws Exception {
          m_layout.getColumnOperations().delete(column.getIndex());
        }
      });
      manager.add(new DimensionHeaderAction<ColumnInfo>(this,
          GefMessages.ColumnHeaderEditPart_deleteContents) {
        @Override
        protected void run(ColumnInfo column) throws Exception {
          m_layout.getColumnOperations().clear(column.getIndex());
        }
      });
      manager.add(new DimensionHeaderAction<ColumnInfo>(this,
          GefMessages.ColumnHeaderEditPart_splitColumn) {
        @Override
        protected void run(ColumnInfo column) throws Exception {
          m_layout.getColumnOperations().split(column.getIndex());
        }
      });
    }
    // alignment
    {
      manager.add(new Separator());
      manager.add(new SetAlignmentColumnAction(this,
          GefMessages.ColumnHeaderEditPart_haLeft,
          AbstractGridBagLayoutInfo.getImageDescriptor("headers/h/menu/left.gif"),
          ColumnInfo.Alignment.LEFT));
      manager.add(new SetAlignmentColumnAction(this,
          GefMessages.ColumnHeaderEditPart_haCenter,
          AbstractGridBagLayoutInfo.getImageDescriptor("headers/h/menu/center.gif"),
          ColumnInfo.Alignment.CENTER));
      manager.add(new SetAlignmentColumnAction(this,
          GefMessages.ColumnHeaderEditPart_haRight,
          AbstractGridBagLayoutInfo.getImageDescriptor("headers/h/menu/right.gif"),
          ColumnInfo.Alignment.RIGHT));
      manager.add(new SetAlignmentColumnAction(this,
          GefMessages.ColumnHeaderEditPart_haFill,
          AbstractGridBagLayoutInfo.getImageDescriptor("headers/h/menu/fill.gif"),
          ColumnInfo.Alignment.FILL));
    }
    // grow
    {
      manager.add(new Separator());
      manager.add(new SetGrowAction<ColumnInfo>(this,
          GefMessages.ColumnHeaderEditPart_grow,
          AbstractGridBagLayoutInfo.getImageDescriptor("headers/h/menu/grow.gif")));
    }
    // properties
    {
      manager.add(new Separator());
      manager.add(new Action(GefMessages.ColumnHeaderEditPart_properties) {
        @Override
        public void run() {
          editDimension();
        }
      });
    }
  }

  @Override
  protected void editDimension() {
    new ColumnEditDialog(DesignerPlugin.getShell(), m_layout, m_column).open();
  }
}
