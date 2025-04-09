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
package org.eclipse.wb.internal.rcp.model.forms.layout.table;

import org.eclipse.wb.internal.core.model.util.grid.GridAlignmentHelper;

/**
 * Preference constants for {@link ITableWrapLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.forms
 */
public interface IPreferenceConstants {
	/**
	 * When <code>true</code>, {@link ITableWrapLayoutInfo} can use
	 * {@link GridAlignmentHelper#V_GRAB_HORIZONTAL} and {@link GridAlignmentHelper#V_GRAB_VERTICAL}.
	 */
	String P_ENABLE_GRAB = "TableWrapLayout.enableGrab";
	/**
	 * When <code>true</code>, {@link ITableWrapLayoutInfo} can use
	 * {@link GridAlignmentHelper#V_RIGHT_LABEL} and {@link GridAlignmentHelper#V_RIGHT_TARGET}.
	 */
	String P_ENABLE_RIGHT_ALIGNMENT = "TableWrapLayout.enableRightAlignment";
}
