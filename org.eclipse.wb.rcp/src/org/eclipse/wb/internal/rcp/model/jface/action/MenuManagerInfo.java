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
package org.eclipse.wb.internal.rcp.model.jface.action;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.broadcast.DisplayEventListener;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.menu.AbstractMenuObject;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuObjectInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPolicy;
import org.eclipse.wb.internal.core.model.menu.JavaMenuMenuObject;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;
import org.eclipse.wb.internal.core.model.menu.MenuVisualData;
import org.eclipse.wb.internal.core.utils.IAdaptable;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.rcp.model.ModelMessages;
import org.eclipse.wb.internal.swt.support.ControlSupport;
import org.eclipse.wb.internal.swt.support.ToolkitSupport;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import java.util.List;

/**
 * Model for {@link IMenuManager}.
 * 
 * @author scheglov_ke
 * @coverage rcp.model.jface
 */
public final class MenuManagerInfo extends ContributionManagerInfo
    implements
      IContributionItemInfo,
      IAdaptable {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MenuManagerInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  private MenuVisualData m_visualData;

  @Override
  protected void refresh_afterCreate() throws Exception {
    // force creation for all MenuManager's
    if (!(getParent() instanceof MenuManagerInfo)) {
      ReflectionUtils.invokeMethod(getObject(), "updateAll(boolean)", true);
    }
    // reset 'setRemoveAllWhenShown', because it causes empty menu.
    ReflectionUtils.invokeMethod2(getObject(), "setRemoveAllWhenShown", boolean.class, false);
    // process Menu widget
    {
      Menu menu = (Menu) ReflectionUtils.invokeMethod2(getObject(), "getMenu");
      // if no any items, create one
      if (menu.getItemCount() == 0) {
        new MenuItem(menu, SWT.NONE).setText(ModelMessages.MenuManagerInfo_emptyMessage);
      }
      // OK, remember as component
      setComponentObject(menu);
    }
    // process children
    super.refresh_afterCreate();
  }

  @Override
  protected void refresh_fetch() throws Exception {
    Menu menu = (Menu) ReflectionUtils.invokeMethod2(getObject(), "getMenu");
    // fetch menu visual data
    DisplayEventListener displayListener = getBroadcast(DisplayEventListener.class);
    try {
      displayListener.beforeMessagesLoop();
      m_visualData = ToolkitSupport.fetchMenuVisualData(menu);
    } finally {
      displayListener.afterMessagesLoop();
    }
    // process children
    super.refresh_fetch();
    // set child items bounds
    MenuItem[] menuItems = menu.getItems();
    for (AbstractComponentInfo contributionItem : getItems()) {
      Object contributionItemObject = contributionItem.getObject();
      for (int i = 0; i < menuItems.length; i++) {
        MenuItem menuItem = menuItems[i];
        if (menuItem.getData() == contributionItemObject) {
          contributionItem.setModelBounds(m_visualData.m_itemBounds.get(i));
          break;
        }
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if this {@link MenuManagerInfo} is represented by {@link Menu}
   *         widgets with {@link SWT#BAR} style.
   */
  public boolean isBar() {
    return ExecutionUtils.runObject(new RunnableObjectEx<Boolean>() {
      public Boolean runObject() throws Exception {
        Menu menu = (Menu) ReflectionUtils.invokeMethod2(getObject(), "getMenu");
        return ControlSupport.isStyle(menu, SWT.BAR);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IAdaptable
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IMenuItemInfo m_itemImpl = new MenuItemImpl();
  private final IMenuInfo m_menuImpl = new MenuImpl();

  public <T> T getAdapter(Class<T> adapter) {
    if (adapter.isAssignableFrom(IMenuItemInfo.class)) {
      return adapter.cast(m_itemImpl);
    }
    if (adapter.isAssignableFrom(IMenuInfo.class)) {
      return adapter.cast(m_menuImpl);
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractMenuImpl
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Abstract superclass for {@link IMenuObjectInfo} implementations.
   * 
   * @author scheglov_ke
   */
  private abstract class MenuAbstractImpl extends JavaMenuMenuObject {
    public MenuAbstractImpl() {
      super(MenuManagerInfo.this);
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // IMenuItemInfo for "this"
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Implementation of {@link IMenuItemInfo} for "this" {@link MenuManagerInfo}.
   * 
   * @author scheglov_ke
   */
  private final class MenuItemImpl extends AbstractMenuObject implements IMenuItemInfo {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public MenuItemImpl() {
      super(MenuManagerInfo.this);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Model
    //
    ////////////////////////////////////////////////////////////////////////////
    public Object getModel() {
      return MenuManagerInfo.this;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Presentation
    //
    ////////////////////////////////////////////////////////////////////////////
    public Image getImage() {
      return null;
    }

    public Rectangle getBounds() {
      return MenuManagerInfo.this.getBounds();
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // IMenuItemInfo
    //
    ////////////////////////////////////////////////////////////////////////////
    public IMenuInfo getMenu() {
      return m_menuImpl;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Policy
    //
    ////////////////////////////////////////////////////////////////////////////
    public IMenuPolicy getPolicy() {
      return IMenuPolicy.NOOP;
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // IMenuInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Implementation of {@link IMenuInfo}.
   * 
   * @author scheglov_ke
   */
  private final class MenuImpl extends MenuAbstractImpl implements IMenuInfo, IMenuPolicy {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Model
    //
    ////////////////////////////////////////////////////////////////////////////
    public Object getModel() {
      return this;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Presentation
    //
    ////////////////////////////////////////////////////////////////////////////
    public Image getImage() {
      return m_visualData.m_menuImage;
    }

    public Rectangle getBounds() {
      return m_visualData.m_menuBounds;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    public boolean isHorizontal() {
      return isBar();
    }

    public List<IMenuItemInfo> getItems() {
      List<IMenuItemInfo> items = Lists.newArrayList();
      for (AbstractComponentInfo item : MenuManagerInfo.this.getItems()) {
        IMenuItemInfo itemObject = MenuObjectInfoUtils.getMenuItemInfo(item);
        items.add(itemObject);
      }
      return items;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Policy
    //
    ////////////////////////////////////////////////////////////////////////////
    public IMenuPolicy getPolicy() {
      return this;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Validation
    //
    ////////////////////////////////////////////////////////////////////////////
    public boolean validateCreate(Object newObject) {
      return newObject instanceof ActionInfo || newObject instanceof IContributionItemInfo;
    }

    public boolean validatePaste(final Object mementoObject) {
      return false;
    }

    public boolean validateMove(Object object) {
      if (object instanceof IContributionItemInfo) {
        AbstractComponentInfo item = (AbstractComponentInfo) object;
        // don't move item on its child menu
        return !item.isParentOf(MenuManagerInfo.this);
      }
      //
      return false;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Operations
    //
    ////////////////////////////////////////////////////////////////////////////
    public void commandCreate(Object newObject, Object nextObject) throws Exception {
      AbstractComponentInfo nextItem = (AbstractComponentInfo) nextObject;
      AbstractComponentInfo newItem;
      if (newObject instanceof ActionInfo) {
        ActionInfo action = (ActionInfo) newObject;
        newItem = command_CREATE(action, nextItem);
      } else {
        newItem = (AbstractComponentInfo) newObject;
        command_CREATE(newItem, nextItem);
      }
      // schedule selection
      MenuObjectInfoUtils.setSelectingObject(newItem);
    }

    public List<?> commandPaste(Object mementoObject, Object nextObject) throws Exception {
      return ImmutableList.of();
    }

    public void commandMove(Object object, Object nextObject) throws Exception {
      AbstractComponentInfo item = (AbstractComponentInfo) object;
      AbstractComponentInfo nextItem = (AbstractComponentInfo) nextObject;
      command_MOVE(item, nextItem);
      // schedule selection
      MenuObjectInfoUtils.setSelectingObject(item);
    }
  }
}
