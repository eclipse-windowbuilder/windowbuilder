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
package org.eclipse.wb.internal.xwt.model.widgets.menu;

import org.eclipse.wb.internal.swt.support.ControlSupport;
import org.eclipse.wb.internal.xwt.model.widgets.StylePresentation;

import org.eclipse.swt.SWT;

/**
 * Presentation for SWT menu item with style: <code>SWT.CHECK</code>, <code>SWT.RADIO</code>,
 * <code>SWT.SEPARATOR</code>.
 * 
 * @author scheglov_ke
 * @coverage XWT.model.widgets
 */
public final class MenuItemStylePresentation extends StylePresentation {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MenuItemStylePresentation(MenuItemInfo item) {
    super(item);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Custom text for separator
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getText() throws Exception {
    if (ControlSupport.isStyle(m_widget.getObject(), SWT.SEPARATOR)) {
      return "<separator>";
    }
    return super.getText();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // StylePresentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void initImages() throws Exception {
    addImage(SWT.CHECK, "wbp-meta/org/eclipse/swt/widgets/MenuItem_check.gif");
    addImage(SWT.RADIO, "wbp-meta/org/eclipse/swt/widgets/MenuItem_radio.gif");
    addImage(SWT.SEPARATOR, "wbp-meta/org/eclipse/swt/widgets/MenuItem_separator.gif");
  }
}
