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
package org.eclipse.wb.internal.rcp.model.forms.layout.table;

import org.eclipse.wb.core.editor.IContextMenuConstants;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.StaticFieldPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.complex.IComplexPropertyEditor;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.rcp.model.ModelMessages;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.actions.SetAlignmentAction;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.actions.SetGrabAction;
import org.eclipse.wb.internal.swt.model.layout.LayoutDataInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.forms.widgets.TableWrapData;

import java.util.List;

/**
 * Model for {@link TableWrapData}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.forms
 */
public final class TableWrapDataInfo extends LayoutDataInfo implements ITableWrapDataInfo {
  private boolean m_initialized;
  int x = -1;
  int y = -1;
  int width = 1;
  int height = 1;
  boolean horizontalGrab;
  boolean verticalGrab;
  int horizontalAlignment;
  int verticalAlignment;
  int heightHint;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TableWrapDataInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initialize
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void initialize() throws Exception {
    super.initialize();
    // events
    addBroadcastListener(new ObjectEventListener() {
      @Override
      public void addContextMenu(List<? extends ObjectInfo> objects,
          ObjectInfo object,
          IMenuManager manager) throws Exception {
        if (getParent() == object) {
          TableWrapDataInfo.this.addContextMenu(manager);
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
  protected void refresh_fetch() throws Exception {
    super.refresh_fetch();
    // prepare x/y
    getCurrentObjectCell(getLayout(), getControl());
    // prepare values from TableWrapData
    getCurrentObjectFields();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Initializes fields of this {@link TableWrapDataInfo} using object of given
   * {@link TableWrapLayoutInfo} and {@link ControlInfo}.
   */
  void initialize(TableWrapLayoutInfo layout, ControlInfo controlInfo) throws Exception {
    if (!m_initialized) {
      m_initialized = true;
      // prepare x/y
      getCurrentObjectCell(layout, controlInfo);
      // prepare values from TableWrapData
      getCurrentObjectFields();
    }
  }

  /**
   * Gets cell using objects of given {@link TableWrapLayoutInfo} and {@link ControlInfo}.
   */
  private void getCurrentObjectCell(TableWrapLayoutInfo layout, ControlInfo controlInfo)
      throws Exception {
    if (layout.getObject() != null && controlInfo.getObject() != null) {
      Point xyPoint = TableWrapLayoutSupport.getXY(layout.getObject(), controlInfo.getObject());
      if (xyPoint != null) {
        x = xyPoint.x;
        y = xyPoint.y;
      }
    }
  }

  /**
   * Gets values from {@link TableWrapData} object to this {@link TableWrapDataInfo} fields.
   */
  private void getCurrentObjectFields() throws Exception {
    Object object = getObject();
    width = ReflectionUtils.getFieldInt(object, "colspan");
    height = ReflectionUtils.getFieldInt(object, "rowspan");
    horizontalGrab = ReflectionUtils.getFieldBoolean(object, "grabHorizontal");
    verticalGrab = ReflectionUtils.getFieldBoolean(object, "grabVertical");
    horizontalAlignment = ReflectionUtils.getFieldInt(object, "align");
    verticalAlignment = ReflectionUtils.getFieldInt(object, "valign");
    heightHint = ReflectionUtils.getFieldInt(object, "heightHint");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal access
  //
  ////////////////////////////////////////////////////////////////////////////
  private ControlInfo getControl() {
    return (ControlInfo) getParent();
  }

  private TableWrapLayoutInfo getLayout() {
    CompositeInfo composite = (CompositeInfo) getControl().getParent();
    if (composite == null) {
      return null;
    }
    return (TableWrapLayoutInfo) composite.getLayout();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Location
  //
  ////////////////////////////////////////////////////////////////////////////
  public int getX() {
    return x;
  }

  public void setX(int x) {
    this.x = x;
  }

  public int getY() {
    return y;
  }

  public void setY(int y) {
    this.y = y;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Span
  //
  ////////////////////////////////////////////////////////////////////////////
  public int getHorizontalSpan() {
    return width;
  }

  public void setHorizontalSpan(int width) throws Exception {
    if (this.width != width) {
      this.width = width;
      getPropertyByTitle("colspan").setValue(width);
    }
  }

  public int getVerticalSpan() {
    return height;
  }

  public void setVerticalSpan(int height) throws Exception {
    if (this.height != height) {
      this.height = height;
      getPropertyByTitle("rowspan").setValue(height);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Grab
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean getHorizontalGrab() {
    return horizontalGrab;
  }

  public void setHorizontalGrab(boolean grab) throws Exception {
    if (horizontalGrab != grab) {
      horizontalGrab = grab;
      materialize();
      // special case: convert constructor FILL into FILL_GRAB and vice-versa
      if (horizontalAlignment == TableWrapData.FILL && constructor_hasHorizontalAlignment()) {
        if (horizontalGrab) {
          constructor_setEnumProperty("align", TableWrapData.FILL_GRAB);
        } else {
          constructor_setEnumProperty("align", TableWrapData.FILL);
        }
        // don't continue
        return;
      }
      // just update property
      getPropertyByTitle("grabHorizontal").setValue(grab);
    }
  }

  public boolean getVerticalGrab() {
    return verticalGrab;
  }

  public void setVerticalGrab(boolean grab) throws Exception {
    if (verticalGrab != grab) {
      verticalGrab = grab;
      materialize();
      // special case: convert constructor FILL into FILL_GRAB and vice-versa
      if (verticalAlignment == TableWrapData.FILL && constructor_hasVerticalAlignment()) {
        if (verticalGrab) {
          constructor_setEnumProperty("valign", TableWrapData.FILL_GRAB);
        } else {
          constructor_setEnumProperty("valign", TableWrapData.FILL);
        }
        // don't continue
        return;
      }
      // just update property
      getPropertyByTitle("grabVertical").setValue(grab);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Alignment
  //
  ////////////////////////////////////////////////////////////////////////////
  public int getHorizontalAlignment() {
    return horizontalAlignment;
  }

  public void setHorizontalAlignment(int alignment) throws Exception {
    if (horizontalAlignment != alignment) {
      materialize();
      // special case: convert constructor FILL_GRAB into simple grab
      if (constructor_hasHorizontalAlignment()
          && horizontalAlignment == TableWrapData.FILL
          && horizontalGrab) {
        setFieldSource("grabHorizontal", "true");
      }
      // set new alignment
      horizontalAlignment = alignment;
      if (constructor_hasHorizontalAlignment()) {
        // special case: convert constructor FILL into FILL_GRAB
        if (horizontalAlignment == TableWrapData.FILL && horizontalGrab) {
          constructor_setEnumProperty("align", TableWrapData.FILL_GRAB);
          getPropertyByTitle("grabHorizontal").setValue(false);
          return;
        }
        // just update constructor property
        constructor_setEnumProperty("align", alignment);
      } else {
        // just update property
        setEnumProperty("align", alignment);
      }
    }
  }

  public int getVerticalAlignment() {
    return verticalAlignment;
  }

  public void setVerticalAlignment(int alignment) throws Exception {
    if (verticalAlignment != alignment) {
      materialize();
      // special case: convert constructor FILL_GRAB into simple grab
      if (constructor_hasVerticalAlignment()
          && verticalAlignment == TableWrapData.FILL
          && verticalGrab) {
        setFieldSource("grabVertical", "true");
      }
      // set new alignment
      verticalAlignment = alignment;
      if (constructor_hasVerticalAlignment()) {
        // special case: convert constructor FILL into FILL_GRAB
        if (verticalAlignment == TableWrapData.FILL && verticalGrab) {
          constructor_setEnumProperty("valign", TableWrapData.FILL_GRAB);
          getPropertyByTitle("grabVertical").setValue(false);
          return;
        }
        // just update constructor property
        constructor_setEnumProperty("valign", alignment);
      } else {
        setEnumProperty("valign", alignment);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Hint
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return value of <code>heightHint</code> property.
   */
  public int getHeightHint() {
    return heightHint;
  }

  /**
   * Sets value of <code>heightHint</code> property.
   */
  public void setHeightHint(int heightHint) throws Exception {
    if (this.heightHint != heightHint) {
      this.heightHint = heightHint;
      getPropertyByTitle("heightHint").setValue(heightHint);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * We need way to "force" assignment to grabHorizontal/grabVertical fields, when we know that its
   * default value will prevent from doing this just by using {@link Property#setValue(Object)}.
   */
  private void setFieldSource(String fieldName, String source) throws Exception {
    removeFieldAssignments(fieldName);
    addFieldAssignment(fieldName, source);
  }

  /**
   * Sets value for {@link Property} with {@link StaticFieldPropertyEditor}.
   */
  private void setEnumProperty(String propertyTitle, int value) throws Exception {
    Property property = getPropertyByTitle(propertyTitle);
    setEnumProperty(property, value);
  }

  /**
   * Sets value for {@link Property} with {@link StaticFieldPropertyEditor}.
   */
  private static void setEnumProperty(Property property, int value) throws Exception {
    StaticFieldPropertyEditor propertyEditor = (StaticFieldPropertyEditor) property.getEditor();
    propertyEditor.setValue(property, value);
  }

  /**
   * @return <code>true</code> if constructor used to create {@link TableWrapData}, has argument
   *         with horizontal alignment (as 0-th argument).
   */
  private boolean constructor_hasHorizontalAlignment() {
    return constructor_getNumberArguments() >= 1;
  }

  /**
   * @return <code>true</code> if constructor used to create {@link TableWrapData}, has argument
   *         with vertical alignment (as 1-th argument).
   */
  private boolean constructor_hasVerticalAlignment() {
    return constructor_getNumberArguments() >= 2;
  }

  /**
   * Sets the "enum" value for {@link Property} of {@link ConstructorCreationSupport}.
   */
  private void constructor_setEnumProperty(String propertyTitle, int value) throws Exception {
    Property constructorProperty = getPropertyByTitle("Constructor");
    if (constructorProperty != null) {
      IComplexPropertyEditor complexEditor =
          (IComplexPropertyEditor) constructorProperty.getEditor();
      for (Property property : complexEditor.getProperties(constructorProperty)) {
        if (property.getTitle().equals(propertyTitle)) {
          setEnumProperty(property, value);
        }
      }
    }
  }

  /**
   * @return the number of constructor arguments used to create {@link TableWrapData} in source.
   */
  private int constructor_getNumberArguments() {
    ConstructorCreationSupport creationSupport = (ConstructorCreationSupport) getCreationSupport();
    return creationSupport.getBinding().getParameterTypes().length;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Images
  //
  ////////////////////////////////////////////////////////////////////////////
  public Image getSmallAlignmentImage(boolean horizontal) {
    if (horizontal) {
      switch (horizontalAlignment) {
        case TableWrapData.LEFT :
          return TableWrapLayoutImages.getImage("h/left.gif");
        case TableWrapData.CENTER :
          return TableWrapLayoutImages.getImage("h/center.gif");
        case TableWrapData.RIGHT :
          return TableWrapLayoutImages.getImage("h/right.gif");
        default :
          Assert.isTrue(horizontalAlignment == TableWrapData.FILL);
          return TableWrapLayoutImages.getImage("h/fill.gif");
      }
    } else {
      switch (verticalAlignment) {
        case TableWrapData.TOP :
          return TableWrapLayoutImages.getImage("v/top.gif");
        case TableWrapData.MIDDLE :
          return TableWrapLayoutImages.getImage("v/middle.gif");
        case TableWrapData.BOTTOM :
          return TableWrapLayoutImages.getImage("v/bottom.gif");
        default :
          Assert.isTrue(verticalAlignment == TableWrapData.FILL);
          return TableWrapLayoutImages.getImage("v/fill.gif");
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Context menu
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds items to the context {@link IMenuManager}.
   */
  public void addContextMenu(IMenuManager manager) {
    // horizontal
    {
      IMenuManager manager2 =
          new MenuManager(ModelMessages.TableWrapDataInfo_managerHorizontalAlignment);
      manager.appendToGroup(IContextMenuConstants.GROUP_TOP, manager2);
      //
      manager2.add(new SetGrabAction(this, ModelMessages.TableWrapDataInfo_haGrab, "grow.gif", true));
      manager2.add(new Separator());
      //
      fillHorizontalAlignmentMenu(manager2);
    }
    // vertical
    {
      IMenuManager manager2 =
          new MenuManager(ModelMessages.TableWrapDataInfo_managerVerticalAlignment);
      manager.appendToGroup(IContextMenuConstants.GROUP_TOP, manager2);
      //
      manager2.add(new SetGrabAction(this,
          ModelMessages.TableWrapDataInfo_vaGrab,
          "grow.gif",
          false));
      manager2.add(new Separator());
      //
      fillVerticalAlignmentMenu(manager2);
    }
  }

  public void fillHorizontalAlignmentMenu(IMenuManager manager) {
    manager.add(new SetAlignmentAction(this,
        ModelMessages.TableWrapDataInfo_haLeft,
        "left.gif",
        true,
        TableWrapData.LEFT));
    manager.add(new SetAlignmentAction(this,
        ModelMessages.TableWrapDataInfo_haCenter,
        "center.gif",
        true,
        TableWrapData.CENTER));
    manager.add(new SetAlignmentAction(this,
        ModelMessages.TableWrapDataInfo_haRight,
        "right.gif",
        true,
        TableWrapData.RIGHT));
    manager.add(new SetAlignmentAction(this,
        ModelMessages.TableWrapDataInfo_haFill,
        "fill.gif",
        true,
        TableWrapData.FILL));
  }

  public void fillVerticalAlignmentMenu(IMenuManager manager) {
    manager.add(new SetAlignmentAction(this,
        ModelMessages.TableWrapDataInfo_vaTop,
        "top.gif",
        false,
        TableWrapData.TOP));
    manager.add(new SetAlignmentAction(this,
        ModelMessages.TableWrapDataInfo_vaMiddle,
        "middle.gif",
        false,
        TableWrapData.MIDDLE));
    manager.add(new SetAlignmentAction(this,
        ModelMessages.TableWrapDataInfo_vaBottom,
        "bottom.gif",
        false,
        TableWrapData.BOTTOM));
    manager.add(new SetAlignmentAction(this,
        ModelMessages.TableWrapDataInfo_vaGill,
        "fill.gif",
        false,
        TableWrapData.FILL));
  }
}