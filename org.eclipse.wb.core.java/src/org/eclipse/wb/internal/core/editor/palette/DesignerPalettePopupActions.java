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
package org.eclipse.wb.internal.core.editor.palette;

import org.eclipse.wb.core.controls.palette.IPalette;
import org.eclipse.wb.core.editor.palette.model.CategoryInfo;
import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.core.editor.palette.model.entry.ComponentEntryInfo;
import org.eclipse.wb.core.editor.palette.model.entry.ToolEntryInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.Messages;
import org.eclipse.wb.internal.core.editor.palette.DesignerPalette.DesignerPaletteOperations;
import org.eclipse.wb.internal.core.editor.palette.model.entry.FactoryEntryInfo;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import java.text.MessageFormat;

/**
 * Helper class adding popup actions into palette.
 *
 * @author mitin_aa
 * @coverage core.editor.palette.ui
 */
final class DesignerPalettePopupActions {
  // image constants
  private static final ImageDescriptor ID_ADD_CATEGORY = getImageDescription("add_category.gif");
  private static final ImageDescriptor ID_ADD_COMPONENT = getImageDescription("add_component.gif");
  private static final ImageDescriptor IMPORT_JAR_IMAGE = getImageDescription("import_jar.png");
  private static final ImageDescriptor ID_REMOVE = getImageDescription("remove.gif");
  private static final ImageDescriptor ID_MANAGER = getImageDescription("manager.gif");
  private static final ImageDescriptor ID_SETTINGS = getImageDescription("settings.png");
  private static final ImageDescriptor ID_IMPORT = getImageDescription("import.png");
  private static final ImageDescriptor ID_EXPORT = getImageDescription("export.png");
  // field
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
    if (m_operations.canEditPalette()) {
      addPopupActions_edit(menuManager, target);
    }
    //
    menuManager.add(new Separator());
    // settings
    {
      Action settingsAction =
          new Action(Messages.DesignerPalettePopupActions_settingsAction, ID_SETTINGS) {
            @Override
            public void run() {
              m_operations.editPreferences();
            }
          };
      menuManager.add(settingsAction);
    }
  }

  private void addPopupActions_edit(IMenuManager menuManager, Object target) {
    final EntryInfo targetEntry = getEntry(target);
    final CategoryInfo targetCategory = getCategory(target);
    // add category
    {
      Action addCategoryAction =
          new Action(Messages.DesignerPalettePopupActions_addCategoryAction, ID_ADD_CATEGORY) {
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
      Action addComponentAction =
          new Action(Messages.DesignerPalettePopupActions_addComponentAction, ID_ADD_COMPONENT) {
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
    // factories
    {
      // prepare parameters
      final CategoryInfo category =
          targetEntry != null ? targetEntry.getCategory() : targetCategory;
      // add static factories
      {
        IMenuManager staticMenuManager =
            new MenuManager(Messages.DesignerPalettePopupActions_addStaticFactoryAction);
        menuManager.add(staticMenuManager);
        // single
        {
          IAction action = popup_createAction_addFactory(category, true);
          staticMenuManager.add(action);
        }
        // multiple
        {
          IAction action = popup_createAction_addFactories(category, true);
          staticMenuManager.add(action);
        }
      }
      // add instance factories
      {
        IMenuManager instanceMenuManager =
            new MenuManager(Messages.DesignerPalettePopupActions_addInstanceFactoryAction);
        menuManager.add(instanceMenuManager);
        // single
        {
          IAction action = popup_createAction_addFactory(category, false);
          instanceMenuManager.add(action);
        }
        // multiple
        {
          IAction action = popup_createAction_addFactories(category, false);
          instanceMenuManager.add(action);
        }
      }
    }
    // separator
    menuManager.add(new Separator());
    // Jar
    menuManager.add(new Action(Messages.DesignerPalettePopupActions_importJarAction,
        IMPORT_JAR_IMAGE) {
      @Override
      public void run() {
        // prepare initial "next category"
        CategoryInfo nextCategory = null;
        if (targetCategory != null) {
          nextCategory = targetCategory;
        } else if (targetEntry != null) {
          nextCategory = targetEntry.getCategory();
        }
        m_operations.importJar(nextCategory);
      }
    });
    // separator
    menuManager.add(new Separator());
    // edit
    {
      Action editAction = new Action(Messages.DesignerPalettePopupActions_editAction) {
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
      editAction.setEnabled(targetCategory != null
          || targetEntry instanceof ComponentEntryInfo
          || targetEntry instanceof FactoryEntryInfo);
    }
    // remove
    {
      Action removeAction =
          new Action(Messages.DesignerPalettePopupActions_removeAction, ID_REMOVE) {
            @Override
            public void run() {
              if (targetEntry != null) {
                if (MessageDialog.openConfirm(
                    getShell(),
                    Messages.DesignerPalettePopupActions_removeEntryTitle,
                    MessageFormat.format(
                        Messages.DesignerPalettePopupActions_removeEntryMessage,
                        targetEntry.getName()))) {
                  m_operations.removeEntry(targetEntry);
                }
              } else if (targetCategory != null) {
                if (MessageDialog.openConfirm(
                    getShell(),
                    Messages.DesignerPalettePopupActions_removeCategoryTitle,
                    MessageFormat.format(
                        Messages.DesignerPalettePopupActions_removeCategoryMessage,
                        targetCategory.getName()))) {
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
      Action defaultAction =
          new Action(Messages.DesignerPalettePopupActions_restoreDefaultsAction) {
            @Override
            public void run() {
              if (MessageDialog.openConfirm(
                  getShell(),
                  Messages.DesignerPalettePopupActions_restoreDefaultsTitle,
                  Messages.DesignerPalettePopupActions_restoreDefaultsMessage)) {
                m_operations.defaultPalette();
              }
            }
          };
      menuManager.add(defaultAction);
    }
    // palette manager
    {
      Action managerAction =
          new Action(Messages.DesignerPalettePopupActions_managerAction, ID_MANAGER) {
            @Override
            public void run() {
              m_operations.editPalette();
            }
          };
      menuManager.add(managerAction);
    }
    // import/export
    addImportExport(menuManager);
  }

  /**
   * @return the {@link IAction} for adding single factory method.
   */
  private IAction popup_createAction_addFactory(final CategoryInfo category, final boolean forStatic) {
    return new Action(Messages.DesignerPalettePopupActions_factorySingleAction) {
      @Override
      public void run() {
        m_operations.addFactory(category, forStatic);
      }
    };
  }

  /**
   * @return the {@link IAction} for adding multiple factory methods.
   */
  private IAction popup_createAction_addFactories(final CategoryInfo category,
      final boolean forStatic) {
    return new Action(Messages.DesignerPalettePopupActions_factoryMultipleAction) {
      @Override
      public void run() {
        m_operations.addFactories(category, forStatic);
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Import/export
  //
  ////////////////////////////////////////////////////////////////////////////
  private void addImportExport(IMenuManager menuManager) {
    Action importAction = new Action(Messages.DesignerPalettePopupActions_importAction, ID_IMPORT) {
      @Override
      public void run() {
        importPalette();
      }
    };
    Action exportAction = new Action(Messages.DesignerPalettePopupActions_exportAction, ID_EXPORT) {
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
    fileDialog.setFilterNames(new String[]{Messages.DesignerPalettePopupActions_paletteFilterName});
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
