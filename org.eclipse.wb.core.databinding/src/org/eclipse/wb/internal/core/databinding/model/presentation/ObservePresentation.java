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
		return m_decorateImage == null ? getInternalImageDescriptor() : m_decorateImage;
	}

	/**
	 * @return {@link ImageDescriptor} for displaying and decorate.
	 */
	protected abstract ImageDescriptor getInternalImageDescriptor() throws Exception;

	////////////////////////////////////////////////////////////////////////////
	//
	// IObservePresentationDecorator
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final void setBindingDecorator(int corner) throws Exception {
		if (corner != 0) {
			ImageDescriptor image = getInternalImageDescriptor();
			if (image != null) {
				m_decorateImage = new DecorationOverlayIcon(image, JavaInfoDecorator.IMAGE_DESCRIPTOR, corner);
			}
		} else {
			m_decorateImage = null;
		}
	}
}