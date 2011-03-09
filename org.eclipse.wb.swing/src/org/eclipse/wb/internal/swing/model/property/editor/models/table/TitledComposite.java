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
package org.eclipse.wb.internal.swing.model.property.editor.models.table;

import org.eclipse.wb.core.controls.Separator;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * {@link Composite} with {@link Separator} title.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class TitledComposite extends Composite {
  private final Composite m_content;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TitledComposite(Composite parent, int style, String text) {
    super(parent, style);
    GridLayoutFactory.create(this).noMargins();
    // separator
    {
      Separator separator = new Separator(this, SWT.NONE);
      separator.setText(text);
      GridDataFactory.create(separator).grabH().fillH();
    }
    // content container
    {
      m_content = new Composite(this, SWT.NONE);
      GridDataFactory.create(m_content).indentHC(2).grab().fill();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Composite} to use as parent for creating content.
   */
  public Composite getContent() {
    return m_content;
  }
}
