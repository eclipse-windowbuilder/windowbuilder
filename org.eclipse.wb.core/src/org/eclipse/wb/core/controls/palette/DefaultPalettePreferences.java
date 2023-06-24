/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *    DSA - layout type added
 *******************************************************************************/
package org.eclipse.wb.core.controls.palette;

import org.eclipse.jface.resource.FontDescriptor;

/**
 * The default implementation of {@link IPalettePreferences}.
 *
 * @author scheglov_ke
 * @coverage core.control.palette
 */
public final class DefaultPalettePreferences implements IPalettePreferences {
  @Override
  public FontDescriptor getCategoryFontDescriptor() {
    return null;
  }

  @Override
  public FontDescriptor getEntryFontDescriptor() {
    return null;
  }

  @Override
  public boolean isOnlyIcons() {
    return false;
  }

  @Override
  public int getMinColumns() {
    return 1;
  }

  public int getLayoutType() {
    return 0;
  }
}
