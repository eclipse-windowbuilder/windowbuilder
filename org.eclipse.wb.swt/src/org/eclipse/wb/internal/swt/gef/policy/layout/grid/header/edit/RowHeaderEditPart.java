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
package org.eclipse.wb.internal.swt.gef.policy.layout.grid.header.edit;

import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.Graphics;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Interval;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.swt.gef.GefMessages;
import org.eclipse.wb.internal.swt.gef.policy.layout.grid.header.actions.DimensionHeaderAction;
import org.eclipse.wb.internal.swt.gef.policy.layout.grid.header.actions.SetAlignmentAction;
import org.eclipse.wb.internal.swt.gef.policy.layout.grid.header.actions.SetGrabAction;
import org.eclipse.wb.internal.swt.model.layout.grid.GridDimensionInfo;
import org.eclipse.wb.internal.swt.model.layout.grid.GridImages;
import org.eclipse.wb.internal.swt.model.layout.grid.GridRowInfo;
import org.eclipse.wb.internal.swt.model.layout.grid.IGridLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;

/**
 * {@link EditPart} for {@link GridRowInfo} header of {@link IGridLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage swt.gef.GridLayout
 */
public final class RowHeaderEditPart<C extends IControlInfo> extends DimensionHeaderEditPart<C> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RowHeaderEditPart(IGridLayoutInfo<C> layout, GridRowInfo<C> row, Figure containerFigure) {
    super(layout, row, containerFigure);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Figure
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Figure createFigure() {
    Figure newFigure = new Figure() {
      @Override
      protected void paintClientArea(Graphics graphics) {
        Rectangle r = getClientArea();
        // draw rectangle
        graphics.setForegroundColor(IColorConstants.buttonDarker);
        graphics.drawLine(r.x, r.y, r.right(), r.y);
        graphics.drawLine(r.x, r.bottom() - 1, r.right(), r.bottom() - 1);
        // draw row index
        int titleTop;
        int titleBottom;
        {
          String title = "" + (1 + m_dimension.getIndex());
          Dimension textExtents = graphics.getTextExtent(title);
          if (r.height < 3 + textExtents.height + 3) {
            return;
          }
          // draw title
          titleTop = r.y + (r.height - textExtents.height) / 2;
          titleBottom = titleTop + textExtents.height;
          int x = r.x + (r.width - textExtents.width) / 2;
          graphics.setForegroundColor(IColorConstants.black);
          graphics.drawText(title, x, titleTop);
        }
        //
        try {
          // draw alignment indicator
          {
            Integer alignmentValue = m_dimension.getAlignment();
            if (alignmentValue != null && titleTop - r.y > 3 + 7 + 3) {
              int alignment = alignmentValue.intValue();
              Image image;
              if (alignment == SWT.TOP) {
                image = getImage("top.gif");
              } else if (alignment == SWT.CENTER) {
                image = getImage("center.gif");
              } else if (alignment == SWT.BOTTOM) {
                image = getImage("bottom.gif");
              } else {
                image = getImage("fill.gif");
              }
              //
              int y = r.y + 2;
              drawCentered(graphics, image, y);
            }
          }
          // draw grow indicator
          if (m_dimension.getGrab()) {
            if (titleBottom + 3 + 7 + 3 < r.bottom()) {
              Image image = getImage("grow.gif");
              drawCentered(graphics, image, r.bottom() - 3 - image.getBounds().height);
            }
          }
        } catch (Throwable e) {
          e.printStackTrace();
        }
      }

      private Image getImage(String name) {
        return GridImages.getImage("v/" + name);
      }

      private void drawCentered(Graphics graphics, Image image, int y) {
        int x = (getBounds().width - image.getBounds().width) / 2;
        graphics.drawImage(image, x, y);
      }
    };
    //
    newFigure.setFont(DEFAULT_FONT);
    newFigure.setOpaque(true);
    return newFigure;
  }

  @Override
  protected void refreshVisuals() {
    super.refreshVisuals();
    // prepare column interval
    Interval interval;
    {
      int index = m_dimension.getIndex();
      IGridInfo gridInfo = m_layout.getGridInfo();
      interval = gridInfo.getRowIntervals()[index];
    }
    // prepare bounds
    Rectangle bounds;
    {
      bounds =
          new Rectangle(0,
              interval.begin,
              ((GraphicalEditPart) getParent()).getFigure().getSize().width,
              interval.length + 1);
      bounds.translate(0, getOffset().y);
    }
    // set bounds
    getFigure().setBounds(bounds);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IHeaderMenuProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public void buildContextMenu(IMenuManager manager) {
    // grab
    {
      manager.add(new SetGrabAction<C>(this,
          GefMessages.RowHeaderEditPart_grabExcessSpace,
          GridImages.getImageDescriptor("v/menu/grow.gif")));
    }
    // alignment
    {
      manager.add(new Separator());
      manager.add(new SetAlignmentAction<C>(this,
          GefMessages.RowHeaderEditPart_top,
          GridImages.getImageDescriptor("v/menu/top.gif"),
          SWT.TOP));
      manager.add(new SetAlignmentAction<C>(this,
          GefMessages.RowHeaderEditPart_center,
          GridImages.getImageDescriptor("v/menu/center.gif"),
          SWT.CENTER));
      manager.add(new SetAlignmentAction<C>(this,
          GefMessages.RowHeaderEditPart_bottom,
          GridImages.getImageDescriptor("v/menu/bottom.gif"),
          SWT.BOTTOM));
      manager.add(new SetAlignmentAction<C>(this,
          GefMessages.RowHeaderEditPart_fill,
          GridImages.getImageDescriptor("h/menu/fill.gif"),
          SWT.FILL));
    }
    // operations
    {
      manager.add(new Separator());
      manager.add(new DimensionHeaderAction<C>(this, GefMessages.RowHeaderEditPart_delete,
          GridImages.getImageDescriptor("v/menu/delete.gif")) {
        @Override
        protected void run(GridDimensionInfo<C> dimension) throws Exception {
          m_layout.command_deleteRow(dimension.getIndex(), true);
        }
      });
    }
  }
}
