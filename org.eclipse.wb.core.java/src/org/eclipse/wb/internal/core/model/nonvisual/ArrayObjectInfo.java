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
package org.eclipse.wb.internal.core.model.nonvisual;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.InvocationChildArrayAssociation;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.lang.reflect.InvocationTargetException;

/**
 * Container for array of {@link ObjectInfo}.
 *
 * @author sablin_aa
 * @coverage core.model.nonvisual
 */
public final class ArrayObjectInfo extends AbstractArrayObjectInfo {
  private final ArrayCreation m_creation;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ArrayObjectInfo(AstEditor editor, String caption, Class<?> itemType, ArrayCreation creation)
      throws Exception {
    super(editor, caption, itemType);
    m_creation = creation;
  }

  /**
   * This constructor can't used for tests, because fires {@link InvocationTargetException}.
   */
  public ArrayObjectInfo(AstEditor editor, String caption, ArrayCreation creation) throws Exception {
    this(editor, caption, ReflectionUtils.getClassByName(
        EditorState.get(editor).getEditorLoader(),
        creation.getType().getComponentType().resolveBinding().getQualifiedName()), creation);
  }

  public ArrayCreation getCreation() {
    return m_creation;
  }

  /*public void setCreationId(String creationId) {
  	m_creationId = creationId;
  }
  public String getCreationId() {
  	return m_creationId;
  }*/
  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Expression createItemExpression(JavaInfo item, int index, String source)
      throws Exception {
    return getEditor().addArrayElement(m_creation.getInitializer(), index, source);
  }

  @Override
  protected Expression getMoveItemExpression(JavaInfo item,
      JavaInfo nextItem,
      AbstractArrayObjectInfo oldAbstractArrayInfo,
      int oldIndex,
      int newIndex) throws Exception {
    Expression element;
    if (oldAbstractArrayInfo instanceof ArrayObjectInfo) {
      // moving item between two arrays
      ArrayObjectInfo oldArrayInfo = (ArrayObjectInfo) oldAbstractArrayInfo;
      ArrayInitializer oldArray = oldArrayInfo.m_creation.getInitializer();
      ArrayInitializer newArray = m_creation.getInitializer();
      if (oldArray.equals(newArray)) {
        // move in same array
        getParent().moveChild(item, nextItem);
        m_items.remove(item);
        m_items.add(newIndex, item);
        // exchange elements
        element = getEditor().moveArrayElement(oldArray, newArray, oldIndex, newIndex);
      } else {
        // move between different arrays
        element = moveFromArray(oldArray, oldIndex, newArray, newIndex, item, nextItem);
      }
      /*} else if (oldAbstractArrayInfo instanceof EllipsisObjectInfo) {
      	// moving item from ellipsis-array to this array
      	EllipsisObjectInfo oldEllipsisInfo = (EllipsisObjectInfo) oldAbstractArrayInfo;
      	// TODO
      	Assert.fail("Not implemented");
      	element = null;*/
    } else {
      // moving item from outside
      element = moveOther(item, nextItem, newIndex);
    }
    return element;
  }

  /**
   * Move item to this {@link ArrayObjectInfo} from other {@link ArrayObjectInfo}.
   */
  private Expression moveFromArray(ArrayInitializer oldArray,
      int oldIndex,
      ArrayInitializer newArray,
      int newIndex,
      JavaInfo item,
      JavaInfo nextItem) throws Exception {
    AstEditor editor = getEditor();
    String source = editor.getSource(DomGenerics.expressions(oldArray).get(oldIndex));
    // remove from old array
    Association association = item.getAssociation();
    if (association != null) {
      if (association.remove()) {
        item.setAssociation(null);
      }
    } else {
      editor.removeArrayElement(oldArray, oldIndex);
    }
    item.getParent().removeChild(item);
    // add to new array
    getParent().addChild(item, nextItem);
    addItem(newIndex, item);
    if (!(item.getVariableSupport() instanceof EmptyVariableSupport)) {
      item.getVariableSupport().ensureInstanceReadyAt(getStatementTarget());
      source = item.getVariableSupport().getReferenceExpression(getNodeTarget());
    }
    return getCreateItemExpression(item, newIndex, source);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Targets
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected NodeTarget getNodeTarget() {
    return new NodeTarget(getStatementTarget());
  }

  @Override
  protected StatementTarget getStatementTarget() {
    return new StatementTarget(AstNodeUtils.getEnclosingStatement(m_creation), true);
  }

  @Override
  protected Association getAssociation(Expression element) {
    ASTNode parentNode = m_creation.getParent();
    if (parentNode instanceof MethodInvocation) {
      return new InvocationChildArrayAssociation((MethodInvocation) parentNode, this);
    }
    // FIXME
    Assert.fail("ArrayObjectInfo supported association only for MethodInvocation.");
    return null;
  }
}
