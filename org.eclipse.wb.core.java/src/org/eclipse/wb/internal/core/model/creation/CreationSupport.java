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
package org.eclipse.wb.internal.core.model.creation;

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.eval.ExecutionFlowUtils.ExecutionFlowFrameVisitor;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.internal.core.model.clipboard.IClipboardCreationSupport;
import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;

import java.util.List;

/**
 * This class supports different patterns of object creation, for example: constructors, accessors,
 * static factories, instance factories, etc.
 *
 * @author scheglov_ke
 * @coverage core.model.creation
 */
public abstract class CreationSupport {
  protected JavaInfo m_javaInfo;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets {@link JavaInfo} that uses this {@link CreationSupport}.
   */
  public void setJavaInfo(JavaInfo javaInfo) throws Exception {
    Assert.isLegal(m_javaInfo == null, "JavaInfo for CreationSupport can be set only one time.");
    Assert.isNotNull(javaInfo, "JavaInfo for CreationSupport can not be 'null'.");
    m_javaInfo = javaInfo;
  }

  /**
   * @return <code>true</code> if given {@link ASTNode} represents {@link JavaInfo} of this
   *         {@link CreationSupport}.
   */
  public abstract boolean isJavaInfo(ASTNode node);

  /**
   * @return the {@link ASTNode} that presents creation of this component.
   */
  public abstract ASTNode getNode();

  /**
   * If this {@link CreationSupport} form an association during creation (for example in SWT all
   * constructors accept parent), the return corresponding {@link Association}, and
   * <code>null</code> in other case.
   *
   * @return the {@link Association} for this {@link CreationSupport} or <code>null</code>.
   */
  public Association getAssociation() throws Exception {
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if {@link JavaInfo} created using this {@link CreationSupport} can be
   *         places (created or added) on given parent. Returns <code>true</code> by default.
   */
  public boolean canUseParent(JavaInfo parent) throws Exception {
    return true;
  }

  /**
   * @return <code>true</code> if this {@link JavaInfo} can be reordered inside of its parent.
   */
  public boolean canReorder() {
    return false;
  }

  /**
   * @return <code>true</code> if this {@link JavaInfo} can be reparented.
   */
  public boolean canReparent() {
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds {@link ExpressionAccessor} for given {@link GenericPropertyDescription}.
   */
  public void addAccessors(GenericPropertyDescription propertyDescription,
      List<ExpressionAccessor> accessors) throws Exception {
    // don't add any new accessors by default
  }

  /**
   * Allows {@link CreationSupport} add zero or more {@link Property}'s, for example for constructor
   * we can add complex property with sub-property for each argument.
   */
  public void addProperties(List<Property> properties) throws Exception {
    // don't add any property by default
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if object with this {@link CreationSupport} can be evaluated using
   *         AST. For example {@link ClassInstanceCreation} can be evaluated, but "this" component -
   *         no.
   */
  public boolean canBeEvaluated() {
    return true;
  }

  /**
   * If needed, creates object for this {@link JavaInfo} manually.
   *
   * Sometimes we need this ("this", "exposed").
   *
   * Sometimes - no (constructor) because {@link AstEvaluationEngine} can evaluate creation node
   * without our assistance.
   *
   * @param context
   *          the {@link EvaluationContext} in which we should evaluate other {@link Expression}'s.
   * @param visitor
   *          the {@link ExecutionFlowFrameVisitor} that initiated this evaluation. We can use it
   *          for "continuation".
   */
  public Object create(EvaluationContext context, ExecutionFlowFrameVisitor visitor)
      throws Exception {
    throw new IllegalStateException();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Adding
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @param target
   *          the {@link NodeTarget} that specifies location where this returned source will be
   *          used.
   *
   * @return the Java source that should be used for adding new component. This method should also
   *         do any operations required to make returned code correct, for example add imports
   *         (almost always), fields (for instance factories).
   */
  public String add_getSource(NodeTarget target) throws Exception {
    throw new IllegalStateException("Generic CreationSupport does not support adding.");
  }

  /**
   * After adding new component using source returned from {@link #add_getSource(NodeTarget)} the
   * {@link Expression} for this source will be set.
   */
  public void add_setSourceExpression(Expression expression) throws Exception {
    throw new IllegalStateException("Generic CreationSupport does not support adding.");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if {@link #delete()} can be used.
   */
  public boolean canDelete() {
    return false;
  }

  /**
   * Deletes this {@link JavaInfo} from its parent.
   */
  public void delete() throws Exception {
    throw new IllegalStateException("Generic CreationSupport does not support delete operation.");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link IClipboardCreationSupport} that can be used during paste to create
   *         {@link CreationSupport} for pasted {@link JavaInfo}. Can return <code>null</code>, if
   *         copy/paste is not available.
   */
  public IClipboardCreationSupport getClipboard() throws Exception {
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Class} of component.
   */
  protected final Class<?> getComponentClass() {
    return m_javaInfo.getDescription().getComponentClass();
  }
}
