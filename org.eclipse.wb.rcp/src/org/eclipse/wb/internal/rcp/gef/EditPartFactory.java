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
package org.eclipse.wb.internal.rcp.gef;

import com.google.common.collect.ImmutableList;

import org.eclipse.wb.core.gef.MatchingEditPartFactory;
import org.eclipse.wb.core.gef.part.menu.MenuEditPartFactory;
import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartFactory;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;
import org.eclipse.wb.internal.rcp.gef.part.forms.FormHeadEditPart;
import org.eclipse.wb.internal.rcp.gef.part.jface.DialogButtonBarEditPart;
import org.eclipse.wb.internal.rcp.model.forms.FormInfo;
import org.eclipse.wb.internal.rcp.model.jface.DialogInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.MenuManagerInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;

/**
 * Implementation of {@link IEditPartFactory} for RCP.
 *
 * @author scheglov_ke
 * @coverage rcp.gef
 */
public final class EditPartFactory implements IEditPartFactory {
  private final static IEditPartFactory MATCHING_FACTORY =
      new MatchingEditPartFactory(ImmutableList.of("org.eclipse.wb.internal.rcp.model"),
          ImmutableList.of("org.eclipse.wb.internal.rcp.gef.part"));

  ////////////////////////////////////////////////////////////////////////////
  //
  // IEditPartFactory
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public EditPart createEditPart(EditPart context, Object model) {
    // special Composite's
    if (model instanceof CompositeInfo) {
      CompositeInfo composite = (CompositeInfo) model;
      // Form.getHead()
      if (composite.getParent() instanceof FormInfo) {
        FormInfo form = (FormInfo) composite.getParent();
        if (form.getHead() == composite) {
          return new FormHeadEditPart(form);
        }
      }
      // Dialog.createButtonsForButtonBar(parent)
      if (DialogInfo.isButtonBar(composite)) {
        return new DialogButtonBarEditPart(composite);
      }
    }
    // MenuManagerInfo
    {
      if (model instanceof MenuManagerInfo) {
        MenuManagerInfo menuManager = (MenuManagerInfo) model;
        if (menuManager.getParent() instanceof MenuManagerInfo) {
          IMenuItemInfo itemObject = MenuObjectInfoUtils.getMenuItemInfo(menuManager);
          return MenuEditPartFactory.createMenuItem(menuManager, itemObject);
        } else {
          return createMenuEditPart(menuManager, MenuObjectInfoUtils.getMenuInfo(model));
        }
      }
      if (model instanceof IMenuInfo) {
        IMenuInfo menu = (IMenuInfo) model;
        return createMenuEditPart(model, menu);
      }
      if (model instanceof AbstractComponentInfo) {
        AbstractComponentInfo item = (AbstractComponentInfo) model;
        if (((AbstractComponentInfo) model).getParent() instanceof MenuManagerInfo) {
          IMenuItemInfo itemObject = MenuObjectInfoUtils.getMenuItemInfo(item);
          return MenuEditPartFactory.createMenuItem(item, itemObject);
        }
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