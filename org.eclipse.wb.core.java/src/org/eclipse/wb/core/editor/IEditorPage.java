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
package org.eclipse.wb.core.editor;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Any page for {@link IDesignerEditor}.
 *
 * @author scheglov_ke
 * @coverage core.editor
 */
public interface IEditorPage {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Initialize this page for given {@link IDesignerEditor} editor.
	 */
	void initialize(IDesignerEditor designerEditor);

	/**
	 * Disposes this page.
	 */
	void dispose();

	////////////////////////////////////////////////////////////////////////////
	//
	// Activation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Handles activation/deactivation this page.
	 */
	void handleActiveState(boolean activate);

	////////////////////////////////////////////////////////////////////////////
	//
	// GUI
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates the SWT control(s) for this page.
	 */
	Control createControl(Composite parent);

	/**
	 * @return the SWT {@link Control} of this page.
	 */
	Control getControl();

	/**
	 * Asks this page to take focus.
	 */
	void setFocus();

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