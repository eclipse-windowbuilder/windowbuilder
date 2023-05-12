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
package org.eclipse.wb.internal.swt.model.widgets.menu;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.AssociationObject;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.core.model.broadcast.DisplayEventListener;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildrenGraphical;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.model.generation.GenerationUtils;
import org.eclipse.wb.internal.core.model.generation.statement.StatementGenerator;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuObjectInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPolicy;
import org.eclipse.wb.internal.core.model.menu.IMenuPopupInfo;
import org.eclipse.wb.internal.core.model.menu.JavaMenuMenuObject;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;
import org.eclipse.wb.internal.core.model.menu.MenuVisualData;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.variable.EmptyPureVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.IAdaptable;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.swt.model.widgets.WidgetInfo;
import org.eclipse.wb.internal.swt.model.widgets.live.SwtLiveManager;
import org.eclipse.wb.internal.swt.model.widgets.live.menu.MenuLiveManager;
import org.eclipse.wb.internal.swt.support.MenuSupport;
import org.eclipse.wb.internal.swt.support.SwtSupport;
import org.eclipse.wb.internal.swt.support.ToolkitSupport;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import java.util.List;

/**
 * Model for SWT menu.
 *
 * @author mitin_aa
 * @coverage swt.model.widgets.menu
 */
public final class MenuInfo extends WidgetInfo implements IAdaptable {
  private final MenuInfo m_this = this;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MenuInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    putPopupAboveOtherChildren();
    addClipboardSupport();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Broadcasts
  //
  ////////////////////////////////////////////////////////////////////////////
  private void putPopupAboveOtherChildren() {
    addBroadcastListener(new ObjectInfoChildrenGraphical() {
      @Override
      public void invoke(List<ObjectInfo> children) throws Exception {
        if (children.remove(m_this)) {
          children.add(0, m_this);
        }
      }
    });
  }

  private void addClipboardSupport() {
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void clipboardCopy_Argument(JavaInfo javaInfo,
          ParameterDescription parameter,
          Expression argument,
          String[] source) throws Exception {
        if (javaInfo == m_this && parameter.getIndex() == 0) {
          source[0] = "%parent%";
        }
      }

      @Override
      public void clipboardCopy(JavaInfo javaInfo, List<ClipboardCommand> commands)
          throws Exception {
        // copy items
        if (javaInfo == m_this) {
          for (MenuItemInfo item : getChildrenItems()) {
            final JavaInfoMemento itemMemento = JavaInfoMemento.createMemento(item);
            commands.add(new ClipboardCommand() {
              private static final long serialVersionUID = 0L;

              @Override
              public void execute(JavaInfo javaInfo) throws Exception {
                MenuItemInfo item = (MenuItemInfo) itemMemento.create(javaInfo);
                IMenuPolicy policy = MenuObjectInfoUtils.getMenuInfo(javaInfo).getPolicy();
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
    // fetch menu visual data
    DisplayEventListener displayListener = getBroadcast(DisplayEventListener.class);
    MenuVisualData visualData = null;
    try {
      displayListener.beforeMessagesLoop();
      // On windows, when one creates a new entry the image for the menu is not displayed correctly
      Display.getDefault().readAndDispatch();
      visualData = ToolkitSupport.fetchMenuVisualData(getObject());//
    } finally {
      displayListener.afterMessagesLoop();
    }
    setModelBounds(visualData.m_menuBounds);
    setBounds(visualData.m_menuBounds);
    setImage(visualData.m_menuImage);
    // set child items bounds
    List<MenuItemInfo> items = getChildrenItems();
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
  protected SwtLiveManager getLiveComponentsManager() {
    return new MenuLiveManager(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link MenuItemInfo} children.
   */
  public List<MenuItemInfo> getChildrenItems() {
    return getChildren(MenuItemInfo.class);
  }

  /**
   * @return <code>true</code> if this {@link MenuInfo} is bar menu.
   */
  public boolean isBar() {
    return (getStyle() & SwtSupport.BAR) != 0;
  }

  /**
   * @return <code>true</code> if this {@link MenuInfo} is popup menu.
   */
  public boolean isPopup() {
    return (getStyle() & SwtSupport.POP_UP) != 0;
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
  public void command_CREATE(WidgetInfo parent) throws Exception {
    JavaInfoUtils.add(this, command_getAssociation(), parent, null);
  }

  /**
   * Moves this existing {@link MenuInfo} on given {@link WidgetInfo}.
   */
  public void command_ADD(WidgetInfo parent) throws Exception {
    JavaInfoUtils.move(this, command_getAssociation(), parent, null);
    replaceParentReferenceInCreation(parent);
  }

  /**
   * We can not mark constructor argument as parent, because this is just no true - "physical"
   * parent for {@link Menu} is {@link Decorations}, and "logical" (which we need) association is
   * established using using {@link Control#setMenu(Menu)} or {@link MenuItem#setMenu(Menu)}.
   * <p>
   * However, we still want replace argument of constructor, at least to be consistent with other
   * SWT widgets. So, we need separate action to do this.
   */
  private void replaceParentReferenceInCreation(WidgetInfo parent) throws Exception {
    if (getCreationSupport() instanceof ConstructorCreationSupport) {
      ConstructorCreationSupport creationSupport =
          (ConstructorCreationSupport) getCreationSupport();
      ClassInstanceCreation creation = creationSupport.getCreation();
      NodeTarget target = JavaInfoUtils.getNodeTarget_afterCreation(this);
      String parentReference = parent.getVariableSupport().getReferenceExpression(target);
      getEditor().replaceCreationArguments(creation, Lists.newArrayList(parentReference));
    }
  }

  /**
   * @return the {@link AssociationObjec} of this {@link MenuInfo} with its parent.
   */
  private AssociationObject command_getAssociation() throws Exception {
    if (isBar()) {
      return AssociationObjects.invocationChild("%parent%.setMenuBar(%child%)", true);
    } else {
      return AssociationObjects.invocationChild("%parent%.setMenu(%child%)", true);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IAdaptable
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IMenuPopupInfo m_popupImpl = new MenuPopupImpl();
  private final IMenuInfo m_menuImpl = new MenuImpl();

  @Override
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
  private abstract class MenuAbstractImpl extends JavaMenuMenuObject {
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
    @Override
    public Object getModel() {
      return m_this;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Presentation
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public Image getImage() {
      return ExecutionUtils.runObjectLog(new RunnableObjectEx<Image>() {
        @Override
        public Image runObject() throws Exception {
          return getPresentation().getIcon();
        }
      }, getDescription().getIcon());
    }

    @Override
    public Rectangle getBounds() {
      Image image = getImage();
      return new Rectangle(0, 0, image.getBounds().width, image.getBounds().height);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // IMenuPopupInfo
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public IMenuInfo getMenu() {
      return m_menuImpl;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Policy
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
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
    @Override
    public Object getModel() {
      return isPopup() ? this : m_this;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Presentation
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public Image getImage() {
      return m_this.getImage();
    }

    @Override
    public Rectangle getBounds() {
      return m_this.getBounds();
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean isHorizontal() {
      return isBar();
    }

    @Override
    public List<IMenuItemInfo> getItems() {
      List<IMenuItemInfo> items = Lists.newArrayList();
      for (MenuItemInfo item : getChildrenItems()) {
        items.add(MenuObjectInfoUtils.getMenuItemInfo(item));
      }
      return items;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Policy
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public IMenuPolicy getPolicy() {
      return this;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Validation
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean validateCreate(Object newObject) {
      return newObject instanceof MenuItemInfo;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean validatePaste(final Object mementoObject) {
      return ExecutionUtils.runObjectLog(new RunnableObjectEx<Boolean>() {
        @Override
        public Boolean runObject() throws Exception {
          List<JavaInfoMemento> mementos = (List<JavaInfoMemento>) mementoObject;
          for (JavaInfoMemento memento : mementos) {
            JavaInfo component = memento.create(m_this);
            if (!(component instanceof MenuItemInfo)) {
              return false;
            }
          }
          return true;
        }
      }, false);
    }

    @Override
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
    @Override
    public void commandCreate(Object newObject, Object nextObject) throws Exception {
      MenuItemInfo newItem = (MenuItemInfo) newObject;
      MenuItemInfo nextItem = (MenuItemInfo) nextObject;
      if ((newItem.getStyle() & SwtSupport.SEPARATOR) != 0) {
        VariableSupport variableSupport = new EmptyPureVariableSupport(newItem);
        StatementGenerator statementGenerator = GenerationUtils.getStatementGenerator(newItem);
        JavaInfoUtils.add(newItem, variableSupport, statementGenerator, null, m_this, nextItem);
      } else {
        JavaInfoUtils.add(newItem, null, m_this, nextItem);
      }
      // schedule selection
      MenuObjectInfoUtils.setSelectingObject(newItem);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<?> commandPaste(Object mementoObject, Object nextObject) throws Exception {
      List<MenuItemInfo> pastedObjects = Lists.newArrayList();
      List<JavaInfoMemento> mementos = (List<JavaInfoMemento>) mementoObject;
      for (JavaInfoMemento memento : mementos) {
        MenuItemInfo item = (MenuItemInfo) memento.create(m_this);
        commandCreate(item, nextObject);
        memento.apply();
        pastedObjects.add(item);
      }
      return pastedObjects;
    }

    @Override
    public void commandMove(Object object, Object nextObject) throws Exception {
      MenuItemInfo item = (MenuItemInfo) object;
      MenuItemInfo nextItem = (MenuItemInfo) nextObject;
      JavaInfoUtils.move(item, null, m_this, nextItem);
      // schedule selection
      MenuObjectInfoUtils.setSelectingObject(item);
    }
  }
}
