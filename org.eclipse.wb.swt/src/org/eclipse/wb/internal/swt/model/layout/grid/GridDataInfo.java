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
package org.eclipse.wb.internal.swt.model.layout.grid;

import org.eclipse.wb.core.editor.IContextMenuConstants;
import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.GenericPropertyGetValue;
import org.eclipse.wb.core.model.broadcast.GenericPropertySetValue;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.StaticFieldPropertyEditor;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.model.ModelMessages;
import org.eclipse.wb.internal.swt.model.layout.LayoutDataInfo;
import org.eclipse.wb.internal.swt.model.layout.grid.actions.ClearHintAction;
import org.eclipse.wb.internal.swt.model.layout.grid.actions.SetAlignmentAction;
import org.eclipse.wb.internal.swt.model.layout.grid.actions.SetGrabAction;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.support.GridLayoutSupport;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;

import java.util.List;

/**
 * Model for SWT {@link GridData}.
 *
 * @author scheglov_ke
 * @coverage swt.model.layout
 */
public final class GridDataInfo extends LayoutDataInfo implements IGridDataInfo {
  private boolean m_internalLocationChange = false;
  int x = -1;
  int y = -1;
  int width = 1;
  int height = 1;
  boolean horizontalGrab;
  boolean verticalGrab;
  int horizontalAlignment;
  int verticalAlignment;
  int widthHint;
  int heightHint;
  boolean exclude;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GridDataInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    useAccessorsForPropertyValues();
    contributeContextMenu();
    validateSpanPropertyValues();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Broadcasts
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Alignment fields may have "old" values, from {@link GridDataInfo}, instead of "modern" - from
   * {@link SWT}, but our property editors want only "modern". So, we intercept value request and
   * return "modern" values.
   */
  private void useAccessorsForPropertyValues() {
    addBroadcastListener(new GenericPropertyGetValue() {
      public void invoke(GenericPropertyImpl property, Object[] value) throws Exception {
        if (property.getJavaInfo() == GridDataInfo.this) {
          String title = property.getTitle();
          if (title.equals("horizontalAlignment")) {
            value[0] = getHorizontalAlignment();
          }
          if (title.equals("verticalAlignment")) {
            value[0] = getVerticalAlignment();
          }
        }
      }
    });
  }

  /**
   * Don't crash if user tries to use invalid span values.
   */
  private void validateSpanPropertyValues() {
    addBroadcastListener(new GenericPropertySetValue() {
      public void invoke(GenericPropertyImpl property, Object[] value, boolean[] shouldSetValue)
          throws Exception {
        if (m_internalLocationChange) {
          return;
        }
        if (property.getJavaInfo() == GridDataInfo.this && value[0] instanceof Integer) {
          String title = property.getTitle();
          if (title.equals("horizontalSpan")) {
            int span = (Integer) value[0];
            if (!isValidHorizontalSpan(span)) {
              shouldSetValue[0] = false;
              return;
            }
          }
          if (title.equals("verticalSpan")) {
            int span = (Integer) value[0];
            if (!isValidVerticalSpan(span)) {
              shouldSetValue[0] = false;
              return;
            }
          }
        }
      }

      private boolean isValidHorizontalSpan(int span) throws Exception {
        if (span <= 0) {
          return false;
        }
        IGridInfo grid = getLayout().getGridInfo();
        Rectangle cells = grid.getComponentCells(getControl());
        if (cells.x + span > grid.getColumnCount()) {
          return false;
        }
        return true;
      }

      private boolean isValidVerticalSpan(int span) throws Exception {
        if (span <= 0) {
          return false;
        }
        IGridInfo grid = getLayout().getGridInfo();
        Rectangle cells = grid.getComponentCells(getControl());
        if (cells.y + span > grid.getRowCount()) {
          return false;
        }
        return true;
      }
    });
  }

  private void contributeContextMenu() {
    addBroadcastListener(new ObjectEventListener() {
      @Override
      public void addContextMenu(List<? extends ObjectInfo> objects,
          ObjectInfo object,
          IMenuManager manager) throws Exception {
        if (getParent() == object) {
          GridDataInfo.this.addContextMenu(manager);
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
    getCurrentObjectFields();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if this {@link GridData} has "exclude" flag.
   */
  public boolean getExclude() {
    return exclude;
  }

  /**
   * Gets values from {@link GridData} object to this {@link GridDataInfo} fields.
   */
  private void getCurrentObjectFields() throws Exception {
    Object object = getObject();
    // prepare GridLayout
    GridLayoutInfo layout = getLayout();
    if (layout == null) {
      return;
    }
    // location
    {
      Object layoutObject = layout.getObject();
      Object controlObject = getControl().getObject();
      Point xyPoint = GridLayoutSupport.getXY(layoutObject, controlObject);
      if (xyPoint != null) {
        x = xyPoint.x;
        y = xyPoint.y;
      }
    }
    // span
    width = ReflectionUtils.getFieldInt(object, "horizontalSpan");
    height = ReflectionUtils.getFieldInt(object, "verticalSpan");
    // grab
    horizontalGrab = ReflectionUtils.getFieldBoolean(object, "grabExcessHorizontalSpace");
    verticalGrab = ReflectionUtils.getFieldBoolean(object, "grabExcessVerticalSpace");
    // alignment
    {
      horizontalAlignment = ReflectionUtils.getFieldInt(object, "horizontalAlignment");
      horizontalAlignment = getModernHorizontalAlignment(horizontalAlignment);
    }
    {
      verticalAlignment = ReflectionUtils.getFieldInt(object, "verticalAlignment");
      verticalAlignment = getModernVerticalAlignment(verticalAlignment);
    }
    // hint
    {
      widthHint = ReflectionUtils.getFieldInt(object, "widthHint");;
      heightHint = ReflectionUtils.getFieldInt(object, "heightHint");;
    }
    // fix "width", if GridData uses invalid value
    {
      int numColumns = layout.getNumColumns();
      width = Math.max(1, Math.min(width, numColumns));
    }
    // exclude
    exclude = ReflectionUtils.getFieldBoolean(object, "exclude");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal access
  //
  ////////////////////////////////////////////////////////////////////////////
  private ControlInfo getControl() {
    return (ControlInfo) getParent();
  }

  private GridLayoutInfo getLayout() {
    CompositeInfo composite = (CompositeInfo) getControl().getParent();
    if (composite == null) {
      return null;
    }
    return (GridLayoutInfo) composite.getLayout();
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
  public void setHorizontalSpan(int width) throws Exception {
    if (this.width != width) {
      this.width = width;
      m_internalLocationChange = true;
      try {
        getPropertyByTitle("horizontalSpan").setValue(width);
      } finally {
        m_internalLocationChange = false;
      }
    }
  }

  public void setVerticalSpan(int height) throws Exception {
    if (this.height != height) {
      this.height = height;
      m_internalLocationChange = true;
      try {
        getPropertyByTitle("verticalSpan").setValue(height);
      } finally {
        m_internalLocationChange = false;
      }
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
    horizontalGrab = grab;
    getPropertyByTitle("grabExcessHorizontalSpace").setValue(grab ? Boolean.TRUE : Boolean.FALSE);
  }

  public boolean getVerticalGrab() {
    return verticalGrab;
  }

  public void setVerticalGrab(boolean grab) throws Exception {
    verticalGrab = grab;
    getPropertyByTitle("grabExcessVerticalSpace").setValue(grab ? Boolean.TRUE : Boolean.FALSE);
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
    alignment = getModernHorizontalAlignment(alignment);
    if (horizontalAlignment != alignment) {
      horizontalAlignment = alignment;
      setEnumProperty("horizontalAlignment", horizontalAlignment);
    }
  }

  public int getVerticalAlignment() {
    return verticalAlignment;
  }

  public void setVerticalAlignment(int alignment) throws Exception {
    alignment = getModernVerticalAlignment(alignment);
    if (verticalAlignment != alignment) {
      verticalAlignment = alignment;
      setEnumProperty("verticalAlignment", verticalAlignment);
    }
  }

  /**
   * @return the {@link GridData} horizontal alignment constant from SWT.
   */
  private int getModernHorizontalAlignment(int alignment) {
    switch (alignment) {
      case GridData.BEGINNING :
        return SWT.LEFT;
      case GridData.CENTER :
        return SWT.CENTER;
      case GridData.END :
      case SWT.END :
        return SWT.RIGHT;
      case GridData.FILL :
        return SWT.FILL;
    }
    // as is, and hope that it is correct
    return alignment;
  }

  /**
   * @return the {@link GridData} vertical alignment constant from SWT.
   */
  private int getModernVerticalAlignment(int alignment) {
    switch (alignment) {
      case GridData.BEGINNING :
        return SWT.TOP;
      case GridData.CENTER :
        return SWT.CENTER;
      case GridData.END :
      case SWT.END :
        return SWT.BOTTOM;
      case GridData.FILL :
        return SWT.FILL;
    }
    // as is, and hope that it is correct
    return alignment;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Hint
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return value of <code>widthHint</code> property.
   */
  public int getWidthHint() {
    return widthHint;
  }

  /**
   * @return value of <code>heightHint</code> property.
   */
  public int getHeightHint() {
    return heightHint;
  }

  public void setWidthHint(int widthHint) throws Exception {
    if (this.widthHint != widthHint) {
      this.widthHint = widthHint;
      getPropertyByTitle("widthHint").setValue(widthHint);
    }
  }

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

  ////////////////////////////////////////////////////////////////////////////
  //
  // Images
  //
  ////////////////////////////////////////////////////////////////////////////
  public Image getSmallAlignmentImage(boolean horizontal) {
    if (horizontal) {
      switch (horizontalAlignment) {
        case SWT.LEFT :
          return GridImages.getImage("h/left.gif");
        case SWT.CENTER :
          return GridImages.getImage("h/center.gif");
        case SWT.RIGHT :
          return GridImages.getImage("h/right.gif");
        case SWT.FILL :
          return GridImages.getImage("h/fill.gif");
        default :
          return null;
      }
    } else {
      switch (verticalAlignment) {
        case SWT.TOP :
          return GridImages.getImage("v/top.gif");
        case SWT.CENTER :
          return GridImages.getImage("v/center.gif");
        case SWT.BOTTOM :
          return GridImages.getImage("v/bottom.gif");
        case SWT.FILL :
          return GridImages.getImage("v/fill.gif");
        default :
          return null;
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
      IMenuManager manager2 = new MenuManager(ModelMessages.GridDataInfo_horizontalAlignment);
      manager.appendToGroup(IContextMenuConstants.GROUP_TOP, manager2);
      //
      manager2.add(new SetGrabAction(this,
          ModelMessages.GridDataInfo_grabExcessSpace,
          "grow.gif",
          true));
      if (getWidthHint() != SWT.DEFAULT) {
        manager2.add(new ClearHintAction(this, ModelMessages.GridDataInfo_clearHint, true));
      }
      manager2.add(new Separator());
      //
      fillHorizontalAlignmentMenu(manager2);
    }
    // vertical
    {
      IMenuManager manager2 = new MenuManager(ModelMessages.GridDataInfo_verticalAlignment);
      manager.appendToGroup(IContextMenuConstants.GROUP_TOP, manager2);
      //
      manager2.add(new SetGrabAction(this,
          ModelMessages.GridDataInfo_grabExcessSpace,
          "grow.gif",
          false));
      if (getHeightHint() != SWT.DEFAULT) {
        manager2.add(new ClearHintAction(this, ModelMessages.GridDataInfo_clearHint, false));
      }
      manager2.add(new Separator());
      //
      fillVerticalAlignmentMenu(manager2);
    }
  }

  public void fillHorizontalAlignmentMenu(IMenuManager manager) {
    manager.add(new SetAlignmentAction(this,
        ModelMessages.GridDataInfo_horLeft,
        "left.gif",
        true,
        SWT.LEFT));
    manager.add(new SetAlignmentAction(this,
        ModelMessages.GridDataInfo_horCenter,
        "center.gif",
        true,
        SWT.CENTER));
    manager.add(new SetAlignmentAction(this,
        ModelMessages.GridDataInfo_horRight,
        "right.gif",
        true,
        SWT.RIGHT));
    manager.add(new SetAlignmentAction(this,
        ModelMessages.GridDataInfo_horFill,
        "fill.gif",
        true,
        SWT.FILL));
  }

  public void fillVerticalAlignmentMenu(IMenuManager manager) {
    manager.add(new SetAlignmentAction(this,
        ModelMessages.GridDataInfo_verTop,
        "top.gif",
        false,
        SWT.TOP));
    manager.add(new SetAlignmentAction(this,
        ModelMessages.GridDataInfo_verCenter,
        "center.gif",
        false,
        SWT.CENTER));
    manager.add(new SetAlignmentAction(this,
        ModelMessages.GridDataInfo_verBottom,
        "bottom.gif",
        false,
        SWT.BOTTOM));
    manager.add(new SetAlignmentAction(this,
        ModelMessages.GridDataInfo_verFill,
        "fill.gif",
        false,
        SWT.FILL));
  }
}