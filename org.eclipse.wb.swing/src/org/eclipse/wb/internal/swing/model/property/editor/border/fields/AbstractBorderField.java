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
package org.eclipse.wb.internal.swing.model.property.editor.border.fields;

import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import javax.swing.border.Border;

/**
 * Abstract labeled field for {@link Border} editing.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public abstract class AbstractBorderField extends Composite {
  private static final int LABEL_WIDTH = 23;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractBorderField(Composite parent, int columns, String labelText) {
    super(parent, SWT.NONE);
    GridLayoutFactory.create(this).noMargins().columns(1 + columns);
    {
      Label label = new Label(this, SWT.NONE);
      GridDataFactory.create(label).hintHC(LABEL_WIDTH);
      label.setText(labelText);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the source corresponding to the made selection.
   */
  public abstract String getSource() throws Exception;
}
