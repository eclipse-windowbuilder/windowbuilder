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
package org.eclipse.wb.core.gef.policy.layout.grid;

import org.eclipse.wb.core.gef.policy.PolicyUtils;
import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.Polyline;
import org.eclipse.wb.draw2d.RectangleFigure;
import org.eclipse.wb.draw2d.events.IFigureListener;
import org.eclipse.wb.draw2d.geometry.Interval;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.draw2d.geometry.Translatable;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.gef.graphical.policies.GraphicalEditPolicy;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.DrawUtils;

import org.eclipse.swt.graphics.Color;

import java.lang.reflect.Field;

/**
 * Helper for displaying grid for grid-based layouts.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy.grid
 */
public abstract class AbstractGridHelper {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Colors
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final Color BORDER_COLOR = new Color(null, 250, 165, 165);
  private static final Color EXISTING_COLOR = new Color(null, 219, 158, 158);
  private static final Color VIRTUAL_COLOR = new Color(null, 240, 204, 204);
  private static final Color SELECTION_COLOR = DrawUtils.getShiftedColor(
      IColorConstants.lightGray,
      -16);
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final GraphicalEditPolicy m_editPolicy;
  private final Color m_borderColor;
  private final Color m_existingLineColor;
  private final Color m_virtualLineColor;
  private Rectangle hostClientArea;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractGridHelper(GraphicalEditPolicy editPolicy, boolean forTarget) {
    m_editPolicy = editPolicy;
    // colors
    if (forTarget) {
      m_borderColor = BORDER_COLOR;
      m_existingLineColor = EXISTING_COLOR;
      m_virtualLineColor = VIRTUAL_COLOR;
    } else {
      m_borderColor = null;
      m_existingLineColor = SELECTION_COLOR;
      m_virtualLineColor = null;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link IGridInfo} for container.
   */
  protected abstract IGridInfo getGridInfo();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Feedback
  //
  ////////////////////////////////////////////////////////////////////////////
  private Figure m_gridFigure = null;

  private class RootFigureListener implements IFigureListener {
    private Figure m_rootFigure;

    public void figureMoved(Figure source) {
      if (source == m_rootFigure) {
        // correct grid figure bounds according host figure
        prepareHostClientArea();
        translateModelToFeedback(hostClientArea);
        m_gridFigure.setBounds(hostClientArea);
      }
    }

    public void figureReparent(Figure source, Figure oldParent, Figure newParent) {
    }

    void install(Figure rootFigure) {
      if (m_rootFigure != null) {
        uninstall();
      }
      if (rootFigure != null) {
        rootFigure.addFigureListener(this);
        m_rootFigure = rootFigure;
      }
    }

    void uninstall() {
      if (m_rootFigure != null) {
        m_rootFigure.removeFigureListener(this);
        m_rootFigure = null;
      }
    }
  }

  private final RootFigureListener m_rootFigureListener = new RootFigureListener();

  /**
   * Shows the grid feedback.
   */
  public final void showGridFeedback() {
    if (m_gridFigure != null) {
      return;
    }
    // create grid figure
    m_gridFigure = new Figure();
    // install listener on root model figure
    {
      ObjectInfo rootObjectInfo = ((ObjectInfo) getHost().getModel()).getRoot();
      GraphicalEditPart rootEditPart =
          (GraphicalEditPart) getHost().getViewer().getEditPartByModel(rootObjectInfo);
      m_rootFigureListener.install(rootEditPart.getFigure());
    }
    // prepare grid information
    IGridInfo gridInfo = getGridInfo();
    Interval[] columnIntervals = gridInfo.getColumnIntervals();
    Interval[] rowIntervals = gridInfo.getRowIntervals();
    // prepare host information
    prepareHostClientArea();
    hostClientArea.crop(gridInfo.getInsets());
    // add horizontal lines
    {
      int y = hostClientArea.top();
      // add existing lines
      if (columnIntervals.length != 0) {
        int x1 = hostClientArea.left();
        int x2 = columnIntervals[columnIntervals.length - 1].end();
        for (Interval interval : rowIntervals) {
          {
            y = interval.begin;
            addGridLine(x1, y, x2, y, m_existingLineColor);
            addGridLine(x2, y, hostClientArea.right(), y, m_virtualLineColor);
          }
          {
            y = interval.end();
            addGridLine(x1, y, x2, y, m_existingLineColor);
            addGridLine(x2, y, hostClientArea.right(), y, m_virtualLineColor);
          }
        }
      }
      // add virtual lines
      if (gridInfo.hasVirtualRows()) {
        while (true) {
          y += gridInfo.getVirtualRowGap();
          if (y >= hostClientArea.bottom()) {
            break;
          }
          addGridLine(hostClientArea.left(), y, hostClientArea.right(), y, m_virtualLineColor);
          //
          y += gridInfo.getVirtualRowSize();
          if (y >= hostClientArea.bottom()) {
            break;
          }
          addGridLine(hostClientArea.left(), y, hostClientArea.right(), y, m_virtualLineColor);
        }
      }
    }
    // add vertical lines
    {
      int x = hostClientArea.left();
      // add existing lines
      if (rowIntervals.length != 0) {
        int y1 = hostClientArea.top();
        int y2 = rowIntervals[rowIntervals.length - 1].end();
        for (Interval interval : columnIntervals) {
          {
            x = interval.begin;
            addGridLine(x, y1, x, y2, m_existingLineColor);
            addGridLine(x, y2, x, hostClientArea.bottom(), m_virtualLineColor);
          }
          {
            x = interval.end();
            addGridLine(x, y1, x, y2, m_existingLineColor);
            addGridLine(x, y2, x, hostClientArea.bottom(), m_virtualLineColor);
          }
        }
      }
      // add virtual lines
      if (gridInfo.hasVirtualColumns()) {
        while (true) {
          //
          x += gridInfo.getVirtualColumnGap();
          if (x >= hostClientArea.right()) {
            break;
          }
          addGridLine(x, hostClientArea.top(), x, hostClientArea.bottom(), m_virtualLineColor);
          //
          x += gridInfo.getVirtualRowSize();
          if (x >= hostClientArea.right()) {
            break;
          }
          addGridLine(x, hostClientArea.top(), x, hostClientArea.bottom(), m_virtualLineColor);
        }
      }
    }
    // add border around container
    {
      RectangleFigure borderFigure = new RectangleFigure();
      borderFigure.setForeground(m_borderColor);
      borderFigure.setBounds(hostClientArea);
      m_gridFigure.add(borderFigure);
    }
    // add feedback figure
    prepareHostClientArea();
    translateModelToFeedback(hostClientArea);
    m_gridFigure.setBounds(hostClientArea);
    getHost().getViewer().getLayer(IEditPartViewer.HANDLE_LAYER_SUB_2).add(m_gridFigure);
  }

  /**
   * Calculate host client area {@link Rectangle}.
   */
  private void prepareHostClientArea() {
    IAbstractComponentInfo containerInfo = (IAbstractComponentInfo) getHost().getModel();
    hostClientArea = getHost().getFigure().getBounds().getCopy();
    hostClientArea.crop(containerInfo.getClientAreaInsets());
    hostClientArea.x = hostClientArea.y = 0;
  }

  /**
   * Adds single line - part of grid to display all cells.<br>
   * Coordinates should be parent-relative (model).<br>
   * Begin point is inclusive, end point is exclusive.
   */
  private void addGridLine(int x1, int y1, int x2, int y2, Color color) {
    if (color != null) {
      Polyline line = new Polyline();
      line.setForeground(color);
      // prepare points
      Point p1 = new Point(x1, y1);
      Point p2 = new Point(x2, y2);
      if (getGridInfo().isRTL()) {
        p1.x = hostClientArea.width - p1.x;
        p2.x = hostClientArea.width - p2.x;
      }
      // end points are exclusive
      if (x1 == x2) {
        p2.y--;
      }
      if (y1 == y2) {
        p2.x--;
      }
      // add points
      line.addPoint(p1);
      line.addPoint(p2);
      // add line
      m_gridFigure.add(line);
    }
  }

  /**
   * Erases the grid feedback.
   */
  public final void eraseGridFeedback() {
    if (m_gridFigure != null) {
      m_rootFigureListener.uninstall();
      FigureUtils.removeFigure(m_gridFigure);
      m_gridFigure = null;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the layout model for this {@link EditPolicy}.
   */
  protected final Object getAbstractLayout() {
    try {
      Field field = m_editPolicy.getClass().getDeclaredField("m_layout");
      field.setAccessible(true);
      return field.get(m_editPolicy);
    } catch (Throwable e) {
      throw ReflectionUtils.propagate(e);
    }
  }

  /**
   * @return the host {@link EditPart}.
   */
  protected GraphicalEditPart getHost() {
    if (m_editPolicy instanceof LayoutEditPolicy) {
      return m_editPolicy.getHost();
    } else {
      return (GraphicalEditPart) m_editPolicy.getHost().getParent();
    }
  }

  /**
   * Translates given {@link Translatable} from model coordinates into feedback layer coordinates.
   */
  protected void translateModelToFeedback(Translatable t) {
    if (m_editPolicy instanceof LayoutEditPolicy) {
      PolicyUtils.translateModelToFeedback((LayoutEditPolicy) m_editPolicy, t);
    } else {
      PolicyUtils.translateModelToFeedback((SelectionEditPolicy) m_editPolicy, t);
    }
  }
}
