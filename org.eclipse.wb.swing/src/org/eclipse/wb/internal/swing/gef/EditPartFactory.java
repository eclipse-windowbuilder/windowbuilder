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
package org.eclipse.wb.internal.swing.gef;

import com.google.common.collect.ImmutableList;

import org.eclipse.wb.core.gef.MatchingEditPartFactory;
import org.eclipse.wb.core.gef.part.menu.MenuEditPartFactory;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartFactory;
import org.eclipse.wb.internal.core.model.creation.factory.StaticFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.description.factory.FactoryMethodDescription;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPopupInfo;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;
import org.eclipse.wb.internal.swing.gef.part.ComponentEditPart;
import org.eclipse.wb.internal.swing.gef.part.ContainerEditPart;
import org.eclipse.wb.internal.swing.gef.part.JScrollPaneEditPart;
import org.eclipse.wb.internal.swing.gef.part.JSplitPaneEditPart;
import org.eclipse.wb.internal.swing.gef.part.JTabbedPaneEditPart;
import org.eclipse.wb.internal.swing.gef.part.JTabbedPaneTabEditPart;
import org.eclipse.wb.internal.swing.gef.part.box.BoxGlueEditPart;
import org.eclipse.wb.internal.swing.gef.part.box.BoxGlueHorizontalEditPart;
import org.eclipse.wb.internal.swing.gef.part.box.BoxGlueVerticalEditPart;
import org.eclipse.wb.internal.swing.gef.part.box.BoxRigidAreaEditPart;
import org.eclipse.wb.internal.swing.gef.part.box.BoxStrutHorizontalEditPart;
import org.eclipse.wb.internal.swing.gef.part.box.BoxStrutVerticalEditPart;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.component.JScrollPaneInfo;
import org.eclipse.wb.internal.swing.model.component.JSplitPaneInfo;
import org.eclipse.wb.internal.swing.model.component.JTabbedPaneInfo;
import org.eclipse.wb.internal.swing.model.component.JTabbedPaneTabInfo;
import org.eclipse.wb.internal.swing.model.component.menu.JMenuBarInfo;
import org.eclipse.wb.internal.swing.model.component.menu.JMenuInfo;
import org.eclipse.wb.internal.swing.model.component.menu.JMenuItemInfo;
import org.eclipse.wb.internal.swing.model.component.menu.JPopupMenuInfo;

import javax.swing.Box;

/**
 * Implementation of {@link IEditPartFactory} for Swing.
 * 
 * @author scheglov_ke
 * @coverage swing.gef
 */
public final class EditPartFactory implements IEditPartFactory {
  private final IEditPartFactory[] FACTORIES = {
      MENU_FACTORY,
      BOX_FACTORY,
      SPECIAL_FACTORY,
      MATCHING_FACTORY,
      GENERIC_FACTORY};

  ////////////////////////////////////////////////////////////////////////////
  //
  // IEditPartFactory
  //
  ////////////////////////////////////////////////////////////////////////////
  public EditPart createEditPart(EditPart context, Object model) {
    for (IEditPartFactory factory : FACTORIES) {
      EditPart editPart = factory.createEditPart(null, model);
      if (editPart != null) {
        return editPart;
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Factories
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final IEditPartFactory MENU_FACTORY = new IEditPartFactory() {
    public EditPart createEditPart(EditPart context, Object model) {
      if (model instanceof JMenuBarInfo) {
        JMenuBarInfo menu = (JMenuBarInfo) model;
        IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menu);
        return MenuEditPartFactory.createMenu(model, menuObject);
      }
      if (model instanceof JPopupMenuInfo) {
        JPopupMenuInfo popup = (JPopupMenuInfo) model;
        IMenuPopupInfo popupObject = MenuObjectInfoUtils.getMenuPopupInfo(popup);
        return MenuEditPartFactory.createPopupMenu(popup, popupObject);
      }
      if (model instanceof IMenuInfo) {
        IMenuInfo menu = (IMenuInfo) model;
        return MenuEditPartFactory.createMenu(model, menu);
      }
      if (model instanceof JMenuInfo) {
        JMenuInfo menu = (JMenuInfo) model;
        IMenuItemInfo itemObject = MenuObjectInfoUtils.getMenuItemInfo(menu);
        return MenuEditPartFactory.createMenuItem(menu, itemObject);
      }
      if (model instanceof JMenuItemInfo) {
        JMenuItemInfo item = (JMenuItemInfo) model;
        IMenuItemInfo itemObject = MenuObjectInfoUtils.getMenuItemInfo(item);
        return MenuEditPartFactory.createMenuItem(item, itemObject);
      }
      return null;
    }
  };
  private static final IEditPartFactory SPECIAL_FACTORY = new IEditPartFactory() {
    public EditPart createEditPart(EditPart context, Object model) {
      if (model instanceof JSplitPaneInfo) {
        return new JSplitPaneEditPart((JSplitPaneInfo) model);
      }
      if (model instanceof JScrollPaneInfo) {
        return new JScrollPaneEditPart((JScrollPaneInfo) model);
      }
      if (model instanceof JTabbedPaneInfo) {
        return new JTabbedPaneEditPart((JTabbedPaneInfo) model);
      }
      if (model instanceof JTabbedPaneTabInfo) {
        return new JTabbedPaneTabEditPart((JTabbedPaneTabInfo) model);
      }
      return null;
    }
  };
  private static final IEditPartFactory BOX_FACTORY = new IEditPartFactory() {
    public EditPart createEditPart(EditPart context, Object model) {
      if (model instanceof ComponentInfo) {
        ComponentInfo component = (ComponentInfo) model;
        if (component.getCreationSupport() instanceof StaticFactoryCreationSupport) {
          StaticFactoryCreationSupport factoryCreationSupport =
              (StaticFactoryCreationSupport) component.getCreationSupport();
          FactoryMethodDescription factoryMethodDescription =
              factoryCreationSupport.getDescription();
          if (factoryMethodDescription.getDeclaringClass() == Box.class) {
            String signature = factoryMethodDescription.getSignature();
            return createEditPart(component, signature);
          }
        }
      }
      return null;
    }

    private EditPart createEditPart(ComponentInfo component, String signature) {
      // glue
      if (signature.equals("createGlue()")) {
        return new BoxGlueEditPart(component);
      }
      if (signature.equals("createHorizontalGlue()")) {
        return new BoxGlueHorizontalEditPart(component);
      }
      if (signature.equals("createVerticalGlue()")) {
        return new BoxGlueVerticalEditPart(component);
      }
      // strut
      if (signature.equals("createRigidArea(java.awt.Dimension)")) {
        return new BoxRigidAreaEditPart(component);
      }
      if (signature.equals("createHorizontalStrut(int)")) {
        return new BoxStrutHorizontalEditPart(component);
      }
      if (signature.equals("createVerticalStrut(int)")) {
        return new BoxStrutVerticalEditPart(component);
      }
      // unknown
      return null;
    }
  };
  private final static IEditPartFactory MATCHING_FACTORY =
      new MatchingEditPartFactory(ImmutableList.of("org.eclipse.wb.internal.swing.model.component"),
          ImmutableList.of("org.eclipse.wb.internal.swing.gef.part"));
  private static final IEditPartFactory GENERIC_FACTORY = new IEditPartFactory() {
    public EditPart createEditPart(EditPart context, Object model) {
      if (model instanceof ContainerInfo) {
        return new ContainerEditPart((ContainerInfo) model);
      }
      if (model instanceof ComponentInfo) {
        return new ComponentEditPart((ComponentInfo) model);
      }
      return null;
    }
  };
}
