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

import org.eclipse.wb.core.gef.header.Headers;
import org.eclipse.wb.core.gef.header.IHeaderMenuProvider;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.core.tools.ParentTargetDragEditPartTracker;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.swt.model.layout.grid.GridDimensionInfo;
import org.eclipse.wb.internal.swt.model.layout.grid.IGridLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

/**
 * {@link EditPart} for column/row header of {@link IGridLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage swt.gef.GridLayout
 */
public abstract class DimensionHeaderEditPart<C extends IControlInfo> extends GraphicalEditPart
    implements
      IHeaderMenuProvider {
  protected static final Color COLOR_NORMAL = Headers.COLOR_HEADER;
  protected static final Font DEFAULT_FONT = new Font(null, "Arial", 7, SWT.NONE);
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final IGridLayoutInfo<C> m_layout;
  protected final GridDimensionInfo<C> m_dimension;
  private final Figure m_containerFigure;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DimensionHeaderEditPart(IGridLayoutInfo<C> layout,
      GridDimensionInfo<C> dimension,
      Figure containerFigure) {
    m_layout = layout;
    m_dimension = dimension;
    m_containerFigure = containerFigure;
    setModel(dimension);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the host {@link IGridLayoutInfo}.
   */
  public final IGridLayoutInfo<C> getLayout() {
    return m_layout;
  }

  /**
   * @return the {@link GridDimensionInfo} model.
   */
  public final GridDimensionInfo<C> getDimension() {
    return m_dimension;
  }

  /**
   * @return the offset of {@link Figure} with headers relative to the absolute layer.
   */
  public final Point getOffset() {
    Point offset = new Point(0, 0);
    FigureUtils.translateFigureToAbsolute2(m_containerFigure, offset);
    offset.translate(m_layout.getComposite().getClientAreaInsets());
    return offset;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dragging
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final Tool getDragTrackerTool(Request request) {
    return new ParentTargetDragEditPartTracker(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Figure support
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refreshVisuals() {
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        getFigure().setToolTipText(m_dimension.getTitle());
        getFigure().setBackground(COLOR_NORMAL);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Edit
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void performRequest(Request request) {
    super.performRequest(request);
    if (request.getType() == Request.REQ_OPEN) {
      ExecutionUtils.run(m_layout.getUnderlyingModel(), new RunnableEx() {
        public void run() throws Exception {
          m_dimension.flipGrab();
        }
      });
    }
  }
}
