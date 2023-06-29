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
package org.eclipse.wb.internal.swing.MigLayout.model;

import org.eclipse.wb.internal.core.model.util.grid.GridAlignmentHelper;

/**
 * Preference constants for {@link MigLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.MigLayout.model
 */
public interface IPreferenceConstants {
	/**
	 * When <code>true</code>, {@link MigLayoutInfo} can use
	 * {@link GridAlignmentHelper#V_GRAB_HORIZONTAL} and {@link #V_GRAB_VERTICAL}.
	 */
	String P_ENABLE_GRAB = "MigLayout.enableGrab";
	/**
	 * When <code>true</code>, {@link MigLayoutInfo} can use {@link GridAlignmentHelper#V_RIGHT_LABEL}
	 * and {@link GridAlignmentHelper#V_RIGHT_TARGET}.
	 */
	String P_ENABLE_RIGHT_ALIGNMENT = "MigLayout.enableRightAlignment";
}
