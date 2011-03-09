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
package org.eclipse.wb.internal.swing.model.property.editor.border.pages;

import org.eclipse.swt.widgets.Composite;

import javax.swing.border.Border;

/**
 * Implementation of {@link AbstractBorderComposite} that sets <code>null</code> {@link Border}.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class NoBorderComposite extends AbstractBorderComposite {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public NoBorderComposite(Composite parent) {
    super(parent, "(no border)");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean setBorder(Border border) throws Exception {
    return border == null;
  }

  @Override
  public String getSource() throws Exception {
    return "null";
  }
}
