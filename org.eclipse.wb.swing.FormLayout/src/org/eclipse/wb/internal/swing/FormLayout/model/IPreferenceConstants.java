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
