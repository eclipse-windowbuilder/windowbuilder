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
import org.eclipse.wb.core.model.association.InvocationChildEllipsisAssociation;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * Container for ellipsis-array of {@link ObjectInfo} in {@link MethodInvocation}.
 *
 * @author sablin_aa
 * @coverage core.model.nonvisual
 */
public final class EllipsisObjectInfo extends AbstractArrayObjectInfo {
  public static final String ON_EMPTY_SOURCE_TAG = "arrayObject.OnEmptySource";
  private MethodInvocation m_invocation;
  private final int m_parameterIndex;
  private String m_onEmptySource;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public EllipsisObjectInfo(AstEditor editor,
      String caption,
      Class<?> itemType,
      MethodInvocation invocation,
      int parameterIndex) throws Exception {
    super(editor, caption, itemType);
    m_invocation = invocation;
    m_parameterIndex = parameterIndex;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setInvocation(MethodInvocation invocation) {
    m_invocation = invocation;
  }

  public MethodInvocation getInvocation() {
    return m_invocation;
  }

  public int getParameterIndex() {
    return m_parameterIndex;
  }

  public void setOnEmptySource(String onEmptySource) {
    m_onEmptySource = onEmptySource;
  }

  public String getOnEmptySource() {
    return m_onEmptySource;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Expression createItemExpression(JavaInfo item, int index, String source)
      throws Exception {
    return getEditor().addInvocationArgument(m_invocation, m_parameterIndex + index, source);
  }

  @Override
  protected Expression getMoveItemExpression(JavaInfo item,
      JavaInfo nextItem,
      AbstractArrayObjectInfo oldAbstractArrayInfo,
      int oldIndex,
      int newIndex) throws Exception {
    Expression element;
    if (oldAbstractArrayInfo instanceof EllipsisObjectInfo) {
      EllipsisObjectInfo oldEllipsisInfo = (EllipsisObjectInfo) oldAbstractArrayInfo;
      MethodInvocation oldInvocation = oldEllipsisInfo.getInvocation();
      if (oldInvocation.equals(m_invocation)) {
        // move in same array
        getParent().moveChild(item, nextItem);
        m_items.remove(item);
        m_items.add(newIndex, item);
        // exchange elements
        element =
            getEditor().moveInvocationArgument(
                oldInvocation,
                oldEllipsisInfo.getParameterIndex() + oldIndex,
                getParameterIndex() + newIndex);
      } else {
        element =
            moveFromEllipsis(item, nextItem, oldInvocation, oldEllipsisInfo, oldIndex, newIndex);
      }
      /*} else if (oldAbstractArrayInfo instanceof ArrayObjectInfo) {
      	// moving item from array to this ellipsis-array
      	ArrayObjectInfo oldArrayInfo = (ArrayObjectInfo) oldAbstractArrayInfo;
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
   * Move item to this {@link EllipsisObjectInfo} from other {@link EllipsisObjectInfo}.
   */
  private Expression moveFromEllipsis(JavaInfo item,
      JavaInfo nextItem,
      MethodInvocation oldInvocation,
      EllipsisObjectInfo oldEllipsisInfo,
      int oldIndex,
      int newIndex) throws Exception {
    AstEditor editor = getEditor();
    int oldInvocationIndex = oldEllipsisInfo.getParameterIndex() + oldIndex;
    String source = editor.getSource(DomGenerics.arguments(oldInvocation).get(oldInvocationIndex));
    // remove from old ellipsis-array
    Association association = item.getAssociation();
    if (association != null) {
      if (association.remove()) {
        item.setAssociation(null);
      }
    } else {
      editor.removeInvocationArgument(oldInvocation, oldInvocationIndex);
    }
    item.getParent().removeChild(item);
    // add to new ellipsis-array
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
    return new StatementTarget(AstNodeUtils.getEnclosingStatement(m_invocation), true);
  }

  @Override
  protected Association getAssociation(Expression element) {
    return new InvocationChildEllipsisAssociation(m_invocation, this);
  }
}
