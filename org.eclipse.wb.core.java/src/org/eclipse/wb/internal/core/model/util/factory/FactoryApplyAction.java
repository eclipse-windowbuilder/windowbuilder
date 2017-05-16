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
package org.eclipse.wb.internal.core.model.util.factory;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.FactoryParentAssociation;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.factory.StaticFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.model.description.factory.FactoryMethodDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.ui.ImageImageDescriptor;

import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jface.action.Action;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * {@link Action} for applying factory for creating component.
 *
 * @author scheglov_ke
 * @coverage core.model.util
 */
public final class FactoryApplyAction extends Action {
  private final JavaInfo m_component;
  private final FactoryMethodDescription m_description;
  private final AstEditor m_editor;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FactoryApplyAction(JavaInfo component, FactoryMethodDescription description) {
    m_component = component;
    m_description = description;
    m_editor = m_component.getEditor();
    // configure presentation
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        ComponentDescription componentDescription =
            ComponentDescriptionHelper.getDescription(m_editor, m_description.getReturnClass());
        setImageDescriptor(new ImageImageDescriptor(componentDescription.getIcon()));
        setText(CodeUtils.getShortClass(m_description.getDeclaringClass().getName())
            + "."
            + m_description.getName()
            + "(...)");
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Run
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void run() {
    ExecutionUtils.run(m_component, new RunnableEx() {
      public void run() throws Exception {
        prepareGenericProperties();
        if (m_component.getCreationSupport() instanceof ConstructorCreationSupport) {
          runConstructor();
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_hasParentArgument;

  /**
   * Apply to the {@link ConstructorCreationSupport}.
   */
  private void runConstructor() throws Exception {
    ConstructorCreationSupport oldCreationSupport =
        (ConstructorCreationSupport) m_component.getCreationSupport();
    ClassInstanceCreation oldExpression = oldCreationSupport.getCreation();
    List<Expression> oldArguments = DomGenerics.arguments(oldExpression);
    // prepare source for factory invocation
    String newSource;
    {
      newSource = m_description.getDeclaringClass().getName() + "." + m_description.getName() + "(";
      newSource += StringUtils.join(getFactoryArguments(oldArguments).iterator(), ", ");
      newSource += ")";
    }
    // set StaticFactoryCreationSupport
    MethodInvocation newExpression;
    {
      // set new creation
      StaticFactoryCreationSupport newCreationSupport =
          new StaticFactoryCreationSupport(m_description);
      m_component.setCreationSupport(newCreationSupport);
      // set Expression
      newExpression = (MethodInvocation) m_component.replaceExpression(oldExpression, newSource);
      newCreationSupport.add_setSourceExpression(newExpression);
    }
    // if has "parent" argument, change association
    if (m_hasParentArgument) {
      m_component.setAssociation(new FactoryParentAssociation(newExpression));
    }
  }

  /**
   * @param oldArguments
   *          the arguments of old {@link ClassInstanceCreation} or {@link MethodInvocation} that
   *          was used to create component.
   *
   * @return the arguments for factory method invocation.
   */
  private List<String> getFactoryArguments(List<Expression> oldArguments) throws Exception {
    List<String> arguments = Lists.newArrayList();
    for (ParameterDescription parameter : m_description.getParameters()) {
      // check for "parent"
      if (parameter.isParent()) {
        m_hasParentArgument = true;
        arguments.add(TemplateUtils.getExpression(m_component.getParentJava()));
        continue;
      }
      // try to use source from bound property
      {
        GenericPropertyImpl property = getGenericProperty(parameter.getProperty());
        if (property != null) {
          Expression expression = ((GenericProperty) property).getExpression();
          // argument of creation
          if (oldArguments.contains(expression)) {
            arguments.add(m_editor.getSource(expression));
            continue;
          }
          // argument of separate method (only literal)
          if (expression.getLocationInParent() == MethodInvocation.ARGUMENTS_PROPERTY
              && (expression instanceof NullLiteral
                  || expression instanceof BooleanLiteral
                  || expression instanceof NumberLiteral || expression instanceof StringLiteral)) {
            arguments.add(m_editor.getSource(expression));
            m_editor.removeEnclosingStatement(expression);
            continue;
          }
        }
      }
      // default source
      arguments.add(parameter.getDefaultSource());
    }
    return arguments;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Generic properties
  //
  ////////////////////////////////////////////////////////////////////////////
  private final List<GenericPropertyImpl> m_genericProperties = Lists.newArrayList();

  /**
   * Prepares list of {@link GenericPropertyImpl}'s that can be bound to the arguments of factory
   * invocation.
   */
  private void prepareGenericProperties() throws Exception {
    for (Property property : m_component.getProperties()) {
      if (property instanceof GenericPropertyImpl) {
        GenericPropertyImpl genericProperty = (GenericPropertyImpl) property;
        if (genericProperty.getExpression() != null) {
          m_genericProperties.add((GenericPropertyImpl) property);
        }
      }
    }
  }

  /**
   * @return the {@link GenericPropertyImpl} that has {@link GenericPropertyDescription} with given
   *         id; or <code>null</code> if not such {@link GenericPropertyImpl} found.
   */
  private GenericPropertyImpl getGenericProperty(String id) {
    for (GenericPropertyImpl property : m_genericProperties) {
      if (property.getDescription() != null && property.getDescription().getId().equals(id)) {
        return property;
      }
    }
    return null;
  }
}
