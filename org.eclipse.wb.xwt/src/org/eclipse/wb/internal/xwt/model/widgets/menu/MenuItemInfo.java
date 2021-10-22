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

import com.google.common.collect.ImmutableList;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildAddBefore;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.menu.AbstractMenuObject;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPolicy;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.IAdaptable;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.state.GlobalState;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.broadcast.GenericPropertySetExpression;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectAdd;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectClipboardCopy;
import org.eclipse.wb.internal.core.xml.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.xml.model.clipboard.XmlObjectMemento;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.creation.ElementCreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.property.GenericProperty;
import org.eclipse.wb.internal.core.xml.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;
import org.eclipse.wb.internal.xwt.model.widgets.ItemInfo;
import org.eclipse.wb.internal.xwt.model.widgets.XwtLiveManager;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import java.util.List;

/**
 * Model for {@link MenuItem}.
 *
 * @author scheglov_ke
 * @coverage XWT.model.widgets
 */
public final class MenuItemInfo extends ItemInfo implements IAdaptable {
  private final MenuItemInfo m_this = this;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MenuItemInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(context, description, creationSupport);
    addBroadcastListeners();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Broadcasts
  //
  ////////////////////////////////////////////////////////////////////////////
  private void addBroadcastListeners() {
    addBroadcastListener(m_stylePropertyListener);
    manageItemStyle_hasSubMenu();
    addClipboardSupport();
    // add child menu for item creation with SWT.CASCADE style
    addBroadcastListener(new XmlObjectAdd() {
      @Override
      public void after(ObjectInfo parent, XmlObjectInfo child) throws Exception {
        if (child == m_this) {
          if (XmlObjectUtils.hasTrueParameter(m_this, "MenuItem.createCascadeMenu")) {
            addSubMenu();
          }
          removeBroadcastListener(this);
        }
      }
    });
  }

  private void addClipboardSupport() {
    addBroadcastListener(new XmlObjectClipboardCopy() {
      public void invoke(XmlObjectInfo object, List<ClipboardCommand> commands) throws Exception {
        if (object == m_this) {
          MenuInfo menu = getSubMenu();
          if (menu != null) {
            final XmlObjectMemento menuMemento = XmlObjectMemento.createMemento(menu);
            commands.add(new ClipboardCommand() {
              private static final long serialVersionUID = 0L;

              @Override
              public void execute(XmlObjectInfo object) throws Exception {
                MenuInfo menu = (MenuInfo) menuMemento.create(object);
                menu.commandCreate((MenuItemInfo) object);
                menuMemento.apply();
              }
            });
          }
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Sub menu related
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the sub-menu associated with this {@link MenuItemInfo}, or <code>null</code> is it has
   *         no sub-menu.
   */
  public MenuInfo getSubMenu() {
    List<MenuInfo> subMenus = getChildren(MenuInfo.class);
    Assert.isLegal(subMenus.size() <= 1);
    return !subMenus.isEmpty() ? subMenus.get(0) : null;
  }

  /**
   * Adds a sub-menu to this item child. This required to items with SWT.CASCADE style set.
   */
  private void addSubMenu() throws Exception {
    if (getSubMenu() == null) {
      MenuInfo subMenu =
          (MenuInfo) XmlObjectUtils.createObject(
              getContext(),
              Menu.class,
              new ElementCreationSupport());
      subMenu.commandCreate(this);
    }
  }

  /**
   * Removes sub-menu of this item, if any.
   */
  private void deleteSubMenu() throws Exception {
    MenuInfo subMenu = getSubMenu();
    if (subMenu != null) {
      subMenu.delete();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Live support
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected XwtLiveManager getLiveComponentsManager() {
    return new MenuItemLiveManager(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IObjectPresentation m_presentation = new MenuItemStylePresentation(this);

  @Override
  public IObjectPresentation getPresentation() {
    return m_presentation;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Style change listener
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Object m_stylePropertyListener = new GenericPropertySetExpression() {
    public void invoke(GenericPropertyImpl property,
        String[] expression,
        Object[] value,
        boolean[] shouldSet) throws Exception {
      if (property.getObject() == m_this
          && !XmlObjectMemento.isApplying(m_this)
          && "Style".equals(property.getTitle())) {
        final boolean wasCascade = ((Integer) property.getValue() & SWT.CASCADE) != 0;
        final boolean wasSeparator = ((Integer) property.getValue() & SWT.SEPARATOR) != 0;
        final boolean becomesCascade = expression[0].indexOf("CASCADE") != -1;
        final boolean becomesSeparator = expression[0].indexOf("SEPARATOR") != -1;
        // do nothing if SWT.CASCADE/SEPARATOR neither set nor reset
        if (wasCascade == becomesCascade && wasSeparator == becomesSeparator) {
          return;
        }
        // OK, we have something to change
        ExecutionUtils.run(m_this, new RunnableEx() {
          public void run() throws Exception {
            // add/remove subMenu
            if (becomesCascade) {
              addSubMenu();
            } else {
              deleteSubMenu();
            }
            // remove "setText" when setting SWT.SEPARATOR
            if (becomesSeparator) {
              getPropertyByTitle("text").setValue(Property.UNKNOWN_VALUE);
            }
          }
        });
      }
    }
  };

  /**
   * Manages {@link MenuItem} depending on if it has or not {@link Menu} child.
   */
  private void manageItemStyle_hasSubMenu() {
    addBroadcastListener(new ObjectEventListener() {
      @Override
      public void childRemoveAfter(ObjectInfo parent, ObjectInfo child) throws Exception {
        // Menu_Info removed from this MenuItem_Info, remove CASCADE
        if (parent == m_this && child instanceof MenuInfo) {
          setStyleSource(null);
        }
      }
    });
    addBroadcastListener(new ObjectInfoChildAddBefore() {
      public void invoke(ObjectInfo parent, ObjectInfo child, ObjectInfo[] nextChild)
          throws Exception {
        if (GlobalState.isParsing()) {
          return;
        }
        // Menu_Info moved to this MenuItem_Info, use CASCADE
        if (parent == m_this && child instanceof MenuInfo) {
          setStyleSource("CASCADE");
        }
      }
    });
  }

  /**
   * Sets new value of "style" property.
   */
  private void setStyleSource(String source) throws Exception {
    removeBroadcastListener(m_stylePropertyListener);
    try {
      GenericProperty styleProperty = (GenericProperty) getPropertyByTitle("Style");
      styleProperty.setExpression(source, Property.UNKNOWN_VALUE);
    } finally {
      addBroadcastListener(m_stylePropertyListener);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IAdaptable
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IMenuItemInfo m_itemImpl = new MenuItemImpl();

  public <T> T getAdapter(Class<T> adapter) {
    if (adapter.isAssignableFrom(IMenuItemInfo.class)) {
      return adapter.cast(m_itemImpl);
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IMenuItemInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Implementation of {@link IMenuItemInfo}.
   *
   * @author scheglov_ke
   */
  private final class MenuItemImpl extends AbstractMenuObject implements IMenuItemInfo, IMenuPolicy {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public MenuItemImpl() {
      super(m_this);
    }

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
      return m_this.getImage();
    }

    public Rectangle getBounds() {
      return m_this.getBounds();
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // IMenuItemInfo
    //
    ////////////////////////////////////////////////////////////////////////////
    public IMenuInfo getMenu() {
      MenuInfo subMenu = getSubMenu();
      return MenuObjectInfoUtils.getMenuInfo(subMenu);
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
    public boolean validateCreate(Object object) {
      // nothing can be dropped on MenuItem
      return false;
    }

    public boolean validatePaste(Object mementoObject) {
      // nothing can be dropped on MenuItem
      return false;
    }

    public boolean validateMove(Object object) {
      if (object instanceof MenuInfo) {
        MenuInfo menuInfo = (MenuInfo) object;
        // don't move Menu on its child Item
        if (menuInfo.isParentOf(m_this)) {
          return false;
        }
        return true;
      }
      // not a Menu_Info
      return false;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Operations
    //
    ////////////////////////////////////////////////////////////////////////////
    public void commandCreate(Object object, Object nextObject) throws Exception {
    }

    public List<?> commandPaste(Object mementoObject, Object nextObject) throws Exception {
      return ImmutableList.of();
    }

    public void commandMove(Object object, Object nextObject) throws Exception {
      MenuInfo menuInfo = (MenuInfo) object;
      menuInfo.commandMove(m_this);
      // schedule selection
      MenuObjectInfoUtils.setSelectingObject(menuInfo);
    }
  }
}
