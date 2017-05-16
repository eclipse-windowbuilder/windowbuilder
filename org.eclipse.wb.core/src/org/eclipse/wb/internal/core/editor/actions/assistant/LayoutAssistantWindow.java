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

import org.eclipse.wb.core.editor.actions.assistant.ILayoutAssistantPage;
import org.eclipse.wb.core.editor.actions.assistant.LayoutAssistantListener;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.Messages;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.TabFactory;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import java.util.List;

/**
 * Floating window with pages for convenient editing layout related properties.
 *
 * @author lobas_av
 * @coverage core.editor.action.assistant
 */
public class LayoutAssistantWindow extends Window {
  private final ShellLocationTracker m_locationTracker;
  private TabFolder m_tabContainer;
  private List<ILayoutAssistantPage> m_pages;
  private List<ObjectInfo> m_selectedComponents;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LayoutAssistantWindow(Shell parentShell) {
    super(parentShell);
    setShellStyle(SWT.CLOSE | SWT.BORDER | SWT.TITLE | SWT.TOOL);
    m_locationTracker =
        new ShellLocationTracker(DesignerPlugin.getDefault().getDialogSettings(),
            getClass().getName());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Hide the shell, if active.
   */
  public void hide() {
    Shell shell = getShell();
    if (shell != null) {
      shell.setVisible(false);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(Messages.LayoutAssistantWindow_title);
    m_locationTracker.setShell(newShell);
  }

  @Override
  protected Control createContents(Composite parent) {
    parent.setLayout(new FillLayout());
    m_tabContainer = new TabFolder(parent, SWT.NONE);
    return m_tabContainer;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Location
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Point getInitialLocation(Point initialSize) {
    return m_locationTracker.getInitialLocation(initialSize);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Update
  //
  ////////////////////////////////////////////////////////////////////////////
  public void update(List<Object> selectedObjects) {
    // check equals to old selection
    if (m_selectedComponents != null && m_selectedComponents.equals(selectedObjects)) {
      // prepare page valid status
      boolean isValidPages = true;
      for (ILayoutAssistantPage page : m_pages) {
        if (!page.isPageValid()) {
          isValidPages = false;
          break;
        }
      }
      // check status: create or update
      if (isValidPages) {
        updatePages();
        return;
      }
    }
    try {
      getShell().setRedraw(false);
      getShell().setEnabled(false);
      // remember title of previously selected page
      String selectedPageTitle = null;
      {
        int index = m_tabContainer.getSelectionIndex();
        if (index > 0) {
          selectedPageTitle = m_tabContainer.getItem(index).getText();
        }
      }
      // dispose old pages
      for (TabItem tabItem : m_tabContainer.getItems()) {
        tabItem.getControl().dispose();
        tabItem.dispose();
      }
      m_pages = Lists.newArrayList();
      // check selection
      {
        // check selection and parent
        m_selectedComponents = Lists.newArrayList();
        {
          ObjectInfo parent = null;
          for (Object object : selectedObjects) {
            if (object instanceof ObjectInfo) {
              ObjectInfo component = (ObjectInfo) object;
              m_selectedComponents.add(component);
              if (parent == null) {
                parent = component.getParent();
              } else if (parent != component.getParent()) {
                showMessage(Messages.LayoutAssistantWindow_singleParent);
                return;
              }
            } else {
              showMessage(Messages.LayoutAssistantWindow_notObjectSelected);
              return;
            }
          }
        }
        // check empty
        if (m_selectedComponents.isEmpty()) {
          showMessage(Messages.LayoutAssistantWindow_emptySelection);
          return;
        }
      }
      // create pages
      ExecutionUtils.runLog(new RunnableEx() {
        public void run() throws Exception {
          ObjectInfo component = m_selectedComponents.get(0);
          LayoutAssistantListener listener = component.getBroadcast(LayoutAssistantListener.class);
          listener.createAssistantPages(m_selectedComponents, m_tabContainer, m_pages);
        }
      });
      // update pages
      if (m_pages.isEmpty()) {
        showMessage(Messages.LayoutAssistantWindow_noAssistanceForSelection);
      } else {
        updatePages();
        // restore page selection
        if (selectedPageTitle != null) {
          for (TabItem item : m_tabContainer.getItems()) {
            if (selectedPageTitle.equals(item.getText())) {
              m_tabContainer.setSelection(item);
              break;
            }
          }
        }
      }
    } finally {
      getShell().setEnabled(true);
      getShell().setRedraw(true);
      getShell().layout();
      getShell().pack();
    }
  }

  private void updatePages() {
    for (ILayoutAssistantPage page : m_pages) {
      page.updatePage();
    }
  }

  private void showMessage(String message) {
    Composite composite = new Composite(m_tabContainer, SWT.NONE);
    GridLayoutFactory.create(composite).margins(15);
    //
    Label label = new Label(composite, SWT.CENTER);
    GridDataFactory.create(label).grab().alignHC().alignVM();
    label.setText(message);
    //
    TabFactory.item(m_tabContainer).text(Messages.LayoutAssistantWindow_infoTab).control(composite);
  }
}