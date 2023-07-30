/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.xwt.gef.part;

import org.eclipse.wb.core.gef.policy.selection.LineSelectionEditPolicy;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.core.requests.SelectionRequest;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.xwt.model.jface.ViewerInfo;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;

import java.util.Collections;
import java.util.List;

/**
 * {@link EditPart} for {@link ViewerInfo}.
 *
 * @author scheglov_ke
 * @coverage XWT.gef
 */
public class ViewerEditPart extends GraphicalEditPart {
	private final ViewerInfo m_viewer;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ViewerEditPart(ViewerInfo viewer) {
		setModel(viewer);
		m_viewer = viewer;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Edit Policies
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
		installEditPolicy(
				EditPolicy.SELECTION_ROLE,
				new LineSelectionEditPolicy(IColorConstants.black) {
					@Override
					protected Rectangle getHostBounds() {
						Rectangle bounds = getIconBounds();
						bounds.performTranslate(getHostFigure().getLocation());
						return bounds;
					}
				});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Figure
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Figure createFigure() {
		return new Figure() {
			@Override
			protected void paintClientArea(Graphics graphics) {
				ImageDescriptor imageDescriptor = m_viewer.getDescription().getIcon();
				if (imageDescriptor != null) {
					Image image = imageDescriptor.createImage();
					Rectangle iconBounds = getIconBounds();
					graphics.drawImage(image, iconBounds.x, iconBounds.y);
					image.dispose();
				}
			}
		};
	}

	@Override
	protected void refreshVisuals() {
		Display.getCurrent().asyncExec(new Runnable() {
			public void run() {
				Rectangle bounds = ((GraphicalEditPart) getParent()).getFigure().getClientArea();
				getFigure().setBounds(bounds);
			}
		});
	}

	private Rectangle getIconBounds() {
		ImageData imageData = m_viewer.getDescription().getIcon().getImageData(100);
		int width = imageData.width;
		int height = imageData.height;
		//
		Point location = getFigure().getBounds().getBottomRight().getTranslated(-width, -height);;
		location.performTranslate(-3, -3);
		return new Rectangle(location.x, location.y, width, height);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Children
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public EditPart getTargetEditPart(Request request) {
		if (request instanceof SelectionRequest) {
			Point location;
			{
				SelectionRequest selectionRequest = (SelectionRequest) request;
				location = selectionRequest.getLocation();
				if (location == null) {
					return this;
				}
				location = location.getCopy();
				FigureUtils.translateAbsoluteToFigure2(getFigure(), location);
			}
			if (!getIconBounds().contains(location)) {
				return getParent().getTargetEditPart(request);
			}
		}
		return super.getTargetEditPart(request);
	}

	@Override
	protected List<?> getModelChildren() {
		return ExecutionUtils.runObjectLog(new RunnableObjectEx<List<?>>() {
			public List<?> runObject() throws Exception {
				return m_viewer.getPresentation().getChildrenGraphical();
			}
		}, Collections.emptyList());
	}
}