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

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Collector of {@link ObjectInfo}.
 *
 * @author sablin_aa
 * @coverage core.model.nonvisual
 */
public class ItemCollectorObjectInfo extends CollectorObjectInfo {
  private final ItemCollectorObjectInfo m_this = this;
  protected final LinkedList<ObjectInfo> m_items = Lists.newLinkedList();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ItemCollectorObjectInfo(AstEditor editor, String caption) throws Exception {
    super(editor, caption);
  }

  @Override
  protected void installListeners() {
    super.installListeners();
    addBroadcastListener(new ObjectEventListener() {
      @Override
      public void childRemoveAfter(ObjectInfo parent, ObjectInfo child) throws Exception {
        m_this.removeItem(child);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Items
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public List<ObjectInfo> getItems() {
    List<ObjectInfo> list = Lists.newArrayList();
    list.addAll(m_items);
    return Collections.unmodifiableList(list);
  }

  @Override
  public void addItem(ObjectInfo item) throws Exception {
    addItem(m_items.size(), item);
  }

  @Override
  protected void addItem(int index, ObjectInfo item) throws Exception {
    if (item != null && !m_items.contains(item)) {
      m_items.add(index, item);
      item.putArbitraryValue(COLLECTOR_ARBITRARY_KEY, this);
    }
  }

  @Override
  protected void removeItem(ObjectInfo item) throws Exception {
    boolean removed = m_items.remove(item);
    if (m_this.equals(item.getArbitraryValue(COLLECTOR_ARBITRARY_KEY))) {
      item.removeArbitraryValue(COLLECTOR_ARBITRARY_KEY);
    }
    if (isRemoveOnEmpty() && m_items.isEmpty() && removed) {
      getParent().removeChild(m_this);
    }
  }
}
