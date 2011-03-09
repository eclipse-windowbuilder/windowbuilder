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
package org.eclipse.wb.internal.xwt.gef;

import com.google.common.collect.ImmutableList;

import org.eclipse.wb.core.gef.MatchingEditPartFactory;
import org.eclipse.wb.core.gef.part.menu.MenuEditPartFactory;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartFactory;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPopupInfo;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;
import org.eclipse.wb.internal.xwt.model.widgets.menu.MenuInfo;
import org.eclipse.wb.internal.xwt.model.widgets.menu.MenuItemInfo;

/**
 * {@link IEditPartFactory} for XWT.
 * 
 * @author scheglov_ke
 * @coverage XWT.gef
 */
public final class EditPartFactory implements IEditPartFactory {
  private final static IEditPartFactory MATCHING_FACTORY =
      new MatchingEditPartFactory(ImmutableList.of(
          "org.eclipse.wb.internal.xwt.model.widgets",
          "org.eclipse.wb.internal.xwt.model.widgets",
          "org.eclipse.wb.internal.xwt.model.jface",
          "org.eclipse.wb.internal.xwt.model.forms"), ImmutableList.of(
          "org.eclipse.wb.internal.xwt.gef.part",
          "org.eclipse.wb.internal.rcp.gef.part.widgets",
          "org.eclipse.wb.internal.xwt.gef.part",
          "org.eclipse.wb.internal.xwt.gef.part"));

  ////////////////////////////////////////////////////////////////////////////
  //
  // IEditPartFactory
  //
  ////////////////////////////////////////////////////////////////////////////
  public EditPart createEditPart(EditPart context, Object model) {
    // menu
    {
      if (model instanceof MenuInfo) {
        MenuInfo menu = (MenuInfo) model;
        if (menu.isPopup()) {
          IMenuPopupInfo popupObject = MenuObjectInfoUtils.getMenuPopupInfo(menu);
          return MenuEditPartFactory.createPopupMenu(menu, popupObject);
        } else {
          IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menu);
          return createMenuEditPart(menu, menuObject);
        }
      }
      if (model instanceof MenuItemInfo) {
        MenuItemInfo item = (MenuItemInfo) model;
        IMenuItemInfo itemObject = MenuObjectInfoUtils.getMenuItemInfo(item);
        return MenuEditPartFactory.createMenuItem(item, itemObject);
      }
    }
    // most EditPart's can be created using matching
    return MATCHING_FACTORY.createEditPart(context, model);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static EditPart createMenuEditPart(Object model, IMenuInfo menuInfo) {
    return EnvironmentUtils.IS_MAC
        ? MenuEditPartFactory.createMenuMac(model, menuInfo)
        : MenuEditPartFactory.createMenu(model, menuInfo);
  }
}