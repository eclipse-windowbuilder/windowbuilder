/*******************************************************************************
 * Copyright (c) 2025 Patrick Ziegler and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.draw2d;

import org.eclipse.wb.internal.draw2d.ICustomTooltipProvider;

/**
 * Subclass of the Draw2D figure used for palette entries and categories.
 */
public class PaletteFigure extends Figure {
	private ICustomTooltipProvider m_customTooltipProvider;

	/**
	 * @return custom tool tip provider {@link ICustomTooltipProvider}, or
	 *         <code>null</code> if it has not been set.
	 */
	public ICustomTooltipProvider getCustomTooltipProvider() {
		return m_customTooltipProvider;
	}

	/**
	 * Sets the custom tool tip provider {@link ICustomTooltipProvider} to the
	 * argument, which may be <code>null</code> indicating that no tool tip text
	 * should be shown.
	 */
	public void setCustomTooltipProvider(ICustomTooltipProvider provider) {
		m_customTooltipProvider = provider;
	}
}