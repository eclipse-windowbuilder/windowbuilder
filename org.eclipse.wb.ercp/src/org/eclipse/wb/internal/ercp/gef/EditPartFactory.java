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
package org.eclipse.wb.internal.ercp.gef;

import com.google.common.collect.ImmutableList;

import org.eclipse.wb.core.gef.MatchingEditPartFactory;
import org.eclipse.wb.core.gef.part.menu.MenuEditPartFactory;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartFactory;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPopupInfo;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;
import org.eclipse.wb.internal.ercp.model.widgets.mobile.CommandInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

/**
 * Implementation of {@link IEditPartFactory} for eRCP.
 * 
 * @author scheglov_ke
 * @coverage ercp.gef
 */
public final class EditPartFactory implements IEditPartFactory {
  private final static IEditPartFactory MATCHING_FACTORY =
      new MatchingEditPartFactory(ImmutableList.of("org.eclipse.wb.internal.ercp.model"),
          ImmutableList.of("org.eclipse.wb.internal.ercp.gef.part"));

  ////////////////////////////////////////////////////////////////////////////
  //
  // IEditPartFactory
  //
  ////////////////////////////////////////////////////////////////////////////
  public EditPart createEditPart(EditPart context, Object model) {
    // Command
    if (model instanceof CommandInfo) {
      CommandInfo command = (CommandInfo) model;
      if (command.getParent() instanceof ControlInfo) {
        IMenuPopupInfo popupObject = MenuObjectInfoUtils.getMenuPopupInfo(command);
        return MenuEditPartFactory.createPopupMenu(command, popupObject);
      } else {
        IMenuItemInfo itemObject = MenuObjectInfoUtils.getMenuItemInfo(command);
        return MenuEditPartFactory.createMenuItem(command, itemObject);
      }
    }
    // most EditPart's can be created using matching
    return MATCHING_FACTORY.createEditPart(context, model);
  }
}