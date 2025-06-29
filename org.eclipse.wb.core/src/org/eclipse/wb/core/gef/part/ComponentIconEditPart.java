/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.core.gef.part;

import org.eclipse.wb.core.gef.policy.selection.NonResizableSelectionEditPolicy;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.core.model.description.IComponentDescription;
import org.eclipse.wb.internal.core.utils.state.GlobalState;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPolicy;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

/**
 * {@link EditPart} that displays icon for its component.
 *
 * @author scheglov_ke
 * @coverage core.gef
 */
public abstract class ComponentIconEditPart extends GraphicalEditPart {
	private final Object m_component;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ComponentIconEditPart(Object component) {
		setModel(component);
		m_component = component;
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
				Image image = getIcon().createImage();
				graphics.drawImage(image, 0, 0);
				image.dispose();
			}
		};
	}

	@Override
	protected void refreshVisuals() {
		ImageData iconBounds = getIcon().getImageData(100);
		Rectangle bounds = getFigureBounds(iconBounds.width, iconBounds.height);
		getFigure().setBounds(bounds);
	}

	private ImageDescriptor getIcon() {
		IComponentDescription description =
				GlobalState.getDescriptionHelper().getDescription(m_component);
		return description.getIcon();
	}

	/**
	 * @return the bounds of {@link Figure} based on size of icon.
	 */
	protected abstract Rectangle getFigureBounds(int width, int height);

	////////////////////////////////////////////////////////////////////////////
	//
	// Policies
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, new NonResizableSelectionEditPolicy());
	}
}
