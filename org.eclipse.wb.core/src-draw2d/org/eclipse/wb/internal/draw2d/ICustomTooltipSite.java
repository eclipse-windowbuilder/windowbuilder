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