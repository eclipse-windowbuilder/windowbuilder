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
 * Default implementation for {@link IPageListener}.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public class EmptyPageListener implements IPageListener {
	public static final IPageListener INSTANCE = new EmptyPageListener();

	////////////////////////////////////////////////////////////////////////////
	//
	// IPageListener
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void setErrorMessage(String newMessage) {
	}

	@Override
	public void setMessage(String newMessage) {
	}

	@Override
	public void setPageComplete(boolean complete) {
	}

	@Override
	public void setTitle(String title) {
	}

	@Override
	public void setTitleImage(Image image) {
	}
}