/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.gef.part;

import org.eclipse.wb.core.gef.policy.selection.LineSelectionEditPolicy;
import org.eclipse.wb.core.model.IWrapper;
import org.eclipse.wb.core.model.IWrapperInfo;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;

/**
 * {@link EditPart} for wrapper {@link IWrapperInfo}.
 *
 * @author scheglov_ke
 * @coverage core.gef
 */
public class AbstractWrapperEditPart extends GraphicalEditPart {
	private final IWrapper m_wrapper;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AbstractWrapperEditPart(IWrapper wrapper) {
		setModel(wrapper.getWrapperInfo());
		m_wrapper = wrapper;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Edit Policies
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
		installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, new LineSelectionEditPolicy(ColorConstants.black));
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
				ImageDescriptor imageDescriptor = m_wrapper.getWrapperInfo().getDescription().getIcon();
				if (imageDescriptor != null) {
					Image image = imageDescriptor.createImage();
					graphics.drawImage(image, 0, 0);
					image.dispose();
				}
			}
		};
	}

	@Override
	protected void refreshVisuals() {
		Display.getCurrent().asyncExec(new Runnable() {
			@Override
			public void run() {
				refreshVisuals0();
			}
		});
	}

	/**
	 * {@link EditPart} refreshes children and then visuals. So, we should wait for parent visuals
	 * refresh.
	 */
	private void refreshVisuals0() {
		ImageData imageData = m_wrapper.getWrapperInfo().getDescription().getIcon().getImageData(100);
		int width = imageData.width;
		int height = imageData.height;
		Rectangle parentClientArea = ((GraphicalEditPart) getParent()).getFigure().getClientArea();
		Point location = parentClientArea.getBottomRight().getTranslated(-width, -height);
		location.performTranslate(-3, -3);
		Rectangle bounds = new Rectangle(location.x, location.y, width, height);
		// no animation
		getFigure().setBounds(bounds);
	}
}