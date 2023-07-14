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
package org.eclipse.wb.internal.core.databinding.model.presentation;

import org.eclipse.wb.internal.core.databinding.model.IObservePresentation;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;

/**
 * Base class for all observable presentations.
 *
 * @author lobas_av
 * @coverage bindings.model
 */
public abstract class ObservePresentation
implements
IObservePresentation,
IObservePresentationDecorator {
	private ImageDescriptor m_decorateImage;

	////////////////////////////////////////////////////////////////////////////
	//
	// IObservePresentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final ImageDescriptor getImageDescriptor() throws Exception {
		return m_decorateImage == null ? getInternalImage() : m_decorateImage;
	}

	/**
	 * @return {@link ImageDescriptor} for displaying and decorate.
	 */
	protected abstract ImageDescriptor getInternalImage() throws Exception;

	////////////////////////////////////////////////////////////////////////////
	//
	// IObservePresentationDecorator
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final void setBindingDecorator(int corner) throws Exception {
		if (corner != 0) {
			ImageDescriptor image = getInternalImage();
			if (image != null) {
				m_decorateImage = new DecorationOverlayIcon(image, JavaInfoDecorator.IMAGE_DESCRIPTOR, corner);
			}
		} else {
			m_decorateImage = null;
		}
	}
}