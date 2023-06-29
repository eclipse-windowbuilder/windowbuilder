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
package org.eclipse.wb.internal.xwt.support;

import org.eclipse.wb.os.OSSupport;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;

/**
 * Toolkit specific utilities for eRCP/RCP.
 *
 * @author scheglov_ke
 * @coverage XWT.support
 */
public class ToolkitSupport {
	////////////////////////////////////////////////////////////////////////////
	//
	// ScreenShot
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Marks given {@link Control} instance as needed screen shot {@link Image}.
	 */
	public static void markAsNeededImage(Control control) throws Exception {
		control.setData(OSSupport.WBP_NEED_IMAGE, Boolean.TRUE);
	}

	/**
	 * @return the screen shot {@link Image} from given {@link Control}.
	 */
	public static Image getShotImage(Control control) throws Exception {
		return (Image) control.getData(OSSupport.WBP_IMAGE);
	}

	/**
	 * Creates screen shots for all {@link Control}'s in hierarchy marked with
	 * <code>WBP_NEED_IMAGE</code>. Created {@link Image}'s are located in <code>WBP_IMAGE</code>
	 * data.
	 */
	public static void makeShots(Object control) throws Exception {
		OSSupport.get().makeShots(control);
	}

	/**
	 * Prepares the process of taking screen shot.
	 */
	public static void beginShot(Object control) throws Exception {
		OSSupport.get().beginShot(control);
	}

	/**
	 * Finalizes the process of taking screen shot.
	 */
	public static void endShot(Object control) throws Exception {
		OSSupport.get().endShot(control);
	}
}