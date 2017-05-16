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
package org.eclipse.wb.internal.core.databinding.ui.decorate;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

/**
 * Base implementation for {@link IObserveDecorator}.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public class ObserveDecorator implements IObserveDecorator {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IObserveDecorator
  //
  ////////////////////////////////////////////////////////////////////////////
  public Color getForeground() {
    return null;
  }

  public Color getBackground() {
    return null;
  }

  public Font getFont(Font baseItalicFont, Font baseBoldFont, Font baseBoldItalicFont) {
    return null;
  }
}