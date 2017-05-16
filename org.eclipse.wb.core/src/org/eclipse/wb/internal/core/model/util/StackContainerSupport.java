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
package org.eclipse.wb.internal.core.model.util;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildAddAfter;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.state.GlobalState;

import java.util.List;

/**
 * Helper for managing stack like containers, where only one child can be displayed.
 *
 * @author scheglov_ke
 * @coverage core.model.util
 */
public abstract class StackContainerSupport<T extends ObjectInfo> {
  private final ObjectInfo m_container;
  private T m_active;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public StackContainerSupport(ObjectInfo container) throws Exception {
    m_container = container;
    container.addBroadcastListener(new ObjectInfoChildAddAfter() {
      public void invoke(ObjectInfo parent, ObjectInfo child) throws Exception {
        if (!GlobalState.isParsing() && isActive() && isChild(child)) {
          m_active = getCasted(child);
        }
      }
    });
    container.addBroadcastListener(new ObjectEventListener() {
      @Override
      public void childRemoveBefore(ObjectInfo parent, ObjectInfo child) throws Exception {
        if (isActive() && child == m_active) {
          List<T> children = getChildren();
          m_active = children.get(0);
          if (m_active == child) {
            m_active = children.size() > 1 ? children.get(1) : null;
          }
        }
      }

      @Override
      public void selecting(ObjectInfo object, boolean[] refreshFlag) throws Exception {
        if (isActive() && getContainer().isParentOf(object)) {
          T child = getChild(object);
          if (child != null && m_active != child) {
            m_active = child;
            refreshFlag[0] = true;
          }
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public final T getActive() {
    if (m_active == null) {
      List<T> children = getChildren();
      if (!children.isEmpty()) {
        m_active = children.get(0);
      }
    }
    return m_active;
  }

  public final void setActive(T active) {
    if (active != m_active) {
      m_active = active;
      ExecutionUtils.refresh(m_container);
    }
  }

  /**
   * @return previous component relative to active.
   */
  public T getPrev() {
    List<T> children = getChildren();
    return GenericsUtils.getPrevOrLast(children, m_active);
  }

  /**
   * @return next component relative to active.
   */
  public T getNext() {
    List<T> children = getChildren();
    return GenericsUtils.getNextOrFirst(children, m_active);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Events
  //
  ////////////////////////////////////////////////////////////////////////////
  @SuppressWarnings("unchecked")
  private T getCasted(ObjectInfo o) {
    return (T) o;
  }

  /**
   * @return the {@link ObjectInfo} from {@link #getChildren()} that is given {@link ObjectInfo} or
   *         its direct/indirect parent.
   */
  private T getChild(ObjectInfo object) {
    List<T> children = getChildren();
    // check if "object" is child
    if (children.contains(object)) {
      return getCasted(object);
    }
    // find child as parent
    return object.getParent(children);
  }

  private boolean isChild(ObjectInfo child) {
    return getChildren().contains(child);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Methods to implement
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if this container is active. For example layout may be inactive when
   *         it is implicit and replace with some explicit one.
   */
  protected boolean isActive() {
    return true;
  }

  /**
   * @return the container to track children on.
   */
  protected ObjectInfo getContainer() {
    return m_container;
  }

  /**
   * @return children managed by this container.
   */
  protected abstract List<T> getChildren();
}