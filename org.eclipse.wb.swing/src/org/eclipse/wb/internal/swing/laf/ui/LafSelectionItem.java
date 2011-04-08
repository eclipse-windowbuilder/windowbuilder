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
package org.eclipse.wb.internal.swing.laf.ui;

import org.eclipse.wb.core.editor.IDesignPageSite;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.swing.Activator;
import org.eclipse.wb.internal.swing.laf.LafSupport;
import org.eclipse.wb.internal.swing.laf.command.Command;
import org.eclipse.wb.internal.swing.laf.model.CategoryInfo;
import org.eclipse.wb.internal.swing.laf.model.LafInfo;
import org.eclipse.wb.internal.swing.laf.model.SeparatorLafInfo;
import org.eclipse.wb.internal.swing.model.ModelMessages;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import org.apache.commons.lang.exception.NestableError;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.UIManager;

/**
 * LAF selection item to contribute onto Designer Toolbar.
 * 
 * @author mitin_aa
 * @coverage swing.laf.ui
 */
public final class LafSelectionItem extends ContributionItem
    implements
      LafSupport.ILookAndFeelsChangeListener {
  private ToolItem m_toolItem;
  private Menu m_menu;
  private final ComponentInfo m_componentInfo;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LafSelectionItem(ComponentInfo componentInfo) {
    m_componentInfo = componentInfo;
    LafSupport.addLookAndFeelsChangeListener(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ContributionItem
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void fill(final ToolBar parent, int index) {
    m_toolItem = new ToolItem(parent, SWT.DROP_DOWN);
    m_toolItem.setImage(Activator.getImage("info/laf/laf.png"));
    m_toolItem.setText(LafSupport.getSelectedLAF(m_componentInfo).getName());
    m_toolItem.setToolTipText(ModelMessages.LafSelectionItem_select);
    // setup drop-down menu 
    m_toolItem.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        createMenu(parent);
        // prepare location
        Point menuLocation;
        {
          Rectangle bounds = m_toolItem.getBounds();
          menuLocation = parent.toDisplay(bounds.x, bounds.y + bounds.height);
        }
        // show menu
        m_menu.setLocation(menuLocation);
        m_menu.setVisible(true);
      }
    });
  }

  @Override
  public void dispose() {
    LafSupport.removeLookAndFeelsChangeListener(this);
    disposeMenu();
    super.dispose();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Drop-down menu
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Disposes existing {@link #m_menu} drop-down.
   */
  private void disposeMenu() {
    if (m_menu != null) {
      m_menu.dispose();
      m_menu = null;
    }
  }

  /**
   * Initializes {@link #m_menu} drop-down.
   */
  private void createMenu(ToolBar parent) {
    disposeMenu();
    // create new menu
    m_menu = new Menu(parent);
    // add MRU items
    addMRUItems();
    // 
    for (final CategoryInfo categoryInfo : LafSupport.getLAFCategoriesList()) {
      if (LafSupport.isRootCategory(categoryInfo)) {
        createLAFMenuItem(m_menu, categoryInfo);
      } else {
        if (categoryInfo.isVisible()) {
          MenuItem categoryMenuItem = new MenuItem(m_menu, SWT.CASCADE);
          categoryMenuItem.setText(categoryInfo.getName());
          Menu parentMenu = new Menu(categoryMenuItem);
          categoryMenuItem.setMenu(parentMenu);
          createLAFMenuItem(parentMenu, categoryInfo);
        }
      }
    }
    // add "add LAF" item
    new MenuItem(m_menu, SWT.SEPARATOR);
    MenuItem addLafItem = new MenuItem(m_menu, SWT.PUSH);
    addLafItem.setText(ModelMessages.LafSelectionItem_addMore);
    addLafItem.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        AddCustomLookAndFeelDialog dialog =
            new AddCustomLookAndFeelDialog(DesignerPlugin.getShell(),
                LafSupport.getCategory(LafSupport.ROOT_ID));
        if (dialog.open() == Window.OK) {
          List<Command> commands = dialog.getCommands();
          for (Command command : commands) {
            command.execute();
            LafSupport.commands_add(command);
          }
          LafSupport.commands_write();
        }
      }
    });
  }

  /**
   * Adds most recently used items into drop-down LAF menu.
   */
  private void addMRUItems() {
    // sort recently used by usage count
    List<LafInfo> resultList = LafSupport.getMRULAFList();
    Collections.sort(resultList, new Comparator<LafInfo>() {
      public int compare(LafInfo o1, LafInfo o2) {
        return o2.getUsageCount() - o1.getUsageCount();
      }
    });
    // add items (no more than 3, this can be optional)
    int itemsCount = 0;
    boolean anyAdded = false;
    for (LafInfo lafInfo : resultList) {
      if (++itemsCount > 3) {
        break;
      }
      createLAFMenuItem(m_menu, lafInfo);
      anyAdded = true;
    }
    // add separator
    if (anyAdded) {
      new MenuItem(m_menu, SWT.SEPARATOR);
    }
  }

  /**
   * Traverses via LAF items in given <code>category</code> and add menu item into given parent
   * <code>menu</code>.
   */
  private void createLAFMenuItem(Menu parentMenu, CategoryInfo category) {
    boolean skippedFirstSeparator = false;
    for (final LafInfo lafInfo : category.getLAFList()) {
      if (lafInfo.isVisible()) {
        // check for separator
        if (lafInfo instanceof SeparatorLafInfo) {
          if (skippedFirstSeparator) {
            new MenuItem(parentMenu, SWT.SEPARATOR);
          }
          continue;
        }
        skippedFirstSeparator = true;
        createLAFMenuItem(parentMenu, lafInfo);
      }
    }
  }

  /**
   * Creates single menu item selecting appropriate <code>lafInfo</code> by being clicked by user.
   */
  private void createLAFMenuItem(Menu parentMenu, final LafInfo lafInfo) {
    MenuItem item = new MenuItem(parentMenu, SWT.CHECK);
    item.setText(lafInfo.getName());
    item.setSelection(LafSupport.getSelectedLAF(m_componentInfo) == lafInfo);
    // add listeners
    item.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        try {
          m_toolItem.setText(lafInfo.getName());
          LafSupport.selectLAF(m_componentInfo, lafInfo);
          m_componentInfo.getRoot().refresh();
        } catch (Throwable ex) {
          handleException(ex);
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Update
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void update() {
    LafInfo selectedLAF = LafSupport.getSelectedLAF(m_componentInfo);
    LafSupport.applySelectedLAF(selectedLAF);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // LAFSupport.ILookAndFeelsChangeListener 
  //
  ////////////////////////////////////////////////////////////////////////////
  public void lookAndFeelsListChanged() {
    LafInfo selectedLAF = LafSupport.getSelectedLAF(m_componentInfo);
    m_toolItem.setText(selectedLAF.getName());
    AstEditor activeEditor = EditorState.getActiveJavaInfo().getEditor();
    if (m_componentInfo.getEditor() == activeEditor) {
      try {
        boolean needRefresh =
            !selectedLAF.getClassName().equals(UIManager.getLookAndFeel().getClass().getName());
        update();
        // refresh if getSelectedLAF() returns another LAF (in case of deleting the currently selected LAF)
        if (needRefresh) {
          m_componentInfo.getRoot().refresh();
        }
      } catch (Throwable ex) {
        handleException(ex);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Misc
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Handles the possibly thrown {@link Throwable} during refresh.
   */
  private void handleException(Throwable ex) throws NestableError {
    IDesignPageSite site = IDesignPageSite.Helper.getSite(m_componentInfo);
    if (site != null) {
      site.handleException(ex);
    } else {
      throw new NestableError(MessageFormat.format(
          ModelMessages.LafSelectionItem_noDesignPage,
          m_componentInfo), ex);
    }
  }
}
