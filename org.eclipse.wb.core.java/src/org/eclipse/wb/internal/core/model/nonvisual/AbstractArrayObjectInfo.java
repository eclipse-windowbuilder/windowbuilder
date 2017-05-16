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

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.AssociationUtils;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildAddAfter;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildAddBefore;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.presentation.DefaultObjectPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.LocalUniqueVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.swt.graphics.Image;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Abstract container for array of {@link ObjectInfo}.
 *
 * @author sablin_aa
 * @coverage core.model.nonvisual
 */
public abstract class AbstractArrayObjectInfo extends ItemCollectorObjectInfo {
  public final static String ARRAY_ARBITRARY_KEY = "ARRAY_OBJECT";
  public final static String REMOVE_ON_EMPTY_TAG = "arrayObject.removeOnEmpty";
  public final static String HIDE_IN_TREE_TAG = "arrayObject.hideInTree";
  //public final static String CREATION_ID_TAG = "arrayObject.creationId";
  private final AbstractArrayObjectInfo m_this = this;
  //private String m_creationId = StringUtils.EMPTY;
  private final Class<?> m_itemType;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  protected AbstractArrayObjectInfo(AstEditor editor, String caption, Class<?> itemType)
      throws Exception {
    super(editor, caption);
    m_itemType = itemType;
  }

  public Class<?> getItemClass() {
    return m_itemType;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IObjectPresentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public IObjectPresentation getPresentation() {
    return new DefaultObjectPresentation(this) {
      public String getText() throws Exception {
        String text = "(array)";
        if (!StringUtils.isEmpty(m_caption)) {
          text = m_caption + text;
        }
        return text;
      }

      @Override
      public Image getIcon() throws Exception {
        return DesignerPlugin.getImage("components/non_visual_beans_container.gif");
      }

      @Override
      public List<ObjectInfo> getChildrenTree() throws Exception {
        if (isHideInTree()) {
          return Lists.<ObjectInfo>newLinkedList();
        } else {
          return getItems();
        }
      }

      @Override
      public boolean isVisible() throws Exception {
        return !isHideInTree();
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Items
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addItem(int index, ObjectInfo item) throws Exception {
    super.addItem(index, item);
    if (item != null) {
      item.putArbitraryValue(ARRAY_ARBITRARY_KEY, this);
    }
  }

  @Override
  protected void removeItem(ObjectInfo item) throws Exception {
    if (m_this.equals(item.getArbitraryValue(ARRAY_ARBITRARY_KEY))) {
      item.removeArbitraryValue(ARRAY_ARBITRARY_KEY);
    }
    super.removeItem(item);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds new item to this array.
   */
  public void command_CREATE(JavaInfo item, JavaInfo nextItem) throws Exception {
    JavaInfo parentJavaInfo = getParent() instanceof JavaInfo ? (JavaInfo) getParent() : null;
    // fire before event
    getBroadcast(ObjectInfoChildAddBefore.class).invoke(
        getParent(),
        item,
        new ObjectInfo[]{nextItem});
    if (parentJavaInfo != null) {
      parentJavaInfo.getBroadcastJava().addBefore(parentJavaInfo, item);
    }
    // setup hierarchy
    int index = m_items.indexOf(nextItem);
    index = index == -1 ? m_items.size() : index;
    getParent().addChild(item, nextItem);
    addItem(index, item);
    // add source
    NodeTarget nodeTarget = getNodeTarget();
    String source = item.getCreationSupport().add_getSource(nodeTarget);
    Expression element =
        getCreateItemExpression(
            item,
            index,
            AssociationUtils.replaceTemplates(item, source, nodeTarget));
    // set association
    item.setAssociation(getAssociation(element));
    // fire after event
    if (parentJavaInfo != null) {
      parentJavaInfo.getBroadcastJava().addAfter(parentJavaInfo, item);
    }
    getBroadcast(ObjectInfoChildAddAfter.class).invoke(getParent(), item);
  }

  protected Expression getCreateItemExpression(JavaInfo item, int index, String source)
      throws Exception {
    Expression itemExpression = createItemExpression(item, index, source);
    item.addRelatedNode(itemExpression);
    // set variable support
    if (item.getVariableSupport() == null) {
      item.setVariableSupport(new EmptyVariableSupport(item, itemExpression));
    }
    // set source creation
    CreationSupport creationSupport = item.getCreationSupport();
    if (creationSupport != null && itemExpression instanceof ClassInstanceCreation) {
      creationSupport.add_setSourceExpression(itemExpression);
    }
    return itemExpression;
  }

  abstract protected Expression createItemExpression(JavaInfo item, int index, String source)
      throws Exception;

  /**
   * Moves item into this array.
   */
  public void command_MOVE(JavaInfo item, JavaInfo nextItem) throws Exception {
    JavaInfo parJavaInfo = getParent() instanceof JavaInfo ? (JavaInfo) getParent() : null;
    JavaInfo oldJavaInfo = item.getParent() instanceof JavaInfo ? item.getParentJava() : null;
    // fire before event
    getBroadcastObject().childMoveBefore(getParent(), item, nextItem);
    if (parJavaInfo != null) {
      parJavaInfo.getBroadcastJava().moveBefore(item, oldJavaInfo, parJavaInfo);
    }
    // move hierarchy
    int newIndex = m_items.indexOf(nextItem);
    newIndex = newIndex == -1 ? m_items.size() : newIndex;
    int oldIndex = item.getParent().getChildren().indexOf(item);
    AbstractArrayObjectInfo oldAbstractArrayInfo = getArrayObjectInfo(item);
    if (oldAbstractArrayInfo != null) {
      oldIndex = oldAbstractArrayInfo.getItems().indexOf(item);
      oldIndex = oldIndex == -1 ? oldAbstractArrayInfo.getItems().size() : oldIndex;
      if (oldAbstractArrayInfo == this && newIndex > oldIndex) {
        newIndex--;
      }
    }
    Expression element =
        getMoveItemExpression(item, nextItem, oldAbstractArrayInfo, oldIndex, newIndex);
    // set association
    item.setAssociation(getAssociation(element));
    // fire after event
    if (parJavaInfo != null) {
      parJavaInfo.getBroadcastJava().moveAfter(item, oldJavaInfo, parJavaInfo);
    }
    getBroadcastObject().childMoveAfter(getParent(), item, nextItem, oldIndex, newIndex);
  }

  abstract protected Expression getMoveItemExpression(JavaInfo item,
      JavaInfo nextItem,
      AbstractArrayObjectInfo oldAbstractArrayInfo,
      int oldIndex,
      int newIndex) throws Exception;

  /**
   * Move item from outside to this {@link AbstractArrayObjectInfo}.
   */
  protected Expression moveOther(JavaInfo item, JavaInfo nextItem, int newIndex) throws Exception {
    // try optimize source
    if (item.getVariableSupport() instanceof LocalUniqueVariableSupport) {
      LocalUniqueVariableSupport localVariableSupport =
          (LocalUniqueVariableSupport) item.getVariableSupport();
      if (localVariableSupport.canInline()) {
        localVariableSupport.inline();
      }
    }
    // source
    String source = null;
    if (item.getVariableSupport() instanceof EmptyVariableSupport) {
      source =
          getEditor().getSource(((EmptyVariableSupport) item.getVariableSupport()).getInitializer());
    }
    // remove from old place
    Association association = item.getAssociation();
    if (association != null) {
      if (association.remove()) {
        item.setAssociation(null);
      }
    }
    item.getParent().removeChild(item);
    // add to array
    getParent().addChild(item, nextItem);
    addItem(newIndex, item);
    if (!(item.getVariableSupport() instanceof EmptyVariableSupport)) {
      item.getVariableSupport().ensureInstanceReadyAt(getStatementTarget());
      source = item.getVariableSupport().getReferenceExpression(getNodeTarget());
    }
    Assert.isNotNull(source, "No source found for.");
    Expression element = getCreateItemExpression(item, newIndex, source);
    return element;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Targets
  //
  ////////////////////////////////////////////////////////////////////////////
  abstract protected NodeTarget getNodeTarget();

  abstract protected StatementTarget getStatementTarget();

  /**
   * @return {@link Association} for array item.
   */
  abstract protected Association getAssociation(Expression element);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  static public AbstractArrayObjectInfo getArrayObjectInfo(ObjectInfo item) {
    return (AbstractArrayObjectInfo) item.getArbitraryValue(ARRAY_ARBITRARY_KEY);
  }

  @Override
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    // creation
    buffer.append(getCaption());
    buffer.append(" []");
    // result
    return buffer.toString();
  }
}
