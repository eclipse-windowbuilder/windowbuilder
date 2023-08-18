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
package org.eclipse.wb.internal.swing.MigLayout.gef.header.selection;

import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.gef.core.requests.KeyRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.handles.SideResizeHandle;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.gef.graphical.tools.ResizeTracker;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.swing.MigLayout.gef.header.edit.RowHeaderEditPart;
import org.eclipse.wb.internal.swing.MigLayout.model.MigLayoutInfo;
import org.eclipse.wb.internal.swing.MigLayout.model.MigRowInfo;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;

/**
 * Implementation of {@link SelectionEditPolicy} for {@link RowHeaderEditPart}.
 *
 * @author scheglov_ke
 * @coverage swing.MigLayout.header
 */
public final class RowSelectionEditPolicy extends DimensionSelectionEditPolicy<MigRowInfo> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public RowSelectionEditPolicy(LayoutEditPolicy mainPolicy) {
		super(mainPolicy);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Resize
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Handle createResizeHandle() {
		Handle handle = new SideResizeHandle(getHost(), IPositionConstants.BOTTOM, 7, true);
		handle.setDragTrackerTool(new ResizeTracker(getHost(), IPositionConstants.SOUTH, REQ_RESIZE));
		return handle;
	}

	@Override
	protected Point getTextFeedbackLocation(Point mouseLocation) {
		return new Point(10, mouseLocation.y + 10);
	}

	@Override
	protected int getPixelSize(Dimension resizeDelta) {
		return getHostFigure().getSize().height + resizeDelta.height;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Keyboard
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void performRequest(Request request) {
		super.performRequest(request);
		if (request instanceof KeyRequest keyRequest) {
			if (keyRequest.isPressed()) {
				char c = keyRequest.getCharacter();
				// vertical
				if (c == 'd' || c == 'D') {
					setAlignment(MigRowInfo.Alignment.DEFAULT);
				} else if (c == 't') {
					setAlignment(MigRowInfo.Alignment.TOP);
				} else if (c == 'm' || c == 'M' || c == 'c' || c == 'C') {
					setAlignment(MigRowInfo.Alignment.CENTER);
				} else if (c == 'b') {
					setAlignment(MigRowInfo.Alignment.BOTTOM);
				} else if (c == 'f' || c == 'F') {
					setAlignment(MigRowInfo.Alignment.FILL);
				} else if (c == 'a') {
					setAlignment(MigRowInfo.Alignment.BASELINE);
				}
			}
		}
	}

	/**
	 * Sets the alignment for {@link MigRowInfo}.
	 */
	private void setAlignment(final MigRowInfo.Alignment alignment) {
		final MigLayoutInfo layout = getLayout();
		ExecutionUtils.run(layout, new RunnableEx() {
			@Override
			public void run() throws Exception {
				getDimension().setAlignment(alignment);
				layout.writeDimensions();
			}
		});
	}
}
