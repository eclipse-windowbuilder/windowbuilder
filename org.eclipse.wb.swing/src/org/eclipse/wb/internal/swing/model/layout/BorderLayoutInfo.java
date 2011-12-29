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
package org.eclipse.wb.internal.swing.model.layout;

import org.eclipse.wb.core.editor.actions.assistant.AbstractAssistantPage;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.core.model.broadcast.JavaInfoAddProperties;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.StringComboPropertyEditor;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.swt.widgets.Composite;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;
import java.util.Locale;

/**
 * Model for {@link BorderLayout}.
 * 
 * @author scheglov_ke
 * @coverage swing.model.layout
 */
public final class BorderLayoutInfo extends LayoutInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BorderLayoutInfo(AstEditor editor,
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
    addBroadcastListener(new JavaInfoAddProperties() {
      public void invoke(JavaInfo javaInfo, List<Property> properties) throws Exception {
        if (isManagedObject(javaInfo)) {
          ComponentInfo component = (ComponentInfo) javaInfo;
          BorderLayoutInfo key = BorderLayoutInfo.this;
          Property constraintsProperty = (Property) component.getArbitraryValue(key);
          if (constraintsProperty == null) {
            constraintsProperty = new ConstraintsProperty(component);
            component.putArbitraryValue(key, constraintsProperty);
          }
          properties.add(constraintsProperty);
        }
      }
    });
    new LayoutAssistantSupport(this) {
      @Override
      protected AbstractAssistantPage createLayoutPage(Composite parent) {
        return new BorderLayoutAssistantPage(parent, m_layout);
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Component} with given constraints.
   */
  public Component getComponent(String constraints) {
    return (Component) ReflectionUtils.invokeMethodEx(
        getObject(),
        "getChild(java.lang.String,boolean)",
        constraints,
        true);
  }

  /**
   * @return the region which has no {@link Component}, may be <code>null</code> if all regions are
   *         filled.
   */
  public String getEmptyRegion() {
    for (String region : CONSTRAINTS_VALUES) {
      if (getComponent(region) == null) {
        return region;
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds new {@link ComponentInfo} to given region.
   */
  public void command_CREATE(ComponentInfo component, String region) throws Exception {
    command_CREATE(component, region, null);
  }

  /**
   * Adds new {@link ComponentInfo} to given region.
   */
  public void command_CREATE(ComponentInfo component, String region, ComponentInfo nextComponent)
      throws Exception {
    String constraintsSource = getConstraintsSourceOfRegion(region);
    add(component, constraintsSource, nextComponent);
  }

  /**
   * Moves {@link ComponentInfo} to this {@link ContainerInfo} and given region.
   */
  public void command_MOVE(ComponentInfo component, String region) throws Exception {
    if (component.getParent() != getContainer()) {
      command_MOVE(component, (ComponentInfo) null);
    }
    command_REGION(component, region);
  }

  /**
   * Moves {@link ComponentInfo} inside of this {@link ContainerInfo}, without changing region.
   */
  public void command_MOVE(ComponentInfo component, ComponentInfo nextComponent) throws Exception {
    move(component, null, nextComponent);
  }

  /**
   * Moves {@link ComponentInfo} to this {@link ContainerInfo} and given region.
   */
  public void command_REGION(ComponentInfo component, String region) throws Exception {
    AstEditor editor = getEditor();
    Association association = component.getAssociation();
    MethodInvocation invocation = ((InvocationChildAssociation) association).getInvocation();
    List<Expression> arguments = DomGenerics.arguments(invocation);
    String constraintsSource = getConstraintsSourceOfRegion(region);
    if (arguments.size() == 2) {
      editor.replaceExpression(arguments.get(1), constraintsSource);
    } else if (invocation.arguments().size() == 1) {
      editor.addInvocationArgument(invocation, 1, constraintsSource);
    }
  }

  private static String getConstraintsSourceOfRegion(String region) {
    if (region == null) {
      return null;
    }
    return "java.awt.BorderLayout." + region.toUpperCase(Locale.ENGLISH);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Constraints" property
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String[] CONSTRAINTS_TITLES = {"North", "South", "West", "East", "Center"};
  private static final String[] CONSTRAINTS_FIELDS = {"NORTH", "SOUTH", "WEST", "EAST", "CENTER"};
  private static final String[] CONSTRAINTS_VALUES = {
      BorderLayout.NORTH,
      BorderLayout.SOUTH,
      BorderLayout.WEST,
      BorderLayout.EAST,
      BorderLayout.CENTER};
  private static final PropertyEditor m_constraintsPropertyEditor =
      new StringComboPropertyEditor(CONSTRAINTS_TITLES);

  /**
   * {@link Property} for modifying region of {@link ComponentInfo} on this {@link BorderLayoutInfo}
   * .
   */
  private final class ConstraintsProperty extends Property {
    private final ComponentInfo m_component;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public ConstraintsProperty(ComponentInfo component) {
      super(m_constraintsPropertyEditor);
      m_component = component;
      setCategory(PropertyCategory.system(6));
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Property
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public String getTitle() {
      return "Constraints";
    }

    @Override
    public Object getValue() throws Exception {
      Object constraints = ((BorderLayout) getObject()).getConstraints(m_component.getComponent());
      for (int i = 0; i < CONSTRAINTS_VALUES.length; i++) {
        String constraintsValue = CONSTRAINTS_VALUES[i];
        if (constraintsValue.equals(constraints)) {
          return CONSTRAINTS_TITLES[i];
        }
      }
      return "";
    }

    @Override
    public boolean isModified() throws Exception {
      return true;
    }

    @Override
    public void setValue(final Object value) throws Exception {
      ExecutionUtils.run(m_component, new RunnableEx() {
        public void run() throws Exception {
          for (int i = 0; i < CONSTRAINTS_TITLES.length; i++) {
            String constraintsTitle = CONSTRAINTS_TITLES[i];
            if (constraintsTitle.equals(value)) {
              command_REGION(m_component, CONSTRAINTS_FIELDS[i]);
            }
          }
        }
      });
    }
  }
}
