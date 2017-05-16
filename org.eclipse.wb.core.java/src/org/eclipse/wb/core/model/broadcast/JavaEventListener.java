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
package org.eclipse.wb.core.model.broadcast;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.variable.AbstractNamedVariableSupport;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;

import java.util.List;

/**
 * Listener for {@link JavaInfo} events.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public abstract class JavaEventListener {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Parsing was complete, but may be not all components already bound with parents.<br>
   * This is good place to perform any special parent/child associations.
   *
   * @param components
   *          all components, bound and not bound, that were created during parsing.
   */
  public void bindComponents(List<JavaInfo> components) throws Exception {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Association
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When given <code>component</code> is adding, this listener can process source code, for example
   * replace template elements such as <code>%parent%</code>, <code>%child%</code>, etc.
   *
   * @param component
   *          the {@link JavaInfo} that is adding now, it already has parent.
   * @param source
   *          the array with single element, listener can change it.
   */
  public void associationTemplate(JavaInfo component, String[] source) throws Exception {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Target
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When we check that {@link Statement} is may be terminal, i.e. all children should be placed
   * before it, so no other {@link Statement}'s on execution flow should be checked.
   *
   * @param parent
   *          the {@link JavaInfo} to which new child will be added. Note, that in
   *          {@link JavaInfoUtils} we check also related {@link Statement}'s of existing
   *          <code>parent</code> children. But here <code>parent</code> is exactly "parent", not
   *          any of its children. This allows us reduce number of requests.
   * @param child
   *          the new child {@link JavaInfo}.
   * @param statement
   *          the {@link Statement} to check for being related.
   * @param terminal
   *          the array with single boolean flag, with initial <code>false</code> value, any can
   *          listener set it to <code>true</code>.
   */
  public void target_isTerminalStatement(JavaInfo parent,
      JavaInfo child,
      Statement statement,
      boolean[] terminal) {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Add
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Before {@link JavaInfo} added to its parent.
   */
  public void addBefore(JavaInfo parent, JavaInfo child) throws Exception {
  }

  /**
   * After {@link JavaInfo} added to its parent.
   */
  public void addAfter(JavaInfo parent, JavaInfo child) throws Exception {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Move
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Notifies that component is going to be moved to different parent.<br>
   * This method is same as {@link #moveBefore(JavaInfo, ObjectInfo, JavaInfo)}, but invoked just
   * before it.
   */
  public void moveBefore0(JavaInfo child, ObjectInfo oldParent, JavaInfo newParent)
      throws Exception {
  }

  /**
   * Notifies that component is going to be moved to different parent.
   *
   * @param child
   *          that component to move.
   * @param oldParent
   *          the old container of component.
   * @param newParent
   *          new new container of component (may be same as old).
   */
  public void moveBefore(JavaInfo child, ObjectInfo oldParent, JavaInfo newParent) throws Exception {
  }

  /**
   * Notifies that component was moved to different parent.
   *
   * @param child
   *          that component to move.
   * @param oldParent
   *          the old container of component.
   * @param newParent
   *          new new container of component (may be same as old).
   */
  public void moveAfter(JavaInfo child, ObjectInfo oldParent, JavaInfo newParent) throws Exception {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Child replace
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Replaces given <code>oldChild</code> with <code>newChild</code>.
   */
  public void replaceChildBefore(JavaInfo parent, JavaInfo oldChild, JavaInfo newChild)
      throws Exception {
  }

  /**
   * Replaces given <code>oldChild</code> with <code>newChild</code>.
   */
  public void replaceChildAfter(JavaInfo parent, JavaInfo oldChild, JavaInfo newChild)
      throws Exception {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Subscribers can use this method to update/validate expression during
   * {@link GenericPropertyImpl#setExpression(String, Object)}.
   *
   * @param property
   *          the {@link GenericPropertyImpl} that sends this event.
   * @param source
   *          the single element array with new expression.
   * @param value
   *          the single element array with new value.
   * @param shouldSet
   *          the single element array that specifies if expression can be set, subscriber may set
   *          it to <code>false</code> if it did required modification itself, or thinks that value
   *          of this property should not be modified at all.
   */
  public void setPropertyExpression(GenericPropertyImpl property,
      String[] source,
      Object[] value,
      boolean[] shouldSet) throws Exception {
  }

  /**
   * Notifies that {@link GenericProperty} was changed its value.
   *
   * @param property
   *          the {@link GenericPropertyImpl} that sends this event.
   */
  public void propertyValueWasSet(GenericPropertyImpl property) throws Exception {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Variable
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Notifies that name of variable in {@link AbstractNamedVariableSupport} was changed to new.
   *
   * @param variableSupport
   *          the {@link AbstractNamedVariableSupport} that was changed the name.
   */
  public void variable_setName(AbstractNamedVariableSupport variableSupport,
      String oldName,
      String newName) throws Exception {
  }

  /**
   * Notifies that some <code>parent</code> {@link JavaInfo} is collecting now {@link Statement}'s
   * to move, and going to include {@link Statement}'s of given children.
   * <p>
   * This notification is often used in SWT, for example in <code>TabFolder</code> components should
   * be created with <code>TabFolder</code> as parent, but in {@link Block} of <code>TabItem</code>,
   * and should be moved then with <code>TabItem</code>.
   */
  public void variable_addStatementsToMove(JavaInfo parent, List<JavaInfo> children)
      throws Exception {
  }

  /**
   * Notifies that {@link EmptyVariableSupport} is going to materialize its {@link JavaInfo} with
   * some "real" {@link VariableSupport}.
   */
  public void variable_emptyMaterializeBefore(EmptyVariableSupport variableSupport)
      throws Exception {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Gives subscribers possibility to participate in copy to clipboard process. For example
   * container can add command for installing layout, layout can commands for creating children,
   * etc.
   *
   * @param javaInfo
   *          the {@link JavaInfo} that is in process of copying.
   * @param commands
   *          the {@link List} of {@link ClipboardCommand}'s to add new commands.
   */
  public void clipboardCopy(JavaInfo javaInfo, List<ClipboardCommand> commands) throws Exception {
  }

  /**
   * Notifies that {@link CreationSupport} needs source for given argument.
   *
   * @param javaInfo
   *          the {@link JavaInfo} that is in process of copying.
   * @param parameter
   *          the {@link ParameterDescription} for parameter, can be used to access its index, tags,
   *          etc.
   * @param argument
   *          the {@link Expression} to get source.
   * @param source
   *          the single element array for parameter source, initially <code>null</code>.
   */
  public void clipboardCopy_Argument(JavaInfo javaInfo,
      ParameterDescription parameter,
      Expression argument,
      String[] source) throws Exception {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Permissions
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Asks subscribers if given {@link JavaInfo} can be moved in some way (may be just change bounds,
   * may be even reorder, but not reparent).
   *
   * @param javaInfo
   *          the {@link JavaInfo} to check.
   * @param forceMoveEnable
   *          the single element array that specifies if {@link JavaInfo} can be moved,
   *          <code>false</code> initially, any subscriber may update.
   * @param forceMoveDisable
   *          the single element array that specifies if {@link JavaInfo} can not be moved,
   *          <code>false</code> initially, any subscriber may update.
   */
  public void canMove(JavaInfo javaInfo, boolean[] forceMoveEnable, boolean[] forceMoveDisable)
      throws Exception {
  }
}