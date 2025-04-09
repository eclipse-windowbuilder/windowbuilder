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
package org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages;

import org.eclipse.wb.internal.core.utils.Messages;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.AbstractImageDialog;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.AbstractImagePage;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.ImageInfo;

import org.eclipse.swt.widgets.Composite;

/**
 * Implementation of {@link AbstractImagePage} that does not set image, i.e. keeps existing one.
 *
 * @author scheglov_ke
 * @coverage core.ui
 */
public final class DefaultImagePage extends AbstractImagePage {
	public static final String ID = "DEFAULT";

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DefaultImagePage(Composite parent, int style, AbstractImageDialog imageDialog) {
		super(parent, style, imageDialog);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getTitle() {
		return Messages.DefaultImagePage_title;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// AbstractImagePage
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void activate() {
		m_imageDialog.setResultImageInfo(new ImageInfo(ID, null, null, -1));
	}

	@Override
	public void setInput(Object data) {
	}
}
