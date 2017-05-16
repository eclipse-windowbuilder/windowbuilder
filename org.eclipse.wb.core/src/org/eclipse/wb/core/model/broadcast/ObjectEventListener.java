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

import org.eclipse.wb.core.model.ObjectInfo;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;

import java.util.List;

/**
 * Listener for {@link ObjectInfo} events.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public abstract class ObjectEventListener {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * During closing editor or other event when we going to throw away this {@link ObjectInfo}
   * hierarchy.
   * <p>
   * Directly before disposing GEF presentation.
   */
  public void dispose_beforePresentation() throws Exception {
  }

  /**
   * During closing editor or other event when we going to throw away this {@link ObjectInfo}
   * hierarchy.
   */
  public void dispose() throws Exception {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Edit cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Notifies that last {@link ObjectInfo#endEdit()} in edit sequence was reached, so
   * {@link ObjectInfo#refresh()} will be started directly after this notification. This is last
   * chance to perform some AST/source editing before refresh.
   */
  public void endEdit_aboutToRefresh() throws Exception {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When {@link ObjectInfo#refresh_dispose()} for root executed.
   */
  public void refreshDispose() throws Exception {
  }

  /**
   * When {@link ObjectInfo#refresh_beforeCreate()} for root executed.
   */
  public void refreshBeforeCreate() throws Exception {
  }

  /**
   * When {@link ObjectInfo#refresh_afterCreate0()} for root executed.
   */
  public void refreshAfterCreate0() throws Exception {
  }

  /**
   * When {@link ObjectInfo#refresh_afterCreate()} for root executed.
   */
  public void refreshAfterCreate() throws Exception {
  }

  /**
   * When {@link ObjectInfo#refresh_afterCreate2()} for root executed.
   */
  public void refreshAfterCreate2() throws Exception {
  }

  /**
   * Last "refreshCreate" method which is called always, even in case of exception.
   */
  public void refreshFinallyRefresh() {
  }

  /**
   * After {@link ObjectInfo#refresh()} cycle executed.
   */
  public void refreshed() throws Exception {
  }

  /**
   * After {@link ObjectInfo#refresh()} cycle executed and also all {@link #refreshed()}
   * notifications finished.
   *
   * Sometimes one level of "refresh" is not enough, for example menu performs GEF refresh in
   * {@link #refreshed()}, but temporary initializes static variables that should be cleared after
   * GEF refresh. So we have choice - do this in "async", or add one more "refreshed" notification.
   */
  public void refreshed2() throws Exception {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Child remove
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Before removing child from parent.
   */
  public void childRemoveBefore(ObjectInfo parent, ObjectInfo child) throws Exception {
  }

  /**
   * After removing child from parent.
   */
  public void childRemoveAfter(ObjectInfo parent, ObjectInfo child) throws Exception {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Child move
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Before moving child in parent.
   */
  public void childMoveBefore(ObjectInfo parent, ObjectInfo child, ObjectInfo nextChild)
      throws Exception {
  }

  /**
   * After moving child in parent.
   *
   * @param oldIndex
   *          the index in children of <code>parent</code>.
   * @param newIndex
   *          the index in children of <code>parent</code>.
   */
  public void childMoveAfter(ObjectInfo parent,
      ObjectInfo child,
      ObjectInfo nextChild,
      int oldIndex,
      int newIndex) throws Exception {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Notifies listeners that presentation or properties of some model object was changed, but
   * {@link ObjectInfo#refresh()} will not happen.
   */
  public void presentationChanged() throws Exception {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * This method is invoked during selecting {@link ObjectInfo}.
   *
   * @param object
   *          the selecting {@link ObjectInfo}, may be <code>null</code>.
   * @param refreshFlag
   *          the array with single boolean flag, if one of the listeners will set it to
   *          <code>true</code> , {@link ObjectInfo#refresh()} will be performed.
   */
  public void selecting(ObjectInfo object, boolean[] refreshFlag) throws Exception {
  }

  /**
   * This method can be used from model code to notify that it wants to select given
   * {@link ObjectInfo}'s.
   *
   * @param objects
   *          the {@link ObjectInfo}'s to select.
   */
  public void select(List<? extends ObjectInfo> objects) throws Exception {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Context menu
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * This method is invoked during building context menu for given {@link ObjectInfo}.
   *
   * @param objects
   *          the selected {@link ObjectInfo}'s, one of them is passed as <code>object</code>.
   * @param object
   *          the current {@link ObjectInfo}, for which menu is requested.
   * @param manager
   *          the {@link IMenuManager} to contribute items.
   */
  public void addContextMenu(List<? extends ObjectInfo> objects,
      ObjectInfo object,
      IMenuManager manager) throws Exception {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Toolbar actions
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * This method is invoked during editor toolbar refresh for building special actions, related with
   * full components hierarchy, in contrast to {@link #addSelectionActions(List, List)} that is used
   * for selection sensitive actions.
   *
   * @param actions
   *          the {@link List} of {@link IAction}'s or {@link IContributionItem}'s to display for
   *          user.
   */
  public void addHierarchyActions(List<Object> actions) throws Exception {
  }

  /**
   * This method is invoked during selecting {@link ObjectInfo}'s for building special actions.
   *
   * @param objects
   *          the List of selected {@link ObjectInfo}'s.
   * @param actions
   *          the {@link List} of {@link IAction}'s or {@link IContributionItem}'s to display for
   *          user.
   */
  public void addSelectionActions(List<ObjectInfo> objects, List<Object> actions) throws Exception {
  }
}
