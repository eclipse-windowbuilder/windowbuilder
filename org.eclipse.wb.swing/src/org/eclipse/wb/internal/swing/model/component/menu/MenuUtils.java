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
package org.eclipse.wb.internal.swing.model.component.menu;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;
import org.eclipse.wb.internal.core.model.menu.MenuVisualData;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import java.util.List;

/**
 * Utilities for Swing menu.
 * 
 * @author scheglov_ke
 * @coverage swing.model.menu
 */
public final class MenuUtils {
  private static final String KEY_MENU_UTILS_ITEM = "Surrogate IMenuItemInfo for ComponentInfo";

  /**
   * Sets bounds for {@link ComponentInfo} children of some menu.
   */
  public static void setItemsBounds(MenuVisualData visualData, List<ComponentInfo> items) {
    for (int i = 0; i < items.size(); i++) {
      ComponentInfo item = items.get(i);
      item.setModelBounds(visualData.m_itemBounds.get(i));
    }
  }

  /**
   * @param container
   *          some menu container, {@link JMenuInfo} or {@link JPopupMenuInfo}.
   * 
   * @return the {@link IMenuItemInfo}'s of given menu container.
   */
  public static List<IMenuItemInfo> getItems(ContainerInfo container) {
    List<IMenuItemInfo> items = Lists.newArrayList();
    for (ComponentInfo component : container.getChildrenComponents()) {
      IMenuItemInfo item = getMenuItem(component);
      items.add(item);
    }
    return items;
  }

  /**
   * @param component
   *          some {@link ComponentInfo}, may be menu related, may be just generic.
   * 
   * @return the {@link IMenuItemInfo} wrapper for given {@link ComponentInfo}.
   */
  public static IMenuItemInfo getMenuItem(ComponentInfo component) {
    if (component instanceof JMenuInfo) {
      return MenuObjectInfoUtils.getMenuItemInfo(component);
    } else if (component instanceof JMenuItemInfo) {
      return MenuObjectInfoUtils.getMenuItemInfo(component);
    } else {
      IMenuItemInfo item = (IMenuItemInfo) component.getArbitraryValue(KEY_MENU_UTILS_ITEM);
      if (item == null) {
        item = new ComponentMenuItemInfo(component);
        component.putArbitraryValue(KEY_MENU_UTILS_ITEM, item);
      }
      return item;
    }
  }

  /**
   * Sets {@link ComponentInfo} which is menu item and should be selected (and expanded) during
   * refresh.
   */
  public static void setSelectingItem(ComponentInfo component) {
    IMenuItemInfo item = getMenuItem(component);
    MenuObjectInfoUtils.setSelectingObject(item);
  }

  /**
   * Adds broadcast listener for copy/paste items of menu container.
   * 
   * @param container
   *          some menu container, {@link JMenuInfo} or {@link JPopupMenuInfo}.
   */
  public static void copyPasteItems(final ContainerInfo container) {
    container.addBroadcastListener(new JavaEventListener() {
      @Override
      public void clipboardCopy(JavaInfo javaInfo, List<ClipboardCommand> commands)
          throws Exception {
        if (javaInfo == container) {
          for (ComponentInfo item : container.getChildrenComponents()) {
            final JavaInfoMemento memento = JavaInfoMemento.createMemento(item);
            commands.add(new ClipboardCommand() {
              private static final long serialVersionUID = 0L;

              @Override
              public void execute(JavaInfo javaInfo) throws Exception {
                IMenuInfo menuObject;
                if (javaInfo instanceof JMenuInfo) {
                  menuObject = MenuObjectInfoUtils.getMenuInfo(javaInfo);
                } else {
                  menuObject = MenuObjectInfoUtils.getMenuPopupInfo(javaInfo).getMenu();
                }
                // paste item
                ComponentInfo item = (ComponentInfo) memento.create(javaInfo);
                menuObject.getPolicy().commandCreate(item, null);
                memento.apply();
              }
            });
          }
        }
      }
    });
  }
}
