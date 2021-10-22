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
package org.eclipse.wb.internal.xwt.model.layout;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildAddAfter;
import org.eclipse.wb.core.model.broadcast.ObjectInfoDelete;
import org.eclipse.wb.core.model.broadcast.ObjectInfoTreeComplete;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.presentation.ButtonPropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.association.Associations;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectAddProperties;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectClipboardCopy;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectMove;
import org.eclipse.wb.internal.core.xml.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;
import org.eclipse.wb.internal.swt.model.layout.ILayoutDataInfo;
import org.eclipse.wb.internal.swt.model.layout.ILayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Menu;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Model for any XWT {@link Layout}.
 *
 * @author scheglov_ke
 * @coverage XWT.model.layout
 */
public class LayoutInfo extends XmlObjectInfo implements ILayoutInfo<ControlInfo> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LayoutInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(context, description, creationSupport);
    addBroadcasts();
    contributeLayoutProperty_toComposite();
    contributeToClipboardCopy();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Broadcasts
  //
  ////////////////////////////////////////////////////////////////////////////
  private void addBroadcasts() {
    // ensure LayoutData after parsing
    addBroadcastListener(new ObjectInfoTreeComplete() {
      public void invoke() throws Exception {
        if (isActive()) {
          ensureLayoutDatas();
        }
      }
    });
    addBroadcastListener(new ObjectInfoChildAddAfter() {
      public void invoke(ObjectInfo parent, ObjectInfo child) throws Exception {
        if (getContext().isParsing()) {
          return;
        }
        // Control was added, create virtual LayoutData
        if (isActiveOnComposite(parent) && isManagedObject(child)) {
          ensureLayoutData((ControlInfo) child);
        }
      }
    });
    addBroadcastListener(new ObjectInfoDelete() {
      @Override
      public void before(ObjectInfo parent, ObjectInfo child) throws Exception {
        handle_maybe_removeControl_before(parent, child, false);
        // this Layout remove
        if (child == LayoutInfo.this) {
          onDelete();
        }
      }

      @Override
      public void after(ObjectInfo parent, ObjectInfo child) throws Exception {
        handle_maybe_removeControl_after(parent, child);
      }
    });
    addBroadcastListener(new XmlObjectMove() {
      @Override
      public void before(XmlObjectInfo child, ObjectInfo oldParent, ObjectInfo newParent)
          throws Exception {
        if (newParent != oldParent) {
          handle_maybe_removeControl_before(oldParent, child, true);
        }
      }

      @Override
      public void after(XmlObjectInfo child, ObjectInfo oldParent, ObjectInfo newParent)
          throws Exception {
        if (newParent != oldParent) {
          handle_maybe_removeControl_after(oldParent, child);
        }
      }
    });
    addBroadcastListener(new ObjectEventListener() {
      @Override
      public void childRemoveAfter(ObjectInfo parent, ObjectInfo child) throws Exception {
        // LayoutData removed, create virtual one
        if (child instanceof LayoutDataInfo && isManagedObject(parent)) {
          ControlInfo control = (ControlInfo) parent;
          if (shouldCreateLayoutData(control)) {
            ensureLayoutData(control);
          }
        }
      }
    });
  }

  // 2kosta: try to contribute to GTK; they like names like this :-P
  private void handle_maybe_removeControl_before(ObjectInfo parent,
      ObjectInfo child,
      boolean deleteLayoutData) throws Exception {
    if (isActiveOnComposite(parent) && isManagedObject(child)) {
      ControlInfo control = (ControlInfo) child;
      onControlRemoveBefore(control);
      if (deleteLayoutData) {
        deleteLayoutData(control);
      }
    }
  }

  private void handle_maybe_removeControl_after(ObjectInfo parent, ObjectInfo child)
      throws Exception {
    if (isActiveOnComposite(parent) && child instanceof ControlInfo) {
      ControlInfo control = (ControlInfo) child;
      onControlRemoveAfter(control);
    }
  }

  private void contributeLayoutProperty_toComposite() {
    addBroadcastListener(new XmlObjectAddProperties() {
      public void invoke(XmlObjectInfo object, List<Property> properties) throws Exception {
        if (isActiveOnComposite(object)) {
          addLayoutProperty(properties);
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Layout notifications
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * This method is invoked when this {@link LayoutInfo} is set on its {@link CompositeInfo}.
   */
  public void onSet() throws Exception {
    ensureLayoutDatas();
  }

  /**
   * This method is invoked when this {@link LayoutInfo} is deleted from its {@link CompositeInfo} .
   */
  protected void onDelete() throws Exception {
    for (ControlInfo control : getControls()) {
      deleteLayoutData(control);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Control notifications
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Notification that given {@link ControlInfo} will be removed from {@link CompositeInfo}.
   */
  protected void onControlRemoveBefore(ControlInfo control) throws Exception {
  }

  /**
   * Notification that given {@link ControlInfo} was removed from {@link CompositeInfo}.
   */
  protected void onControlRemoveAfter(ControlInfo control) throws Exception {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // LayoutData management
  //
  ////////////////////////////////////////////////////////////////////////////
  private static String KEY_DONT_CREATE_VIRTUAL_DATA =
      "don't create virtual LayoutData for this Control_Info";

  /**
   * We may be {@link ControlInfo} that virtual {@link LayoutDataInfo} should not be created for it,
   * when we intentionally delete {@link LayoutDataInfo}, for example during process of moving this
   * {@link ControlInfo} from this {@link LayoutData_Support} or deleting this
   * {@link LayoutData_Support}.
   *
   * @return <code>true</code> if for given {@link ControlInfo} we should create
   *         {@link LayoutDataInfo}.
   */
  private boolean shouldCreateLayoutData(ControlInfo control) {
    return !control.isDeleting() && control.getArbitraryValue(KEY_DONT_CREATE_VIRTUAL_DATA) == null;
  }

  /**
   * Delete {@link LayoutDataInfo} associated with given {@link ControlInfo}.
   * <p>
   * Note that this is different than {@link LayoutDataInfo#delete()} because we don't remove
   * implicit/virtual {@link LayoutDataInfo} from list of children in
   * {@link CreationSupport#delete()}. {@link CreationSupport#delete()} has to remove only
   * {@link ASTNode}'s related with {@link LayoutDataInfo}. So, we need separate operation to remove
   * {@link LayoutDataInfo} from list of children.
   */
  protected void deleteLayoutData(ControlInfo control) throws Exception {
    control.putArbitraryValue(KEY_DONT_CREATE_VIRTUAL_DATA, Boolean.TRUE);
    try {
      LayoutDataInfo layoutData = getLayoutData(control);
      if (layoutData != null) {
        layoutData.delete();
        // if implicit/virtual, so still alive, force remove from children
        if (!layoutData.isDeleted()) {
          control.removeChild(layoutData);
        }
      }
    } finally {
      control.removeArbitraryValue(KEY_DONT_CREATE_VIRTUAL_DATA);
    }
  }

  /**
   * @return {@link LayoutDataInfo} associated with given {@link ControlInfo}, or <code>null</code>
   *         if no {@link LayoutDataInfo} expected for this {@link LayoutData_Support}.
   */
  public static LayoutDataInfo getLayoutData(ControlInfo control) {
    for (ObjectInfo object : control.getChildren()) {
      if (object instanceof LayoutDataInfo) {
        return (LayoutDataInfo) object;
      }
    }
    return null;
  }

  public ILayoutDataInfo getLayoutData2(IControlInfo control) {
    return getLayoutData((ControlInfo) control);
  }

  /**
   * Ensures {@link LayoutDataInfo} for managed {@link ControlInfo}.
   */
  private void ensureLayoutDatas() throws Exception {
    for (ControlInfo control : getControls()) {
      ensureLayoutData(control);
    }
  }

  /**
   * Ensure that if {@link LayoutDataInfo} should exist for given component, there is "real"
   * {@link LayoutDataInfo}, or create "virtual"/"implicit" {@link LayoutDataInfo}.
   */
  private void ensureLayoutData(ControlInfo control) throws Exception {
    if (hasLayoutData()) {
      LayoutDataInfo layoutData = getLayoutData(control);
      if (layoutData == null) {
        createVirtualLayoutData(control);
      }
    }
  }

  /**
   * Creates virtual {@link LayoutDataInfo} for given {@link ControlInfo}.
   * <p>
   * "Virtual" {@link LayoutDataInfo} is placeholder for "layout data" when "layout data" should
   * exist, but does not exist yet in source code. Most layout managers in this case use
   * "layout data" with some default values. So, we show these values in properties and allow to
   * change them, at this moment we "materialize" {@link LayoutDataInfo} in source code.
   */
  private void createVirtualLayoutData(ControlInfo control) throws Exception {
    Object dataObject = getLayoutDataVirtualObject();
    // create model
    LayoutDataInfo layoutData;
    {
      CreationSupport creationSupport = new VirtualLayoutDataCreationSupport(control, dataObject);
      layoutData =
          (LayoutDataInfo) XmlObjectUtils.createObject(
              getContext(),
              getLayoutDataClass(),
              creationSupport);
    }
    // add to control
    control.addChild(layoutData);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Low level LayoutData
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String KEY_LAYOUT_DATA_HAS = "layout-data.has";
  private static final String KEY_LAYOUT_DATA_CLASS = "layout-data.class";
  private Class<?> m_layoutDataClass;
  private Object m_layoutDataVirtualObject;

  /**
   * @return <code>true</code> if this layout has layout data, for user.
   */
  private boolean hasLayoutData() {
    return XmlObjectUtils.hasTrueParameter(this, KEY_LAYOUT_DATA_HAS);
  }

  /**
   * @return {@link Class} of layout data objects.
   */
  private Class<?> getLayoutDataClass() throws Exception {
    if (m_layoutDataClass == null) {
      // extract class name
      String layoutDataClassName = XmlObjectUtils.getParameter(this, KEY_LAYOUT_DATA_CLASS);
      Assert.isTrue2(
          !StringUtils.isEmpty(layoutDataClassName),
          "No 'layout-data.class' parameter for {0}.",
          this);
      // load class
      m_layoutDataClass = getContext().getClassLoader().loadClass(layoutDataClassName);
    }
    return m_layoutDataClass;
  }

  /**
   * @return the object used for "virtual" {@link LayoutDataInfo}.
   */
  protected final Object getLayoutDataVirtualObject() throws Exception {
    if (m_layoutDataVirtualObject == null) {
      String script = XmlObjectUtils.getParameter(this, "layout-data.virtual");
      Assert.isNotNull2(
          script,
          "No 'layout-data.virtual' script for creating virtual LayoutData object. {0}",
          this);
      script = StringUtils.replace(script, "%LDC%", getLayoutDataClass().getName());
      m_layoutDataVirtualObject = ScriptUtils.evaluate(getContext().getClassLoader(), script);
    }
    return m_layoutDataVirtualObject;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final IObjectPresentation getPresentation() {
    return new LayoutPresentation(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link CompositeInfo} that contains this {@link LayoutInfo}.
   */
  public final CompositeInfo getComposite() {
    return (CompositeInfo) getParent();
  }

  /**
   * @return <code>true</code> if this {@link LayoutInfo} is active on its {@link CompositeInfo}.
   *         For example implicit {@link LayoutInfo}'s replaced by "real" {@link LayoutInfo} are
   *         inactive.
   */
  public final boolean isActive() {
    CompositeInfo composite = getComposite();
    return isActiveOnComposite(composite);
  }

  /**
   * @return <code>true</code> if this {@link LayoutInfo} is active on its {@link CompositeInfo}.
   */
  private boolean isActiveOnComposite(ObjectInfo composite) {
    return composite != null && composite.getChildren().contains(this);
  }

  public boolean isManagedObject(Object object) {
    if (object instanceof ControlInfo
        && isActive()
        && getComposite().getChildren().contains(object)) {
      ControlInfo control = (ControlInfo) object;
      if (control.isDeleted()) {
        return false;
      }
      return true;
    }
    return false;
  }

  /**
   * @return the object casted to {@link ControlInfo}.
   */
  public final ControlInfo getControl(Object object) {
    return (ControlInfo) object;
  }

  /**
   * @return the {@link ControlInfo} that are managed by this {@link LayoutInfo}.
   */
  public final List<ControlInfo> getControls() {
    List<ControlInfo> controls = Lists.newArrayList();
    for (ControlInfo control : getComposite().getChildrenControls()) {
      if (isManagedObject(control)) {
        controls.add(control);
      }
    }
    return controls;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Layout" property
  //
  ////////////////////////////////////////////////////////////////////////////
  private ComplexProperty m_layoutComplexProperty;

  /**
   * Adds properties of this {@link LayoutInfo} to the properties of its {@link CompositeInfo}.
   */
  private void addLayoutProperty(List<Property> properties) throws Exception {
    // prepare layout complex property
    if (m_layoutComplexProperty == null) {
      String text;
      {
        Class<?> componentClass = getDescription().getComponentClass();
        if (componentClass != null) {
          text = "(" + componentClass.getName() + ")";
        } else {
          text = "(absolute)";
        }
      }
      // create ComplexProperty
      m_layoutComplexProperty = new ComplexProperty("Layout", text) {
        @Override
        public boolean isModified() throws Exception {
          return true;
        }

        @Override
        public void setValue(Object value) throws Exception {
          if (value == UNKNOWN_VALUE) {
            delete();
          }
        }
      };
      m_layoutComplexProperty.setCategory(PropertyCategory.system(5));
      m_layoutComplexProperty.setEditorPresentation(new ButtonPropertyEditorPresentation() {
        @Override
        protected Image getImage() {
          return DesignerPlugin.getImage("properties/down.png");
        }

        @Override
        protected void onClick(PropertyTable propertyTable, Property property) throws Exception {
          MenuManager manager = new MenuManager();
          getComposite().fillLayoutsManager(manager);
          Menu menu = manager.createContextMenu(propertyTable);
          UiUtils.showAndDisposeOnHide(menu);
        }
      });
    }
    m_layoutComplexProperty.setProperties(getProperties());
    // add property
    properties.add(m_layoutComplexProperty);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates new {@link ControlInfo} on this {@link CompositeInfo}.
   */
  public void command_CREATE(ControlInfo control, ControlInfo nextControl) throws Exception {
    XmlObjectUtils.add(control, Associations.direct(), getComposite(), nextControl);
  }

  /**
   * Move existing {@link ControlInfo} on this {@link CompositeInfo}.
   */
  public void command_MOVE(ControlInfo control, ControlInfo nextControl) throws Exception {
    XmlObjectUtils.move(control, Associations.direct(), getComposite(), nextControl);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  private void contributeToClipboardCopy() {
    addBroadcastListener(new XmlObjectClipboardCopy() {
      public void invoke(XmlObjectInfo object, List<ClipboardCommand> commands) throws Exception {
        if (isActiveOnComposite(object)) {
          clipboardCopy_addCompositeCommands(commands);
        }
      }
    });
  }

  /**
   * Adds commands for coping parent {@link CompositeInfo}.
   */
  protected void clipboardCopy_addCompositeCommands(List<ClipboardCommand> commands)
      throws Exception {
    for (ControlInfo control : getControls()) {
      clipboardCopy_addControlCommands(control, commands);
    }
  }

  /**
   * Adds commands for coping managed {@link ControlInfo}.
   */
  protected void clipboardCopy_addControlCommands(ControlInfo control,
      List<ClipboardCommand> commands) throws Exception {
  }
}
