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
package org.eclipse.wb.internal.swing.model.layout.absolute;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfoUtils;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.converter.IntegerConverter;
import org.eclipse.wb.internal.core.model.util.AbsoluteLayoutCreationFlowSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.swing.Activator;
import org.eclipse.wb.internal.swing.ToolkitProvider;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.util.Collections;
import java.util.List;

/**
 * Model for "null" (absolute) layout.
 * 
 * @author scheglov_ke
 * @author mitin_aa
 * @coverage swing.model.layout
 */
public final class AbsoluteLayoutInfo extends AbstractAbsoluteLayoutInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbsoluteLayoutInfo(AstEditor editor, CreationSupport creationSupport) throws Exception {
    super(editor, new ComponentDescription(null), creationSupport);
    ObjectInfoUtils.setNewId(this);
    getDescription().setToolkit(ToolkitProvider.DESCRIPTION);
    getDescription().setOrder("first");
    getDescription().setIcon(Activator.getImage("info/layout/absolute/layout.gif"));
    setVariableSupport(new AbsoluteLayoutVariableSupport(this));
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
  // Conversion
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void removeComponentConstraints(ContainerInfo container, ComponentInfo component)
      throws Exception {
    super.removeComponentConstraints(container, component);
    // remove absolute location/size invocations
    component.removeMethodInvocations("setLocation(int,int)");
    component.removeMethodInvocations("setLocation(java.awt.Point)");
    component.removeMethodInvocations("setSize(int,int)");
    component.removeMethodInvocations("setSize(java.awt.Dimension)");
    component.removeMethodInvocations("setBounds(int,int,int,int)");
    component.removeMethodInvocations("setBounds(java.awt.Rectangle)");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Variable support
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class AbsoluteLayoutVariableSupport extends VariableSupport {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public AbsoluteLayoutVariableSupport(JavaInfo javaInfo) {
      super(javaInfo);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Object
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public String toString() {
      return "absolute";
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Name
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean hasName() {
      return false;
    }

    @Override
    public String getName() {
      throw new IllegalStateException();
    }

    @Override
    public void setName(String newName) throws Exception {
      throw new IllegalStateException();
    }

    @Override
    public String getTitle() throws Exception {
      throw new IllegalStateException();
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Expressions
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public String getReferenceExpression(NodeTarget target) throws Exception {
      throw new IllegalStateException();
    }

    @Override
    public String getAccessExpression(NodeTarget target) throws Exception {
      throw new IllegalStateException();
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Conversion
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean canConvertLocalToField() {
      return false;
    }

    @Override
    public void convertLocalToField() throws Exception {
      throw new IllegalStateException();
    }

    @Override
    public boolean canConvertFieldToLocal() {
      return false;
    }

    @Override
    public void convertFieldToLocal() throws Exception {
      throw new IllegalStateException();
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Target
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public StatementTarget getStatementTarget() throws Exception {
      throw new IllegalStateException();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Bounds property
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setBoundsX(ComponentInfo component, int value) throws Exception {
    Rectangle modelBounds = component.getModelBounds();
    Point location = new Point(value, modelBounds.y);
    command_BOUNDS(component, location, null);
  }

  @Override
  protected void setBoundsY(ComponentInfo component, int value) throws Exception {
    Rectangle modelBounds = component.getModelBounds();
    Point location = new Point(modelBounds.x, value);
    command_BOUNDS(component, location, null);
  }

  @Override
  protected void setBoundsWidth(ComponentInfo component, int value) throws Exception {
    Rectangle modelBounds = component.getModelBounds();
    Dimension size = new Dimension(value, modelBounds.height);
    command_BOUNDS(component, null, size);
  }

  @Override
  protected void setBoundsHeight(ComponentInfo component, int value) throws Exception {
    Rectangle modelBounds = component.getModelBounds();
    Dimension size = new Dimension(modelBounds.width, value);
    command_BOUNDS(component, null, size);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Perform "move" or "resize" operation. Modifies location/size values by modifying appropriate
   * "setLocation", "setSize", "setBounds" arguments.
   * 
   * @param component
   *          the {@link ComponentInfo} which modifications applies to.
   * @param location
   *          the {@link Point} of new location of component. May be null.
   * @param size
   *          the {@link Dimension} of new size of component. May be null.
   */
  @Override
  public void command_BOUNDS(ComponentInfo component, Point location, Dimension size)
      throws Exception {
    command_BOUNDS0(component, location, size);
    // apply creation flow
    if (location != null && useCreationFlow()) {
      AbsoluteLayoutCreationFlowSupport.apply(
          getContainer(),
          getComponents(),
          component,
          location,
          size);
    }
  }

  public void command_BOUNDS0(ComponentInfo component, Point location, Dimension size)
      throws Exception {
    Assert.isLegal(location != null || size != null, "Either location or size may not be null.");
    AstEditor editor = component.getEditor();
    // setBounds(int,int,int,int)
    {
      MethodInvocation mi = component.getMethodInvocation("setBounds(int,int,int,int)");
      if (mi != null) {
        if (location != null) {
          setExpression(mi, 0, location.x);
          setExpression(mi, 1, location.y);
        }
        if (size != null) {
          setExpression(mi, 2, size.width);
          setExpression(mi, 3, size.height);
        }
        return;
      }
    }
    // setBounds(java.awt.Rectangle)
    {
      MethodInvocation mi = component.getMethodInvocation("setBounds(java.awt.Rectangle)");
      if (mi != null) {
        Rectangle widgetBounds = component.getModelBounds();
        Expression exp = (Expression) mi.arguments().get(0);
        String newCode = "new java.awt.Rectangle(";
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
        return;
      }
    }
    boolean setLocationFound = false;
    boolean setSizeFound = false;
    if (location != null) {
      // setLocation(int,int)
      {
        MethodInvocation mi = component.getMethodInvocation("setLocation(int,int)");
        if (mi != null) {
          setLocationFound = true;
          setExpression(mi, 0, location.x);
          setExpression(mi, 1, location.y);
        }
      }
      // setLocation(new java.awt.Point)
      {
        MethodInvocation mi = component.getMethodInvocation("setLocation(java.awt.Point)");
        if (mi != null) {
          setLocationFound = true;
          Expression exp = (Expression) mi.arguments().get(0);
          String newCode = "new java.awt.Point(";
          newCode += location.x + ", " + location.y + ")";
          editor.replaceExpression(exp, newCode);
        }
      }
    }
    if (size != null) {
      // setSize(int,int)
      {
        MethodInvocation mi = component.getMethodInvocation("setSize(int,int)");
        if (mi != null) {
          setSizeFound = true;
          setExpression(mi, 0, size.width);
          setExpression(mi, 1, size.height);
        }
      }
      // setSize(new java.awt.Dimension)
      {
        MethodInvocation mi = component.getMethodInvocation("setSize(java.awt.Dimension)");
        if (mi != null) {
          setSizeFound = true;
          Expression exp = (Expression) mi.arguments().get(0);
          String newCode = "new java.awt.Dimension(";
          newCode += size.width + ", " + size.height + ")";
          editor.replaceExpression(exp, newCode);
        }
      }
    }
    // nothing found, add setBounds(int, int, int, int)
    if (location != null && !setLocationFound && size != null && !setSizeFound) {
      component.addMethodInvocation("setBounds(int,int,int,int)", location.x
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
      component.addMethodInvocation("setLocation(int,int)", location.x + ", " + location.y);
    }
    if (size != null && !setSizeFound) {
      component.addMethodInvocation("setSize(int,int)", size.width + ", " + size.height);
    }
  }

  private boolean useCreationFlow() {
    return getToolkit().getPreferences().getBoolean(
        org.eclipse.wb.internal.core.model.layout.absolute.IPreferenceConstants.P_CREATION_FLOW);
  }

  private ToolkitDescription getToolkit() {
    return getDescription().getToolkit();
  }

  /**
   * Set the expression as integer value to avoid cast expressions in Designer code.
   */
  private void setExpression(MethodInvocation mi, int index, int arg) throws Exception {
    getEditor().replaceExpression(
        (Expression) mi.arguments().get(index),
        IntegerConverter.INSTANCE.toJavaSource(this, Integer.valueOf(arg)));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link AbsoluteLayoutInfo} that generate code <code>setLayout(null)</code>.
   */
  public static AbsoluteLayoutInfo createExplicit(AstEditor editor) throws Exception {
    AbsoluteLayoutCreationSupport creationSupport = new AbsoluteLayoutCreationSupport();
    return new AbsoluteLayoutInfo(editor, creationSupport);
  }
}
