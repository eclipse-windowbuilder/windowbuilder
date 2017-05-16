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
package org.eclipse.wb.internal.core.model.creation.factory;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.FactoryParentAssociation;
import org.eclipse.wb.internal.core.model.creation.ILiveCreationSupport;
import org.eclipse.wb.internal.core.model.description.CreationInvocationDescription;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.model.description.factory.FactoryMethodDescription;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.text.MessageFormat;

/**
 * Implementation of {@link AbstractFactoryCreationSupport} for explicitly defined factory methods,
 * from some <code>*.wbp-factory.xml</code>.
 *
 * @author scheglov_ke
 * @coverage core.model.creation
 */
public abstract class AbstractExplicitFactoryCreationSupport extends AbstractFactoryCreationSupport
    implements
      ILiveCreationSupport {
  protected String m_addArguments;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractExplicitFactoryCreationSupport(FactoryMethodDescription description) {
    super(description);
  }

  public AbstractExplicitFactoryCreationSupport(FactoryMethodDescription description,
      MethodInvocation invocation) {
    super(description, invocation);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link FactoryMethodDescription} for this factory method.
   */
  @Override
  public final FactoryMethodDescription getDescription() {
    return (FactoryMethodDescription) m_description;
  }

  @Override
  public final Association getAssociation() throws Exception {
    if (add_getSource(null).indexOf("%parent%") != -1) {
      return new FactoryParentAssociation();
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean canUseParent(JavaInfo parent) throws Exception {
    // check that if constructor has "parent" parameter, it is compatible with given "parent"
    for (ParameterDescription parameter : m_description.getParameters()) {
      if (parameter.isParent()) {
        Class<?> requiredType = parameter.getType();
        Class<?> parentType = parent.getDescription().getComponentClass();
        if (!requiredType.isAssignableFrom(parentType)) {
          return false;
        }
      }
    }
    // continue
    return super.canUseParent(parent);
  }

  @Override
  public final boolean canReorder() {
    return true;
  }

  @Override
  public final boolean canReparent() {
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Adding
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final String add_getSource(NodeTarget target) throws Exception {
    String argumentsSource;
    if (m_addArguments != null) {
      argumentsSource = m_addArguments;
    } else {
      argumentsSource = "";
      for (ParameterDescription parameter : m_description.getParameters()) {
        // comma
        if (argumentsSource.length() != 0) {
          argumentsSource += ", ";
        }
        // source
        if (parameter.isParent()) {
          argumentsSource += "%parent%";
        } else {
          Assert.isNotNull(parameter.getDefaultSource());
          argumentsSource += parameter.getDefaultSource();
        }
      }
    }
    //
    return MessageFormat.format(
        "{0}{1}({2})",
        add_getSource_invocationExpression(target),
        m_description.getName(),
        argumentsSource);
  }

  /**
   * @return the source for expression part of {@link MethodInvocation}, name of class for static
   *         method or access expression for instance method.
   */
  protected abstract String add_getSource_invocationExpression(NodeTarget target) throws Exception;

  @Override
  public final void add_setSourceExpression(Expression expression) throws Exception {
    m_invocation = (MethodInvocation) expression;
    m_javaInfo.bindToExpression(m_invocation);
    // add invocations
    for (CreationInvocationDescription invocation : getDescription().getInvocations()) {
      m_javaInfo.addMethodInvocation(invocation.getSignature(), invocation.getArguments());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the source of arguments for copy/paste.
   */
  protected final String getClipboardArguments() throws Exception {
    // prepare source for arguments
    String argumentsSource = "";
    for (ParameterDescription parameter : m_description.getParameters()) {
      // append separator
      if (argumentsSource.length() != 0) {
        argumentsSource += ", ";
      }
      // append argument
      if (parameter.isParent()) {
        argumentsSource += "%parent%";
      } else {
        String argumentSource = null;
        // ask property
        {
          GenericPropertyImpl argumentProperty =
              (GenericPropertyImpl) getGenericProperty(parameter);
          if (argumentProperty != null) {
            argumentSource = argumentProperty.getClipboardSource();
          }
        }
        // use default source for parameter
        if (argumentSource == null) {
          if (Object.class.isAssignableFrom(parameter.getType())) {
            argumentSource = parameter.getDefaultSource();
          }
        }
        // do append argument
        Assert.isNotNull(argumentSource, "No source for "
            + parameter.getIndex()
            + "-th argument of "
            + m_invocation);
        argumentsSource += argumentSource;
      }
    }
    // return source of arguments
    return argumentsSource;
  }
}
