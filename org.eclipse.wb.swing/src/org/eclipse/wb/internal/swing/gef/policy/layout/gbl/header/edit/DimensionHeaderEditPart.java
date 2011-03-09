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
import org.eclipse.wb.internal.swing.model.layout.gbl.AbstractGridBagLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.DimensionInfo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

/**
 * {@link EditPart} for column/row header of {@link AbstractGridBagLayoutInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.gef.policy
 */
public abstract class DimensionHeaderEditPart<T extends DimensionInfo> extends GraphicalEditPart
    implements
      IHeaderMenuProvider {
  protected static final Font DEFAULT_FONT = new Font(null, "Arial", 7, SWT.NONE);
  protected static final Color COLOR_NORMAL = Headers.COLOR_HEADER;
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final AbstractGridBagLayoutInfo m_layout;
  protected final T m_dimension;
  private final Figure m_containerFigure;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DimensionHeaderEditPart(AbstractGridBagLayoutInfo layout,
      T dimension,
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
   * @return the index of this {@link DimensionInfo}.
   */
  public abstract int getIndex();

  /**
   * @return the host {@link AbstractGridBagLayoutInfo}.
   */
  public final AbstractGridBagLayoutInfo getLayout() {
    return m_layout;
  }

  /**
   * @return the {@link DimensionInfo} model.
   */
  public final T getDimension() {
    return m_dimension;
  }

  /**
   * @return the offset of {@link Figure} with headers relative to the absolute layer.
   */
  public final Point getOffset() {
    Point offset = new Point(0, 0);
    FigureUtils.translateFigureToAbsolute2(m_containerFigure, offset);
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
  // Edit
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void performRequest(Request request) {
    super.performRequest(request);
    if (request.getType() == Request.REQ_OPEN) {
      editDimension();
    }
  }

  /**
   * Opens the {@link DimensionInfo} edit dialog.
   */
  protected abstract void editDimension();
}
