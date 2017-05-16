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
package org.eclipse.wb.gef.graphical.handles;

import org.eclipse.wb.draw2d.ICursorConstants;
import org.eclipse.wb.draw2d.ILocator;
import org.eclipse.wb.draw2d.border.LineBorder;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.tools.DragEditPartTracker;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;

/**
 * A Handle used for moving {@link EditPart}s.
 *
 * @author lobas_av
 * @coverage gef.graphical
 */
public class MoveHandle extends Handle {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates a handle for the given <code>{@link GraphicalEditPart}</code> using
   * <code>{@link MoveHandleLocator}</code>.
   */
  public MoveHandle(GraphicalEditPart owner) {
    this(owner, new MoveHandleLocator(owner.getFigure()));
  }

  /**
   * Creates a handle for the given <code>{@link EditPart}</code> using the given
   * <code>{@link ILocator}</code>.
   */
  public MoveHandle(GraphicalEditPart owner, ILocator locator) {
    super(owner, locator);
    setBorder(new LineBorder(1));
    setCursor(ICursorConstants.SIZEALL);
    // set drag tracker
    {
      Tool tracker = new DragEditPartTracker(owner);
      tracker.setDefaultCursor(getCursor());
      setDragTrackerTool(tracker);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Figure
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean containsPoint(int x, int y) {
    if (!super.containsPoint(x, y)) {
      return false;
    }
    return !getBounds().getCopy().shrink(2, 2).contains(x, y);
  }
}