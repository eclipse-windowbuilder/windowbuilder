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
package org.eclipse.wb.core.gef.header;

import org.eclipse.wb.internal.core.utils.ui.DrawUtils;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

/**
 * Utils for headers.
 *
 * @author scheglov_ke
 * @coverage core.gef.header
 */
public final class Headers {
	/**
	 * {@link Color} for header background.
	 */
	public static final Color COLOR_HEADER = getColorHeader();

	private static Color getColorHeader() {
		return DrawUtils.getShiftedColor(ColorConstants.white, -16);
	}
}
