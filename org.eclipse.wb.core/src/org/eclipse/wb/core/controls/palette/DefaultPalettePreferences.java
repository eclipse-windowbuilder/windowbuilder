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

import org.eclipse.swt.graphics.Font;

/**
 * The default implementation of {@link IPalettePreferences}.
 *
 * @author scheglov_ke
 * @coverage core.control.palette
 */
public final class DefaultPalettePreferences implements IPalettePreferences {
  public Font getCategoryFont() {
    return null;
  }

  public Font getEntryFont() {
    return null;
  }

  public boolean isOnlyIcons() {
    return false;
  }

  public int getMinColumns() {
    return 1;
  }
}
