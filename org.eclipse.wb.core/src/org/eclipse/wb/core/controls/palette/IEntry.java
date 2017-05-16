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
package org.eclipse.wb.core.controls.palette;

import org.eclipse.swt.graphics.Image;

/**
 * Single entry on {@link PaletteComposite}.
 *
 * @author scheglov_ke
 * @coverage core.control.palette
 */
public interface IEntry {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sometimes we want to show entry, but don't allow to select it.
   */
  boolean isEnabled();

  /**
   * @return the icon of {@link IEntry}.
   */
  Image getIcon();

  /**
   * @return the title text of {@link IEntry}.
   */
  String getText();

  /**
   * @return the tooltip text of {@link IEntry}.
   */
  String getToolTipText();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Activation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Activates this {@link IEntry}.
   *
   * @param reload
   *          is <code>true</code> if entry should be automatically reloaded after successful using.
   *
   * @return <code>true</code> if {@link IEntry} was successfully activated.
   */
  boolean activate(boolean reload);
}
