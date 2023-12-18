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
package org.eclipse.wb.internal.swing.FormLayout.gef.header.selection;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.handles.SideResizeHandle;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.gef.graphical.tools.ResizeTracker;
import org.eclipse.wb.internal.swing.FormLayout.gef.GefMessages;
import org.eclipse.wb.internal.swing.FormLayout.gef.header.edit.ColumnHeaderEditPart;
import org.eclipse.wb.internal.swing.FormLayout.model.FormColumnInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.FormLayoutInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.FormSizeConstantInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.FormSizeInfo;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Point;

import com.jgoodies.forms.layout.ConstantSize;
import com.jgoodies.forms.layout.ConstantSize.Unit;
import com.jgoodies.forms.layout.Sizes;

import java.text.MessageFormat;

/**
 * Implementation of {@link SelectionEditPolicy} for {@link ColumnHeaderEditPart}.
 *
 * @author scheglov_ke
 * @coverage swing.FormLayout.header
 */
public final class ColumnSelectionEditPolicy extends DimensionSelectionEditPolicy<FormColumnInfo> {
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
		Handle handle = new SideResizeHandle(getHost(), PositionConstants.RIGHT, 7, false);
		handle.setDragTrackerTool(new ResizeTracker(getHost(), PositionConstants.EAST, REQ_RESIZE));
		return handle;
	}

	@Override
	protected Point getTextFeedbackLocation(Point mouseLocation) {
		return new Point(mouseLocation.x + 10, 10);
	}

	@Override
	protected String getTextFeedbackText(ChangeBoundsRequest request, boolean inverse) {
		final FormLayoutInfo layout = getLayout();
		int pixels = getHostFigure().getSize().width;
		int pixelsDelta = request.getSizeDelta().width;
		try {
			FormSizeInfo sizeCopy = getDimension().copy().getSize();
			if (sizeCopy.getComponentSize() == null ^ inverse) {
				// set current size if "null"
				if (sizeCopy.getConstantSize() == null) {
					FormSizeConstantInfo constantSize = new FormSizeConstantInfo(pixels, ConstantSize.PIXEL);
					Unit newUnit =
							sizeCopy.getLowerSize() != null
							? sizeCopy.getLowerSize().getUnit()
									: ConstantSize.DIALOG_UNITS_X;
					constantSize.setUnit(newUnit);
					sizeCopy.setConstantSize(constantSize);
				}
				// update size
				final FormSizeConstantInfo constantSize;
				{
					constantSize = sizeCopy.getConstantSize();
					constantSize.setAsPixels(pixels + pixelsDelta);
				}
				// prepare command
				m_resizeCommand = new EditCommand(layout) {
					@Override
					protected void executeEdit() throws Exception {
						getDimension().getSize().setConstantSize(constantSize);
						layout.writeDimensions();
					}
				};
				// return text
				return MessageFormat.format(
						GefMessages.ColumnSelectionEditPolicy_widthPattern,
						constantSize.getSource(true, true));
			} else {
				// set current size if "null"
				if (sizeCopy.getLowerSize() == null) {
					FormSizeConstantInfo lowerSize = new FormSizeConstantInfo(pixels, ConstantSize.PIXEL);
					Unit newUnit =
							sizeCopy.getConstantSize() != null
							? sizeCopy.getConstantSize().getUnit()
									: ConstantSize.DIALOG_UNITS_X;
					lowerSize.setUnit(newUnit);
					sizeCopy.setLowerSize(lowerSize);
				}
				// update size
				final FormSizeConstantInfo lowerSize;
				{
					lowerSize = sizeCopy.getLowerSize();
					lowerSize.setAsPixels(pixels + pixelsDelta);
				}
				// prepare command
				m_resizeCommand = new EditCommand(layout) {
					@Override
					protected void executeEdit() throws Exception {
						FormSizeInfo size = getDimension().getSize();
						// set component size
						if (size.getComponentSize() == null) {
							size.setComponentSize(Sizes.DEFAULT);
						}
						// set lower size
						size.setLowerSize(lowerSize);
						layout.writeDimensions();
					}
				};
				// return text
				return MessageFormat.format(
						GefMessages.ColumnSelectionEditPolicy_minimumWidthPattern,
						lowerSize.getSource(true, true));
			}
		} catch (Throwable e) {
			return GefMessages.ColumnSelectionEditPolicy_exception;
		}
	}
}
