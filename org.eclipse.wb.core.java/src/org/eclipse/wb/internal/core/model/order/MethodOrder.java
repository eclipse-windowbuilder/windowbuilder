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
package org.eclipse.wb.internal.core.model.order;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.model.variable.ThisVariableSupport;
import org.eclipse.wb.internal.core.utils.Pair;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.List;

/**
 * Description for location of {@link MethodInvocation} of method marked with this
 * {@link MethodOrder}.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public abstract class MethodOrder {
  /**
   * {@link MethodOrder} for default order, i.e. when new invocation should be added directly after
   * component creation.
   */
  public static final MethodOrder DEFAULT = new MethodOrderDefault();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parsing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link MethodOrder} parsed from given string specification.
   */
  public static MethodOrder parse(String specification) {
    if (specification.startsWith("first")) {
      return new MethodOrderFirst();
    } else if (specification.startsWith("afterCreation")) {
      return new MethodOrderAfterCreation();
    } else if (specification.startsWith("beforeAssociation")) {
      return new MethodOrderBeforeAssociation();
    } else if (specification.startsWith("afterAssociation")) {
      return new MethodOrderAfterAssociation();
    } else if (specification.startsWith("after ")) {
      String targetSignature = specification.substring("after ".length());
      return new MethodOrderAfter(targetSignature);
    } else if (specification.startsWith("afterChildren ")) {
      String targetChild = specification.substring("afterChildren ".length());
      return new MethodOrderAfterChildren(targetChild);
    } else if (specification.startsWith("afterParentChildren ")) {
      String targetChild = specification.substring("afterParentChildren ".length());
      return new MethodOrderAfterParentChildren(targetChild);
    } else if (specification.equals("last")) {
      return new MethodOrderLast();
    } else {
      throw new IllegalArgumentException("Unsupported order specification: " + specification);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @param javaInfo
   *          the {@link JavaInfo} that has invocation with such {@link MethodOrder}.
   *
   * @return <code>true</code> {@link MethodInvocation} of method with such {@link MethodOrder} can
   *         be used as reference for adding new {@link Statement} or {@link JavaInfo} child. This
   *         means also that such {@link MethodInvocation} should be added as last one, after all
   *         existing invocations/children. Also new children also should be added before this
   *         method.
   */
  public abstract boolean canReference(JavaInfo javaInfo);

  /**
   * @return the {@link StatementTarget} to add new {@link MethodInvocation}, using
   *         {@link MethodOrder}.
   */
  public final StatementTarget getTarget(JavaInfo javaInfo, String newSignature) throws Exception {
    // check for inversion
    for (Pair<MethodInvocation, MethodOrder> pair : getInvocationOrders(javaInfo)) {
      MethodInvocation existingInvocation = pair.getLeft();
      MethodOrder existingOrder = pair.getRight();
      if (existingOrder instanceof MethodOrderAfter) {
        MethodOrderAfter afterOrder = (MethodOrderAfter) existingOrder;
        if (afterOrder.isTarget(newSignature)) {
          Statement statement = AstNodeUtils.getEnclosingStatement(existingInvocation);
          return new StatementTarget(statement, true);
        }
      }
    }
    // check for "this" specific target
    if (javaInfo.getVariableSupport() instanceof ThisVariableSupport) {
      MethodDescription newDescription = javaInfo.getDescription().getMethod(newSignature);
      if (newDescription != null) {
        String targetMethodSignature = newDescription.getTag("thisTargetMethod");
        if (targetMethodSignature != null) {
          TypeDeclaration typeDeclaration = JavaInfoUtils.getTypeDeclaration(javaInfo);
          MethodDeclaration targetMethod =
              AstNodeUtils.getMethodBySignature(typeDeclaration, targetMethodSignature);
          if (targetMethod != null) {
            Block targetBlock = targetMethod.getBody();
            // if first Statement if "super()" invocation, add after it
            List<Statement> statements = DomGenerics.statements(targetBlock);
            if (!statements.isEmpty() && statements.get(0) instanceof ExpressionStatement) {
              ExpressionStatement firstStatement = (ExpressionStatement) statements.get(0);
              if (firstStatement.getExpression() instanceof SuperMethodInvocation) {
                return new StatementTarget(firstStatement, false);
              }
            }
            // add as first Statement of target method
            return new StatementTarget(targetBlock, true);
          }
        }
      }
    }
    // use MethodOrder specific target
    return getSpecificTarget(javaInfo, newSignature);
  }

  /**
   * @return the information about existing {@link MethodInvocation}s.
   */
  protected static List<Pair<MethodInvocation, MethodOrder>> getInvocationOrders(JavaInfo javaInfo) {
    List<Pair<MethodInvocation, MethodOrder>> invocationOrders = Lists.newArrayList();
    List<MethodInvocation> invocations = javaInfo.getMethodInvocations();
    for (MethodInvocation invocation : invocations) {
      String signature = AstNodeUtils.getMethodSignature(invocation);
      MethodDescription description = javaInfo.getDescription().getMethod(signature);
      if (description != null) {
        MethodOrder order = description.getOrder();
        invocationOrders.add(Pair.create(invocation, order));
      }
    }
    return invocationOrders;
  }

  /**
   * @return the {@link MethodOrder} specific target.
   */
  protected StatementTarget getSpecificTarget(JavaInfo javaInfo, String newSignature)
      throws Exception {
    // redirect to default MethodOrder of JavaInfo
    return javaInfo.getDescription().getDefaultMethodOrder().getTarget(javaInfo, newSignature);
  }
}
