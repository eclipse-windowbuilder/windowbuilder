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
package org.eclipse.wb.internal.rcp.model.rcp.perspective;

import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.eval.ExecutionFlowUtils.ExecutionFlowFrameVisitor;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * Implementation of {@link CreationSupport} for {@link IPageLayout} parameter of
 * {@link IPerspectiveFactory}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.rcp
 */
public final class PageLayoutCreationSupport extends CreationSupport {
  private final SingleVariableDeclaration m_parameter;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PageLayoutCreationSupport(SingleVariableDeclaration parameter) {
    m_parameter = parameter;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "parameter: " + m_parameter.getName().getIdentifier();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public ASTNode getNode() {
    return m_parameter;
  }

  @Override
  public boolean isJavaInfo(ASTNode node) {
    return node == m_parameter;
  }

  @Override
  public boolean canBeEvaluated() {
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean canDelete() {
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Object create(EvaluationContext context, ExecutionFlowFrameVisitor visitor)
      throws Exception {
    ((PageLayoutInfo) m_javaInfo).render();
    //
    Enhancer enhancer = new Enhancer();
    enhancer.setClassLoader(JavaInfoUtils.getClassLoader(m_javaInfo));
    enhancer.setSuperclass(m_javaInfo.getDescription().getComponentClass());
    enhancer.setCallback(new MethodInterceptor() {
      public Object intercept(Object obj,
          java.lang.reflect.Method method,
          Object[] args,
          MethodProxy proxy) throws Throwable {
        String signature = ReflectionUtils.getMethodSignature(method);
        if (signature.equals("getEditorArea()")) {
          return IPageLayout.ID_EDITOR_AREA;
        }
        if (signature.equals("isEditorAreaVisible()")) {
          return true;
        }
        if (signature.equals("isFixed()")) {
          return false;
        }
        return null;
      }
    });
    return enhancer.create();
  }
}
