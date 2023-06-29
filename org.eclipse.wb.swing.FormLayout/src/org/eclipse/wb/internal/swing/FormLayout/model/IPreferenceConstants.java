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
package org.eclipse.wb.internal.swing.FormLayout.model;

import org.eclipse.wb.internal.core.model.util.grid.GridAlignmentHelper;

/**
 * Preference constants for {@link FormLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.FormLayout.model
 */
public interface IPreferenceConstants {
	/**
	 * When <code>true</code>, {@link FormLayoutInfo} can use
	 * {@link GridAlignmentHelper#V_GRAB_HORIZONTAL} and {@link #V_GRAB_VERTICAL}.
	 */
	String P_ENABLE_GRAB = "FormLayout.enableGrab";
	/**
	 * When <code>true</code>, {@link FormLayoutInfo} can use
	 * {@link GridAlignmentHelper#V_RIGHT_LABEL} and {@link GridAlignmentHelper#V_RIGHT_TARGET}.
	 */
	String P_ENABLE_RIGHT_ALIGNMENT = "FormLayout.enableRightAlignment";
}
