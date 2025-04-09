/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.internal.swt.support;

import org.eclipse.wb.internal.core.model.menu.MenuVisualData;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

/**
 * RCP plugins provide implementation of this interface to provide access to the toolkit
 * specific operations.
 *
 * @author mitin_aa
 * @author lobas_av
 * @author scheglov_ke
 * @coverage swt.support
 */
public interface IToolkitSupport {
	////////////////////////////////////////////////////////////////////////////
	//
	// Screen shot
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Prepares shots for all {@link Control}'s in hierarchy that have flag {@link #WBP_NEED_IMAGE}.
	 * Created image can be requested using {@link #getShotImage(Object)}.
	 */
	void makeShots(Control control) throws Exception;

	/**
	 * @return the SWT shot {@link Image} created by {@link #makeShots(Object)}.
	 */
	Image getShotImage(Control control) throws Exception;

	/**
	 * Prepares the process of taking screen shot.
	 */
	void beginShot(Control control);

	/**
	 * Finalizes the process of taking screen shot.
	 */
	void endShot(Control control);

	/**
	 * @return the menu visual data (image, bounds, item bounds) for given menu object.
	 */
	MenuVisualData fetchMenuVisualData(Menu menu) throws Exception;

	/**
	 * @return the default height of single-line menu bar according to system metrics (if available).
	 */
	int getDefaultMenuBarHeight() throws Exception;

	////////////////////////////////////////////////////////////////////////////
	//
	// Shell
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Shows given {@link Shell} object to user. On close {@link Shell} will be hidden, not disposed.
	 */
	void showShell(Shell shell) throws Exception;

	////////////////////////////////////////////////////////////////////////////
	//
	// Font
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the array of font families registered in system.
	 */
	String[] getFontFamilies(boolean scalable) throws Exception;

	/**
	 * @return {@link Image} with preview for given {@link Font}.
	 */
	Image getFontPreview(Font font) throws Exception;
}