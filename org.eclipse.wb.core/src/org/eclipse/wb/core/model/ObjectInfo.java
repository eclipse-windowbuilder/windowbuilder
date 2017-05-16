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
package org.eclipse.wb.core.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.core.model.broadcast.BroadcastSupport;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectInfoAllProperties;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildAddAfter;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildAddBefore;
import org.eclipse.wb.internal.core.model.ObjectInfoVisitor;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Abstract model for any object that should be handled by Designer.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public abstract class ObjectInfo implements IObjectInfo {
  public static final String KEY_NO_REFRESHED_BROADCAST =
      "ObjectInfo: don't send refreshed() broadcast";
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private ObjectInfo m_parent;
  private final List<ObjectInfo> m_children = Lists.newLinkedList();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link IObjectPresentation} for visual presentation of this object.
   */
  public abstract IObjectPresentation getPresentation();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Hierarchy
  //
  ////////////////////////////////////////////////////////////////////////////
  public ObjectInfo getUnderlyingModel() {
    return this;
  }

  /**
   * @return <code>true</code> if this object is root, i.e. has no parent.
   */
  public final boolean isRoot() {
    return m_parent == null;
  }

  /**
   * @return <code>true</code> if this object is given object or its parent.
   */
  public final boolean isItOrParentOf(ObjectInfo o) {
    return o == this || isParentOf(o);
  }

  /**
   * @param o
   *          the potential child.
   *
   * @return <code>true</code> if this object is direct/indirect parent of given object.
   */
  public final boolean isParentOf(ObjectInfo o) {
    while (o != null) {
      ObjectInfo parent = o.getParent();
      if (parent != null && !parent.getChildren().contains(o)) {
        return false;
      }
      if (parent == this) {
        return true;
      }
      o = parent;
    }
    return false;
  }

  /**
   * @param parents
   *          the list of possible parents.
   *
   * @return the direct/indirect parent of this object from the list of possible parents.
   */
  @SuppressWarnings("unchecked")
  public final <T extends ObjectInfo> T getParent(List<T> parents) {
    ObjectInfo o = getParent();
    for (; o != null; o = o.getParent()) {
      if (parents.contains(o)) {
        return (T) o;
      }
    }
    // no parent found
    return null;
  }

  /**
   * @return the root {@link ObjectInfo}.
   */
  public final ObjectInfo getRoot() {
    return m_parent == null ? this : m_parent.getRoot();
  }

  public final ObjectInfo getParent() {
    return m_parent;
  }

  /**
   * Sets the parent of this object, but does not add it to parent.
   */
  public final void setParent(ObjectInfo parent) {
    m_parent = parent;
  }

  /**
   * @return list of {@link ObjectInfo} children.
   */
  public final List<ObjectInfo> getChildren() {
    return m_children;
  }

  /**
   * @return list of children with given type.
   */
  public final <T extends ObjectInfo> List<T> getChildren(Class<T> clazz) {
    return GenericsUtils.select(m_children, clazz);
  }

  /**
   * Adds given child to the end of children list.
   */
  public final void addChild(ObjectInfo child) throws Exception {
    addChild(child, null);
  }

  /**
   * Adds given child as first into children list.
   */
  public final void addChildFirst(ObjectInfo child) throws Exception {
    ObjectInfo nextChild = GenericsUtils.getFirstOrNull(m_children);
    addChild(child, nextChild);
  }

  /**
   * Adds given <code>child</code> to the children list directly before <code>nextChild</code>.
   */
  public final void addChild(final ObjectInfo child, ObjectInfo nextChild) throws Exception {
    // pre-checks
    {
      Assert.isNotNull(child);
      Assert.isTrue(!m_children.contains(child));
      // don't allow add parent to its child
      child.accept(new ObjectInfoVisitor() {
        @Override
        public void endVisit(ObjectInfo objectInfo) throws Exception {
          Assert.isTrue(
              objectInfo != ObjectInfo.this,
              "<code>%s</code> is parent of <code>%s</code>, so first can not be added as child of second.",
              child,
              ObjectInfo.this);
        }
      });
    }
    // send "before" broadcast, may be "nextChild" will be changed
    {
      ObjectInfo[] nextChild_ref = new ObjectInfo[]{nextChild};
      getBroadcast(ObjectInfoChildAddBefore.class).invoke(this, child, nextChild_ref);
      nextChild = nextChild_ref[0];
    }
    // prepare index
    int index;
    if (nextChild != null) {
      index = m_children.indexOf(nextChild);
    } else {
      index = m_children.size();
    }
    // do add
    m_children.add(index, child);
    child.m_parent = this;
    //
    getBroadcast(ObjectInfoChildAddAfter.class).invoke(this, child);
  }

  /**
   * Moves existing <code>child</code> in children list directly before <code>nextChild</code>.
   */
  public final void moveChild(ObjectInfo child, ObjectInfo nextChild) throws Exception {
    Assert.isTrue(m_children.contains(child));
    // if not before itself
    if (child != nextChild) {
      getBroadcastObject().childMoveBefore(this, child, nextChild);
      // remove from old index
      int oldIndex = m_children.indexOf(child);
      m_children.remove(child);
      // prepare target index
      int index;
      if (nextChild != null) {
        index = m_children.indexOf(nextChild);
      } else {
        index = m_children.size();
      }
      // add to target index
      m_children.add(index, child);
      //
      getBroadcastObject().childMoveAfter(this, child, nextChild, oldIndex, index);
    }
  }

  /**
   * Removes given child from the list of children.
   */
  public final void removeChild(ObjectInfo child) throws Exception {
    Assert.isTrue(m_children.contains(child));
    //
    getBroadcastObject().childRemoveBefore(this, child);
    m_children.remove(child);
    getBroadcastObject().childRemoveAfter(this, child);
  }

  /**
   * Replaces given <code>oldChild</code> with <code>newChild</code>.
   */
  public final void replaceChild(ObjectInfo oldChild, ObjectInfo newChild) throws Exception {
    Assert.isTrue(m_children.contains(oldChild));
    Assert.isNotNull(newChild);
    int index = m_children.indexOf(oldChild);
    m_children.set(index, newChild);
    newChild.m_parent = this;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visiting
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Visits this {@link ObjectInfo} and its children using given {@link ObjectInfoVisitor}.
   */
  public final void accept(final ObjectInfoVisitor visitor) {
    ExecutionUtils.runRethrow(new RunnableEx() {
      public void run() throws Exception {
        accept0(visitor);
      }
    });
  }

  /**
   * Unsafe implementation of {@link #accept(ObjectInfoVisitor)}.
   */
  public final void accept0(final ObjectInfoVisitor visitor) throws Exception {
    if (visitor.visit(ObjectInfo.this)) {
      for (ObjectInfo child : getChildren()) {
        child.accept0(visitor);
      }
      visitor.endVisit(ObjectInfo.this);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Broadcasting
  //
  ////////////////////////////////////////////////////////////////////////////
  private BroadcastSupport m_broadcastSupport;

  /**
   * Returns the {@link BroadcastSupport} for this {@link ObjectInfo} hierarchy.<br>
   * Note that for add/get listeners the appropriate methods should be used instead of this method.
   *
   * @see #addBroadcastListener(Object)
   * @see #removeBroadcastListener(Object)
   * @see #targetBroadcastListener(ObjectInfo)
   * @see #getBroadcast(Class)
   * @return the {@link BroadcastSupport} for this {@link ObjectInfo} hierarchy.
   */
  public BroadcastSupport getBroadcastSupport() {
    return isRoot() ? m_broadcastSupport : getRoot().getBroadcastSupport();
  }

  /**
   * Returns the broadcast listener of given class in this {@link ObjectInfo} hierarchy.
   *
   * @see BroadcastSupport#getListener(Class)
   * @return the broadcast listener of given class in this {@link ObjectInfo} hierarchy.
   */
  public <T> T getBroadcast(Class<T> listenerClass) {
    return getBroadcastSupport().getListener(listenerClass);
  }

  public final void addBroadcastListener(Object listenerImpl) {
    getBroadcastSupport().addListener(this, listenerImpl);
  }

  /**
   * Adds new listener with type of superclass.
   */
  public final void removeBroadcastListener(Object listenerImpl) {
    getBroadcastSupport().removeListener(this, listenerImpl);
  }

  /**
   * Sets different target instead of "this".
   */
  public final void targetBroadcastListener(ObjectInfo newTarget) {
    getBroadcastSupport().targetListener(this, newTarget);
  }

  /**
   * Externally sets the {@link BroadcastSupport}.
   */
  protected final void setBroadcastSupport(BroadcastSupport broadcastSupport) {
    m_broadcastSupport = broadcastSupport;
  }

  /**
   * @return the {@link ObjectEventListener} for hierarchy.
   */
  public final ObjectEventListener getBroadcastObject() {
    return ExecutionUtils.runObject(new RunnableObjectEx<ObjectEventListener>() {
      public ObjectEventListener runObject() throws Exception {
        return getBroadcastSupport().getListener(ObjectEventListener.class);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Return array of {@link Property}'s for this information object.
   */
  public Property[] getProperties() throws Exception {
    List<Property> propertyList = getPropertyList();
    getBroadcast(ObjectInfoAllProperties.class).invoke(this, propertyList);
    sortPropertyList(propertyList);
    return propertyList.toArray(new Property[propertyList.size()]);
  }

  /**
   * Do properties sorting. Default implementation sorts properties by title alphabetically.
   */
  protected void sortPropertyList(List<Property> properties) {
    Collections.sort(properties, new Comparator<Property>() {
      public int compare(Property o1, Property o2) {
        return o1.getTitle().compareTo(o2.getTitle());
      }
    });
  }

  /**
   * @return the {@link List} of {@link Property}'s.
   */
  protected List<Property> getPropertyList() throws Exception {
    return Lists.newArrayList();
  }

  public final Property getPropertyByTitle(String title) throws Exception {
    for (Property property : getPropertyList()) {
      if (title.equals(property.getTitle())) {
        return property;
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete operation
  //
  ////////////////////////////////////////////////////////////////////////////
  protected static final String FLAG_DELETING = "we are in process of deleting";

  public final boolean isDeleting() {
    return getArbitraryValue(FLAG_DELETING) != null;
  }

  /**
   * @return <code>true</code> if {@link #delete()} can be used.
   */
  public boolean canDelete() {
    return false;
  }

  public void delete() throws Exception {
  }

  public final boolean isDeleted() {
    return m_parent != null && !m_parent.m_children.contains(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Edit operations
  //
  ////////////////////////////////////////////////////////////////////////////
  private int m_editDepth;

  /**
   * Prepare this component to start a new sequence of edits.
   */
  public final void startEdit() throws Exception {
    ObjectInfo root = getRoot();
    root.m_editDepth++;
  }

  /**
   * End a possibly nested sequence of edits. If this marks the end of the outermost sequence of
   * edits, then commit the edits that have been made and call {@link #refresh()}.
   */
  public final void endEdit() throws Exception {
    ObjectInfo root = getRoot();
    root.m_editDepth--;
    if (root.m_editDepth == 0) {
      root.fire_aboutToRefresh();
      root.saveEdit();
      root.refresh();
    }
  }

  /**
   * Fires {@link ObjectEventListener#endEdit_aboutToRefresh()} listener, wraps it in "fake" edit
   * operation to avoid possible double {@link #refresh()}.
   */
  private void fire_aboutToRefresh() throws Exception {
    m_editDepth++;
    getBroadcastObject().endEdit_aboutToRefresh();
    m_editDepth--;
  }

  /**
   * Saves changes done during edit transaction.
   */
  protected void saveEdit() throws Exception {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Refreshes tree of model objects.
   *
   * Components can for example create components tree, fetch any visual information (image, bounds,
   * etc), and as last step dispose components tree to clean up resources.
   */
  public final void refresh() throws Exception {
    Assert.isLegal(isRoot());
    // clean up broadcast
    getBroadcastSupport().cleanUpTargets(ObjectInfo.this);
    // do refresh
    execRefreshOperation(new RunnableEx() {
      public void run() throws Exception {
        ExecutionUtils.runDesignTime(new RunnableEx() {
          public void run() throws Exception {
            refreshCreate0();
          }
        });
      }
    });
    // split fetch operations into separate parts
    execRefreshOperation(new RunnableEx() {
      public void run() throws Exception {
        ExecutionUtils.runDesignTime(new RunnableEx() {
          public void run() throws Exception {
            refresh_fetch();
            refresh_finish();
          }
        });
      }
    });
    // send notifications
    if (getArbitraryValue(KEY_NO_REFRESHED_BROADCAST) != Boolean.FALSE) {
      getBroadcastObject().refreshed();
      getBroadcastObject().refreshed2();
    }
  }

  /**
   * Runs {@link RunnableEx} performing refresh. This is overridden in Swing because it requires
   * doing refresh in AWT dispatch thread.
   */
  protected void execRefreshOperation(RunnableEx runnableEx) throws Exception {
    runnableEx.run();
  }

  /**
   * Performs "light" version of {@link #refresh()}, just create new objects, but don't fetch.
   */
  public final void refreshLight() throws Exception {
    Assert.isLegal(isRoot());
    // do refresh
    execRefreshOperation(new RunnableEx() {
      public void run() throws Exception {
        ExecutionUtils.runDesignTime(new RunnableEx() {
          public void run() throws Exception {
            refreshCreate0();
          }
        });
      }
    });
    execRefreshOperation(new RunnableEx() {
      public void run() throws Exception {
        ExecutionUtils.runDesignTime(new RunnableEx() {
          public void run() throws Exception {
            refresh_finish();
          }
        });
      }
    });
  }

  /**
   * Performs "create" steps.
   */
  private void refreshCreate0() throws Exception {
    refresh_dispose();
    try {
      refresh_beforeCreate();
      refresh_create();
      refresh_afterCreate0();
      refresh_afterCreate();
      refresh_afterCreate2();
    } finally {
      getBroadcast(ObjectEventListener.class).refreshFinallyRefresh();
    }
  }

  /**
   * This method is invoked before {@link #refresh_create()}. It may be called multiple times, every
   * time when disposing the resources needed.<br>
   * It can for example clear any resource allocated by previous {@link #refresh_create()} or
   * {@link #refresh_fetch()}.<br>
   * Sends the {@link ObjectEventListener#refreshDispose()} broadcast for root object.
   */
  public void refresh_dispose() throws Exception {
    if (isRoot()) {
      getBroadcastObject().refreshDispose();
    }
    for (ObjectInfo child : getChildren()) {
      child.refresh_dispose();
    }
  }

  /**
   * This method is invoked just before {@link #refresh_create()} and called only once.<br>
   * Sends the {@link ObjectEventListener#refreshBeforeCreate()} broadcast for root object.
   */
  public void refresh_beforeCreate() throws Exception {
    if (isRoot()) {
      getBroadcastObject().refreshBeforeCreate();
    }
    for (ObjectInfo child : getChildren()) {
      child.refresh_beforeCreate();
    }
  }

  /**
   * This method creates its object.
   */
  protected void refresh_create() throws Exception {
    for (ObjectInfo child : getChildren()) {
      child.refresh_create();
    }
  }

  /**
   * This method is invoked after {@link #refresh_create()}.<br>
   * This method is invoked before {@link #refresh_afterCreate()}.<br>
   * Sometimes it is not enough to have single <code>afterCreate</code> notification.
   */
  protected void refresh_afterCreate0() throws Exception {
    if (isRoot()) {
      getBroadcastObject().refreshAfterCreate0();
    }
    for (ObjectInfo child : getChildren()) {
      child.refresh_afterCreate0();
    }
  }

  /**
   * This method is invoked after {@link #refresh_afterCreate0()}.<br>
   * This is good place to do any operations that require full objects tree to be build.<br>
   * Sends the {@link ObjectEventListener#refreshAfterCreate()} broadcast for root object.
   */
  protected void refresh_afterCreate() throws Exception {
    if (isRoot()) {
      getBroadcastObject().refreshAfterCreate();
    }
    for (ObjectInfo child : getChildren()) {
      child.refresh_afterCreate();
    }
  }

  /**
   * This method is invoked after {@link #refresh_afterCreate()}.<br>
   * This is good place to do any operations that require full objects tree to be build.<br>
   * Sends the {@link ObjectEventListener#refreshAfterCreate2()} broadcast for root object.
   */
  protected void refresh_afterCreate2() throws Exception {
    if (isRoot()) {
      getBroadcastObject().refreshAfterCreate2();
    }
    for (ObjectInfo child : getChildren()) {
      child.refresh_afterCreate2();
    }
  }

  /**
   * This method should fetch any information about object that needed for future operations, for
   * example its image, bounds, etc.
   */
  protected void refresh_fetch() throws Exception {
    for (ObjectInfo child : getChildren()) {
      child.refresh_fetch();
    }
  }

  /**
   * This is last method invoked during "refresh" cycle. It can clean up any resource that should be
   * accessible only during refresh. In contrast {@link #refresh_dispose()} clear more long lived
   * resources, that should live between {@link #refresh()} invocations.
   */
  protected void refresh_finish() throws Exception {
    List<ObjectInfo> children = getChildren();
    children = ImmutableList.copyOf(getChildren());
    for (ObjectInfo child : children) {
      child.refresh_finish();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Arbitrary values map
  //
  ////////////////////////////////////////////////////////////////////////////
  private Map<Object, Object> m_arbitraryMap;

  /**
   * Associates the given value with the given key.
   */
  public final void putArbitraryValue(Object key, Object value) {
    if (m_arbitraryMap == null) {
      m_arbitraryMap = Maps.newHashMap();
    }
    m_arbitraryMap.put(key, value);
  }

  /**
   * @return the value to which the given key is mapped, or <code>null</code>.
   */
  public final Object getArbitraryValue(Object key) {
    if (m_arbitraryMap != null) {
      return m_arbitraryMap.get(key);
    }
    return null;
  }

  /**
   * Removes the mapping for a key.
   */
  public final void removeArbitraryValue(Object key) {
    if (m_arbitraryMap != null) {
      m_arbitraryMap.remove(key);
    }
  }

  /**
   * @return the mapped arbitrary keys.
   */
  public final Map<Object, Object> getArbitraries() {
    Map<Object, Object> arbitraries;
    if (m_arbitraryMap != null) {
      arbitraries = Maps.newHashMap(m_arbitraryMap);
    } else {
      arbitraries = ImmutableMap.of();
    }
    return arbitraries;
  }
}
