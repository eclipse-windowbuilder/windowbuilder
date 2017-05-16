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
package org.eclipse.wb.core.model;

import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.WrappedObjectAssociation;
import org.eclipse.wb.internal.core.model.AbstractWrapper;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.WrapperMethodControlCreationSupport;
import org.eclipse.wb.internal.core.model.creation.WrapperMethodLiveCreationSupport;
import org.eclipse.wb.internal.core.model.description.AbstractInvocationDescription;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.model.variable.WrapperMethodControlVariableSupport;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.swt.widgets.Control;

import java.lang.reflect.Method;

/**
 * Implementation {@link AbstractWrapper} for wrapper accessing wrapped object by method getter.
 *
 * @author sablin_aa
 * @coverage core.model
 */
public class WrapperByMethod extends AbstractWrapper {
  protected Method m_method;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public WrapperByMethod(JavaInfo host, String methodName) {
    super(host);
    setControlMethodName(methodName);
  }

  public WrapperByMethod(JavaInfo host) {
    this(host, JavaInfoUtils.getParameter(host, "Wrapper.method"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Prepares {@link #m_method} used to access {@link Control}.
   */
  public final void setControlMethodName(String methodName) {
    Class<?> componentClass = m_wrapperInfo.getDescription().getComponentClass();
    m_method = ReflectionUtils.getMethodBySignature(componentClass, methodName + "()");
    Assert.isNotNull(m_method, "Viewer control access method \""
        + methodName
        + "\" not found for viewer class "
        + ReflectionUtils.getFullyQualifiedName(componentClass, false));
  }

  /**
   * @return the {@link Method} to use for accessing {@link Control}.
   */
  public Method getControlMethod() {
    return m_method;
  }

  @Override
  public Class<?> getWrappedType() {
    return getControlMethod().getReturnType();
  }

  @Override
  protected CreationSupport newWrappedCreationSupport() throws Exception {
    return new WrapperMethodLiveCreationSupport(this);
  }

  public boolean isWrappedInfo(ASTNode node) {
    if (node instanceof MethodInvocation) {
      MethodInvocation invocation = (MethodInvocation) node;
      return invocation.arguments().isEmpty()
          && invocation.getName().getIdentifier().equals(m_method.getName())
          && m_wrapperInfo.isRepresentedBy(invocation.getExpression());
    }
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parent/child utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Configures parent/child links for given {@link WrapperByMethod}, its parent and control.
   */
  public void configureWrapper(AbstractInvocationDescription methodDescription,
      JavaInfo argumentInfos[]) throws Exception {
    for (ParameterDescription parameter : methodDescription.getParameters()) {
      JavaInfo parameterJavaInfo = argumentInfos[parameter.getIndex()];
      if (parameter.isParent() && isParameterWithWrapped(parameter)) {
        m_wrappedInfo = parameterJavaInfo;
      }
      configureParameter(parameter, parameterJavaInfo);
    }
  }

  protected void configureParameter(ParameterDescription parameter, JavaInfo parameterJavaInfo)
      throws Exception {
    if (parameter.isParent() && !isParameterWithWrapped(parameter)) {
      configureHierarchy(parameterJavaInfo);
    }
  }

  private boolean isParameterWithWrapped(ParameterDescription parameter) {
    return parameter.hasTrueTag("Wrapper.wrapped");
  }

  /**
   * Configures parent/child links.
   */
  public void configureHierarchy(JavaInfo parent) throws Exception {
    if (m_wrappedInfo == null) {
      // create control info
      m_wrappedInfo =
          JavaInfoUtils.createJavaInfo(
              m_wrapperInfo.getEditor(),
              getWrappedType(),
              newControlCreationSupport());
      // tune control
      m_wrappedInfo.setVariableSupport(newControlVariableSupport(m_wrappedInfo));
      m_wrappedInfo.setAssociation(newControlAssociation());
    }
    // add control/viewer
    configureHierarchy(parent, m_wrappedInfo);
  }

  protected CreationSupport newControlCreationSupport() {
    return new WrapperMethodControlCreationSupport(this);
  }

  protected VariableSupport newControlVariableSupport(JavaInfo control) {
    return new WrapperMethodControlVariableSupport(control, this);
  }

  protected Association newControlAssociation() {
    return new WrappedObjectAssociation(this);
  }

  protected void configureHierarchy(JavaInfo parent, JavaInfo control) throws Exception {
    softAddChild(parent, control);
    softAddChild(control, m_wrapperInfo);
  }

  protected final static void softAddChild(ObjectInfo parent, ObjectInfo child) throws Exception {
    if (!parent.getChildren().contains(child)) {
      parent.addChild(child);
    }
  }
}
