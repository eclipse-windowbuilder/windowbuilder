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
package org.eclipse.wb.internal.swt.model.widgets.menu;

import org.eclipse.wb.internal.swt.model.widgets.StylePresentation;
import org.eclipse.wb.internal.swt.support.SwtSupport;

/**
 * Presentation for SWT menu with style: <code>SWT.POP_UP</code>, <code>SWT.DROP_DOWN</code>,
 * <code>SWT.BAR</code>.
 * 
 * @author mitin_aa
 * @author lobas_av
 * @coverage swt.model.widgets.menu
 */
public final class MenuStylePresentation extends StylePresentation {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MenuStylePresentation(MenuInfo menu) {
    super(menu);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // StylePresentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void initImages() throws Exception {
    addImage(SwtSupport.BAR, "wbp-meta/org/eclipse/swt/widgets/Menu_bar.gif");
    addImage(SwtSupport.POP_UP, "wbp-meta/org/eclipse/swt/widgets/Menu.gif");
    addImage(SwtSupport.DROP_DOWN, "wbp-meta/org/eclipse/swt/widgets/Menu_dropdown.gif");
  }
}
