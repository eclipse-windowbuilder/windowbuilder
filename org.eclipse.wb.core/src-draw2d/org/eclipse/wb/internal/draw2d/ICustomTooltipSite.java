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
package org.eclipse.wb.internal.draw2d;

import org.eclipse.swt.widgets.Listener;

/**
 * Interface that allows control of {@link ICustomTooltipProvider} interact with
 * {@link CustomTooltipManager}.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public interface ICustomTooltipSite {
	/**
	 * Hides current tooltip.
	 */
	void hideTooltip();

	/**
	 * @return {@link Listener} that hides tooltip on mouse exit or click.
	 */
	Listener getHideListener();
}