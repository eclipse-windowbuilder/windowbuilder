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
package org.eclipse.wb.internal.core.xml.editor.palette;

import org.eclipse.wb.core.controls.palette.IPalette;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.xml.editor.palette.DesignerPalette.DesignerPaletteOperations;
import org.eclipse.wb.internal.core.xml.editor.palette.model.CategoryInfo;
import org.eclipse.wb.internal.core.xml.editor.palette.model.ComponentEntryInfo;
import org.eclipse.wb.internal.core.xml.editor.palette.model.EntryInfo;
import org.eclipse.wb.internal.core.xml.editor.palette.model.ToolEntryInfo;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Helper class adding popup actions into palette.
 * 
 * @author mitin_aa
 * @coverage XML.editor.palette.ui
 */
final class DesignerPalettePopupActions {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constants
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final ImageDescriptor ID_ADD_CATEGORY = getImageDescription("add_category.gif");
  private static final ImageDescriptor ID_ADD_COMPONENT = getImageDescription("add_component.gif");
  private static final ImageDescriptor ID_REMOVE = getImageDescription("remove.gif");
  private static final ImageDescriptor ID_MANAGER = getImageDescription("manager.gif");
  private static final ImageDescriptor ID_SETTINGS = getImageDescription("settings.png");
  private static final ImageDescriptor ID_IMPORT = getImageDescription("import.png");
  private static final ImageDescriptor ID_EXPORT = getImageDescription("export.png");
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final DesignerPaletteOperations m_operations;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DesignerPalettePopupActions(DesignerPaletteOperations operations) {
    m_operations = operations;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Popup
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link ImageDescriptor} for palette image.
   */
  private static ImageDescriptor getImageDescription(String path) {
    return DesignerPlugin.getImageDescriptor("palette/" + path);
  }

  /**
   * @see IPalette#addPopupActions(IMenuManager, Object)
   */
  void addPopupActions(IMenuManager menuManager, Object target) {
    final EntryInfo targetEntry = getEntry(target);
    final CategoryInfo targetCategory = getCategory(target);
    // add category
    {
      Action addCategoryAction = new Action("Add category...", ID_ADD_CATEGORY) {
        @Override
        public void run() {
          // prepare initial "next category"
          CategoryInfo nextCategory = null;
          if (targetCategory != null) {
            nextCategory = targetCategory;
          } else if (targetEntry != null) {
            nextCategory = targetEntry.getCategory();
          }
          m_operations.addCategory(nextCategory);
        }
      };
      menuManager.add(addCategoryAction);
    }
    // add component
    {
      Action addComponentAction = new Action("Add component...", ID_ADD_COMPONENT) {
        @Override
        public void run() {
          // prepare category for new component
          CategoryInfo category = targetCategory;
          if (targetEntry != null) {
            category = targetEntry.getCategory();
          }
          m_operations.addComponent(category);
        }
      };
      menuManager.add(addComponentAction);
    }
    // separator
    menuManager.add(new Separator());
    // edit
    {
      Action editAction = new Action("Edit...") {
        @Override
        public void run() {
          if (targetCategory != null) {
            m_operations.editCategory(targetCategory);
          } else if (targetEntry instanceof ToolEntryInfo) {
            m_operations.editEntry((ToolEntryInfo) targetEntry);
          }
        }
      };
      menuManager.add(editAction);
      editAction.setEnabled(targetCategory != null || targetEntry instanceof ComponentEntryInfo);
    }
    // remove
    {
      Action removeAction = new Action("Remove", ID_REMOVE) {
        @Override
        public void run() {
          if (targetEntry != null) {
            if (MessageDialog.openConfirm(
                getShell(),
                "Confirm",
                "Are you sure you want to remove entry '" + targetEntry.getName() + "'?")) {
              m_operations.removeEntry(targetEntry);
            }
          } else if (targetCategory != null) {
            if (MessageDialog.openConfirm(
                getShell(),
                "Confirm",
                "Are you sure you want to remove category '" + targetCategory.getName() + "'?")) {
              m_operations.removeCategory(targetCategory);
            }
          }
        }
      };
      menuManager.add(removeAction);
      removeAction.setEnabled(targetEntry != null || targetCategory != null);
    }
    // separator
    menuManager.add(new Separator());
    // default
    {
      Action defaultAction = new Action("Restore default palette...") {
        @Override
        public void run() {
          if (MessageDialog.openConfirm(
              getShell(),
              "Confirm",
              "Are you sure you want to set the default palette?")) {
            m_operations.defaultPalette();
          }
        }
      };
      menuManager.add(defaultAction);
    }
    // palette manager
    {
      Action managerAction = new Action("Palette manager...", ID_MANAGER) {
        @Override
        public void run() {
          m_operations.editPalette();
        }
      };
      menuManager.add(managerAction);
    }
    // import/export
    addImportExport(menuManager);
    //
    menuManager.add(new Separator());
    // settings
    {
      Action settingsAction = new Action("Settings...", ID_SETTINGS) {
        @Override
        public void run() {
          m_operations.editPreferences();
        }
      };
      menuManager.add(settingsAction);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Import/export
  //
  ////////////////////////////////////////////////////////////////////////////
  private void addImportExport(IMenuManager menuManager) {
    Action importAction = new Action("Import palette...", ID_IMPORT) {
      @Override
      public void run() {
        importPalette();
      }
    };
    Action exportAction = new Action("Export palette...", ID_EXPORT) {
      @Override
      public void run() {
        exportPalette();
      }
    };
    menuManager.add(importAction);
    menuManager.add(exportAction);
  }

  private void exportPalette() {
    final String path = getImportExportPath(SWT.SAVE);
    if (path != null) {
      ExecutionUtils.runLog(new RunnableEx() {
        public void run() throws Exception {
          m_operations.exportPalette(path);
        }
      });
    }
  }

  private void importPalette() {
    final String path = getImportExportPath(SWT.OPEN);
    if (path != null) {
      ExecutionUtils.runLog(new RunnableEx() {
        public void run() throws Exception {
          m_operations.importPalette(path);
        }
      });
    }
  }

  private String getImportExportPath(int style) {
    FileDialog fileDialog = new FileDialog(getShell(), style);
    fileDialog.setFilterExtensions(new String[]{"*.xml"});
    fileDialog.setFilterNames(new String[]{"XML file with palette commands"});
    fileDialog.setFileName(m_operations.getToolkitId() + ".xml");
    return fileDialog.open();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Misc
  //
  ////////////////////////////////////////////////////////////////////////////
  private Shell getShell() {
    return m_operations.getShell();
  }

  private CategoryInfo getCategory(Object target) {
    return m_operations.getCategory(target);
  }

  private EntryInfo getEntry(Object target) {
    return m_operations.getEntry(target);
  }
}
