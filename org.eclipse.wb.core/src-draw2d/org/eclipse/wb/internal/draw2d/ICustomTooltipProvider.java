/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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

import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Provider for tooltip's controls.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public interface ICustomTooltipProvider {
	/**
	 * Create tooltip control.
	 */
	Control createTooltipControl(Composite parent, ICustomTooltipSite site, IFigure figure);

	void show(Shell shell);
}