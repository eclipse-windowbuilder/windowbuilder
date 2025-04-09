/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.internal.core.databinding.ui.editor;

import org.eclipse.swt.graphics.Image;

/**
 * {@link IPageListener} wrapper.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public final class PageListenerWrapper implements IPageListener {
	private final IPageListener m_pageListener;
	private final ICompleteListener m_completeListener;
	private String m_message;
	private String m_errorMessage;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PageListenerWrapper(IPageListener pageListener, ICompleteListener completeListener) {
		m_pageListener = pageListener;
		m_completeListener = completeListener;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IPageListener
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void setTitle(String title) {
		m_pageListener.setTitle(title);
	}

	@Override
	public void setTitleImage(Image image) {
		m_pageListener.setTitleImage(image);
	}

	@Override
	public void setMessage(String newMessage) {
		m_message = newMessage;
	}

	@Override
	public void setErrorMessage(String newMessage) {
		m_errorMessage = newMessage;
	}

	@Override
	public void setPageComplete(boolean complete) {
		m_completeListener.calculateFinish();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return current state message or <code>null</code>.
	 */
	public String getMessage() {
		return m_message;
	}

	/**
	 * @return current error state message or <code>null</code>.
	 */
	public String getErrorMessage() {
		return m_errorMessage;
	}
}