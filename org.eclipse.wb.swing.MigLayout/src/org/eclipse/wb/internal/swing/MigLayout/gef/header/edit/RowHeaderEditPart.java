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
package org.eclipse.wb.internal.swing.MigLayout.gef.header.edit;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.Graphics;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.swing.MigLayout.Activator;
import org.eclipse.wb.internal.swing.MigLayout.gef.GefMessages;
import org.eclipse.wb.internal.swing.MigLayout.gef.header.actions.DimensionHeaderAction;
import org.eclipse.wb.internal.swing.MigLayout.gef.header.actions.SetAlignmentRowAction;
import org.eclipse.wb.internal.swing.MigLayout.gef.header.actions.SetGrowAction;
import org.eclipse.wb.internal.swing.MigLayout.gef.header.actions.SetSizeAction;
import org.eclipse.wb.internal.swing.MigLayout.model.MigLayoutInfo;
import org.eclipse.wb.internal.swing.MigLayout.model.MigRowInfo;
import org.eclipse.wb.internal.swing.MigLayout.model.ui.RowEditDialog;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Interval;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.graphics.Image;

/**
 * {@link EditPart} for {@link MigRowInfo} header of {@link MigLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.MigLayout.header
 */
public class RowHeaderEditPart extends DimensionHeaderEditPart<MigRowInfo> {
  private static final String GROW_SMALL_PATH = "alignment/v/small/grow.gif";
  private static final String GROW_MENU_PATH = "alignment/v/menu/grow.gif";
  private static final String PREF_TITLE = "[pref!]";
  private static final String PREF_CODE = "pref!";
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final MigRowInfo m_row;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RowHeaderEditPart(MigLayoutInfo layout, MigRowInfo row, Figure containerFigure) {
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
          int index = getIndex();
          String title = Integer.toString(index);
          Dimension textExtents = graphics.getTextExtent(title);
          if (r.height < textExtents.height) {
            return;
          }
          // draw title
          titleTop = r.y + (r.height - textExtents.height) / 2;
          titleBottom = titleTop + textExtents.height;
          int x = r.x + (r.width - textExtents.width) / 2;
          graphics.setForegroundColor(IColorConstants.black);
          graphics.drawText(title, x, titleTop);
        }
        // draw alignment indicator
        if (titleTop - r.y > 3 + 7 + 3) {
          Image image = m_row.getAlignment(true).getSmallImage();
          int y = r.y + 2;
          drawCentered(graphics, image, y);
        }
        // draw grow indicator
        if (m_dimension.hasGrow()) {
          if (titleBottom + 3 + 7 + 3 < r.bottom()) {
            Image image = getImage(GROW_SMALL_PATH);
            drawCentered(graphics, image, r.bottom() - 3 - image.getBounds().height);
          }
        }
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
    Figure figure = getFigure();
    // bounds
    {
      int index = getIndex();
      Interval interval = m_layout.getGridInfo().getRowIntervals()[index];
      Rectangle bounds =
          new Rectangle(0,
              interval.begin(),
              ((GraphicalEditPart) getParent()).getFigure().getSize().width,
              interval.length()+ 1);
      bounds.performTranslate(0, getOffset().y);
      figure.setBounds(bounds);
    }
    // tooltip
    figure.setToolTipText(m_row.getTooltip());
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
    if (!m_layout.canChangeDimensions()) {
      return;
    }
    // operations
    {
      manager.add(new DimensionHeaderAction<MigRowInfo>(this,
          GefMessages.RowHeaderEditPart_insertRow) {
        @Override
        protected void run(MigRowInfo dimension, int index) throws Exception {
          m_layout.insertRow(index);
        }
      });
      manager.add(new DimensionHeaderAction<MigRowInfo>(this,
          GefMessages.RowHeaderEditPart_appendRow) {
        @Override
        protected void run(MigRowInfo dimension, int index) throws Exception {
          m_layout.insertRow(index + 1);
        }
      });
      manager.add(new DimensionHeaderAction<MigRowInfo>(this,
          GefMessages.RowHeaderEditPart_deleteRow) {
        @Override
        protected void run(MigRowInfo dimension, int index) throws Exception {
          m_layout.deleteRow(index);
        }
      });
      manager.add(new DimensionHeaderAction<MigRowInfo>(this,
          GefMessages.RowHeaderEditPart_clearRow) {
        @Override
        protected void run(MigRowInfo dimension, int index) throws Exception {
          m_layout.clearRow(index);
        }
      });
      manager.add(new DimensionHeaderAction<MigRowInfo>(this,
          GefMessages.RowHeaderEditPart_splitRow) {
        @Override
        protected void run(MigRowInfo dimension, int index) throws Exception {
          m_layout.splitRow(index);
        }
      });
    }
    // alignment
    {
      manager.add(new Separator());
      manager.add(new SetAlignmentRowAction(this, MigRowInfo.Alignment.DEFAULT));
      manager.add(new SetAlignmentRowAction(this, MigRowInfo.Alignment.TOP));
      manager.add(new SetAlignmentRowAction(this, MigRowInfo.Alignment.CENTER));
      manager.add(new SetAlignmentRowAction(this, MigRowInfo.Alignment.BOTTOM));
      manager.add(new SetAlignmentRowAction(this, MigRowInfo.Alignment.FILL));
      manager.add(new SetAlignmentRowAction(this, MigRowInfo.Alignment.BASELINE));
    }
    // grow
    {
      manager.add(new Separator());
      manager.add(new SetGrowAction<MigRowInfo>(this,
          GefMessages.RowHeaderEditPart_grow,
          Activator.getImageDescriptor(GROW_MENU_PATH)));
    }
    // size
    {
      manager.add(new Separator());
      manager.add(new SetSizeAction<MigRowInfo>(this,
          GefMessages.RowHeaderEditPart_defaultSize,
          null));
      manager.add(new SetSizeAction<MigRowInfo>(this, PREF_TITLE, PREF_CODE));
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
