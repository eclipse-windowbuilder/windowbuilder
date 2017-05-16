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
package org.eclipse.wb.internal.core.utils.ui.dialogs.color;

import org.eclipse.swt.widgets.Composite;

/**
 * Abstract {@link Composite} for color selection.
 *
 * @author scheglov_ke
 * @coverage core.ui
 */
public abstract class AbstractColorsComposite extends Composite {
  protected final AbstractColorDialog m_colorDialog;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractColorsComposite(Composite parent, int style, AbstractColorDialog colorDialog) {
    super(parent, style);
    m_colorDialog = colorDialog;
  }
}
