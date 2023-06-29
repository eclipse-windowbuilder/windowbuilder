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
package org.eclipse.wb.internal.core.xml.editor;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Interface for page in {@link AbstractXmlEditor}.
 *
 * @author scheglov_ke
 * @coverage XML.editor
 */
public interface IXmlEditorPage {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Initialize this page for given {@link AbstractXmlEditor}.
	 */
	void initialize(AbstractXmlEditor editor);

	/**
	 * Disposes this page.
	 */
	void dispose();

	/**
	 * Notifies when this page become active or inactive.
	 */
	void setActive(boolean active);

	////////////////////////////////////////////////////////////////////////////
	//
	// Page
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the index of this page into multi-page editor.
	 */
	int getPageIndex();

	/**
	 * Sets index of this page into multi-page editor.
	 */
	void setPageIndex(int index);

	////////////////////////////////////////////////////////////////////////////
	//
	// GUI
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates the {@link Control} for this page.
	 */
	Control createControl(Composite parent);

	/**
	 * @return the {@link Control} of this page.
	 */
	Control getControl();

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the display name for this page.
	 */
	String getName();

	/**
	 * @return the display {@link Image} image for this page.
	 */
	Image getImage();
}