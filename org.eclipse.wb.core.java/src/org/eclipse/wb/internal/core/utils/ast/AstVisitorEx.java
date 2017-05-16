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
package org.eclipse.wb.internal.core.utils.ast;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * Implementation of {@link ASTVisitor} that wraps methods {@link #preVisitEx(ASTNode)} and
 * {@link #postVisitEx(ASTNode)} in try/catch.
 *
 * @author scheglov_ke
 * @coverage core.util.ast
 */
public class AstVisitorEx extends ASTVisitor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Methods to wrap
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final void preVisit(ASTNode node) {
    try {
      preVisitEx(node);
    } catch (Throwable e) {
      throw ReflectionUtils.propagate(e);
    }
  }

  @Override
  public final void postVisit(ASTNode node) {
    try {
      postVisitEx(node);
    } catch (Throwable e) {
      throw ReflectionUtils.propagate(e);
    }
  }

  @Override
  public final boolean visit(QualifiedName node) {
    try {
      return visitEx(node);
    } catch (Throwable e) {
      throw ReflectionUtils.propagate(e);
    }
  }

  @Override
  public final void endVisit(QualifiedName node) {
    try {
      endVisitEx(node);
    } catch (Throwable e) {
      throw ReflectionUtils.propagate(e);
    }
  }

  @Override
  public final void endVisit(MethodInvocation node) {
    try {
      endVisitEx(node);
    } catch (Throwable e) {
      throw ReflectionUtils.propagate(e);
    }
  }

  @Override
  public final void endVisit(SuperMethodInvocation node) {
    try {
      endVisitEx(node);
    } catch (Throwable e) {
      throw ReflectionUtils.propagate(e);
    }
  }

  @Override
  public final void endVisit(TypeDeclaration node) {
    try {
      endVisitEx(node);
    } catch (Throwable e) {
      throw ReflectionUtils.propagate(e);
    }
  }

  @Override
  public final void endVisit(TryStatement node) {
    try {
      endVisitEx(node);
    } catch (Throwable e) {
      throw ReflectionUtils.propagate(e);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Methods to implement
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Implementation of {@link #preVisit(ASTNode)}.
   */
  public void preVisitEx(ASTNode node) throws Exception {
  }

  /**
   * Implementation of {@link #postVisit(ASTNode)}.
   */
  public void postVisitEx(ASTNode node) throws Exception {
  }

  /**
   * Implementation of {@link #visit(QualifiedName)}.
   */
  public boolean visitEx(QualifiedName node) throws Exception {
    return true;
  }

  /**
   * Implementation of {@link #endVisit(QualifiedName)}.
   */
  public void endVisitEx(QualifiedName node) throws Exception {
  }

  /**
   * Implementation of {@link #endVisit(MethodInvocation)}.
   */
  public void endVisitEx(MethodInvocation node) throws Exception {
  }

  /**
   * Implementation of {@link #endVisit(SuperMethodInvocation)}.
   */
  public void endVisitEx(SuperMethodInvocation node) throws Exception {
  }

  /**
   * Implementation of {@link #endVisit(TypeDeclaration)}.
   */
  public void endVisitEx(TypeDeclaration node) throws Exception {
  }

  /**
   * Implementation of {@link #endVisit(TryStatement)}.
   */
  public void endVisitEx(TryStatement node) throws Exception {
  }
}
