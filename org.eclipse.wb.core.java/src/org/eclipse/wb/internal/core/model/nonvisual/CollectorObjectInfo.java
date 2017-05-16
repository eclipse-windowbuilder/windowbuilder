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

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildTree;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.presentation.DefaultObjectPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import org.eclipse.swt.graphics.Image;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Collector of {@link ObjectInfo}.
 *
 * @author sablin_aa
 * @coverage core.model.nonvisual
 */
public abstract class CollectorObjectInfo extends ObjectInfo {
  public final static String COLLECTOR_ARBITRARY_KEY = "CONTAINER_OBJECT";
  private boolean m_removeOnEmpty = false;
  private boolean m_hideInTree = false;
  private final AstEditor m_editor;
  protected final String m_caption;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CollectorObjectInfo(AstEditor editor, String caption) throws Exception {
    m_editor = editor;
    m_caption = StringUtils.isEmpty(caption) ? "(container)" : caption;
    // initialize
    setBroadcastSupport(EditorState.get(m_editor).getBroadcast());
    installListeners();
  }

  protected void installListeners() {
    addBroadcastListener(new ObjectInfoChildTree() {
      public void invoke(ObjectInfo object, boolean[] visible) throws Exception {
        visible[0] &= isHideInTree() || !getItems().contains(object);
      }
    });
  }

  public AstEditor getEditor() {
    return m_editor;
  }

  public void setRemoveOnEmpty(boolean removeOnEmpty) {
    m_removeOnEmpty = removeOnEmpty;
  }

  public boolean isRemoveOnEmpty() {
    return m_removeOnEmpty;
  }

  public void setHideInTree(boolean hideInTree) {
    m_hideInTree = hideInTree;
  }

  public boolean isHideInTree() {
    return m_hideInTree;
  }

  public String getCaption() {
    return m_caption;
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
        return m_caption;
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
  // Delete operation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean canDelete() {
    // ask each item
    for (ObjectInfo item : getItems()) {
      if (!item.canDelete()) {
        return false;
      }
    }
    // yes, ArrayObjectInfo can be deleted
    return true;
  }

  @Override
  public void delete() throws Exception {
    ExecutionUtils.run(this, new RunnableEx() {
      public void run() throws Exception {
        List<ObjectInfo> items = getItems();
        for (int i = 0; i < items.size(); i++) {
          ObjectInfo itemInfo = items.get(i);
          if (!itemInfo.isDeleted()) {
            itemInfo.delete();
          }
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Items
  //
  ////////////////////////////////////////////////////////////////////////////
  public abstract List<ObjectInfo> getItems();

  public abstract void addItem(ObjectInfo item) throws Exception;

  protected abstract void addItem(int index, ObjectInfo item) throws Exception;

  protected abstract void removeItem(ObjectInfo item) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visuals
  //
  ////////////////////////////////////////////////////////////////////////////
  public Rectangle getBounds() {
    Rectangle bounds = null;
    for (ObjectInfo objectInfo : getItems()) {
      if (objectInfo instanceof AbstractComponentInfo) {
        AbstractComponentInfo componentInfo = (AbstractComponentInfo) objectInfo;
        Rectangle itemBounds = componentInfo.getBounds();
        if (bounds == null && itemBounds != null) {
          bounds = new Rectangle(itemBounds);
        } else {
          if (bounds != null && itemBounds != null) {
            bounds.union(componentInfo.getBounds());
          }
        }
      }
    }
    if (bounds == null) {
      return new Rectangle(0, 0, 0, 0);
    } else {
      return bounds;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  static public CollectorObjectInfo getCollectorObjectInfo(ObjectInfo item) {
    return (CollectorObjectInfo) item.getArbitraryValue(COLLECTOR_ARBITRARY_KEY);
  }
}
