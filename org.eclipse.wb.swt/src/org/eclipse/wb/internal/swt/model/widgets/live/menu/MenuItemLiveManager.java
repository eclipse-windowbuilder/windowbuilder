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
package org.eclipse.wb.internal.swt.model.widgets.live.menu;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.association.AssociationObject;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.WidgetInfo;
import org.eclipse.wb.internal.swt.model.widgets.live.SwtLiveManager;
import org.eclipse.wb.internal.swt.model.widgets.menu.MenuInfo;

/**
 * Special {@link SwtLiveManager} for SWT menu item.
 * 
 * @author mitin_aa
 * @author scheglov_ke
 * @coverage swt.model.widgets.live
 */
public class MenuItemLiveManager extends SwtLiveManager {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MenuItemLiveManager(AbstractComponentInfo component) {
    super(component);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // LiveComponentsManager
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addWidget(CompositeInfo shell, WidgetInfo widget) throws Exception {
    AstEditor editor = shell.getEditor();
    // add menu
    MenuInfo menu;
    {
      menu =
          (MenuInfo) JavaInfoUtils.createJavaInfo(
              editor,
              "org.eclipse.swt.widgets.Menu",
              new ConstructorCreationSupport("bar", false));
      AssociationObject association =
          AssociationObjects.invocationChild("%parent%.setMenuBar(%child%)", true);
      JavaInfoUtils.add(menu, association, shell, null);
    }
    // add item
    JavaInfoUtils.add(widget, null, menu, null);
  }
}
