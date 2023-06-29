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