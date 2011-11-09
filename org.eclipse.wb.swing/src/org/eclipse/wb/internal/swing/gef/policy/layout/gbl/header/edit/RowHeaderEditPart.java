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
import org.eclipse.wb.internal.swing.gef.policy.layout.gbl.header.actions.SetAlignmentRowAction;
import org.eclipse.wb.internal.swing.gef.policy.layout.gbl.header.actions.SetGrowAction;
import org.eclipse.wb.internal.swing.model.layout.gbl.AbstractGridBagLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.RowInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.RowInfo.Alignment;
import org.eclipse.wb.internal.swing.model.layout.gbl.ui.RowEditDialog;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.graphics.Image;

import org.apache.commons.lang.SystemUtils;

/**
 * {@link EditPart} for {@link RowInfo} header of {@link AbstractGridBagLayoutInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.gef.policy
 */
public final class RowHeaderEditPart extends DimensionHeaderEditPart<RowInfo> {
  private final RowInfo m_row;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RowHeaderEditPart(AbstractGridBagLayoutInfo layout, RowInfo row, Figure containerFigure) {
    super(layout, row, containerFigure);
    m_row = row;
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
        graphics.drawLine(r.x, r.y, r.right(), r.y);
        graphics.drawLine(r.x, r.bottom() - 1, r.right(), r.bottom() - 1);
        // draw row index
        int titleTop;
        int titleBottom;
        {
          String title = "" + getIndex();
          Dimension textExtents = graphics.getTextExtent(title);
          if (r.height < textExtents.height) {
            return;
          }
          titleTop = r.y + (r.height - textExtents.height) / 2;
          titleBottom = titleTop + textExtents.height;
          int x = r.x + (r.width - textExtents.width) / 2;
          graphics.setForegroundColor(IColorConstants.black);
          graphics.drawText(title, x, titleTop);
        }
        // draw alignment indicator
        if (titleTop - r.y > 3 + 7 + 3) {
          Image image = null;
          Alignment alignment = m_dimension.getAlignment();
          if (alignment == RowInfo.Alignment.TOP) {
            image = getImage("top.gif");
          } else if (alignment == RowInfo.Alignment.BOTTOM) {
            image = getImage("bottom.gif");
          } else if (alignment == RowInfo.Alignment.CENTER) {
            image = getImage("center.gif");
          } else if (alignment == RowInfo.Alignment.FILL) {
            image = getImage("fill.gif");
          } else if (alignment == RowInfo.Alignment.BASELINE) {
            image = getImage("baseline.gif");
          } else if (alignment == RowInfo.Alignment.BASELINE_ABOVE) {
            image = getImage("baseline_above.gif");
          } else if (alignment == RowInfo.Alignment.BASELINE_BELOW) {
            image = getImage("baseline_below.gif");
          }
          if (image != null) {
            int y = r.y + 2;
            drawCentered(graphics, image, y);
          }
        }
        // draw grow indicator
        if (m_dimension.hasWeight()) {
          if (titleBottom + 3 + 7 + 3 < r.bottom()) {
            Image image = getImage("grow.gif");
            drawCentered(graphics, image, r.bottom() - 3 - image.getBounds().height);
          }
        }
      }

      private Image getImage(String name) {
        return AbstractGridBagLayoutInfo.getImage("headers/v/alignment/" + name);
      }

      private void drawCentered(Graphics graphics, Image image, int y) {
        int x = (getBounds().width - image.getBounds().width) / 2;
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
    Interval interval = m_layout.getGridInfo().getRowIntervals()[index];
    Rectangle bounds =
        new Rectangle(0,
            interval.begin,
            ((GraphicalEditPart) getParent()).getFigure().getSize().width,
            interval.length + 1);
    bounds.translate(0, getOffset().y);
    getFigure().setBounds(bounds);
  }

  @Override
  public int getIndex() {
    return m_layout.getRows().indexOf(m_row);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IHeaderMenuProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public void buildContextMenu(IMenuManager manager) {
    // operations
    {
      manager.add(new DimensionHeaderAction<RowInfo>(this, GefMessages.RowHeaderEditPart_insertRow) {
        @Override
        protected void run(RowInfo row) throws Exception {
          m_layout.getRowOperations().insert(row.getIndex());
        }
      });
      manager.add(new DimensionHeaderAction<RowInfo>(this, GefMessages.RowHeaderEditPart_appendRow) {
        @Override
        protected void run(RowInfo row) throws Exception {
          m_layout.getRowOperations().insert(row.getIndex() + 1);
        }
      });
      manager.add(new DimensionHeaderAction<RowInfo>(this, GefMessages.RowHeaderEditPart_deleteRow,
          AbstractGridBagLayoutInfo.getImageDescriptor("headers/v/menu/delete.gif")) {
        @Override
        protected void run(RowInfo row) throws Exception {
          m_layout.getRowOperations().delete(row.getIndex());
        }
      });
      manager.add(new DimensionHeaderAction<RowInfo>(this,
          GefMessages.RowHeaderEditPart_deleteContents) {
        @Override
        protected void run(RowInfo row) throws Exception {
          m_layout.getRowOperations().clear(row.getIndex());
        }
      });
      manager.add(new DimensionHeaderAction<RowInfo>(this, GefMessages.RowHeaderEditPart_splitRow) {
        @Override
        protected void run(RowInfo row) throws Exception {
          m_layout.getRowOperations().split(row.getIndex());
        }
      });
    }
    // alignment
    {
      manager.add(new Separator());
      manager.add(new SetAlignmentRowAction(this,
          GefMessages.RowHeaderEditPart_vaTop,
          AbstractGridBagLayoutInfo.getImageDescriptor("headers/v/menu/top.gif"),
          RowInfo.Alignment.TOP));
      manager.add(new SetAlignmentRowAction(this,
          GefMessages.RowHeaderEditPart_vaCenter,
          AbstractGridBagLayoutInfo.getImageDescriptor("headers/v/menu/center.gif"),
          RowInfo.Alignment.CENTER));
      manager.add(new SetAlignmentRowAction(this,
          GefMessages.RowHeaderEditPart_vaBottom,
          AbstractGridBagLayoutInfo.getImageDescriptor("headers/v/menu/bottom.gif"),
          RowInfo.Alignment.BOTTOM));
      manager.add(new SetAlignmentRowAction(this,
          GefMessages.RowHeaderEditPart_vaFill,
          AbstractGridBagLayoutInfo.getImageDescriptor("headers/v/menu/fill.gif"),
          RowInfo.Alignment.FILL));
      if (SystemUtils.IS_JAVA_1_6 || SystemUtils.IS_JAVA_1_7) {
        manager.add(new SetAlignmentRowAction(this,
            GefMessages.RowHeaderEditPart_vaBaseline,
            AbstractGridBagLayoutInfo.getImageDescriptor("headers/v/menu/baseline.gif"),
            RowInfo.Alignment.BASELINE));
        manager.add(new SetAlignmentRowAction(this,
            GefMessages.RowHeaderEditPart_vaAboveBaseline,
            AbstractGridBagLayoutInfo.getImageDescriptor("headers/v/menu/baseline_above.gif"),
            RowInfo.Alignment.BASELINE_ABOVE));
        manager.add(new SetAlignmentRowAction(this,
            GefMessages.RowHeaderEditPart_vaBelowBaseline,
            AbstractGridBagLayoutInfo.getImageDescriptor("headers/v/menu/baseline_below.gif"),
            RowInfo.Alignment.BASELINE_BELOW));
      }
    }
    // grow
    {
      manager.add(new Separator());
      manager.add(new SetGrowAction<RowInfo>(this,
          GefMessages.RowHeaderEditPart_grow,
          AbstractGridBagLayoutInfo.getImageDescriptor("headers/v/menu/grow.gif")));
    }
    // properties
    {
      manager.add(new Separator());
      manager.add(new Action(GefMessages.RowHeaderEditPart_properties) {
        @Override
        public void run() {
          editDimension();
        }
      });
    }
  }

  @Override
  protected void editDimension() {
    new RowEditDialog(DesignerPlugin.getShell(), m_layout, m_row).open();
  }
}
