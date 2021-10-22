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

import org.eclipse.wb.core.gef.policy.PolicyUtils;
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
import org.eclipse.wb.internal.swt.model.layout.grid.GridColumnInfo;
import org.eclipse.wb.internal.swt.model.layout.grid.GridDimensionInfo;
import org.eclipse.wb.internal.swt.model.layout.grid.GridImages;
import org.eclipse.wb.internal.swt.model.layout.grid.IGridLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;

/**
 * {@link EditPart} for {@link GridColumnInfo} header of {@link IGridLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage swt.gef.GridLayout
 */
public final class ColumnHeaderEditPart<C extends IControlInfo> extends DimensionHeaderEditPart<C> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ColumnHeaderEditPart(IGridLayoutInfo<C> layout,
      GridColumnInfo<C> column,
      Figure containerFigure) {
    super(layout, column, containerFigure);
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
        graphics.drawLine(r.x, r.y, r.x, r.bottom());
        graphics.drawLine(r.right() - 1, r.y, r.right() - 1, r.bottom());
        // draw column index
        int titleLeft;
        int titleRight;
        {
          String title = "" + (1 + m_dimension.getIndex());
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
        //
        try {
          // draw alignment indicator
          {
            Integer alignmentValue = m_dimension.getAlignment();
            if (alignmentValue != null && titleLeft - r.x > 3 + 7 + 3) {
              int alignment = alignmentValue.intValue();
              Image image;
              if (alignment == SWT.LEFT) {
                image = getImage("left.gif");
              } else if (alignment == SWT.CENTER) {
                image = getImage("center.gif");
              } else if (alignment == SWT.RIGHT) {
                image = getImage("right.gif");
              } else {
                image = getImage("fill.gif");
              }
              //
              int x = r.x + 2;
              drawCentered(graphics, image, x);
            }
          }
          // draw grow indicator
          if (m_dimension.getGrab()) {
            if (titleRight + 3 + 7 + 3 < r.right()) {
              Image image = getImage("grow.gif");
              drawCentered(graphics, image, r.right() - 3 - image.getBounds().width);
            }
          }
        } catch (Throwable e) {
          e.printStackTrace();
        }
      }

      private Image getImage(String name) {
        return GridImages.getImage("h/" + name);
      }

      private void drawCentered(Graphics graphics, Image image, int x) {
        int y = (getBounds().height - image.getBounds().height) / 2;
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
      interval = gridInfo.getColumnIntervals()[index];
    }
    // prepare bounds
    Rectangle bounds;
    {
      bounds =
          new Rectangle(interval.begin,
              0,
              interval.length + 1,
              ((GraphicalEditPart) getParent()).getFigure().getSize().height);
      translateModelToFeedback(bounds);
    }
    // set bounds
    getFigure().setBounds(bounds);
  }

  /**
   * Converts "model" {@link Rectangle} into feedback coordinates.
   */
  public void translateModelToFeedback(Rectangle r) {
    PolicyUtils.modelToFeedback_rightToLeft(r, m_layout.getComposite());
    r.translate(getOffset().x, 0);
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
          GefMessages.ColumnHeaderEditPart_grabExcessSpace,
          GridImages.getImageDescriptor("h/menu/grow.gif")));
    }
    // alignment
    {
      manager.add(new Separator());
      manager.add(new SetAlignmentAction<C>(this,
          GefMessages.ColumnHeaderEditPart_left,
          GridImages.getImageDescriptor("h/menu/left.gif"),
          SWT.LEFT));
      manager.add(new SetAlignmentAction<C>(this,
          GefMessages.ColumnHeaderEditPart_center,
          GridImages.getImageDescriptor("h/menu/center.gif"),
          SWT.CENTER));
      manager.add(new SetAlignmentAction<C>(this,
          GefMessages.ColumnHeaderEditPart_right,
          GridImages.getImageDescriptor("h/menu/right.gif"),
          SWT.RIGHT));
      manager.add(new SetAlignmentAction<C>(this,
          GefMessages.ColumnHeaderEditPart_fill,
          GridImages.getImageDescriptor("h/menu/fill.gif"),
          SWT.FILL));
    }
    // operations
    {
      manager.add(new Separator());
      manager.add(new DimensionHeaderAction<C>(this, GefMessages.ColumnHeaderEditPart_delete,
          GridImages.getImageDescriptor("h/menu/delete.gif")) {
        @Override
        protected void run(GridDimensionInfo<C> dimension) throws Exception {
          m_layout.command_deleteColumn(dimension.getIndex(), true);
        }
      });
    }
  }
}
