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
package org.eclipse.wb.internal.swt.model.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;

/**
 * Presentation for {@link Label} with style.
 * 
 * @author scheglov_ke
 * @coverage swt.model.presentation
 */
public final class LabelStylePresentation extends StylePresentation {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LabelStylePresentation(LabelInfo label) {
    super(label);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // StylePresentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void initImages() throws Exception {
    addImage(
        SWT.SEPARATOR | SWT.HORIZONTAL,
        "wbp-meta/org/eclipse/swt/widgets/Label_separatorHorizontal.gif");
    addImage(
        SWT.SEPARATOR | SWT.VERTICAL,
        "wbp-meta/org/eclipse/swt/widgets/Label_separatorVertical.gif");
  }
}