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
package org.eclipse.wb.internal.core.utils.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Event;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Instances compute the intersection of some menus; that is, items common to all the menus. It
 * produces composite menu items that can be added to a menu, and that when invoked simply run the
 * corresponding items from the original menus.
 *
 * @author lobas_av
 * @coverage core.ui
 */
public class MenuIntersector {
  private List<IMenuElement> m_elements;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Merge Menu
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Merge {@link List} of <code>menus</code> and add result to <code>mainMenu</code>.
   */
  public static void merge(IMenuManager mainMenu, List<IMenuManager> menus) {
    MenuIntersector intersector = new MenuIntersector();
    for (IMenuManager menu : menus) {
      intersector.merge(menu);
    }
    intersector.addToMenu(mainMenu);
  }

  /**
   * Add given <code>menu</code> to merge.
   */
  public void merge(IMenuManager menu) {
    IContributionItem[] items = menu.getItems();
    if (m_elements == null) {
      m_elements = Lists.newArrayList();
      for (IContributionItem item : items) {
        // create wrapper's
        if (SeparatorElement.accept(item)) {
          m_elements.add(new SeparatorElement(item));
        } else if (MenuElement.accept(item)) {
          m_elements.add(new MenuElement(item));
        } else if (MenuItemElement.accept(item)) {
          m_elements.add(new MenuItemElement(item));
        }
      }
    } else {
      for (Iterator<IMenuElement> I = m_elements.iterator(); I.hasNext();) {
        IMenuElement element = I.next();
        // merge menu
        if (!element.incorporate(menu)) {
          I.remove();
        }
      }
    }
  }

  /**
   * Add result of merge to given <code>menu</code>.
   */
  public void addToMenu(IMenuManager menu) {
    for (IMenuElement element : m_elements) {
      element.addToMenu(menu);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Any menu item wrapper.
   */
  private static interface IMenuElement {
    /**
     * Incorporate a menu in this instance if it matches.
     */
    boolean incorporate(IMenuManager menu);

    /**
     * Add wrapped items to given <code>menu</code>.
     */
    void addToMenu(IMenuManager menu);
  }
  /**
   * Abstract wrapper for menu and menu items.
   */
  private static abstract class AbstractMenuElement implements IMenuElement {
    ////////////////////////////////////////////////////////////////////////////
    //
    // IMenuElement
    //
    ////////////////////////////////////////////////////////////////////////////
    public final boolean incorporate(IMenuManager menu) {
      IContributionItem[] items = menu.getItems();
      for (int i = 0; i < items.length; ++i) {
        IContributionItem item = items[i];
        // skip separator's
        if (SeparatorElement.accept(item)) {
          continue;
        }
        // merge
        if (incorporate(item)) {
          return true;
        }
      }
      return false;
    }

    /**
     * Incorporate a menu item in this instance if it matches.
     */
    protected abstract boolean incorporate(IContributionItem item);
  }
  /**
   * A wrapper for a menu.
   */
  private static class MenuElement extends AbstractMenuElement {
    private final MenuIntersector m_intersector = new MenuIntersector();
    private String m_menuTitle;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public MenuElement(IContributionItem item) {
      incorporate(item);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // AbstractMenuElement
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected boolean incorporate(IContributionItem item) {
      if (accept(item)) {
        MenuManager menu = (MenuManager) item;
        // first add
        if (m_menuTitle == null) {
          m_menuTitle = menu.getMenuText();
          m_intersector.merge(menu);
          return true;
        }
        // add equals
        if (m_menuTitle.equals(menu.getMenuText())) {
          m_intersector.merge(menu);
          return true;
        }
      }
      return false;
    }

    /**
     * @return <code>true</code> if given <code>item</code> is {@link MenuManager}.
     */
    public static boolean accept(IContributionItem item) {
      return item instanceof MenuManager;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // IMenuElement
    //
    ////////////////////////////////////////////////////////////////////////////
    public void addToMenu(IMenuManager menu) {
      // create sub menu
      MenuManager subMenu = new MenuManager(m_menuTitle);
      m_intersector.addToMenu(subMenu);
      // add to parent
      if (!subMenu.isEmpty()) {
        menu.add(subMenu);
      }
    }
  }
  /**
   * A wrapper for a {@link IAction}.
   */
  private static class MenuItemElement extends AbstractMenuElement {
    private final Collection<IAction> m_actions = Sets.newHashSet();

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public MenuItemElement(IContributionItem item) {
      incorporate(item);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // AbstractMenuElement
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected boolean incorporate(IContributionItem item) {
      if (accept(item)) {
        // prepare actions
        ActionContributionItem actionItem = (ActionContributionItem) item;
        IAction action = actionItem.getAction();
        // first add
        if (m_actions.isEmpty()) {
          m_actions.add(action);
          return true;
        }
        IAction original = getAction();
        // add if "look" equals
        if (!action.getText().equals(original.getText())) {
          return false;
        }
        if (action.getStyle() != original.getStyle()) {
          return false;
        }
        // add next "look" action
        if (!(action instanceof IActionSingleton)) {
          m_actions.add(action);
        }
        return true;
      }
      return false;
    }

    /**
     * @return first wrapped action.
     */
    private IAction getAction() {
      return m_actions.iterator().next();
    }

    /**
     * @return <code>true</code> if given <code>item</code> is {@link IAction}.
     */
    public static boolean accept(IContributionItem item) {
      return item instanceof ActionContributionItem;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // IMenuElement
    //
    ////////////////////////////////////////////////////////////////////////////
    public void addToMenu(IMenuManager menu) {
      IAction original = getAction();
      // create "call wrapper" action
      IAction action = new Action(original.getText(), original.getStyle()) {
        @Override
        public void runWithEvent(Event event) {
          updateChecked();
          for (IAction innerAction : m_actions) {
            innerAction.runWithEvent(event);
          }
        }

        @Override
        public void run() {
          updateChecked();
          for (IAction innerAction : m_actions) {
            innerAction.run();
          }
        }

        private void updateChecked() {
          for (IAction innerAction : m_actions) {
            innerAction.setChecked(isChecked());
          }
        }
      };
      // calculate checked
      boolean checked = true;
      for (IAction innerAction : m_actions) {
        checked &= innerAction.isChecked();
      }
      // fill action properties
      action.setChecked(checked);
      action.setImageDescriptor(original.getImageDescriptor());
      action.setHoverImageDescriptor(original.getHoverImageDescriptor());
      action.setDisabledImageDescriptor(original.getDisabledImageDescriptor());
      action.setEnabled(original.isEnabled());
      // add to menu
      menu.add(action);
    }
  }
  /**
   * A wrapper for a marker separating menu items.
   */
  private static class SeparatorElement implements IMenuElement {
    private final IContributionItem m_item;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public SeparatorElement(IContributionItem item) {
      m_item = item;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // IMenuElement
    //
    ////////////////////////////////////////////////////////////////////////////
    public boolean incorporate(IMenuManager menu) {
      return true;
    }

    public void addToMenu(IMenuManager menu) {
      menu.add(m_item);
    }

    /**
     * @return <code>true</code> if given <code>item</code> is separator.
     */
    public static boolean accept(IContributionItem item) {
      return item.isSeparator() || item.isGroupMarker();
    }
  }
}