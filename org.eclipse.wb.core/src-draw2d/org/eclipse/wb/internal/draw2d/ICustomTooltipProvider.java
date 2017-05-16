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
package org.eclipse.wb.internal.draw2d;

import org.eclipse.wb.draw2d.Figure;

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
  Control createTooltipControl(Composite parent, ICustomTooltipSite site, Figure figure);

  void show(Shell shell);
}