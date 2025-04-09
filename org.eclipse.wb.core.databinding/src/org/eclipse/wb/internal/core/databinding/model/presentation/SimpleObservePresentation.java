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

import org.eclipse.jface.resource.ImageDescriptor;

/**
 *
 * @author lobas_av
 *
 */
public class SimpleObservePresentation extends ObservePresentation {
	private final String m_text;
	private final String m_textForBinding;
	private final ImageDescriptor m_image;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SimpleObservePresentation(String text, ImageDescriptor image) {
		this(text, text, image);
	}

	public SimpleObservePresentation(String text, String textForBinding, ImageDescriptor image) {
		m_text = text;
		m_textForBinding = textForBinding;
		m_image = image;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ObservePresentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected ImageDescriptor getInternalImageDescriptor() throws Exception {
		return m_image == null ? null : m_image;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IObservePresentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getText() throws Exception {
		return m_text;
	}

	@Override
	public String getTextForBinding() throws Exception {
		return m_textForBinding;
	}
}