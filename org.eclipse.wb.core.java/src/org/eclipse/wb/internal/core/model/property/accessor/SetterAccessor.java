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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ThisCreationSupport;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipProvider;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.lang.reflect.Method;

/**
 * The implementation of {@link ExpressionAccessor} for <code>setXXX(value)</code> method that can
 * have corresponding <code>getXXX()</code>, usually for usual bean property.
 *
 * @author scheglov_ke
 * @coverage core.model.property.accessor
 */
public final class SetterAccessor extends ExpressionAccessor {
  private final Method m_setter;
  private final String m_setterSignature;
  private Method m_getter;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SetterAccessor(Method setter, Method getter) throws Exception {
    m_setter = setter;
    m_setterSignature = ReflectionUtils.getMethodSignature(m_setter);
    // initialize IAdaptable
    m_accessibleAccessor = AccessorUtils.IAccessibleExpressionAccessor_forMethod(m_setter);
    m_tooltipProvider = AccessorUtils.PropertyTooltipProvider_forMethod(m_setter);
    // getter
    m_getter = getter;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public Method getSetter() {
    return m_setter;
  }

  public Method getGetter() {
    return m_getter;
  }

  public void setGetter(Method getter) {
    m_getter = getter;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visiting
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void visit(JavaInfo javaInfo, int state) throws Exception {
    super.visit(javaInfo, state);
    if (isTimeToGetDefaultValue(javaInfo, state)) {
      javaInfo.putArbitraryValue(this, askDefaultValue(javaInfo));
    }
  }

  private boolean isTimeToGetDefaultValue(JavaInfo javaInfo, int state) {
    return state == STATE_OBJECT_READY;
  }

  private Object askDefaultValue(final JavaInfo javaInfo) throws Exception {
    if (m_getter != null && isDefaultEnabled(javaInfo)) {
      return ExecutionUtils.runObjectIgnore(new RunnableObjectEx<Object>() {
        public Object runObject() throws Exception {
          return m_getter.invoke(javaInfo.getObject());
        }
      }, Property.UNKNOWN_VALUE);
    } else {
      return Property.UNKNOWN_VALUE;
    }
  }

  private boolean isDefaultEnabled(JavaInfo javaInfo) {
    if (javaInfo.getCreationSupport() instanceof ThisCreationSupport) {
      if (JavaInfoUtils.hasTrueParameter(javaInfo, NO_DEFAULT_VALUES_THIS_TAG)) {
        return false;
      }
    }
    return m_propertyDescription == null || !m_propertyDescription.hasTrueTag(NO_DEFAULT_VALUE_TAG);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ExpressionAccessor
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Expression getExpression(JavaInfo javaInfo) throws Exception {
    MethodInvocation invocation = getMethodInvocation(javaInfo);
    return getExpression(invocation);
  }

  @Override
  public boolean setExpression(final JavaInfo javaInfo, final String source) throws Exception {
    // modify expression
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
        final Expression oldExpression = getExpression(invocation);
        if (!editor.getSource(oldExpression).equals(source)) {
          ExecutionUtils.run(javaInfo, new RunnableEx() {
            public void run() throws Exception {
              javaInfo.replaceExpression(oldExpression, source);
            }
          });
        }
      }
    } else if (source != null) {
      ExecutionUtils.run(javaInfo, new RunnableEx() {
        public void run() throws Exception {
          javaInfo.addMethodInvocation(m_setterSignature, source);
        }
      });
    }
    // success
    return true;
  }

  @Override
  public Object getDefaultValue(JavaInfo javaInfo) throws Exception {
    return javaInfo.getArbitraryValue(this);
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
    if (adapter == IExposableExpressionAccessor.class) {
      return adapter.cast(m_exposableAccessor);
    }
    if (adapter == PropertyTooltipProvider.class) {
      return adapter.cast(m_tooltipProvider);
    }
    // other
    return super.getAdapter(adapter);
  }

  /**
   * Implementation of {@link IExposableExpressionAccessor}.
   */
  private final IExposableExpressionAccessor m_exposableAccessor =
      new IExposableExpressionAccessor() {
        public Class<?> getValueClass(JavaInfo javaInfo) {
          return m_setter.getParameterTypes()[0];
        }

        public String getGetterCode(JavaInfo javaInfo) throws Exception {
          return m_getter.getName() + "()";
        }

        public String getSetterCode(JavaInfo javaInfo, String source) throws Exception {
          return m_setter.getName() + "(" + source + ")";
        }
      };

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link MethodInvocation} of this setter for given {@link JavaInfo}.
   */
  private MethodInvocation getMethodInvocation(JavaInfo javaInfo) throws Exception {
    return javaInfo.getMethodInvocation(m_setterSignature);
  }

  /**
   * @return the sole argument (because {@link SetterAccessor} is only for such methods) of given
   *         {@link MethodInvocation}.
   */
  private Expression getExpression(MethodInvocation invocation) {
    return invocation != null ? (Expression) invocation.arguments().get(0) : null;
  }
}
