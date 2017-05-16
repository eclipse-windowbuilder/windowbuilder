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
package org.eclipse.wb.core.model.association;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.binding.DesignerMethodBinding;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.awt.GridBagLayout;
import java.util.List;

/**
 * Implementation of {@link Association} for {@link MethodInvocation} as separate
 * {@link ExpressionStatement}, when both, <em>parent</em> and <em>child</em> passed as arguments.
 * Can be used to establish some special parent/child link, used method that does not belong to
 * parent or child, but just separate method (sometimes used for {@link GridBagLayout}).
 *
 * @author scheglov_ke
 * @coverage core.model.association
 */
public final class InvocationSecondaryAssociation extends InvocationAssociation {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public InvocationSecondaryAssociation(MethodInvocation invocation) {
    super(invocation);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean canDelete() {
    // check for forced delete
    if (hasForcedDeleteTag()) {
      return true;
    }
    // prepare binding for existing invocation
    IMethodBinding binding = AstNodeUtils.getMethodBinding(m_invocation);
    // prepare new binding - without this JavaInfo arguments
    DesignerMethodBinding newBinding;
    {
      newBinding = m_editor.getBindingContext().get(binding);
      List<Expression> arguments = DomGenerics.arguments(m_invocation);
      for (int i = arguments.size() - 1; i >= 0; i--) {
        Expression argument = arguments.get(i);
        if (m_javaInfo.isRepresentedBy(argument)) {
          newBinding.removeParameterType(i);
        }
      }
    }
    // we can delete association only if there is alternative method, without child
    return AstNodeUtils.getMethodBySignature(
        binding.getDeclaringClass(),
        AstNodeUtils.getMethodSignature(newBinding)) != null;
  }

  @Override
  public boolean remove() throws Exception {
    if (!AstNodeUtils.isDanglingNode(m_invocation)) {
      while (true) {
        // forced delete
        if (hasForcedDeleteTag()) {
          break;
        }
        // update MethodInvocation
        List<Expression> arguments = DomGenerics.arguments(m_invocation);
        for (int i = arguments.size() - 1; i >= 0; i--) {
          Expression argument = arguments.get(i);
          if (m_javaInfo.isRepresentedBy(argument)) {
            m_javaInfo.getEditor().removeInvocationArgument(m_invocation, i);
          }
        }
        // leave loop
        break;
      }
    }
    // remove association
    return super.remove();
  }

  /**
   * @return <code>true</code> if association {@link MethodInvocation} has tag
   *         <code>"secondaryAssociation.alwaysDelete"</code>, that allows association delete.
   */
  private boolean hasForcedDeleteTag() {
    JavaInfo hostJavaInfo =
        m_javaInfo.getRootJava().getChildRepresentedBy(m_invocation.getExpression());
    if (hostJavaInfo != null) {
      String signature = AstNodeUtils.getMethodSignature(m_invocation);
      MethodDescription methodDescription = hostJavaInfo.getDescription().getMethod(signature);
      return methodDescription.hasTrueTag("secondaryAssociation.alwaysDelete");
    }
    return false;
  }
}
