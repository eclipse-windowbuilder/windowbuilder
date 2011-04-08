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
package org.eclipse.wb.internal.swing.gef.policy.layout.gbl.header.selection;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.handles.SideResizeHandle;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.gef.graphical.tools.ResizeTracker;
import org.eclipse.wb.internal.swing.gef.GefMessages;
import org.eclipse.wb.internal.swing.gef.policy.layout.gbl.header.edit.ColumnHeaderEditPart;
import org.eclipse.wb.internal.swing.model.layout.gbl.ColumnInfo;

import java.text.MessageFormat;

/**
 * Implementation of {@link SelectionEditPolicy} for {@link ColumnHeaderEditPart}.
 * 
 * @author scheglov_ke
 * @coverage swing.gef.policy
 */
public final class ColumnSelectionEditPolicy extends DimensionSelectionEditPolicy<ColumnInfo> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ColumnSelectionEditPolicy(LayoutEditPolicy mainPolicy) {
    super(mainPolicy);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Resize
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Handle createResizeHandle() {
    Handle handle = new SideResizeHandle(getHost(), IPositionConstants.RIGHT, 7, false);
    handle.setDragTrackerTool(new ResizeTracker(getHost(), IPositionConstants.EAST, REQ_RESIZE));
    return handle;
  }

  @Override
  protected Point getTextFeedbackLocation(Point mouseLocation) {
    return new Point(mouseLocation.x + 10, 10);
  }

  @Override
  protected String getFeedbackText(ChangeBoundsRequest request) {
    int pixels = getDimensionSize(getLayout().getGridInfo().getColumnIntervals());
    int pixelsDelta = request.getSizeDelta().width;
    final int newPixels = pixels + pixelsDelta;
    // prepare command
    m_resizeCommand = new EditCommand(getLayout()) {
      @Override
      protected void executeEdit() throws Exception {
        getDimension().setSize(newPixels);
      }
    };
    // return text
    String deltaText = pixelsDelta > 0 ? "+" + pixelsDelta : "" + pixelsDelta;
    return MessageFormat.format(
        GefMessages.ColumnSelectionEditPolicy_feedbackPattern,
        newPixels,
        deltaText,
        pixels);
  }
}
