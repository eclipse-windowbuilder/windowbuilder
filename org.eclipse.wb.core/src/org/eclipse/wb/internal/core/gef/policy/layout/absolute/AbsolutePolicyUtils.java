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
package org.eclipse.wb.internal.core.gef.policy.layout.absolute;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

/**
 * Utilities related to absolute layouts.
 *
 * @author mitin_aa
 * @coverage core.gef.policy
 */
public final class AbsolutePolicyUtils {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	////////////////////////////////////////////////////////////////////////////
	public static final Color COLOR_FEEDBACK = ColorConstants.lightBlue;
	public static final Color COLOR_OUTLINE = ColorConstants.orange;
	public static final int DEFAULT_COMPONENT_GAP = 6;
	public static final int DEFAULT_CONTAINER_GAP = 6;

	////////////////////////////////////////////////////////////////////////////
	//
	// Private constructor
	//
	////////////////////////////////////////////////////////////////////////////
	private AbsolutePolicyUtils() {
	}
}
