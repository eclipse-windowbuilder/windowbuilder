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
package org.eclipse.wb.internal.swt.support;

import org.eclipse.wb.internal.core.model.menu.MenuVisualData;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * RCP/eRCP plugins provide implementation of this interface to provide access to the toolkit
 * specific operations.
 *
 * @author mitin_aa
 * @author lobas_av
 * @author scheglov_ke
 * @coverage swt.support
 */
public interface IToolkitSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Screen shot
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Prepares shots for all {@link Control}'s in hierarchy that have flag {@link #WBP_NEED_IMAGE}.
   * Created image can be requested using {@link #getShotImage(Object)}.
   */
  void makeShots(Object control) throws Exception;

  /**
   * @return the SWT shot {@link Image} created by {@link #makeShots(Object)}.
   */
  Image getShotImage(Object control) throws Exception;

  /**
   * Prepares the process of taking screen shot.
   */
  void beginShot(Object control);

  /**
   * Finalizes the process of taking screen shot.
   */
  void endShot(Object control);

  /**
   * @return the menu visual data (image, bounds, item bounds) for given menu object.
   */
  MenuVisualData fetchMenuVisualData(Object menu) throws Exception;

  /**
   * @return the default height of single-line menu bar according to system metrics (if available).
   */
  int getDefaultMenuBarHeight() throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Images
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the new toolkit {@link Image} for given SWT {@link Image}.
   */
  Object createToolkitImage(Image image) throws Exception;

  /**
   * @return the new SWT {@link Image} for given toolkit {@link Image}.
   */
  Image createSWTImage(Object image) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Shell
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Shows given {@link Shell} object to user. On close {@link Shell} will be hidden, not disposed.
   */
  void showShell(Object shell) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Font
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the array of font families registered in system.
   */
  String[] getFontFamilies(boolean scalable) throws Exception;

  /**
   * @return {@link Image} with preview for given {@link Font}.
   */
  Image getFontPreview(Object font) throws Exception;
}