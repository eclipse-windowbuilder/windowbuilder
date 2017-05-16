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
package org.eclipse.wb.internal.core.model.variable;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.order.MethodOrder;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jface.preference.IPreferenceStore;

import org.apache.commons.lang.NotImplementedException;

import java.util.List;

/**
 * Implementations of this class support different variants of storing instance of component in
 * variable/field.
 *
 * We extract variable operations into separate class and and its subclasses for each case because
 * this makes {@link JavaInfo} itself easy and isolates each case in separate class instead of doing
 * checking each time.
 *
 * @author scheglov_ke
 * @coverage core.model.variable
 */
public abstract class VariableSupport {
  protected JavaInfo m_javaInfo;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public VariableSupport(JavaInfo javaInfo) {
    m_javaInfo = javaInfo;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link JavaInfo} that has this {@link VariableSupport}.
   */
  public final JavaInfo getJavaInfo() {
    return m_javaInfo;
  }

  /**
   * Moves {@link VariableSupport} to new {@link JavaInfo}, during morphing.
   */
  public final void moveTo(JavaInfo newJavaInfo) throws Exception {
    newJavaInfo.setVariableSupport(this);
    m_javaInfo = newJavaInfo;
  }

  /**
   * @return <code>true</code> if given {@link ASTNode} represents this {@link JavaInfo}.
   */
  public boolean isJavaInfo(ASTNode node) {
    return false;
  }

  /**
   * @return <code>true</code> if this {@link VariableSupport} is "default", i.e. is installed on
   *         component creation, for example "exposed". But we still can support "real"
   *         {@link VariableSupport} for it.
   */
  public boolean isDefault() {
    return false;
  }

  /**
   * This method should be implemented for variables that have related statements. For example,
   * virtual variables can not have related statements, so it can omit implementation.
   *
   * @return <code>true</code> if given {@link Statement} can be used as reference for adding new
   *         child.
   */
  public boolean isValidStatementForChild(Statement statement) {
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Name
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> in there is concept of variable name for this {@link VariableSupport}
   *         . For example "this" components don't have variable.
   */
  public abstract boolean hasName();

  /**
   * @return the name of variable/field to which this component is assigned first time or
   *         <code>null</code> if there are no any variable.
   */
  public abstract String getName();

  /**
   * Changes the name of variable/field of object.
   */
  public abstract void setName(String newName) throws Exception;

  /**
   * @return the title to display for user. Usually this is the name of variable.
   */
  public abstract String getTitle() throws Exception;

  /**
   * @return the name of component. Usually this is the name of variable.<br>
   *
   *         Difference between {@link #getName()} is that this method can return not only name of
   *         variable, but also some other, specific string for special {@link VariableSupport}'s,
   *         for example "this" for {@link ThisVariableSupport}, or name of property for
   *         {@link ExposedPropertyVariableSupport} .
   */
  public String getComponentName() {
    return "other";
  }

  /**
   * Allows {@link VariableSupport} add zero or more {@link Property}'s. Some implementation support
   * variable property, some - not.
   */
  public void addProperties(List<Property> properties) {
    // don't add any property by default
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Expressions
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if invocations of {@link #getReferenceExpression(NodeTarget)} and
   *         {@link #getAccessExpression(NodeTarget)} can be used for this {@link VariableSupport}.
   */
  public boolean hasExpression(NodeTarget target) {
    return false;
  }

  /**
   * Returns piece of Java code that should be used for passing reference on this object. It can
   * return different code depending on <code>target</code> argument, i.e. where it will be used.
   * For example, here is typical code:
   * <code>parentInfo.addMethodInvocation("add", info.getReferenceExpression(target));</code>
   *
   * @param target
   *          that {@link NodeTarget} that specifies position where expression will be used.
   */
  public abstract String getReferenceExpression(NodeTarget target) throws Exception;

  /**
   * Returns piece of Java code that should be used for calling some method. For example, here is
   * typical code:
   * <code>addMethodInvocationCode(info.getAccessExpression(true) + "setExpanded(true)");</code>
   * <p>
   * Result is almost same as #getAccessExpression(boolean), but adds "." in most cases. Only
   * exception are "this" components, where reference expression is "this", but access expression -
   * empty string.
   */
  public abstract String getAccessExpression(NodeTarget target) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Conversion
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if this variable can be converted from local to field. Usually if
   *         there is variable at all, it is always possible, except case when variable is already
   *         field.
   */
  public abstract boolean canConvertLocalToField();

  /**
   * Converts local variable into field. If variable is already field, ignore conversion request.
   */
  public abstract void convertLocalToField() throws Exception;

  /**
   * @return <code>true</code> if this variable can be converted from field to local.
   */
  public abstract boolean canConvertFieldToLocal();

  /**
   * Converts field into local variable. If variable is already local variable, ignore conversion
   * request.
   */
  public abstract void convertFieldToLocal() throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Target
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the {@link StatementTarget} that describes default location for new {@link Statement}.<br>
   *
   * NOTE: in some cases it may change the {@link VariableSupport} instance of the {@link JavaInfo}.
   * So the example code below is wrong:
   *
   * <pre>
	 * <code>
	 * 		VariableSupport variableSupport = javaInfo.getVariableSupport();
	 * 		StatementTarget statementTarget = variableSupport.getStatementTarget();
	 * 		// it's possible here that variableSupport != javaInfo.getVariableSupport()
	 * 		return variableSupport.getReferenceExpression(new NodeTarget(statementTarget));
	 * </code>
	 * </pre>
   *
   * @return the {@link StatementTarget} that describes default location for new {@link Statement}.
   */
  public abstract StatementTarget getStatementTarget() throws Exception;

  /**
   * Returns the {@link StatementTarget} that describes location for new child {@link JavaInfo}.
   * Usually it uses {@link #getStatementTarget()}, but sometimes (for example for exposed
   * components) special handling required.
   * <p>
   * This method is used only when this {@link JavaInfo} has no related {@link Statement}'s, so such
   * special target should be prepared.
   *
   * @return the {@link StatementTarget} for new child {@link JavaInfo}.
   */
  public StatementTarget getChildTarget() throws Exception {
    return getStatementTarget();
  }

  /**
   * This method ensures that instance of component is ready at given target. For example in case of
   * "lazy
   * creation" we can ask for instance from any place, and method will create it. But for "usual"
   * variables, for example local, we should move statements of {@link JavaInfo} and return new
   * {@link StatementTarget} , located after component instance assignment.
   *
   * @param target
   *          the {@link StatementTarget} where instance of component should be ready.
   *
   * @return the {@link StatementTarget} for adding association.
   */
  public void ensureInstanceReadyAt(StatementTarget target) throws Exception {
    throw new NotImplementedException();
  }

  /**
   * @param target
   *          the {@link StatementTarget} where instance of component should be ready. For block
   *          mode this is location where block of component should be placed, and association -
   *          somewhere inside of this block. For lazy mode this is directly location of
   *          association.
   *
   * @return the {@link StatementTarget} that describes location for adding association
   *         {@link Statement} during create/move operations. In many cases this is same as
   *         {@link #getStatementTarget()}, i.e. just any place where instance of component is
   *         ready. But when some {@link MethodInvocation}'s of component have {@link MethodOrder}
   *         before/after association, then we need to consider these invocations.
   */
  public StatementTarget getAssociationTarget(StatementTarget target) throws Exception {
    throw new NotImplementedException();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Adding
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @param associationTarget
   *          the {@link StatementTarget} where component association should be located
   *
   * @return the source of statement that should be added as part of new component adding. For local
   *         variables this is variable declaration, for field - assignment to field. This method
   *         should also do any operations required to make returned code correct, for example add
   *         imports, declare field (for variable as field).
   */
  public String add_getVariableStatementSource(StatementTarget associationTarget) throws Exception {
    throw new NotImplementedException();
  }

  /**
   * This method accepts {@link Statement} for source returned before from
   * {@link #add_getVariableStatementSource(StatementTarget)}. It can for example add related nodes
   * for its {@link JavaInfo}.
   */
  public void add_setVariableStatement(Statement statement) throws Exception {
    throw new NotImplementedException();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Allows {@link VariableSupport} do some action before component delete.<br>
   * For example we can remember {@link VariableDeclaration}, that is not removed yet - in
   * {@link #deleteAfter()} it can be already removed.
   */
  public void deleteBefore() throws Exception {
  }

  /**
   * Allows {@link VariableSupport} do some action after component delete.<br>
   * For example it can delete variable/field declaration.
   */
  public void deleteAfter() throws Exception {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link IPreferenceStore} for accessing this {@link JavaInfo} toolkit preferences.
   */
  protected final IPreferenceStore getPreferences() {
    return m_javaInfo.getDescription().getToolkit().getPreferences();
  }
}
