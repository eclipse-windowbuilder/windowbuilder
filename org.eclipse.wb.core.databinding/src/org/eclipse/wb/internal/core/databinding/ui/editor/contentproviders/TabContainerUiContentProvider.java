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
package org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.databinding.Messages;
import org.eclipse.wb.internal.core.databinding.ui.editor.ICompleteListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.utils.ui.CTabFactory;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import java.util.List;

/**
 * {@link IUiContentProvider} container as {@link CTabFolder}.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public abstract class TabContainerUiContentProvider implements IUiContentProvider {
  private final TabContainerConfiguration m_configuration;
  protected ICompleteListener m_listener;
  private CTabFolder m_folder;
  private ToolItem m_removeButton;
  private ToolItem m_upButton;
  private ToolItem m_downButton;
  private boolean m_showEmptyPage;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TabContainerUiContentProvider(TabContainerConfiguration configuration) {
    m_configuration = configuration;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Complete
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setCompleteListener(ICompleteListener listener) {
    m_listener = listener;
  }

  public String getErrorMessage() {
    // no errors
    if (m_showEmptyPage) {
      return null;
    }
    // find first with error
    int count = m_folder.getItemCount();
    for (int i = 0; i < count; i++) {
      IUiContentProvider contentProvider = (IUiContentProvider) m_folder.getItem(i).getData();
      String errorMessage = contentProvider.getErrorMessage();
      if (errorMessage != null) {
        return errorMessage;
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  public int getNumberOfControls() {
    return 1;
  }

  public void createContent(Composite parent, int columns) {
    // create folder
    m_folder = new CTabFolder(parent, SWT.BORDER);
    GridDataFactory.create(m_folder).fill().grab().spanH(columns);
    m_folder.setUnselectedCloseVisible(false);
    // folder events
    m_folder.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        calculateButtonsState();
      }
    });
    m_folder.addCTabFolder2Listener(new CTabFolder2Adapter() {
      @Override
      public void close(CTabFolderEvent event) {
        deletePage((CTabItem) event.item);
      }
    });
    // configure
    if (m_configuration.isUseAddButton()
        || m_configuration.isUseMultiAddButton()
        || m_configuration.isUseRemoveButton()
        || m_configuration.isUseUpDownButtons()) {
      Composite buttonsComposite = new Composite(m_folder, SWT.NONE);
      GridLayoutFactory.create(buttonsComposite).noMargins();
      //
      ToolBar buttonsBar = new ToolBar(buttonsComposite, SWT.FLAT | SWT.RIGHT);
      GridDataFactory.create(buttonsBar).fillH().grabH();
      // add
      if (m_configuration.isUseAddButton()) {
        ToolItem addButton = new ToolItem(buttonsBar, SWT.NONE);
        addButton.setText(Messages.TabContainerUiContentProvider_addButton);
        addButton.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent event) {
            addPage();
          }
        });
      }
      // multi add
      if (m_configuration.isUseMultiAddButton()) {
        final ToolItem addButton = new ToolItem(buttonsBar, SWT.DROP_DOWN);
        addButton.setText(Messages.TabContainerUiContentProvider_addItem);
        //
        final MenuManager menuManager = new MenuManager();
        menuManager.createContextMenu(buttonsBar);
        //
        addButton.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent event) {
            menuManager.removeAll();
            //
            int insertIndex = m_folder.getSelectionIndex();
            if (insertIndex != -1) {
              insertIndex++;
            }
            try {
              chooseAddPage(menuManager, insertIndex);
            } catch (Throwable e) {
              DesignerPlugin.log(e);
            }
            //
            menuManager.update(true);
            //
            Rectangle bounds = addButton.getBounds();
            Point location =
                addButton.getParent().toDisplay(new Point(bounds.x, bounds.y + bounds.height));
            menuManager.getMenu().setLocation(location.x, location.y);
            // show context menu
            menuManager.getMenu().setVisible(true);
          }
        });
      }
      // remove
      if (m_configuration.isUseRemoveButton()) {
        m_removeButton = new ToolItem(buttonsBar, SWT.NONE);
        m_removeButton.setText(Messages.TabContainerUiContentProvider_removeButton);
        m_removeButton.setEnabled(false);
        m_removeButton.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            int index = m_folder.getSelectionIndex();
            if (index != -1) {
              deletePage(m_folder.getItem(index));
            }
          }
        });
      }
      // up/down
      if (m_configuration.isUseUpDownButtons()) {
        new ToolItem(buttonsBar, SWT.SEPARATOR);
        // up
        m_upButton = new ToolItem(buttonsBar, SWT.NONE);
        m_upButton.setText("<<");
        m_upButton.setEnabled(false);
        m_upButton.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            movePage(-1);
          }
        });
        // down
        m_downButton = new ToolItem(buttonsBar, SWT.NONE);
        m_downButton.setText(">>");
        m_downButton.setEnabled(false);
        m_downButton.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            movePage(1);
          }
        });
      }
      //
      m_folder.setTopRight(buttonsComposite);
    }
  }

  private void calculateButtonsState() {
    int itemCount = m_folder.getItemCount();
    int selectionIndex = m_folder.getSelectionIndex();
    if (selectionIndex == -1 || itemCount == 1 && m_showEmptyPage) {
      if (m_removeButton != null) {
        m_removeButton.setEnabled(false);
      }
      if (m_upButton != null) {
        m_upButton.setEnabled(false);
        m_downButton.setEnabled(false);
      }
      return;
    }
    if (m_removeButton != null) {
      m_removeButton.setEnabled(true);
    }
    if (m_upButton != null) {
      m_upButton.setEnabled(selectionIndex > 0);
      m_downButton.setEnabled(selectionIndex < itemCount - 1);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Add
  //
  ////////////////////////////////////////////////////////////////////////////
  protected void chooseAddPage(MenuManager menuManager, int insertIndex) throws Exception {
    throw new UnsupportedOperationException();
  }

  private void addPage() {
    try {
      int insertIndex = m_folder.getSelectionIndex();
      if (insertIndex != -1) {
        insertIndex++;
      }
      createPage(insertIndex, createNewPageContentProvider(), true);
      configure();
      m_listener.calculateFinish();
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
  }

  protected final void createPage(int insertIndex,
      IUiContentProvider contentProvider,
      boolean selection) throws Exception {
    int itemCount = m_folder.getItemCount();
    insertIndex = insertIndex == -1 ? itemCount : insertIndex;
    if (itemCount == 1 && m_showEmptyPage) {
      deleteTabItem(m_folder.getItem(0));
      m_showEmptyPage = false;
      insertIndex = 0;
    }
    //
    Composite pageComposite = new Composite(m_folder, SWT.NONE);
    GridLayoutFactory.create(pageComposite).columns(contentProvider.getNumberOfControls()).margins(
        3);
    contentProvider.createContent(pageComposite, contentProvider.getNumberOfControls());
    //
    CTabItem tabItem = new CTabItem(m_folder, SWT.CLOSE, insertIndex);
    tabItem.setControl(pageComposite);
    tabItem.setData(contentProvider);
    //
    contentProvider.setCompleteListener(m_listener);
    contentProvider.updateFromObject();
    //
    if (selection) {
      m_folder.setSelection(tabItem);
      calculateButtonsState();
    }
  }

  protected final void configure() {
    int count = m_folder.getItemCount();
    for (int i = 0; i < count; i++) {
      CTabItem tabItem = m_folder.getItem(i);
      IUiContentProvider contentProvider = (IUiContentProvider) tabItem.getData();
      configute(tabItem, i, contentProvider);
    }
  }

  /**
   * Create new {@link IUiContentProvider} editor.
   */
  protected abstract IUiContentProvider createNewPageContentProvider() throws Exception;

  /**
   * Configure {@link CTabItem} for given {@link IUiContentProvider} after create, update, delete or
   * reorder.
   */
  protected abstract void configute(CTabItem tabItem, int index, IUiContentProvider provider);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Remove
  //
  ////////////////////////////////////////////////////////////////////////////
  private void deletePage(CTabItem item) {
    try {
      postDelete((IUiContentProvider) item.getData());
      deleteTabItem(item);
      postDelete();
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
  }

  protected final void deleteTabItem(CTabItem tabItem) {
    Control control = tabItem.getControl();
    tabItem.dispose();
    control.dispose();
  }

  protected void postDelete(IUiContentProvider provider) throws Exception {
  }

  protected void postDelete() throws Exception {
    if (m_folder.getItemCount() == 0 && m_configuration.isCreateEmptyPage()) {
      createEmptyPage();
    } else {
      configure();
    }
    m_listener.calculateFinish();
  }

  private void createEmptyPage() {
    m_showEmptyPage = true;
    Label messageLabel = new Label(m_folder, SWT.WRAP);
    messageLabel.setText(m_configuration.getEmptyPageMessage());
    CTabFactory.item(m_folder).control(messageLabel).name(m_configuration.getEmptyPageTitle());
    m_folder.setSelection(0);
    calculateButtonsState();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Move
  //
  ////////////////////////////////////////////////////////////////////////////
  private void movePage(int delta) {
    int index = m_folder.getSelectionIndex();
    if (index == -1) {
      return;
    }
    //
    CTabItem tabItem = m_folder.getItem(index);
    Control tabControl = tabItem.getControl();
    String tabName = tabItem.getText();
    Object tabData = tabItem.getData();
    //
    tabItem.dispose();
    //
    tabItem = new CTabItem(m_folder, SWT.CLOSE, index + delta);
    tabItem.setControl(tabControl);
    tabItem.setText(tabName);
    tabItem.setData(tabData);
    //
    configure();
    //
    m_folder.setSelection(tabItem);
    calculateButtonsState();
    m_listener.calculateFinish();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Update
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final void updateFromObject(List<IUiContentProvider> providers) throws Exception {
    if (providers.isEmpty() && m_configuration.isCreateEmptyPage()) {
      createEmptyPage();
    } else {
      boolean selection = true;
      for (IUiContentProvider contentProvider : providers) {
        createPage(-1, contentProvider, selection);
        selection = false;
      }
      configure();
    }
    m_listener.calculateFinish();
  }

  public void saveToObject() throws Exception {
    List<IUiContentProvider> providers = Lists.newArrayList();
    if (!m_showEmptyPage) {
      int count = m_folder.getItemCount();
      for (int i = 0; i < count; i++) {
        IUiContentProvider provider = (IUiContentProvider) m_folder.getItem(i).getData();
        providers.add(provider);
        provider.saveToObject();
      }
    }
    saveToObject(providers);
  }

  protected final CTabItem providerToItem(IUiContentProvider provider) {
    int count = m_folder.getItemCount();
    for (int i = 0; i < count; i++) {
      CTabItem item = m_folder.getItem(i);
      if (provider == item.getData()) {
        return item;
      }
    }
    return null;
  }

  protected final List<IUiContentProvider> getProviders() {
    List<IUiContentProvider> providers = Lists.newArrayList();
    if (!m_showEmptyPage) {
      int count = m_folder.getItemCount();
      for (int i = 0; i < count; i++) {
        IUiContentProvider provider = (IUiContentProvider) m_folder.getItem(i).getData();
        providers.add(provider);
      }
    }
    return providers;
  }

  /**
   * Invoke for save changes of edit objects.
   */
  protected abstract void saveToObject(List<IUiContentProvider> providers) throws Exception;
}