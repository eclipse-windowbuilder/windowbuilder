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
package org.eclipse.wb.internal.swing.FormLayout.gef.header.edit;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.Graphics;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Interval;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.swing.FormLayout.Activator;
import org.eclipse.wb.internal.swing.FormLayout.gef.GefMessages;
import org.eclipse.wb.internal.swing.FormLayout.gef.header.actions.DimensionHeaderAction;
import org.eclipse.wb.internal.swing.FormLayout.gef.header.actions.SetAlignmentAction;
import org.eclipse.wb.internal.swing.FormLayout.gef.header.actions.SetGrowAction;
import org.eclipse.wb.internal.swing.FormLayout.model.FormLayoutInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.FormRowInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.ui.RowEditDialog;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.graphics.Image;

import com.jgoodies.forms.layout.RowSpec;

import java.util.List;

/**
 * {@link EditPart} for {@link FormRowInfo} header of {@link FormLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.FormLayout.header
 */
public class RowHeaderEditPart extends DimensionHeaderEditPart<FormRowInfo> {
  private final FormRowInfo m_row;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RowHeaderEditPart(FormLayoutInfo layout, FormRowInfo row, Figure containerFigure) {
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
          String title = "" + (1 + getIndex());
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
          Image image;
          if (m_dimension.getAlignment() == RowSpec.TOP) {
            image = getImage("top.gif");
          } else if (m_dimension.getAlignment() == RowSpec.BOTTOM) {
            image = getImage("bottom.gif");
          } else if (m_dimension.getAlignment() == RowSpec.CENTER) {
            image = getImage("center.gif");
          } else {
            image = getImage("fill.gif");
          }
          //
          int y = r.y + 2;
          drawCentered(graphics, image, y);
        }
        // draw grow indicator
        if (m_dimension.hasGrow()) {
          if (titleBottom + 3 + 7 + 3 < r.bottom()) {
            Image image = getImage("grow.gif");
            drawCentered(graphics, image, r.bottom() - 3 - image.getBounds().height);
          }
        }
      }

      private Image getImage(String name) {
        return RowHeaderEditPart.this.getImage("alignment/v/" + name);
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
  @Override
  public void buildContextMenu(IMenuManager manager) {
    if (!m_layout.canChangeDimensions()) {
      return;
    }
    // operations
    {
      manager.add(new DimensionHeaderAction<FormRowInfo>(this,
          GefMessages.RowHeaderEditPart_insertRow) {
        @Override
        protected void run(FormRowInfo dimension) throws Exception {
          int index = m_layout.getRows().indexOf(dimension);
          m_layout.insertRow(index);
        }
      });
      manager.add(new DimensionHeaderAction<FormRowInfo>(this,
          GefMessages.RowHeaderEditPart_appendRow) {
        @Override
        protected void run(FormRowInfo dimension) throws Exception {
          int index = m_layout.getRows().indexOf(dimension);
          m_layout.insertRow(index + 1);
        }
      });
      manager.add(new DimensionHeaderAction<FormRowInfo>(this,
          GefMessages.RowHeaderEditPart_deleteRow) {
        @Override
        protected void run(FormRowInfo dimension) throws Exception {
          int index = m_layout.getRows().indexOf(dimension);
          m_layout.deleteRow(index);
        }
      });
      manager.add(new DimensionHeaderAction<FormRowInfo>(this,
          GefMessages.RowHeaderEditPart_deleteContents) {
        @Override
        protected void run(FormRowInfo dimension) throws Exception {
          int index = m_layout.getRows().indexOf(dimension);
          m_layout.deleteRowContents(index);
        }
      });
      manager.add(new DimensionHeaderAction<FormRowInfo>(this,
          GefMessages.RowHeaderEditPart_splitRow) {
        @Override
        protected void run(FormRowInfo dimension) throws Exception {
          int index = m_layout.getRows().indexOf(dimension);
          m_layout.splitRow(index);
        }
      });
    }
    // alignment
    {
      manager.add(new Separator());
      manager.add(new SetAlignmentAction<FormRowInfo>(this,
          GefMessages.RowHeaderEditPart_vaTop,
          Activator.getImageDescriptor("alignment/v/menu/top.gif"),
          RowSpec.TOP));
      manager.add(new SetAlignmentAction<FormRowInfo>(this,
          GefMessages.RowHeaderEditPart_vaCenter,
          Activator.getImageDescriptor("alignment/v/menu/center.gif"),
          RowSpec.CENTER));
      manager.add(new SetAlignmentAction<FormRowInfo>(this,
          GefMessages.RowHeaderEditPart_vaBottom,
          Activator.getImageDescriptor("alignment/v/menu/bottom.gif"),
          RowSpec.BOTTOM));
      manager.add(new SetAlignmentAction<FormRowInfo>(this,
          GefMessages.RowHeaderEditPart_vaFill,
          Activator.getImageDescriptor("alignment/v/menu/fill.gif"),
          RowSpec.FILL));
    }
    // grow
    {
      manager.add(new Separator());
      manager.add(new SetGrowAction<FormRowInfo>(this,
          GefMessages.RowHeaderEditPart_grow,
          Activator.getImageDescriptor("alignment/h/menu/grow.gif")));
    }
    // templates
    {
      manager.add(new Separator());
      addTemplateActions(manager, m_dimension.getTemplates(true));
      {
        IMenuManager otherManager = new MenuManager(GefMessages.RowHeaderEditPart_otherTemplates);
        manager.add(otherManager);
        addTemplateActions(otherManager, m_dimension.getTemplates(false));
      }
    }
    // group
    {
      manager.add(new Separator());
      {
        DimensionHeaderAction<FormRowInfo> action =
            new DimensionHeaderAction<FormRowInfo>(this, GefMessages.RowHeaderEditPart_group) {
              @Override
              protected void run(List<FormRowInfo> dimensions) throws Exception {
                m_layout.groupRows(dimensions);
              }
            };
        action.setEnabled(getViewer().getSelectedEditParts().size() >= 2);
        manager.add(action);
      }
      {
        DimensionHeaderAction<FormRowInfo> action =
            new DimensionHeaderAction<FormRowInfo>(this, GefMessages.RowHeaderEditPart_unGroup) {
              @Override
              protected void run(List<FormRowInfo> dimensions) throws Exception {
                m_layout.unGroupRows(dimensions);
              }
            };
        manager.add(action);
        // check if there is groupped dimension selected
        boolean hasGroup = false;
        for (EditPart editPart : getViewer().getSelectedEditParts()) {
          RowHeaderEditPart headerEditPart = (RowHeaderEditPart) editPart;
          if (m_layout.getRowGroup(headerEditPart.m_row) != null) {
            hasGroup = true;
            break;
          }
        }
        // enable action
        action.setEnabled(hasGroup);
      }
    }
    // properties
    {
      manager.add(new Separator());
      manager.add(new ObjectInfoAction(m_layout, GefMessages.RowHeaderEditPart_properties) {
        @Override
        protected void runEx() throws Exception {
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
