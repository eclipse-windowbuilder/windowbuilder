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
package org.eclipse.wb.core.gef.header;

import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.utils.ui.DrawUtils;

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
    if (EnvironmentUtils.IS_WINDOWS_VISTA || EnvironmentUtils.IS_WINDOWS_7) {
      return DrawUtils.getShiftedColor(IColorConstants.white, 0);
    } else {
      return DrawUtils.getShiftedColor(IColorConstants.white, -16);
    }
  }
}
