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
package org.eclipse.wb.internal.swt.model.layout.absolute;

import org.eclipse.wb.core.editor.IContextMenuConstants;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.ObjectInfoUtils;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.core.model.broadcast.JavaInfoAddProperties;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.layout.absolute.BoundsProperty;
import org.eclipse.wb.internal.core.model.layout.absolute.IPreferenceConstants;
import org.eclipse.wb.internal.core.model.layout.absolute.OrderingSupport;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.converter.IntegerConverter;
import org.eclipse.wb.internal.core.model.util.AbsoluteLayoutCreationFlowSupport;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.swt.Activator;
import org.eclipse.wb.internal.swt.model.layout.LayoutClipboardCommand;
import org.eclipse.wb.internal.swt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.layout.FormLayout;

import java.util.Collections;
import java.util.List;

/**
 * Model for "null" (absolute) layout.
 * 
 * @author mitin_aa
 * @author scheglov_ke
 * @author lobas_av
 * @coverage swt.model.layout
 */
public final class AbsoluteLayoutInfo extends LayoutInfo
    implements
      IAbsoluteLayoutInfo<ControlInfo> {
  // method names
  private static final String SET_SIZE_POINT = "setSize(org.eclipse.swt.graphics.Point)";
  private static final String SET_SIZE_INT_INT = "setSize(int,int)";
  private static final String SET_LOCATION_POINT = "setLocation(org.eclipse.swt.graphics.Point)";
  private static final String SET_LOCATION_INT_INT = "setLocation(int,int)";

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbsoluteLayoutInfo(AstEditor editor,
      ToolkitDescription toolkit,
      CreationSupport creationSupport) throws Exception {
    super(editor, new ComponentDescription(null), creationSupport);
    ObjectInfoUtils.setNewId(this);
    getDescription().setToolkit(toolkit);
    getDescription().setOrder("first");
    getDescription().setIcon(Activator.getImage("info/layout/absolute/layout.gif"));
    setVariableSupport(new AbsoluteLayoutVariableSupport(this));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initialization
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void initialize() throws Exception {
    super.initialize();
    whenSetComplexLayout_forChildComposite();
    // auto size
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void setPropertyExpression(GenericPropertyImpl property,
          String[] source,
          Object[] value,
          boolean[] shouldSet) throws Exception {
        checkForAutoSize(property);
      }

      private void checkForAutoSize(GenericPropertyImpl property) throws Exception {
        IPreferenceStore preferences = getDescription().getToolkit().getPreferences();
        if (preferences.getBoolean(IPreferenceConstants.P_AUTOSIZE_ON_PROPERTY_CHANGE)
            && isManagedObject(property.getJavaInfo())) {
          GenericPropertyDescription propertyDescription = property.getDescription();
          if (propertyDescription != null) {
            boolean isTextProperty = propertyDescription.hasTrueTag("isText");
            boolean isImageProperty = propertyDescription.hasTrueTag("isImage");
            if (isTextProperty || isImageProperty) {
              // schedule auto-size
              final ControlInfo control = (ControlInfo) property.getJavaInfo();
              ExecutionUtils.runLater(control, new RunnableEx() {
                public void run() throws Exception {
                  commandChangeBounds(control, null, control.getPreferredSize());
                }
              });
            }
          }
        }
      }
    });
    // Bounds property
    addBroadcastListener(new JavaInfoAddProperties() {
      public void invoke(JavaInfo javaInfo, List<Property> properties) throws Exception {
        if (isManagedObject(javaInfo)) {
          ControlInfo control = (ControlInfo) javaInfo;
          properties.add(getBoundsProperty(control));
        }
      }
    });
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void canMove(JavaInfo javaInfo, boolean[] forceMoveEnable, boolean[] forceMoveDisable)
          throws Exception {
        if (isManagedObject(javaInfo)) {
          forceMoveEnable[0] = true;
        }
      }
    });
    // context menu
    addBroadcastListener(new ObjectEventListener() {
      @Override
      public void addContextMenu(List<? extends ObjectInfo> objects,
          ObjectInfo object,
          IMenuManager manager) throws Exception {
        if (isManagedObject(object)) {
          ControlInfo control = (ControlInfo) object;
          contributeControlContextMenu(manager, control);
        }
      }
    });
  }

  /**
   * Absolute layout is place when layout stops. So, even while at design time we show correct GUI,
   * at run time complex layout of child will not work without special tricks. So, we ask user to
   * use {@link FormLayout} instead on "absolute".
   */
  private void whenSetComplexLayout_forChildComposite() {
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void addAfter(JavaInfo parent, JavaInfo child) throws Exception {
        if (child instanceof LayoutInfo
            && !(child instanceof AbsoluteLayoutInfo)
            && parent instanceof CompositeInfo
            && parent.getParent() == getComposite()) {
          int result = openQuestion();
          if (result == 0) {
            LayoutInfo formLayout =
                (LayoutInfo) JavaInfoUtils.createJavaInfo(
                    getEditor(),
                    "org.eclipse.swt.layout.FormLayout",
                    new ConstructorCreationSupport());
            getComposite().setLayout(formLayout);
          }
        }
      }

      private int openQuestion() {
        MessageDialog dialog =
            new MessageDialog(DesignerPlugin.getShell(),
                "Confirm",
                null,
                "You are attempting to use a normal layout with a container that is within a parent container using 'null' layout. "
                    + "Using 'null' layout in the parent will prevent the child layouts from working due to a bug in SWT "
                    + "(in Designer you will see the correct layout, but at runtime you will see empty containers). "
                    + "You can use FormLayout in the parent, if you need a free form layout and want to keep the child layouts working. "
                    + "Do you want to use FormLayout in the parent container?",
                MessageDialog.WARNING,
                new String[]{"Yes, use FormLayout", "No, keep 'null' layout"},
                0);
        return dialog.open();
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Context menu
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Contributes {@link Action}'s into {@link ControlInfo} context menu.
   */
  private void contributeControlContextMenu(IMenuManager manager, final ControlInfo control) {
    // order
    {
      List<ControlInfo> controls = getComposite().getChildrenControls();
      new OrderingSupport(controls, control).contributeActions(manager);
    }
    // auto-size
    {
      IAction action =
          new ObjectInfoAction(control, "Autosize control",
              DesignerPlugin.getImageDescriptor("info/layout/absolute/fit_to_size.png")) {
            @Override
            protected void runEx() throws Exception {
              commandChangeBounds(control, null, control.getPreferredSize());
            }
          };
      manager.appendToGroup(IContextMenuConstants.GROUP_CONSTRAINTS, action);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Conversion
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void onSet() throws Exception {
    for (ControlInfo control : getComposite().getChildrenControls()) {
      Rectangle bounds = control.getModelBounds();
      control.addMethodInvocation("setBounds(int,int,int,int)", bounds.x
          + ", "
          + bounds.y
          + ", "
          + bounds.width
          + ", "
          + bounds.height);
    }
  }

  @Override
  protected void deleteLayoutData(ControlInfo control) throws Exception {
    super.deleteLayoutData(control);
    control.removeMethodInvocations(SET_LOCATION_INT_INT);
    control.removeMethodInvocations(SET_LOCATION_POINT);
    control.removeMethodInvocations(SET_SIZE_INT_INT);
    control.removeMethodInvocations(SET_SIZE_POINT);
    control.removeMethodInvocations("setBounds(int,int,int,int)");
    control.removeMethodInvocations("setBounds(org.eclipse.swt.graphics.Rectangle)");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<Property> getPropertyList() throws Exception {
    return Collections.emptyList();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Bounds Property
  //
  ////////////////////////////////////////////////////////////////////////////
  private Property getBoundsProperty(ControlInfo control) {
    ComplexProperty boundsProperty = (ComplexProperty) control.getArbitraryValue(this);
    if (boundsProperty == null) {
      boundsProperty = new ComplexProperty("Bounds", null);
      boundsProperty.setCategory(PropertyCategory.system(5));
      boundsProperty.setModified(true);
      control.putArbitraryValue(this, boundsProperty);
      // x
      BoundsProperty<?> xProperty = new BoundsProperty<ControlInfo>(control, "x") {
        @Override
        public void setValue2(int value, Rectangle modelBounds) throws Exception {
          commandChangeBounds(m_component, new Point(value, modelBounds.y), null);
        }
      };
      // y
      BoundsProperty<?> yProperty = new BoundsProperty<ControlInfo>(control, "y") {
        @Override
        public void setValue2(int value, Rectangle modelBounds) throws Exception {
          commandChangeBounds(m_component, new Point(modelBounds.x, value), null);
        }
      };
      // width
      BoundsProperty<?> widthProperty = new BoundsProperty<ControlInfo>(control, "width") {
        @Override
        public void setValue2(int value, Rectangle modelBounds) throws Exception {
          commandChangeBounds(m_component, null, new Dimension(value, modelBounds.height));
        }
      };
      // height
      BoundsProperty<?> heightProperty = new BoundsProperty<ControlInfo>(control, "height") {
        @Override
        public void setValue2(int value, Rectangle modelBounds) throws Exception {
          commandChangeBounds(m_component, null, new Dimension(modelBounds.width, value));
        }
      };
      boundsProperty.setProperties(new Property[]{
          xProperty,
          yProperty,
          widthProperty,
          heightProperty});
    }
    Rectangle modelBounds = control.getModelBounds();
    if (modelBounds != null) {
      boundsProperty.setText("("
          + modelBounds.x
          + ", "
          + modelBounds.y
          + ", "
          + modelBounds.width
          + ", "
          + modelBounds.height
          + ")");
    }
    return boundsProperty;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  public void commandCreate(ControlInfo control, ControlInfo nextControl) throws Exception {
    command_CREATE(control, nextControl);
  }

  public void commandMove(ControlInfo control, ControlInfo nextControl) throws Exception {
    command_MOVE(control, nextControl);
  }

  public void commandChangeBounds(ControlInfo widget, Point location, Dimension size)
      throws Exception {
    commandChangeBounds0(widget, location, size);
    // apply creation flow
    if (location != null && useCreationFlow()) {
      AbsoluteLayoutCreationFlowSupport.apply(getComposite(), getControls(), widget, location, size);
    }
  }

  private void commandChangeBounds0(ControlInfo widget, Point location, Dimension size)
      throws Exception {
    Assert.isLegal(location != null || size != null, "Either location or size may not be null.");
    AstEditor editor = widget.getEditor();
    // setBounds(int,int,int,int)
    {
      MethodInvocation mi = widget.getMethodInvocation("setBounds(int,int,int,int)");
      if (mi != null) {
        if (location != null) {
          setExpression(mi, 0, location.x);
          setExpression(mi, 1, location.y);
        }
        if (size != null) {
          setExpression(mi, 2, size.width);
          setExpression(mi, 3, size.height);
        }
        removeUnusedMethodsAfterSetBounds(widget);
        return;
      }
    }
    // setBounds(org.eclipse.swt.graphics.Rectangle)
    {
      MethodInvocation mi =
          widget.getMethodInvocation("setBounds(org.eclipse.swt.graphics.Rectangle)");
      if (mi != null) {
        Rectangle widgetBounds = widget.getModelBounds();
        Expression exp = (Expression) mi.arguments().get(0);
        String newCode = "new org.eclipse.swt.graphics.Rectangle(";
        if (location != null) {
          newCode += location.x + ", " + location.y;
        } else {
          newCode += widgetBounds.x + ", " + widgetBounds.y;
        }
        newCode += ", ";
        if (size != null) {
          newCode += size.width + ", " + size.height;
        } else {
          newCode += widgetBounds.width + ", " + widgetBounds.height;
        }
        newCode += ")";
        editor.replaceExpression(exp, newCode);
        removeUnusedMethodsAfterSetBounds(widget);
        return;
      }
    }
    boolean setLocationFound = false;
    boolean setSizeFound = false;
    if (location != null) {
      // setLocation(int,int)
      {
        MethodInvocation mi = widget.getMethodInvocation(SET_LOCATION_INT_INT);
        if (mi != null) {
          setLocationFound = true;
          setExpression(mi, 0, location.x);
          setExpression(mi, 1, location.y);
        }
      }
      // setLocation(new org.eclipse.swt.graphics.Point)
      {
        MethodInvocation mi = widget.getMethodInvocation(SET_LOCATION_POINT);
        if (mi != null) {
          setLocationFound = true;
          Expression exp = (Expression) mi.arguments().get(0);
          String newCode = "new org.eclipse.swt.graphics.Point(";
          newCode += location.x + ", " + location.y + ")";
          editor.replaceExpression(exp, newCode);
        }
      }
    }
    if (size != null) {
      // setSize(int,int)
      {
        MethodInvocation mi = widget.getMethodInvocation(SET_SIZE_INT_INT);
        if (mi != null) {
          setSizeFound = true;
          setExpression(mi, 0, size.width);
          setExpression(mi, 1, size.height);
        }
      }
      // setSize(new org.eclipse.swt.graphics.Point)
      {
        MethodInvocation mi = widget.getMethodInvocation(SET_SIZE_POINT);
        if (mi != null) {
          setSizeFound = true;
          Expression exp = (Expression) mi.arguments().get(0);
          String newCode = "new org.eclipse.swt.graphics.Point(";
          newCode += size.width + ", " + size.height + ")";
          editor.replaceExpression(exp, newCode);
        }
      }
    }
    // nothing found, add setBounds(int, int, int, int)
    if (location != null && !setLocationFound && size != null && !setSizeFound) {
      widget.addMethodInvocation("setBounds(int,int,int,int)", location.x
          + ", "
          + location.y
          + ", "
          + size.width
          + ", "
          + size.height);
      return;
    }
    // something found, add as needed
    if (location != null && !setLocationFound) {
      widget.addMethodInvocation(SET_LOCATION_INT_INT, location.x + ", " + location.y);
    }
    if (size != null && !setSizeFound) {
      widget.addMethodInvocation(SET_SIZE_INT_INT, size.width + ", " + size.height);
    }
  }

  private boolean useCreationFlow() {
    return getToolkit().getPreferences().getBoolean(IPreferenceConstants.P_CREATION_FLOW);
  }

  private ToolkitDescription getToolkit() {
    return getDescription().getToolkit();
  }

  /**
   * set the expression as integer value to avoid cast expressions in Designer code.
   */
  private void setExpression(MethodInvocation mi, int index, int arg) throws Exception {
    getEditor().replaceExpression(
        (Expression) mi.arguments().get(index),
        IntegerConverter.INSTANCE.toJavaSource(this, Integer.valueOf(arg)));
  }

  private void removeUnusedMethodsAfterSetBounds(ControlInfo widget) throws Exception {
    {
      MethodInvocation mi = widget.getMethodInvocation(SET_LOCATION_INT_INT);
      if (mi != null) {
        widget.removeMethodInvocations(SET_LOCATION_INT_INT);
      }
    }
    {
      MethodInvocation mi = widget.getMethodInvocation(SET_LOCATION_POINT);
      if (mi != null) {
        widget.removeMethodInvocations(SET_LOCATION_POINT);
      }
    }
    {
      MethodInvocation mi = widget.getMethodInvocation(SET_SIZE_INT_INT);
      if (mi != null) {
        widget.removeMethodInvocations(SET_SIZE_INT_INT);
      }
    }
    {
      MethodInvocation mi = widget.getMethodInvocation(SET_SIZE_POINT);
      if (mi != null) {
        widget.removeMethodInvocations(SET_SIZE_POINT);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void clipboardCopy_addControlCommands(ControlInfo control,
      List<ClipboardCommand> commands) throws Exception {
    final Rectangle bounds = control.getModelBounds();
    commands.add(new LayoutClipboardCommand<AbsoluteLayoutInfo>(control) {
      private static final long serialVersionUID = 0L;

      @Override
      protected void add(AbsoluteLayoutInfo layout, ControlInfo control) throws Exception {
        layout.command_CREATE(control, null);
        layout.commandChangeBounds(control, bounds.getLocation(), bounds.getSize());
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link AbsoluteLayoutInfo} that generate code <code>setLayout(null)</code>.
   */
  public static AbsoluteLayoutInfo createExplicit(CompositeInfo parent) throws Exception {
    AstEditor editor = parent.getEditor();
    ToolkitDescription toolkit = parent.getDescription().getToolkit();
    AbsoluteLayoutCreationSupport creationSupport = new AbsoluteLayoutCreationSupport();
    return new AbsoluteLayoutInfo(editor, toolkit, creationSupport);
  }
}