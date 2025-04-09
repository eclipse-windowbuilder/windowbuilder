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
package org.eclipse.wb.internal.core.utils.ui.dialogs.image;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Abstract {@link Composite} for {@link ImageInfo} selection.
 *
 * @author scheglov_ke
 * @coverage core.ui
 */
public abstract class AbstractImagePage extends Composite {
	protected final AbstractImageDialog m_imageDialog;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AbstractImagePage(Composite parent, int style, AbstractImageDialog imageDialog) {
		super(parent, style);
		m_imageDialog = imageDialog;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * This method is invoked when user activates this {@link AbstractImagePage}.
	 */
	public abstract void activate();

	/**
	 * Sets the initial data for page. It is expected that page will use method
	 * {@link AbstractImageDialog#setResultImageInfo(ImageInfo)} to display image corresponding to
	 * given data.
	 */
	public abstract void setInput(Object data);

	/**
	 * XXX
	 */
	public void init(Object data) {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the id of this page.
	 */
	public abstract String getId();

	/**
	 * @return the title of this page.
	 */
	public abstract String getTitle();

	////////////////////////////////////////////////////////////////////////////
	//
	// GUI
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return {@link Control} represented this page.
	 */
	protected Control getPageControl() {
		return this;
	}
}
