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

import com.google.common.collect.Maps;

import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.core.model.association.InvocationSecondaryAssociation;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.LayoutInfo;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.util.List;
import java.util.Map;

/**
 * Absolute layout which sets location/size as constraints.
 * 
 * @author scheglov_ke
 * @coverage swing.model.layout
 */
public final class ConstraintsAbsoluteLayoutInfo extends AbstractAbsoluteLayoutInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ConstraintsAbsoluteLayoutInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constraints access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link ConstraintsAbsoluteLayoutDataInfo} for given {@link ComponentInfo}.
   */
  public static ConstraintsAbsoluteLayoutDataInfo getConstraints(final ComponentInfo component) {
    return ExecutionUtils.runObject(new RunnableObjectEx<ConstraintsAbsoluteLayoutDataInfo>() {
      public ConstraintsAbsoluteLayoutDataInfo runObject() throws Exception {
        // prepare constraints
        ConstraintsAbsoluteLayoutDataInfo constraints;
        {
          List<ConstraintsAbsoluteLayoutDataInfo> constraintsList =
              component.getChildren(ConstraintsAbsoluteLayoutDataInfo.class);
          Assert.isLegal(constraintsList.size() <= 1);
          if (constraintsList.size() == 1) {
            constraints = constraintsList.get(0);
          } else {
            String constraintsClassName;
            {
              ContainerInfo container = (ContainerInfo) component.getParent();
              LayoutInfo layout = container.getLayout();
              constraintsClassName =
                  JavaInfoUtils.getParameter(layout, "absoluteLayout.constraintsClass");
            }
            constraints =
                (ConstraintsAbsoluteLayoutDataInfo) JavaInfoUtils.createJavaInfo(
                    component.getEditor(),
                    constraintsClassName,
                    new ConstructorCreationSupport());
            // prepare add() invocation
            InvocationChildAssociation association =
                (InvocationChildAssociation) component.getAssociation();
            MethodInvocation invocation = association.getInvocation();
            // ensure LayoutData expression
            Expression expression;
            {
              String source = constraints.getDescription().getCreation(null).getSource();
              expression = constraints.getEditor().addInvocationArgument(invocation, 1, source);
            }
            // set CreationSupport
            {
              constraints.setCreationSupport(new ConstructorCreationSupport((ClassInstanceCreation) expression));
              constraints.addRelatedNode(expression);
            }
            // set Association
            constraints.setAssociation(new InvocationSecondaryAssociation(invocation));
            // set VariableSupport
            VariableSupport variableSupport = new EmptyVariableSupport(constraints, expression);
            constraints.setVariableSupport(variableSupport);
            // add
            component.addChild(constraints);
          }
        }
        // initialize and return
        return constraints;
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Bounds property
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setBoundsX(ComponentInfo component, int value) throws Exception {
    setBoundsProperty(component, "x", value);
  }

  @Override
  protected void setBoundsY(ComponentInfo component, int value) throws Exception {
    setBoundsProperty(component, "y", value);
  }

  @Override
  protected void setBoundsWidth(ComponentInfo component, int value) throws Exception {
    setBoundsProperty(component, "width", value);
  }

  @Override
  protected void setBoundsHeight(ComponentInfo component, int value) throws Exception {
    setBoundsProperty(component, "height", value);
  }

  private void setBoundsProperty(ComponentInfo component, String title, int value) throws Exception {
    value = updateValueUsingScript(component, title, value);
    ConstraintsAbsoluteLayoutDataInfo constraints = getConstraints(component);
    if (constraints != null) {
      Property property = constraints.getPropertyByTitle(title);
      if (property != null) {
        property.setValue(value);
      }
    }
  }

  private int updateValueUsingScript(ComponentInfo component, String title, int value)
      throws Exception {
    String scriptName = "absoluteLayout.setBounds." + title;
    String script = JavaInfoUtils.getParameter(this, scriptName);
    if (script != null) {
      ClassLoader classLoader = JavaInfoUtils.getClassLoader(this);
      Map<String, Object> variables = Maps.newHashMap();
      variables.put("component", component);
      variables.put("value", value);
      value = (Integer) ScriptUtils.evaluate(classLoader, script, variables);
    }
    return value;
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
    // set location
    if (location != null) {
      setBoundsX(component, location.x);
      setBoundsY(component, location.y);
    }
    // set size
    if (size != null) {
      setBoundsWidth(component, size.width);
      setBoundsHeight(component, size.height);
    }
  }
}
