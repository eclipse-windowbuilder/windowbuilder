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
package org.eclipse.wb.internal.core.xml.editor.actions;

import com.google.common.collect.Maps;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.internal.core.editor.actions.DeleteAction;
import org.eclipse.wb.internal.core.editor.actions.assistant.LayoutAssistantAction;
import org.eclipse.wb.internal.core.xml.editor.XmlDesignPage;
import org.eclipse.wb.internal.core.xml.editor.XmlEditorPage;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;

import java.util.Map;

/**
 * Helper for managing {@link XmlDesignPage} actions.
 *
 * @author scheglov_ke
 * @coverage XML.editor.action
 */
public final class DesignPageActions {
  private final IEditPartViewer m_viewer;
  private final IActionBars m_actionBars;
  ////////////////////////////////////////////////////////////////////////////
  //
  // Actions
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IAction m_cutAction;
  private final IAction m_copyAction;
  private final IAction m_pasteAction;
  private final IAction m_deleteAction;
  private final TestAction m_testAction;
  private final IAction m_refreshAction;
  private final LayoutAssistantAction m_assistantAction;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Initializes new {@link DesignPageActions}.
   *
   * @param viewer
   *          the {@link IEditPartViewer} of {@link XmlEditorPage}.
   * @param actionBars
   *          the editor site {@link IActionBars}.
   */
  public DesignPageActions(IEditorPart editor, IEditPartViewer viewer) {
    m_viewer = viewer;
    m_actionBars = editor.getEditorSite().getActionBars();
    m_cutAction = new CutAction(m_viewer);
    m_copyAction = new CopyAction(m_viewer);
    m_pasteAction = new PasteAction(m_viewer);
    m_deleteAction = new DeleteAction(m_viewer);
    m_testAction = new TestAction();
    m_refreshAction = new RefreshAction();
    m_assistantAction = new LayoutAssistantAction(editor, viewer);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setRoot(ObjectInfo root) {
    m_testAction.setRoot(root);
    m_assistantAction.setRoot(root);
  }

  public IAction getCutAction() {
    return m_cutAction;
  }

  public IAction getCopyAction() {
    return m_copyAction;
  }

  public IAction getPasteAction() {
    return m_pasteAction;
  }

  public IAction getDeleteAction() {
    return m_deleteAction;
  }

  public IAction getTestAction() {
    return m_testAction;
  }

  public IAction getRefreshAction() {
    return m_refreshAction;
  }

  public LayoutAssistantAction getAssistantAction() {
    return m_assistantAction;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Map<String, IAction> m_originalActions = Maps.newTreeMap();

  /**
   * Installs Designer handlers for global actions.
   */
  public void installActions() {
    m_assistantAction.showWindow();
    installAction(m_cutAction);
    installAction(m_copyAction);
    installAction(m_pasteAction);
    installAction(m_deleteAction);
    installAction(m_refreshAction);
    m_actionBars.updateActionBars();
  }

  /**
   * Installs single handler for global action with given <code>id</code>.
   */
  private void installAction(IAction action) {
    String id = action.getId();
    IAction oldAction = m_actionBars.getGlobalActionHandler(id);
    if (!m_originalActions.containsKey(id)) {
      m_originalActions.put(id, oldAction);
      m_actionBars.setGlobalActionHandler(id, action);
    }
  }

  /**
   * Uninstalls actions installed by {@link #installActions()} and restores original ones.
   */
  public void uninstallActions() {
    m_assistantAction.hideWindow();
    //
    for (Map.Entry<String, IAction> entry : m_originalActions.entrySet()) {
      String id = entry.getKey();
      IAction action = entry.getValue();
      m_actionBars.setGlobalActionHandler(id, action);
    }
    m_originalActions.clear();
    //
    m_actionBars.updateActionBars();
  }

  /**
   * Disposed actions created for this {@link XmlEditorPage}.
   */
  public void dispose() {
    m_assistantAction.closeWindow();
  }
}
