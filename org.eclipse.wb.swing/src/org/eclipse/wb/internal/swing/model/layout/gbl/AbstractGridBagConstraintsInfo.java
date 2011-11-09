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
package org.eclipse.wb.internal.swing.model.layout.gbl;

import org.eclipse.wb.core.editor.IContextMenuConstants;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.GenericPropertySetValue;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.state.EditorWarning;
import org.eclipse.wb.internal.swing.model.CoordinateUtils;
import org.eclipse.wb.internal.swing.model.ModelMessages;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.actions.SetAlignmentHorizontalAction;
import org.eclipse.wb.internal.swing.model.layout.gbl.actions.SetAlignmentVerticalAction;
import org.eclipse.wb.internal.swing.model.layout.gbl.actions.SetGrowAction;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.SystemUtils;

import java.awt.GridBagConstraints;
import java.util.List;

/**
 * Model for abstraction of {@link GridBagConstraints}.
 * 
 * @author scheglov_ke
 * @author sablin_aa
 * @coverage swing.model.layout
 */
public abstract class AbstractGridBagConstraintsInfo extends JavaInfo {
  private final AbstractGridBagConstraintsInfo m_this = this;
  protected boolean m_initialized = false;
  private boolean m_internalLocationChange = false;
  public int x;
  public int y;
  public int width;
  public int height;
  protected int anchor;
  protected int fill;
  public Insets insets;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractGridBagConstraintsInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    whenSetLocation_expandGrid();
    new GridBagConstraintsNameSupport(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initialize
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void initialize() throws Exception {
    super.initialize();
    addBroadcastListener(new ObjectEventListener() {
      @Override
      public void addContextMenu(List<? extends ObjectInfo> objects,
          ObjectInfo object,
          IMenuManager manager) throws Exception {
        if (getParent() == object) {
          AbstractGridBagConstraintsInfo.this.addContextMenu(manager);
        }
      }
    });
  }

  /**
   * When user sets gridx/gridy manually, he may try to set value outside of current grid. So, we
   * should expand grid to make this value valid.
   */
  private void whenSetLocation_expandGrid() {
    addBroadcastListener(new GenericPropertySetValue() {
      public void invoke(GenericPropertyImpl property, Object[] value, boolean[] shouldSetValue)
          throws Exception {
        if (!m_internalLocationChange
            && property.getJavaInfo() == m_this
            && value[0] instanceof Integer) {
          int newLocation = (Integer) value[0];
          if (newLocation >= 100) {
            shouldSetValue[0] = false;
            return;
          }
          if (property.getTitle().equals("gridx")) {
            getLayout().getColumnOperations().prepare(newLocation, false);
          }
          if (property.getTitle().equals("gridy")) {
            getLayout().getRowOperations().prepare(newLocation, false);
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
  protected void refresh_fetch() throws Exception {
    super.refresh_fetch();
    getCurrentObjectFields(false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link ColumnInfo} where located this {@link ComponentInfo}.
   */
  public ColumnInfo getColumn() {
    return getLayout().getColumns().get(x);
  }

  /**
   * @return the {@link RowInfo} where located this {@link ComponentInfo}.
   */
  public RowInfo getRow() {
    return getLayout().getRows().get(y);
  }

  /**
   * @return the {@link AbstractGridBagConstraintsInfo} that contains {@link ComponentInfo} of this
   *         {@link AbstractGridBagConstraintsInfo}.
   */
  private AbstractGridBagLayoutInfo getLayout() {
    ComponentInfo component = (ComponentInfo) getParent();
    ContainerInfo container = (ContainerInfo) component.getParent();
    return (AbstractGridBagLayoutInfo) container.getLayout();
  }

  /**
   * Initializes fields of {@link AbstractGridBagConstraintsInfo} from instance of
   * {@link AbstractGridBagConstraintsInfo}.
   */
  public void init() throws Exception {
    if (!m_initialized) {
      m_initialized = true;
      getCurrentObjectFields(true);
    }
  }

  /**
   * Gets values from {@link GridBagConstraints} object to this
   * {@link AbstractGridBagConstraintsInfo} fields.
   */
  public abstract void getCurrentObjectFields(boolean init) throws Exception;

  /**
   * This method materializes {@link AbstractGridBagConstraintsInfo} before asking properties to
   * allow using {@link ExpressionAccessor}'s from {@link CreationSupport}.
   * 
   * @return the {@link Property} with given title.
   */
  private Property getProperty(String title) throws Exception {
    if (isVirtual()) {
      materialize();
    }
    return getPropertyByTitle(title);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access: location
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Ensures that location values are expressed directly, not using
   * {@link GridBagConstraints#RELATIVE}.
   */
  public abstract void materializeLocation() throws Exception;

  /**
   * @return the "gridx" property value.
   */
  public int getX() {
    return x;
  }

  /**
   * Sets the "gridx" property.
   */
  public void setX(int x) throws Exception {
    this.x = x;
    setLocationPropertyValue("gridx", x);
  }

  /**
   * @return the "gridy" property value.
   */
  public int getY() {
    return y;
  }

  /**
   * Sets the "gridy" property.
   */
  public void setY(int y) throws Exception {
    this.y = y;
    setLocationPropertyValue("gridy", y);
  }

  /**
   * @return the "gridwidth" property value.
   */
  public int getWidth() {
    return width;
  }

  /**
   * Sets the "gridwidth" property.
   */
  public void setWidth(int width) throws Exception {
    this.width = width;
    getProperty("gridwidth").setValue(width);
  }

  /**
   * @return the "gridheight" property value.
   */
  public int getHeight() {
    return height;
  }

  /**
   * Sets the "gridheight" property.
   */
  public void setHeight(int height) throws Exception {
    this.height = height;
    getProperty("gridheight").setValue(height);
  }

  private void setLocationPropertyValue(String title, int value) throws Exception {
    m_internalLocationChange = true;
    try {
      getProperty(title).setValue(value);
    } finally {
      m_internalLocationChange = false;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access: insets
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the value of single side of "insets" property.
   */
  public int getInsets(String side) throws Exception {
    return ReflectionUtils.getFieldInt(insets, side);
  }

  /**
   * Sets the value for single side of "insets" property.
   */
  public void setInsets(String side, int value) throws Exception {
    ReflectionUtils.setField(insets, side, value);
    getProperty("insets").setValue(CoordinateUtils.get(insets));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access: alignment
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the horizontal alignment.
   */
  public abstract ColumnInfo.Alignment getHorizontalAlignment();

  /**
   * @return the vertical alignment.
   */
  public abstract RowInfo.Alignment getVerticalAlignment();

  /**
   * Sets horizontal alignment.
   */
  public void setHorizontalAlignment(ColumnInfo.Alignment alignment) throws Exception {
    setAlignment(alignment, getVerticalAlignment());
  }

  /**
   * Sets vertical alignment.
   */
  public void setVerticalAlignment(RowInfo.Alignment alignment) throws Exception {
    setAlignment(getHorizontalAlignment(), alignment);
  }

  /**
   * Sets horizontal/vertical alignments.<br>
   * This updates "fill" and "anchor" properties.
   */
  public void setAlignment(ColumnInfo.Alignment hAlignment, RowInfo.Alignment vAlignment)
      throws Exception {
    for (AlignmentInfo alignment : getAlignments()) {
      if (alignment.equals(hAlignment, vAlignment)) {
        {
          int newFill = getClassStaticFieldByName(alignment.fill);
          if (newFill != fill) {
            fill = newFill;
            GenericPropertyImpl property = (GenericPropertyImpl) getProperty("fill");
            String source = getClassNameForConstants() + "." + alignment.fill;
            property.setExpression(source, fill);
          }
        }
        {
          int newAnchor = getClassStaticFieldByName(alignment.anchor);
          if (newAnchor != anchor) {
            anchor = newAnchor;
            GenericPropertyImpl property = (GenericPropertyImpl) getProperty("anchor");
            String source = getClassNameForConstants() + "." + alignment.anchor;
            property.setExpression(source, anchor);
          }
        }
        break;
      }
    }
  }

  /**
   * @return constant value by name
   */
  protected int getClassStaticFieldByName(String fieldName) throws Exception {
    return ReflectionUtils.getFieldInt(getDescription().getComponentClass(), fieldName);
  }

  protected int getClassStaticFieldByNameSoft(String fieldName) {
    int value;
    try {
      value = getClassStaticFieldByName(fieldName);
    } catch (Exception e) {
      EditorState.get(getEditor()).addWarning(
          new EditorWarning("AbstractGridBagConstraintsInfo.getClassStaticFieldByName(String) :", e));
      value = 0;
    }
    return value;
  }

  /**
   * @return class name for source
   */
  public String getClassNameForConstants() {
    return ReflectionUtils.getFullyQualifiedName(getDescription().getComponentClass(), false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AlignmentInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Container with information about conversion column/row alignments into "fill" and "anchor".
   * 
   * @author scheglov_ke
   */
  protected static class AlignmentInfo {
    final public ColumnInfo.Alignment hAlignment;
    final public RowInfo.Alignment vAlignment;
    final public String fill;
    final public String anchor;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public AlignmentInfo(String alignmentString, final String fill, final String anchor) {
      Assert.isTrue(alignmentString.length() == 2);
      {
        char c = alignmentString.charAt(0);
        switch (c) {
          case 'L' :
            hAlignment = ColumnInfo.Alignment.LEFT;
            break;
          case 'C' :
            hAlignment = ColumnInfo.Alignment.CENTER;
            break;
          case 'R' :
            hAlignment = ColumnInfo.Alignment.RIGHT;
            break;
          default :
            Assert.isTrue(c == 'F');
            hAlignment = ColumnInfo.Alignment.FILL;
        }
      }
      {
        char c = alignmentString.charAt(1);
        switch (c) {
          case 'T' :
            vAlignment = RowInfo.Alignment.TOP;
            break;
          case 'C' :
            vAlignment = RowInfo.Alignment.CENTER;
            break;
          case 'B' :
            vAlignment = RowInfo.Alignment.BOTTOM;
            break;
          case 's' :
            vAlignment = RowInfo.Alignment.BASELINE;
            break;
          case 'a' :
            vAlignment = RowInfo.Alignment.BASELINE_ABOVE;
            break;
          case 'b' :
            vAlignment = RowInfo.Alignment.BASELINE_BELOW;
            break;
          default :
            Assert.isTrue(c == 'F');
            vAlignment = RowInfo.Alignment.FILL;
        }
      }
      // fill/anchor
      this.fill = fill;
      this.anchor = anchor;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    public boolean equals(ColumnInfo.Alignment hAlignment, RowInfo.Alignment vAlignment) {
      return this.hAlignment == hAlignment && this.vAlignment == vAlignment;
    }
  }

  protected abstract AlignmentInfo[] getAlignments();

  ////////////////////////////////////////////////////////////////////////////
  //
  // "virtual" and materialization
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if this {@link AbstractGridBagConstraintsInfo} is virtual.
   */
  public boolean isVirtual() {
    return getVariableSupport() instanceof VirtualConstraintsVariableSupport;
  }

  /**
   * Materializes this "virtual" {@link AbstractGridBagConstraintsInfo}.
   */
  private void materialize() throws Exception {
    ((VirtualConstraintsVariableSupport) getVariableSupport()).materialize();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Constraints" property
  //
  ////////////////////////////////////////////////////////////////////////////
  private ComplexProperty m_complexProperty;

  /**
   * Adds properties of this {@link AbstractGridBagConstraintsInfo} to the properties of its
   * {@link ComponentInfo}.
   */
  void addConstraintsProperties(List<Property> properties) throws Exception {
    // prepare complex property
    {
      if (m_complexProperty == null) {
        // prepare text
        String text;
        {
          Class<?> componentClass = getDescription().getComponentClass();
          text = "(" + componentClass.getName() + ")";
        }
        //
        m_complexProperty = new ComplexProperty("Constraints", text) {
          @Override
          public boolean isModified() throws Exception {
            return true;
          }
        };
        m_complexProperty.setCategory(PropertyCategory.system(6));
        // set sub-properties
        final Property[] constraintsProperties = getProperties();
        m_complexProperty.setProperties(constraintsProperties);
        // materialize constraints on sub-property modification
        if (isVirtual()) {
          addBroadcastListener(new JavaEventListener() {
            @Override
            public void setPropertyExpression(GenericPropertyImpl property,
                String[] source,
                Object[] value,
                boolean[] shouldSet) throws Exception {
              if (isVirtual() && ArrayUtils.contains(constraintsProperties, property)) {
                materialize();
              }
            }
          });
        }
      }
    }
    // add property
    properties.add(m_complexProperty);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Context menu
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds items to the context {@link IMenuManager}.
   */
  public void addContextMenu(IMenuManager manager) throws Exception {
    // horizontal
    {
      IMenuManager manager2 =
          new MenuManager(ModelMessages.AbstractGridBagConstraintsInfo_horizontalAlignment);
      manager.appendToGroup(IContextMenuConstants.GROUP_TOP, manager2);
      fillHorizontalAlignmentMenu(manager2);
    }
    // vertical
    {
      IMenuManager manager2 =
          new MenuManager(ModelMessages.AbstractGridBagConstraintsInfo_verticalAlignment);
      manager.appendToGroup(IContextMenuConstants.GROUP_TOP, manager2);
      fillVerticalAlignmentMenu(manager2);
    }
  }

  /**
   * Adds the horizontal alignment {@link Action}'s.
   */
  public void fillHorizontalAlignmentMenu(IMenuManager manager) {
    manager.add(new SetAlignmentHorizontalAction(this,
        ModelMessages.AbstractGridBagConstraintsInfo_haLeft,
        "left.gif",
        ColumnInfo.Alignment.LEFT));
    manager.add(new SetAlignmentHorizontalAction(this,
        ModelMessages.AbstractGridBagConstraintsInfo_haCenter,
        "center.gif",
        ColumnInfo.Alignment.CENTER));
    manager.add(new SetAlignmentHorizontalAction(this,
        ModelMessages.AbstractGridBagConstraintsInfo_haRight,
        "right.gif",
        ColumnInfo.Alignment.RIGHT));
    manager.add(new SetAlignmentHorizontalAction(this,
        ModelMessages.AbstractGridBagConstraintsInfo_haFill,
        "fill.gif",
        ColumnInfo.Alignment.FILL));
    manager.add(new Separator());
    manager.add(new SetGrowAction(this,
        ModelMessages.AbstractGridBagConstraintsInfo_haGrow,
        "grow.gif",
        true));
  }

  /**
   * Adds the vertical alignment {@link Action}'s.
   */
  public void fillVerticalAlignmentMenu(IMenuManager manager) {
    manager.add(new SetAlignmentVerticalAction(this,
        ModelMessages.AbstractGridBagConstraintsInfo_vaTop,
        "top.gif",
        RowInfo.Alignment.TOP));
    manager.add(new SetAlignmentVerticalAction(this,
        ModelMessages.AbstractGridBagConstraintsInfo_vaCenter,
        "center.gif",
        RowInfo.Alignment.CENTER));
    manager.add(new SetAlignmentVerticalAction(this,
        ModelMessages.AbstractGridBagConstraintsInfo_vaBottom,
        "bottom.gif",
        RowInfo.Alignment.BOTTOM));
    manager.add(new SetAlignmentVerticalAction(this,
        ModelMessages.AbstractGridBagConstraintsInfo_vaFill,
        "fill.gif",
        RowInfo.Alignment.FILL));
    if (SystemUtils.IS_JAVA_1_6 || SystemUtils.IS_JAVA_1_7) {
      manager.add(new SetAlignmentVerticalAction(this,
          ModelMessages.AbstractGridBagConstraintsInfo_vaBaseline,
          "baseline.gif",
          RowInfo.Alignment.BASELINE));
      manager.add(new SetAlignmentVerticalAction(this,
          ModelMessages.AbstractGridBagConstraintsInfo_vaAboveBaseline,
          "baseline_above.gif",
          RowInfo.Alignment.BASELINE_ABOVE));
      manager.add(new SetAlignmentVerticalAction(this,
          ModelMessages.AbstractGridBagConstraintsInfo_vaBelowBaseline,
          "baseline_below.gif",
          RowInfo.Alignment.BASELINE_BELOW));
    }
    manager.add(new Separator());
    manager.add(new SetGrowAction(this,
        ModelMessages.AbstractGridBagConstraintsInfo_vaGrow,
        "grow.gif",
        false));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Source utils 
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return source for initializing new constraints object instance
   */
  public abstract String newInstanceSourceLong();

  public abstract String newInstanceSourceShort();
}
