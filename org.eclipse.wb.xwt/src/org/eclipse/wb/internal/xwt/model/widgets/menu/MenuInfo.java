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

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.broadcast.DisplayEventListener;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuObjectInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPolicy;
import org.eclipse.wb.internal.core.model.menu.IMenuPopupInfo;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;
import org.eclipse.wb.internal.core.model.menu.MenuVisualData;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.utils.IAdaptable;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlMenuMenuObject;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.association.Association;
import org.eclipse.wb.internal.core.xml.model.association.Associations;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectClipboardCopy;
import org.eclipse.wb.internal.core.xml.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.xml.model.clipboard.XmlObjectMemento;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;
import org.eclipse.wb.internal.swt.support.MenuSupport;
import org.eclipse.wb.internal.swt.support.ToolkitSupport;
import org.eclipse.wb.internal.xwt.model.widgets.WidgetInfo;
import org.eclipse.wb.internal.xwt.model.widgets.XwtLiveManager;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;

import java.util.List;

/**
 * Model for {@link Menu}.
 *
 * @author scheglov_ke
 * @coverage XWT.model.widgets
 */
public final class MenuInfo extends WidgetInfo implements IAdaptable {
  private final MenuInfo m_this = this;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MenuInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(context, description, creationSupport);
    addClipboardSupport();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Broadcasts
  //
  ////////////////////////////////////////////////////////////////////////////
  private void addClipboardSupport() {
    addBroadcastListener(new XmlObjectClipboardCopy() {
      public void invoke(XmlObjectInfo object, List<ClipboardCommand> commands) throws Exception {
        if (object == m_this) {
          for (MenuItemInfo item : getItems()) {
            final XmlObjectMemento itemMemento = XmlObjectMemento.createMemento(item);
            commands.add(new ClipboardCommand() {
              private static final long serialVersionUID = 0L;

              @Override
              public void execute(XmlObjectInfo object) throws Exception {
                MenuItemInfo item = (MenuItemInfo) itemMemento.create(object);
                IMenuPolicy policy = MenuObjectInfoUtils.getMenuInfo(object).getPolicy();
                policy.commandCreate(item, null);
                itemMemento.apply();
              }
            });
          }
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link MenuItemInfo} children.
   */
  public List<MenuItemInfo> getItems() {
    return getChildren(MenuItemInfo.class);
  }

  /**
   * @return <code>true</code> if this {@link MenuInfo} is bar menu.
   */
  public boolean isBar() {
    return (getStyle() & SWT.BAR) != 0;
  }

  /**
   * @return <code>true</code> if this {@link MenuInfo} is popup menu.
   */
  public boolean isPopup() {
    return (getStyle() & SWT.POP_UP) != 0;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_afterCreate() throws Exception {
    // add a placeholder
    Object[] items = MenuSupport.getItems(getObject());
    if (items.length == 0) {
      MenuSupport.addPlaceholder(getObject());
    }
    super.refresh_afterCreate();
  }

  @Override
  protected void refresh_fetch() throws Exception {
    if (getContext().isLiveComponent()) {
      return;
    }
    // fetch menu visual data
    DisplayEventListener displayListener = getBroadcast(DisplayEventListener.class);
    MenuVisualData visualData = null;
    try {
      displayListener.beforeMessagesLoop();
      visualData = ToolkitSupport.fetchMenuVisualData(getObject());
    } finally {
      displayListener.afterMessagesLoop();
    }
    setModelBounds(visualData.m_menuBounds);
    setBounds(visualData.m_menuBounds);
    setImage(visualData.m_menuImage);
    // set child items bounds
    List<MenuItemInfo> items = getItems();
    for (int i = 0; i < items.size(); ++i) {
      MenuItemInfo itemInfo = items.get(i);
      itemInfo.setModelBounds(visualData.m_itemBounds.get(i));
    }
    super.refresh_fetch();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Live support
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected XwtLiveManager getLiveComponentsManager() {
    return new MenuLiveManager(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IObjectPresentation m_presentation = new MenuStylePresentation(this);

  @Override
  public IObjectPresentation getPresentation() {
    return m_presentation;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds this new {@link MenuInfo} on given {@link WidgetInfo}.
   */
  public void commandCreate(WidgetInfo parent) throws Exception {
    Association association;
    if (isBar()) {
      association = Associations.property("menuBar");
    } else {
      association = Associations.property("menu");
    }
    XmlObjectUtils.add(this, association, parent, null);
  }

  /**
   * Moves this existing {@link MenuInfo} on given {@link WidgetInfo}.
   */
  public void commandMove(WidgetInfo parent) throws Exception {
    XmlObjectUtils.move(this, Associations.property("menu"), parent, null);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IAdaptable
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IMenuPopupInfo m_popupImpl = new MenuPopupImpl();
  private final IMenuInfo m_menuImpl = new MenuImpl();

  public <T> T getAdapter(Class<T> adapter) {
    if (adapter.isAssignableFrom(IMenuInfo.class)) {
      return adapter.cast(m_menuImpl);
    }
    if (adapter.isAssignableFrom(IMenuPopupInfo.class)) {
      return adapter.cast(m_popupImpl);
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
  private abstract class MenuAbstractImpl extends XmlMenuMenuObject {
    public MenuAbstractImpl() {
      super(m_this);
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // IMenuPopupInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Implementation of {@link IMenuPopupInfo}.
   *
   * @author scheglov_ke
   */
  private final class MenuPopupImpl extends MenuAbstractImpl implements IMenuPopupInfo {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Model
    //
    ////////////////////////////////////////////////////////////////////////////
    public Object getModel() {
      return m_this;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Presentation
    //
    ////////////////////////////////////////////////////////////////////////////
    public Image getImage() {
      return ExecutionUtils.runObjectLog(new RunnableObjectEx<Image>() {
        public Image runObject() throws Exception {
          return getPresentation().getIcon();
        }
      }, getDescription().getIcon());
    }

    public Rectangle getBounds() {
      Image image = getImage();
      return new Rectangle(0, 0, image.getBounds().width, image.getBounds().height);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // IMenuPopupInfo
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
      return isPopup() ? this : m_this;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Presentation
    //
    ////////////////////////////////////////////////////////////////////////////
    public Image getImage() {
      return m_this.getImage();
    }

    public Rectangle getBounds() {
      return m_this.getBounds();
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
      for (MenuItemInfo item : m_this.getItems()) {
        items.add(MenuObjectInfoUtils.getMenuItemInfo(item));
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
      return newObject instanceof MenuItemInfo;
    }

    @SuppressWarnings("unchecked")
    public boolean validatePaste(final Object mementoObject) {
      return ExecutionUtils.runObjectLog(new RunnableObjectEx<Boolean>() {
        public Boolean runObject() throws Exception {
          List<XmlObjectMemento> mementos = (List<XmlObjectMemento>) mementoObject;
          for (XmlObjectMemento memento : mementos) {
            XmlObjectInfo component = memento.create(m_this);
            if (!(component instanceof MenuItemInfo)) {
              return false;
            }
          }
          return true;
        }
      }, false);
    }

    public boolean validateMove(Object object) {
      if (object instanceof MenuItemInfo) {
        MenuItemInfo item = (MenuItemInfo) object;
        // don't move item on its child menu
        return !item.isParentOf(m_this);
      }
      return false;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Operations
    //
    ////////////////////////////////////////////////////////////////////////////
    public void commandCreate(Object newObject, Object nextObject) throws Exception {
      MenuItemInfo newItem = (MenuItemInfo) newObject;
      MenuItemInfo nextItem = (MenuItemInfo) nextObject;
      XmlObjectUtils.add(newItem, Associations.direct(), m_this, nextItem);
      // schedule selection
      MenuObjectInfoUtils.setSelectingObject(newItem);
    }

    @SuppressWarnings("unchecked")
    public List<?> commandPaste(Object mementoObject, Object nextObject) throws Exception {
      List<MenuItemInfo> pastedObjects = Lists.newArrayList();
      List<XmlObjectMemento> mementos = (List<XmlObjectMemento>) mementoObject;
      for (XmlObjectMemento memento : mementos) {
        MenuItemInfo item = (MenuItemInfo) memento.create(m_this);
        commandCreate(item, nextObject);
        memento.apply();
        pastedObjects.add(item);
      }
      return pastedObjects;
    }

    public void commandMove(Object object, Object nextObject) throws Exception {
      MenuItemInfo item = (MenuItemInfo) object;
      MenuItemInfo nextItem = (MenuItemInfo) nextObject;
      XmlObjectUtils.move(item, Associations.direct(), m_this, nextItem);
      // schedule selection
      MenuObjectInfoUtils.setSelectingObject(item);
    }
  }
}
