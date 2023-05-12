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
package org.eclipse.wb.internal.swing.FormLayout.model;

import org.eclipse.wb.core.editor.IContextMenuConstants;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.IntegerPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.StaticFieldPropertyEditor;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;
import org.eclipse.wb.internal.swing.FormLayout.Activator;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.graphics.Image;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.CellConstraints.Alignment;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Field;
import java.text.MessageFormat;

/**
 * The object that provides access for the JGoodies CellConstraints.<br>
 * It should be used for single read/modification and thrown away.
 *
 * @author scheglov_ke
 * @coverage swing.FormLayout.model
 */
public final class CellConstraintsSupport {
  private final FormLayoutInfo m_layout;
  private final ComponentInfo m_component;
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constraints values
  //
  ////////////////////////////////////////////////////////////////////////////
  int x;
  int y;
  int width;
  int height;
  CellConstraints.Alignment alignH;
  CellConstraints.Alignment alignV;
  ////////////////////////////////////////////////////////////////////////////
  //
  // Saved values
  //
  ////////////////////////////////////////////////////////////////////////////
  private int saved_x;
  private int saved_y;
  private int saved_width;
  private int saved_height;
  private CellConstraints.Alignment saved_alignH;
  private CellConstraints.Alignment saved_alignV;

  /**
   * @return <code>true</code> if {@link CellConstraintsSupport} was modified since last write.
   */
  private boolean needWrite() {
    return saved_x != x
        || saved_y != y
        || saved_width != width
        || saved_height != height
        || saved_alignH != alignH
        || saved_alignV != alignV;
  }

  /**
   * Copies current values into saved ones.
   */
  private void rememberWrittenState() {
    saved_x = x;
    saved_y = y;
    saved_width = width;
    saved_height = height;
    saved_alignH = alignH;
    saved_alignV = alignV;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  CellConstraintsSupport(FormLayoutInfo layoutInfo, ComponentInfo componentInfo) {
    m_layout = layoutInfo;
    m_component = componentInfo;
    // fetch values
    FormLayout layout = (FormLayout) m_layout.getObject();
    if (layout != null
        && m_component.getComponent() != null
        && m_component.getComponent().getParent() == m_layout.getContainer().getContainer()) {
      CellConstraints constraints = layout.getConstraints(m_component.getComponent());
      x = constraints.gridX;
      y = constraints.gridY;
      width = constraints.gridWidth;
      height = constraints.gridHeight;
      alignH = constraints.hAlign;
      alignV = constraints.vAlign;
    } else {
      x = y = width = height = 1;
      alignH = alignV = CellConstraints.DEFAULT;
    }
    // remember state
    rememberWrittenState();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the location/span.
   */
  public void setSpan(boolean horizontal, Rectangle cells) throws Exception {
    x = cells.x;
    y = cells.y;
    width = cells.width;
    height = cells.height;
  }

  /**
   * Sets the horizontal alignment.
   */
  public void setAlignH(CellConstraints.Alignment alignment) {
    alignH = alignment;
  }

  /**
   * Sets the vertical alignment.
   */
  public void setAlignV(CellConstraints.Alignment alignment) {
    alignV = alignment;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Write
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Writes current values as constraints in component association using
   * {@link Container#add(Component, Object)}.
   */
  public void write() throws Exception {
    // check if write required
    if (needWrite()) {
      // prepare constraints source
      String source;
      {
        source = x + ", " + y;
        // span
        if (width != 1 || height != 1) {
          source += ", " + width + ", " + height;
        }
        // align
        if (alignH != CellConstraints.DEFAULT || alignV != CellConstraints.DEFAULT) {
          source += ", " + alignH + ", " + alignV;
        }
      }
      // update association constraints
      if (m_component.getAssociation() instanceof InvocationChildAssociation) {
        InvocationChildAssociation association =
            (InvocationChildAssociation) m_component.getAssociation();
        MethodInvocation invocation = association.getInvocation();
        String signature = AstNodeUtils.getMethodSignature(invocation);
        if (signature.equals("add(java.awt.Component,java.lang.Object)")) {
          Expression constraintsExpression = (Expression) invocation.arguments().get(1);
          m_layout.getEditor().replaceExpression(constraintsExpression, "\"" + source + "\"");
        } else if (signature.equals("add(java.awt.Component)")) {
          m_layout.getEditor().addInvocationArgument(invocation, 1, "\"" + source + "\"");
        }
      }
      // remember state
      rememberWrittenState();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  private ComplexProperty m_complexProperty;

  /**
   * @return the {@link Property} with given title.
   */
  public Property getPropertyByTitle(String title) throws Exception {
    for (Property property : getCellProperty().getProperties()) {
      if (property.getTitle().equals(title)) {
        return property;
      }
    }
    return null;
  }

  /**
   * @return the {@link ComplexProperty} for this {@link CellConstraintsSupport}.
   */
  public ComplexProperty getCellProperty() throws Exception {
    if (m_complexProperty == null) {
      m_complexProperty = new ComplexProperty("Constraints", null);
      m_complexProperty.setCategory(PropertyCategory.system(6));
      // grid properties
      Property xProperty = new IntegerCellProperty("grid x", "x") {
        @Override
        public boolean isModified() throws Exception {
          return true;
        }

        @Override
        protected String validate(int value) {
          int columns = m_layout.getColumns().size();
          if (1 <= value && value + width - 1 <= columns) {
            return null;
          }
          return MessageFormat.format(
              ModelMessages.CellConstraintsSupport_outOfRange,
              value,
              (columns - width + 1));
        }
      };
      Property wProperty = new IntegerCellProperty("grid width", "width") {
        @Override
        public boolean isModified() throws Exception {
          return width != 1;
        }

        @Override
        protected Object getDefaultValue() {
          return 1;
        }

        @Override
        protected String validate(int value) {
          int columns = m_layout.getColumns().size();
          if (1 <= value && x + value - 1 <= columns) {
            return null;
          }
          return MessageFormat.format(
              ModelMessages.CellConstraintsSupport_outOfRange,
              value,
              (columns - x + 1));
        }
      };
      Property yProperty = new IntegerCellProperty("grid y", "y") {
        @Override
        public boolean isModified() throws Exception {
          return true;
        }

        @Override
        protected String validate(int value) {
          int rows = m_layout.getRows().size();
          if (1 <= value && value + height - 1 <= rows) {
            return null;
          }
          return MessageFormat.format(ModelMessages.CellConstraintsSupport_outOfRange, value, (rows
              - y + 1));
        }
      };
      Property hProperty = new IntegerCellProperty("grid height", "height") {
        @Override
        public boolean isModified() throws Exception {
          return height != 1;
        }

        @Override
        protected Object getDefaultValue() {
          return 1;
        }

        @Override
        protected String validate(int value) {
          int rows = m_layout.getRows().size();
          if (1 <= value && y + value - 1 <= rows) {
            return null;
          }
          return MessageFormat.format(ModelMessages.CellConstraintsSupport_outOfRange, value, (rows
              - y + 1));
        }
      };
      // alignment properties
      Property hAlignmentProperty =
          new AlignmentCellProperty("h alignment", "alignH", CellConstraints.class, new String[]{
              "DEFAULT",
              "LEFT",
              "CENTER",
              "RIGHT",
              "FILL",});
      Property vAlignmentProperty =
          new AlignmentCellProperty("v alignment", "alignV", CellConstraints.class, new String[]{
              "DEFAULT",
              "TOP",
              "CENTER",
              "BOTTOM",
              "FILL",});
      // set sub-properties
      m_complexProperty.setProperties(new Property[]{
          xProperty,
          yProperty,
          wProperty,
          hProperty,
          hAlignmentProperty,
          vAlignmentProperty});
    }
    //
    m_complexProperty.setText(MessageFormat.format(
        "{0}, {1}, {2}, {3}, {4}, {5}",
        x,
        y,
        width,
        height,
        alignH,
        alignV));
    return m_complexProperty;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractCellProperty
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Abstract implementation of {@link Property} for {@link CellConstraintsSupport}.
   *
   * @author scheglov_ke
   */
  private abstract class AbstractCellProperty extends Property {
    private final String m_title;
    protected final Field m_field;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public AbstractCellProperty(String title, String fieldName, PropertyEditor propertyEditor)
        throws Exception {
      super(propertyEditor);
      m_title = title;
      {
        m_field = CellConstraintsSupport.class.getDeclaredField(fieldName);
        m_field.setAccessible(true);
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Property
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public final String getTitle() {
      return m_title;
    }

    @Override
    public final void setValue(Object value) throws Exception {
      // try to replace unknown value with some default value
      if (value == UNKNOWN_VALUE) {
        value = getDefaultValue();
      }
      // set known value
      if (value != UNKNOWN_VALUE) {
        // validate
        {
          String errorMessage = validate(value);
          if (errorMessage != null) {
            UiUtils.openWarning(DesignerPlugin.getShell(), getTitle(), errorMessage);
            return;
          }
        }
        // do modification
        m_layout.startEdit();
        try {
          m_field.set(CellConstraintsSupport.this, value);
          write();
        } finally {
          m_layout.endEdit();
        }
      }
    }

    /**
     * @return the default property value.
     */
    protected Object getDefaultValue() {
      return UNKNOWN_VALUE;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Value
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public Object getValue() throws Exception {
      return m_field.get(CellConstraintsSupport.this);
    }

    /**
     * @return <code>null</code> if given value is valid and can be set, or return some
     *         {@link String} with error message.
     */
    protected abstract String validate(Object value) throws Exception;
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // IntegerCellProperty
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Implementation of {@link Property} for integer fields of {@link CellConstraintsSupport}.
   *
   * @author scheglov_ke
   */
  private abstract class IntegerCellProperty extends AbstractCellProperty {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public IntegerCellProperty(String title, String fieldName) throws Exception {
      super(title, fieldName, IntegerPropertyEditor.INSTANCE);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Value
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected final String validate(Object value) throws Exception {
      if (!(value instanceof Integer)) {
        return ModelMessages.CellConstraintsSupport_integerExpected;
      }
      int intValue = ((Integer) value).intValue();
      return validate(intValue);
    }

    /**
     * @return <code>true</code> if given value is valid for this property. For example we should
     *         not allow to set width outside of number of existing columns.
     */
    protected abstract String validate(int value);
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // AlignmentCellProperty
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Implementation of {@link Property} for alignment fields of {@link CellConstraintsSupport}.
   *
   * @author scheglov_ke
   */
  private final class AlignmentCellProperty extends AbstractCellProperty {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public AlignmentCellProperty(String title, String fieldName, Class<?> clazz, String[] fieldNames)
        throws Exception {
      super(title, fieldName, new StaticFieldPropertyEditor());
      // configure editor
      StaticFieldPropertyEditor editor = (StaticFieldPropertyEditor) getEditor();
      editor.configure(clazz, fieldNames);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Value
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public final boolean isModified() throws Exception {
      return getValue() != CellConstraints.DEFAULT;
    }

    @Override
    protected Object getDefaultValue() {
      return CellConstraints.DEFAULT;
    }

    @Override
    protected String validate(Object value) throws Exception {
      return value instanceof CellConstraints.Alignment
          ? null
          : ModelMessages.CellConstraintsSupport_alignmentExpected;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Images
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the small {@link Image} that represents horizontal/vertical alignment.
   */
  public Image getSmallAlignmentImage(boolean horizontal) {
    if (horizontal) {
      if (alignH == CellConstraints.LEFT) {
        return Activator.getImage("alignment/h/left.gif");
      } else if (alignH == CellConstraints.CENTER) {
        return Activator.getImage("alignment/h/center.gif");
      } else if (alignH == CellConstraints.RIGHT) {
        return Activator.getImage("alignment/h/right.gif");
      } else if (alignH == CellConstraints.FILL) {
        return Activator.getImage("alignment/h/fill.gif");
      } else {
        return null;
      }
    } else {
      if (alignV == CellConstraints.TOP) {
        return Activator.getImage("alignment/v/top.gif");
      } else if (alignV == CellConstraints.CENTER) {
        return Activator.getImage("alignment/v/center.gif");
      } else if (alignV == CellConstraints.BOTTOM) {
        return Activator.getImage("alignment/v/bottom.gif");
      } else if (alignV == CellConstraints.FILL) {
        return Activator.getImage("alignment/v/fill.gif");
      } else {
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
  public void addContextMenu(IMenuManager manager) throws Exception {
    // horizontal
    {
      IMenuManager manager2 =
          new MenuManager(ModelMessages.CellConstraintsSupport_horizontalAlignment);
      manager.appendToGroup(IContextMenuConstants.GROUP_TOP, manager2);
      fillHorizontalAlignmentMenu(manager2);
    }
    // vertical
    {
      IMenuManager manager2 =
          new MenuManager(ModelMessages.CellConstraintsSupport_verticalAlignment);
      manager.appendToGroup(IContextMenuConstants.GROUP_TOP, manager2);
      fillVerticalAlignmentMenu(manager2);
    }
  }

  /**
   * Adds the horizontal alignment {@link Action}'s.
   */
  public void fillHorizontalAlignmentMenu(IMenuManager manager) {
    manager.add(new SetAlignmentAction(ModelMessages.CellConstraintsSupport_haDefault,
        "default.gif",
        true,
        CellConstraints.DEFAULT));
    manager.add(new SetAlignmentAction(ModelMessages.CellConstraintsSupport_haLeft,
        "left.gif",
        true,
        CellConstraints.LEFT));
    manager.add(new SetAlignmentAction(ModelMessages.CellConstraintsSupport_haCenter,
        "center.gif",
        true,
        CellConstraints.CENTER));
    manager.add(new SetAlignmentAction(ModelMessages.CellConstraintsSupport_haRight,
        "right.gif",
        true,
        CellConstraints.RIGHT));
    manager.add(new SetAlignmentAction(ModelMessages.CellConstraintsSupport_haFill,
        "fill.gif",
        true,
        CellConstraints.FILL));
  }

  /**
   * Adds the vertical alignment {@link Action}'s.
   */
  public void fillVerticalAlignmentMenu(IMenuManager manager2) {
    manager2.add(new SetAlignmentAction(ModelMessages.CellConstraintsSupport_vaDefault,
        "default.gif",
        false,
        CellConstraints.DEFAULT));
    manager2.add(new SetAlignmentAction(ModelMessages.CellConstraintsSupport_vaTop,
        "top.gif",
        false,
        CellConstraints.TOP));
    manager2.add(new SetAlignmentAction(ModelMessages.CellConstraintsSupport_vaCenter,
        "center.gif",
        false,
        CellConstraints.CENTER));
    manager2.add(new SetAlignmentAction(ModelMessages.CellConstraintsSupport_vaBottom,
        "bottom.gif",
        false,
        CellConstraints.BOTTOM));
    manager2.add(new SetAlignmentAction(ModelMessages.CellConstraintsSupport_vaFill,
        "fill.gif",
        false,
        CellConstraints.FILL));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // SetAlignmentAction
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link Action} for modifying horizontal/vertical alignment.
   */
  private class SetAlignmentAction extends ObjectInfoAction {
    private final boolean m_horizontal;
    private final Alignment m_alignment;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public SetAlignmentAction(String text, String iconPath, boolean horizontal, Alignment alignment) {
      super(m_layout, text, AS_RADIO_BUTTON);
      if (iconPath != null) {
        String path = "alignment/" + (horizontal ? "h" : "v") + "/menu/" + iconPath;
        setImageDescriptor(Activator.getImageDescriptor(path));
      }
      // remember values
      m_horizontal = horizontal;
      m_alignment = alignment;
      // set check for current alignment
      if (m_horizontal) {
        setChecked(alignH == m_alignment);
      } else {
        setChecked(alignV == m_alignment);
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Run
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected void runEx() throws Exception {
      if (m_horizontal) {
        alignH = m_alignment;
      } else {
        alignV = m_alignment;
      }
      write();
    }
  }
}
