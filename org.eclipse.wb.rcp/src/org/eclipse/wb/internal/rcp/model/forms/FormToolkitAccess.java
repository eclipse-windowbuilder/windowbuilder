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
package org.eclipse.wb.internal.rcp.model.forms;

import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.ListGatherer;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.rcp.IExceptionConstants;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

import java.util.List;

/**
 * Provider for {@link FormToolkit} access in {@link IDetailsPage}.
 * <p>
 * In analyzes component {@link Class} and {@link TypeDeclaration} and tries to find field/method
 * that provide {@link IManagedForm} (so we can use method {@link IManagedForm#getToolkit()}) or
 * directly {@link FormToolkit}.
 * 
 * @author scheglov_ke
 * @coverage rcp.model.forms
 */
public final class FormToolkitAccess {
  private final String m_toolkitMethodName;
  private final String m_toolkitFieldName;
  private final String m_formMethodName;
  private final String m_formFieldName;
  private final String m_toolkitSource;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Not found exception
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final class NoFormToolkitError extends Error {
    private static final long serialVersionUID = 0L;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the valid {@link FormToolkitAccess} or <code>null</code>.
   */
  public static FormToolkitAccess get(TypeDeclaration typeDeclaration) {
    try {
      return new FormToolkitAccess(typeDeclaration);
    } catch (NoFormToolkitError e) {
      return null;
    }
  }

  /**
   * @return the valid {@link FormToolkitAccess} or throws {@link Exception}.
   */
  public static FormToolkitAccess getOrFail(TypeDeclaration typeDeclaration) {
    try {
      return new FormToolkitAccess(typeDeclaration);
    } catch (NoFormToolkitError e) {
      throw new DesignerException(IExceptionConstants.NO_FORM_TOOLKIT);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private FormToolkitAccess(TypeDeclaration typeDeclaration) {
    // try to find FormToolkit in local variable declaration
    {
      ListGatherer<SimpleName> listGatherer = new ListGatherer<SimpleName>() {
        @Override
        public void postVisit(ASTNode node) {
          if (node instanceof SimpleName
              && node.getLocationInParent() == VariableDeclarationFragment.NAME_PROPERTY) {
            SimpleName variable = (SimpleName) node;
            if (AstNodeUtils.isSuccessorOf(
                variable.resolveTypeBinding(),
                "org.eclipse.ui.forms.widgets.FormToolkit")) {
              addResult(variable);
            }
          }
        }
      };
      typeDeclaration.accept(listGatherer);
      List<SimpleName> variables = listGatherer.getResultList();
      if (!variables.isEmpty()) {
        m_toolkitMethodName = null;
        m_toolkitFieldName = variables.get(0).getIdentifier();
        m_formMethodName = null;
        m_formFieldName = null;
        m_toolkitSource = m_toolkitFieldName;
        return;
      }
    }
    // try to find FormToolkit: method
    {
      ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(typeDeclaration);
      List<IMethodBinding> methodBindings =
          AstNodeUtils.getMethodBindings(typeBinding.getSuperclass(), Modifier.PUBLIC
              | Modifier.PROTECTED);
      for (IMethodBinding methodBinding : methodBindings) {
        if (methodBinding.getParameterTypes().length == 0
            && AstNodeUtils.isSuccessorOf(
                methodBinding.getReturnType(),
                "org.eclipse.ui.forms.widgets.FormToolkit")) {
          m_toolkitMethodName = methodBinding.getName();
          m_toolkitFieldName = null;
          m_formMethodName = null;
          m_formFieldName = null;
          m_toolkitSource = m_toolkitMethodName + "()";
          return;
        }
      }
    }
    // try to find FormToolkit: field
    {
      ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(typeDeclaration);
      List<IVariableBinding> fields =
          AstNodeUtils.getFieldBindings(typeBinding, Modifier.PUBLIC
              | Modifier.PROTECTED
              | Modifier.PRIVATE);
      for (IVariableBinding field : fields) {
        if (AstNodeUtils.isSuccessorOf(field.getType(), "org.eclipse.ui.forms.widgets.FormToolkit")) {
          m_toolkitMethodName = null;
          m_toolkitFieldName = field.getName();
          m_formMethodName = null;
          m_formFieldName = null;
          m_toolkitSource = m_toolkitFieldName;
          return;
        }
      }
    }
    // try to find IManagedForm: method
    {
      ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(typeDeclaration);
      List<IMethodBinding> methodBindings =
          AstNodeUtils.getMethodBindings(typeBinding.getSuperclass(), Modifier.PUBLIC
              | Modifier.PROTECTED);
      for (IMethodBinding methodBinding : methodBindings) {
        if (methodBinding.getParameterTypes().length == 0
            && AstNodeUtils.isSuccessorOf(
                methodBinding.getReturnType(),
                "org.eclipse.ui.forms.IManagedForm")) {
          m_toolkitMethodName = null;
          m_toolkitFieldName = null;
          m_formMethodName = methodBinding.getName();
          m_formFieldName = null;
          m_toolkitSource = m_formMethodName + "().getToolkit()";
          return;
        }
      }
    }
    // try to find IManagedForm: field
    {
      ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(typeDeclaration);
      List<IVariableBinding> fields =
          AstNodeUtils.getFieldBindings(typeBinding, Modifier.PUBLIC
              | Modifier.PROTECTED
              | Modifier.PRIVATE);
      for (IVariableBinding field : fields) {
        if (AstNodeUtils.isSuccessorOf(field.getType(), "org.eclipse.ui.forms.IManagedForm")) {
          m_toolkitMethodName = null;
          m_toolkitFieldName = null;
          m_formMethodName = null;
          m_formFieldName = field.getName();
          m_toolkitSource = m_formFieldName + ".getToolkit()";
          return;
        }
      }
    }
    // can not find FormToolkit/IManagedForm
    throw new NoFormToolkitError();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if given {@link ASTNode} represents access of our {@link FormToolkit}
   *         .
   */
  public boolean isToolkit(ASTNode node) {
    if (node instanceof MethodInvocation) {
      MethodInvocation invocation = (MethodInvocation) node;
      if (invocation.arguments().isEmpty()) {
        // getMyToolkit()
        if (m_toolkitMethodName != null
            && invocation.getName().getIdentifier().equals(m_toolkitMethodName)) {
          return true;
        }
        // someExpression.getToolkit()
        if (invocation.getName().getIdentifier().equals("getToolkit")) {
          // getMyManagedForm().getToolkit()
          if (invocation.getExpression() instanceof MethodInvocation) {
            MethodInvocation formInvocation = (MethodInvocation) invocation.getExpression();
            if (formInvocation.arguments().isEmpty()
                && m_formMethodName != null
                && formInvocation.getName().getIdentifier().equals(m_formMethodName)) {
              return true;
            }
          }
          // m_myManagedForm.getToolkit()
          if (invocation.getExpression() instanceof SimpleName) {
            SimpleName simpleName = (SimpleName) invocation.getExpression();
            if (m_formFieldName != null && simpleName.getIdentifier().equals(m_formFieldName)) {
              return true;
            }
          }
        }
      }
    }
    // m_myToolkit
    if (node instanceof SimpleName) {
      SimpleName simpleName = (SimpleName) node;
      if (m_toolkitFieldName != null && simpleName.getIdentifier().equals(m_toolkitFieldName)) {
        return true;
      }
    }
    // unknown node
    return false;
  }

  /**
   * @return the Java source code to reference our {@link FormToolkit}.
   */
  public String getReferenceExpression() {
    return m_toolkitSource;
  }
}
