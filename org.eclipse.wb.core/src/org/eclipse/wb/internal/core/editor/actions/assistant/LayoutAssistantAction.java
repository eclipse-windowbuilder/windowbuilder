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
package org.eclipse.wb.internal.core.editor.actions.assistant;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.Messages;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

import java.util.List;

/**
 * {@link Action} for displaying floating window with pages for convenient editing layout related
 * properties.
 *
 * @author lobas_av
 * @coverage core.editor.action.assistant
 */
public final class LayoutAssistantAction extends Action {
  private final IEditorPart m_editor;
  private final IEditPartViewer m_viewer;
  private LayoutAssistantWindow m_assistantWindow;
  private final IWorkbenchWindow m_workbenchWindow;
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LayoutAssistantAction(IEditorPart editor, IEditPartViewer viewer) {
    super(Messages.LayoutAssistantAction_text, IAction.AS_CHECK_BOX);
    setToolTipText(Messages.LayoutAssistantAction_toolTip);
    setImageDescriptor(DesignerPlugin.getImageDescriptor("actions/assistant/assistant.png"));
    setDisabledImageDescriptor(DesignerPlugin.getImageDescriptor("actions/assistant/assistant_disabled.png"));
    // initialize editor
    m_editor = editor;
    m_workbenchWindow = m_editor.getEditorSite().getWorkbenchWindow();
    // initialize viewer
    m_viewer = viewer;
    m_viewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        updateWindow();
      }
    });
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the root {@link ObjectInfo} on design page.
   */
  public void setRoot(ObjectInfo rootObject) {
    rootObject.addBroadcastListener(new ObjectEventListener() {
      @Override
      public void refreshed() throws Exception {
        updateWindow();
      }
    });
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Window part listener
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IPartListener m_windowPartListener = new IPartListener() {
    public void partActivated(IWorkbenchPart part) {
      if (m_editor == part && m_assistantWindow != null) {
        m_assistantWindow.open();
      }
    }
    public void partDeactivated(IWorkbenchPart part) {
      if (m_editor == part && m_assistantWindow != null) {
        m_assistantWindow.hide();
      }
    }
    public void partClosed(IWorkbenchPart part) {
      if (m_editor == part) {
        closeWindow();
      }
    }
    public void partOpened(IWorkbenchPart part) {
    }
    public void partBroughtToTop(IWorkbenchPart part) {
    }
  };
  ////////////////////////////////////////////////////////////////////////////
  //
  // Action
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void run() {
    if (m_assistantWindow == null) {
      // create assistant window
      m_assistantWindow = new LayoutAssistantWindow(DesignerPlugin.getShell());
      setChecked(true);
      showWindow();
      updateWindow();
    } else {
      closeWindow();
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Window
  //
  ////////////////////////////////////////////////////////////////////////////
  public void showWindow() {
    if (m_assistantWindow != null) {
      m_workbenchWindow.getPartService().addPartListener(m_windowPartListener);
      m_assistantWindow.open();
      m_assistantWindow.getShell().addDisposeListener(new DisposeListener() {
        public void widgetDisposed(DisposeEvent e) {
          setChecked(false);
          m_assistantWindow = null;
        }
      });
    }
  }
  public void hideWindow() {
    if (m_assistantWindow != null) {
      m_workbenchWindow.getPartService().removePartListener(m_windowPartListener);
      m_assistantWindow.hide();
    }
  }
  public void closeWindow() {
    if (m_assistantWindow != null) {
      hideWindow();
      m_assistantWindow.close();
      m_assistantWindow = null;
    }
  }
  private void updateWindow() {
    if (m_assistantWindow != null) {
      // prepare selection object's
      List<Object> selectedObjects = Lists.newArrayList();
      for (EditPart editPart : m_viewer.getSelectedEditParts()) {
        selectedObjects.add(editPart.getModel());
      }
      // update assistant window
      m_assistantWindow.update(selectedObjects);
    }
  }
}