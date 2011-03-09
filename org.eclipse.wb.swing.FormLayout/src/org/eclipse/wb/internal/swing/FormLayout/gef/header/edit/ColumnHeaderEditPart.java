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
import org.eclipse.wb.internal.swing.FormLayout.gef.header.actions.DimensionHeaderAction;
import org.eclipse.wb.internal.swing.FormLayout.gef.header.actions.SetAlignmentAction;
import org.eclipse.wb.internal.swing.FormLayout.gef.header.actions.SetGrowAction;
import org.eclipse.wb.internal.swing.FormLayout.model.FormColumnInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.FormLayoutInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.ui.ColumnEditDialog;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.graphics.Image;

import com.jgoodies.forms.layout.ColumnSpec;

import java.util.List;

/**
 * {@link EditPart} for {@link FormColumnInfo} header of {@link FormLayoutInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.FormLayout.header
 */
public class ColumnHeaderEditPart extends DimensionHeaderEditPart<FormColumnInfo> {
  private final FormColumnInfo m_column;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ColumnHeaderEditPart(FormLayoutInfo layout, FormColumnInfo column, Figure containerFigure) {
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
          String title = "" + (1 + getIndex());
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
          Image image;
          if (m_column.getAlignment() == ColumnSpec.LEFT) {
            image = getImage("left.gif");
          } else if (m_column.getAlignment() == ColumnSpec.RIGHT) {
            image = getImage("right.gif");
          } else if (m_column.getAlignment() == ColumnSpec.CENTER) {
            image = getImage("center.gif");
          } else {
            image = getImage("fill.gif");
          }
          //
          int x = r.x + 2;
          drawCentered(graphics, image, x);
        }
        // draw grow indicator
        if (m_column.hasGrow()) {
          if (titleRight + 3 + 7 + 3 < r.right()) {
            Image image = getImage("grow.gif");
            drawCentered(graphics, image, r.right() - 3 - image.getBounds().width);
          }
        }
      }

      private Image getImage(String name) {
        return ColumnHeaderEditPart.this.getImage("alignment/h/" + name);
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
    if (!m_layout.canChangeDimensions()) {
      return;
    }
    // operations
    {
      manager.add(new DimensionHeaderAction<FormColumnInfo>(this, "Insert Column") {
        @Override
        protected void run(FormColumnInfo dimension) throws Exception {
          int index = m_layout.getColumns().indexOf(dimension);
          m_layout.insertColumn(index);
        }
      });
      manager.add(new DimensionHeaderAction<FormColumnInfo>(this, "Append Column") {
        @Override
        protected void run(FormColumnInfo dimension) throws Exception {
          int index = m_layout.getColumns().indexOf(dimension);
          m_layout.insertColumn(index + 1);
        }
      });
      manager.add(new DimensionHeaderAction<FormColumnInfo>(this, "Delete Column") {
        @Override
        protected void run(FormColumnInfo dimension) throws Exception {
          int index = m_layout.getColumns().indexOf(dimension);
          m_layout.deleteColumn(index);
        }
      });
      manager.add(new DimensionHeaderAction<FormColumnInfo>(this, "Delete Contents") {
        @Override
        protected void run(FormColumnInfo dimension) throws Exception {
          int index = m_layout.getColumns().indexOf(dimension);
          m_layout.deleteColumnContents(index);
        }
      });
      manager.add(new DimensionHeaderAction<FormColumnInfo>(this, "Split Column") {
        @Override
        protected void run(FormColumnInfo dimension) throws Exception {
          int index = m_layout.getColumns().indexOf(dimension);
          m_layout.splitColumn(index);
        }
      });
    }
    // alignment
    {
      manager.add(new Separator());
      manager.add(new SetAlignmentAction<FormColumnInfo>(this,
          "Left",
          Activator.getImageDescriptor("alignment/h/menu/left.gif"),
          ColumnSpec.LEFT));
      manager.add(new SetAlignmentAction<FormColumnInfo>(this,
          "Fill",
          Activator.getImageDescriptor("alignment/h/menu/fill.gif"),
          ColumnSpec.FILL));
      manager.add(new SetAlignmentAction<FormColumnInfo>(this,
          "Center",
          Activator.getImageDescriptor("alignment/h/menu/center.gif"),
          ColumnSpec.CENTER));
      manager.add(new SetAlignmentAction<FormColumnInfo>(this,
          "Right",
          Activator.getImageDescriptor("alignment/h/menu/right.gif"),
          ColumnSpec.RIGHT));
    }
    // grow
    {
      manager.add(new Separator());
      manager.add(new SetGrowAction<FormColumnInfo>(this,
          "Grow",
          Activator.getImageDescriptor("alignment/h/menu/grow.gif")));
    }
    // templates
    {
      manager.add(new Separator());
      addTemplateActions(manager, m_dimension.getTemplates(true));
      {
        IMenuManager otherManager = new MenuManager("Other Templates");
        manager.add(otherManager);
        addTemplateActions(otherManager, m_dimension.getTemplates(false));
      }
    }
    // group
    {
      manager.add(new Separator());
      {
        DimensionHeaderAction<FormColumnInfo> action =
            new DimensionHeaderAction<FormColumnInfo>(this, "Group") {
              @Override
              protected void run(List<FormColumnInfo> dimensions) throws Exception {
                m_layout.groupColumns(dimensions);
              }
            };
        action.setEnabled(getViewer().getSelectedEditParts().size() >= 2);
        manager.add(action);
      }
      {
        DimensionHeaderAction<FormColumnInfo> action =
            new DimensionHeaderAction<FormColumnInfo>(this, "UnGroup") {
              @Override
              protected void run(List<FormColumnInfo> dimensions) throws Exception {
                m_layout.unGroupColumns(dimensions);
              }
            };
        manager.add(action);
        // check if there is grouped dimension selected
        boolean hasGroup = false;
        for (EditPart editPart : getViewer().getSelectedEditParts()) {
          ColumnHeaderEditPart headerEditPart = (ColumnHeaderEditPart) editPart;
          if (m_layout.getColumnGroup(headerEditPart.m_column) != null) {
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
      manager.add(new ObjectInfoAction(m_layout, "Properties...") {
        @Override
        protected void runEx() throws Exception {
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
