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
package org.eclipse.wb.internal.core.model.property.accessor;

import com.google.common.collect.ImmutableList;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipProvider;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.lang.reflect.Method;

/**
 * The implementation of {@link ExpressionAccessor} for accessing {@link MethodInvocation} of some
 * method.
 * <p>
 * It returns {@link MethodInvocation} from {@link #getExpression(JavaInfo)}.<br>
 * It expects comma separated arguments are source in {@link #setExpression(JavaInfo, String)}.
 *
 * @author scheglov_ke
 * @coverage core.model.property.accessor
 */
public final class MethodInvocationAccessor extends ExpressionAccessor {
  private final Method m_method;
  private final String m_methodSignature;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MethodInvocationAccessor(Method setMethod) throws Exception {
    m_method = setMethod;
    m_methodSignature = ReflectionUtils.getMethodSignature(m_method);
    // initialize IAdaptable
    m_accessibleAccessor = AccessorUtils.IAccessibleExpressionAccessor_forMethod(m_method);
    m_tooltipProvider = AccessorUtils.PropertyTooltipProvider_forMethod(m_method);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ExpressionAccessor
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Expression getExpression(JavaInfo javaInfo) throws Exception {
    return getMethodInvocation(javaInfo);
  }

  @Override
  public boolean setExpression(final JavaInfo javaInfo, final String source) throws Exception {
    final MethodInvocation invocation = getMethodInvocation(javaInfo);
    if (invocation != null) {
      final AstEditor editor = javaInfo.getEditor();
      if (source == null) {
        ExecutionUtils.run(javaInfo, new RunnableEx() {
          public void run() throws Exception {
            editor.removeEnclosingStatement(invocation);
          }
        });
      } else {
        ExecutionUtils.run(javaInfo, new RunnableEx() {
          public void run() throws Exception {
            editor.replaceInvocationArguments(invocation, ImmutableList.of(source));
          }
        });
      }
    } else if (source != null) {
      ExecutionUtils.run(javaInfo, new RunnableEx() {
        public void run() throws Exception {
          javaInfo.addMethodInvocation(m_methodSignature, source);
        }
      });
    }
    // success
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IAdaptable
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IAccessibleExpressionAccessor m_accessibleAccessor;
  private final PropertyTooltipProvider m_tooltipProvider;

  @Override
  public <T> T getAdapter(Class<T> adapter) {
    if (adapter == IAccessibleExpressionAccessor.class) {
      return adapter.cast(m_accessibleAccessor);
    }
    if (adapter == PropertyTooltipProvider.class) {
      return adapter.cast(m_tooltipProvider);
    }
    // other
    return super.getAdapter(adapter);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link MethodInvocation} of this setter for given {@link JavaInfo}.
   */
  private MethodInvocation getMethodInvocation(JavaInfo javaInfo) throws Exception {
    return javaInfo.getMethodInvocation(m_methodSignature);
  }
}
