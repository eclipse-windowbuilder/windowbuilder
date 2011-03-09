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
package org.eclipse.wb.internal.ercp.model.widgets.mobile;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.menu.AbstractMenuObject;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuObjectInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPolicy;
import org.eclipse.wb.internal.core.model.menu.IMenuPopupInfo;
import org.eclipse.wb.internal.core.model.menu.JavaMenuMenuObject;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;
import org.eclipse.wb.internal.core.utils.IAdaptable;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.DrawUtils;
import org.eclipse.wb.internal.ercp.Activator;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.model.widgets.ItemInfo;
import org.eclipse.wb.internal.swt.support.ToolkitSupport;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

import java.util.Iterator;
import java.util.List;

/**
 * Model for eSWT {@link org.eclipse.wb.gef.core.ercp.swt.mobile.Command}.
 * 
 * @author scheglov_ke
 * @coverage ercp.model.widgets.mobile
 */
public final class CommandInfo extends ItemInfo implements IAdaptable {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CommandInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    preventMemoryLeaks();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Events
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * eRCP has memory leaks related with <code>Command</code>.
   */
  private void preventMemoryLeaks() {
    addBroadcastListener(new ObjectEventListener() {
      /**
       * eRCP has leak: it does not remove disposed ControlNode objects, so keeps references on
       * Control objects, so holds ClassLoader in memory.
       */
      private void removeDisposed_ControlNodes(Object controlNode) throws Exception {
        List<?> childs = (List<?>) ReflectionUtils.getFieldObject(controlNode, "childs");
        for (Iterator<?> I = childs.iterator(); I.hasNext();) {
          Object child = I.next();
          if ((Boolean) ReflectionUtils.invokeMethod(child, "isDispose()")) {
            I.remove();
          } else {
            removeDisposed_ControlNodes(child);
          }
        }
      }

      @Override
      public void refreshDispose() throws Exception {
        Object objectCommandHandle = getCommandHandle();
        // remove disposed ControlNode-s, from root
        {
          Object rootControlNode = ReflectionUtils.getFieldObject(objectCommandHandle, "root");
          removeDisposed_ControlNodes(rootControlNode);
        }
        // also eRCP does not remove Command-s from "CommandHandle.commandList"
        {
          Object thisCommand = getObject();
          List<?> commandList =
              (List<?>) ReflectionUtils.getFieldObject(objectCommandHandle, "commandList");
          commandList.remove(thisCommand);
        }
      }

      private Object getCommandHandle() throws ClassNotFoundException {
        ClassLoader classLoader = JavaInfoUtils.getClassLoader(CommandInfo.this);
        Class<?> classCommand = classLoader.loadClass("org.eclipse.ercp.swt.mobile.Command");
        return ReflectionUtils.getFieldObject(classCommand, "cmdHandle");
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  private Image m_itemImage;
  private Rectangle m_itemBounds;

  /**
   * @return <code>true</code> if this {@link CommandInfo} is group for other {@link CommandInfo}'s.
   */
  public boolean isGroup() {
    return ExecutionUtils.runObjectLog(new RunnableObjectEx<Boolean>() {
      public Boolean runObject() throws Exception {
        Object command = getObject();
        int type = ReflectionUtils.getFieldInt(command, "type");
        return type == ReflectionUtils.getFieldInt(command, "COMMANDGROUP");
      }
    }, false);
  }

  /**
   * @return the {@link CommandInfo} children.
   */
  public List<CommandInfo> getChildrenCommands() {
    return getChildren(CommandInfo.class);
  }

  /**
   * {@link CommandInfo} has two presentations - as item and as group (if it is group).
   * 
   * @return the {@link Image} of {@link CommandInfo} as item.
   */
  private Image getItemImage() {
    return m_itemImage;
  }

  /**
   * {@link CommandInfo} has two presentations - as item and as group (if it is group).
   * 
   * @return the bounds of {@link CommandInfo} as item.
   */
  private Rectangle getItemBounds() {
    return m_itemBounds;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final int GROUP_BORDER = 3;
  private static final int GROUP_BORDER_X = 24;
  private static final Image SUB_MENU = Activator.getImage("command/sub_menu.png");

  @Override
  protected void refresh_fetch() throws Exception {
    setClientAreaInsets(new Insets(GROUP_BORDER));
    super.refresh_fetch();
    // in any case prepare our "item" presentation
    updateItemPresentation();
    // if group, prepare "container" image and update children
    if (isGroup()) {
      // prepare items width
      int itemsWidth = 0;
      for (CommandInfo child : getChildrenCommands()) {
        itemsWidth = Math.max(itemsWidth, child.m_itemBounds.width);
      }
      // prepare size
      int width = GROUP_BORDER + itemsWidth + GROUP_BORDER;
      int height;
      {
        height = GROUP_BORDER;
        for (CommandInfo child : getChildrenCommands()) {
          child.m_itemBounds.x = GROUP_BORDER;
          child.m_itemBounds.y = height;
          child.m_itemBounds.width = itemsWidth;
          height += child.m_itemBounds.height;
        }
        height += GROUP_BORDER;
      }
      // set image
      {
        Image image = new Image(null, width, height);
        setImage(image);
        GC gc = new GC(image);
        try {
          gc.setForeground(IColorConstants.buttonDarker);
          gc.drawRectangle(0, 0, width - 1, height - 1);
        } finally {
          gc.dispose();
        }
        setImage(image);
      }
      // set bounds
      setModelBounds(new Rectangle(0, 0, width, height));
    } else {
      // in any case initialize bounds
      setBounds(new Rectangle(0, 0, 0, 0));
    }
  }

  /**
   * Updates item's image and bounds.
   */
  private void updateItemPresentation() throws Exception {
    Object command = getObject();
    // prepare image/text
    Object commandImage = ReflectionUtils.invokeMethod2(command, "getImage");
    String commandText = (String) ReflectionUtils.invokeMethod2(command, "getText");
    // prepare size
    int width = GROUP_BORDER_X * 2;
    int height = 0;
    {
      Image image = new Image(null, 1, 1);
      GC gc = new GC(image);
      try {
        org.eclipse.swt.graphics.Point textExtent = gc.textExtent(commandText);
        width += textExtent.x;
        height += textExtent.y;
      } finally {
        gc.dispose();
      }
      image.dispose();
      // set minimal size
      height = Math.max(height, 1);
    }
    // set image
    {
      m_itemImage = new Image(null, width, height);
      GC gc = new GC(m_itemImage);
      try {
        // background
        gc.setBackground(IColorConstants.listBackground);
        gc.fillRectangle(m_itemImage.getBounds());
        // icon
        if (commandImage != null) {
          Image commandImage_SWT = ToolkitSupport.createSWTImage(commandImage);
          DrawUtils.drawImageCHCV(gc, commandImage_SWT, 0, 0, GROUP_BORDER_X, height);
          commandImage_SWT.dispose();
        }
        // text
        gc.setForeground(IColorConstants.listForeground);
        gc.drawText(commandText, GROUP_BORDER_X, 0);
        // sub-menu sign
        if (isGroup()) {
          DrawUtils.drawImageCHCV(gc, SUB_MENU, width - GROUP_BORDER_X, 0, GROUP_BORDER_X, height);
        }
      } finally {
        gc.dispose();
      }
    }
    // set bounds
    m_itemBounds = new Rectangle(0, 0, width, height);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds new {@link CommandInfo}.
   */
  public void commandCreate(JavaInfo newParent, CommandInfo nextCommand) throws Exception {
    JavaInfoUtils.add(this, null, newParent, nextCommand);
  }

  /**
   * Moves existing {@link CommandInfo} inside of same or to the new parent.
   */
  public void commandMove(JavaInfo newParent, CommandInfo nextCommand) throws Exception {
    JavaInfoUtils.move(this, null, newParent, nextCommand);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IAdaptable
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IMenuItemInfo m_itemImpl = new MenuItemImpl();
  private final IMenuInfo m_menuImpl = new MenuImpl();
  private final IMenuPopupInfo m_popupImpl = new MenuPopupImpl();

  public <T> T getAdapter(Class<T> adapter) {
    if (adapter.isAssignableFrom(IMenuItemInfo.class)) {
      return adapter.cast(m_itemImpl);
    }
    if (adapter.isAssignableFrom(IMenuInfo.class)) {
      return adapter.cast(m_menuImpl);
    }
    if (adapter.isAssignableFrom(IMenuPopupInfo.class) && getParent() instanceof ControlInfo) {
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
  private abstract class MenuAbstractImpl extends JavaMenuMenuObject {
    public MenuAbstractImpl() {
      super(CommandInfo.this);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IMenuPolicy
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Implementation of {@link IMenuPolicy}.
   */
  private final IMenuPolicy m_menuPolicy = new IMenuPolicy() {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Validation
    //
    ////////////////////////////////////////////////////////////////////////////
    public boolean validateCreate(Object object) {
      // we can update "type" only for Command created with constructor
      return object instanceof CommandInfo
          && (isGroup() || getCreationSupport() instanceof ConstructorCreationSupport);
    }

    public boolean validatePaste(final Object mementoObject) {
      return ExecutionUtils.runObjectLog(new RunnableObjectEx<Boolean>() {
        @SuppressWarnings("unchecked")
        public Boolean runObject() throws Exception {
          List<JavaInfoMemento> mementos = (List<JavaInfoMemento>) mementoObject;
          for (JavaInfoMemento memento : mementos) {
            JavaInfo component = memento.create(CommandInfo.this);
            if (!(component instanceof CommandInfo)) {
              return false;
            }
          }
          return true;
        }
      }, false);
    }

    public boolean validateMove(Object object) {
      if (object instanceof CommandInfo) {
        CommandInfo command = (CommandInfo) object;
        // don't move Command on its child Command
        if (command.isParentOf(CommandInfo.this)) {
          return false;
        }
        return true;
      }
      // not a CommandInfo
      return false;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Operations
    //
    ////////////////////////////////////////////////////////////////////////////
    public void commandCreate(final Object newObject, final Object nextObject) throws Exception {
      ExecutionUtils.run(CommandInfo.this, new RunnableEx() {
        public void run() throws Exception {
          ensureGroup();
          CommandInfo newCommand = (CommandInfo) newObject;
          CommandInfo nextCommand = (CommandInfo) nextObject;
          JavaInfoUtils.add(newCommand, null, CommandInfo.this, nextCommand);
          // schedule selection
          MenuObjectInfoUtils.setSelectingObject(newCommand);
        }
      });
    }

    public List<?> commandPaste(final Object mementoObject, final Object nextObject)
        throws Exception {
      return ExecutionUtils.runObject(new RunnableObjectEx<List<?>>() {
        @SuppressWarnings("unchecked")
        public List<?> runObject() throws Exception {
          List<CommandInfo> pastedObjects = Lists.newArrayList();
          // ensure "group"
          if (!isGroup()) {
            ensureGroup();
            getRoot().refreshLight();
          }
          // do paste
          List<JavaInfoMemento> mementos = (List<JavaInfoMemento>) mementoObject;
          for (JavaInfoMemento memento : mementos) {
            CommandInfo command = (CommandInfo) memento.create(CommandInfo.this);
            pastedObjects.add(command);
            commandCreate(command, nextObject);
            memento.apply();
          }
          return pastedObjects;
        }
      });
    }

    public void commandMove(final Object object, final Object nextObject) throws Exception {
      ExecutionUtils.run(CommandInfo.this, new RunnableEx() {
        public void run() throws Exception {
          ensureGroup();
          CommandInfo command = (CommandInfo) object;
          CommandInfo nextCommand = (CommandInfo) nextObject;
          JavaInfoUtils.move(command, null, CommandInfo.this, nextCommand);
          // schedule selection
          MenuObjectInfoUtils.setSelectingObject(command);
        }
      });
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Utils
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Checks that this {@link CommandInfo} is "group", and converts it into "group" if needed.
     */
    private void ensureGroup() throws Exception {
      if (!isGroup()) {
        ClassInstanceCreation creation =
            ((ConstructorCreationSupport) getCreationSupport()).getCreation();
        getEditor().replaceExpression(
            DomGenerics.arguments(creation).get(1),
            "org.eclipse.ercp.swt.mobile.Command.COMMANDGROUP");
      }
    }
  };

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
  private final class MenuItemImpl extends AbstractMenuObject implements IMenuItemInfo {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public MenuItemImpl() {
      super(CommandInfo.this);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Model
    //
    ////////////////////////////////////////////////////////////////////////////
    public Object getModel() {
      return CommandInfo.this;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Presentation
    //
    ////////////////////////////////////////////////////////////////////////////
    public Image getImage() {
      return getItemImage();
    }

    public Rectangle getBounds() {
      return getItemBounds();
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // IMenuItemInfo
    //
    ////////////////////////////////////////////////////////////////////////////
    public IMenuInfo getMenu() {
      return isGroup() ? m_menuImpl : null;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Policy
    //
    ////////////////////////////////////////////////////////////////////////////
    public IMenuPolicy getPolicy() {
      return m_menuPolicy;
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
      return CommandInfo.this;
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
      // prepare information about parent
      ControlInfo parentControl = (ControlInfo) getParent();
      Insets parentInsets = parentControl.getClientAreaInsets();
      Rectangle parentArea = parentControl.getBounds().getCropped(parentInsets);
      // prepare size of "popup"
      int width;
      int height;
      {
        Image image = getImage();
        width = image.getBounds().width;
        height = image.getBounds().height;
      }
      // prepare bounds for "popup"
      int index = parentControl.getChildren(CommandInfo.class).indexOf(CommandInfo.this);
      int x = 3 + parentInsets.left + (width + 5) * index;
      int y = parentInsets.top + parentArea.height - height - 3;
      return new Rectangle(x, y, width, height);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // IMenuPopupInfo
    //
    ////////////////////////////////////////////////////////////////////////////
    public IMenuInfo getMenu() {
      return isGroup() ? m_menuImpl : null;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Policy
    //
    ////////////////////////////////////////////////////////////////////////////
    public IMenuPolicy getPolicy() {
      return m_menuPolicy;
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
  private final class MenuImpl extends MenuAbstractImpl implements IMenuInfo {
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
      return CommandInfo.this.getImage();
    }

    public Rectangle getBounds() {
      return CommandInfo.this.getBounds();
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    public boolean isHorizontal() {
      return false;
    }

    public List<IMenuItemInfo> getItems() {
      List<IMenuItemInfo> items = Lists.newArrayList();
      for (CommandInfo child : getChildrenCommands()) {
        items.add(MenuObjectInfoUtils.getMenuItemInfo(child));
      }
      return items;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Policy
    //
    ////////////////////////////////////////////////////////////////////////////
    public IMenuPolicy getPolicy() {
      return m_menuPolicy;
    }
  }
}
