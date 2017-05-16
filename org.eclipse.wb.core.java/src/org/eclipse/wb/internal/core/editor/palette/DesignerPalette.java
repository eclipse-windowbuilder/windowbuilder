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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.eclipse.wb.core.controls.palette.ICategory;
import org.eclipse.wb.core.controls.palette.IEntry;
import org.eclipse.wb.core.controls.palette.IPalette;
import org.eclipse.wb.core.controls.palette.PaletteComposite;
import org.eclipse.wb.core.editor.palette.PaletteEventListener;
import org.eclipse.wb.core.editor.palette.model.CategoryInfo;
import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.core.editor.palette.model.IPaletteSite;
import org.eclipse.wb.core.editor.palette.model.PaletteInfo;
import org.eclipse.wb.core.editor.palette.model.entry.ComponentEntryInfo;
import org.eclipse.wb.core.editor.palette.model.entry.ToolEntryInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.graphical.tools.SelectionTool;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.DesignPage;
import org.eclipse.wb.internal.core.editor.palette.command.CategoryMoveCommand;
import org.eclipse.wb.internal.core.editor.palette.command.CategoryRemoveCommand;
import org.eclipse.wb.internal.core.editor.palette.command.Command;
import org.eclipse.wb.internal.core.editor.palette.command.EntryMoveCommand;
import org.eclipse.wb.internal.core.editor.palette.command.EntryRemoveCommand;
import org.eclipse.wb.internal.core.editor.palette.dialogs.AbstractPaletteElementDialog;
import org.eclipse.wb.internal.core.editor.palette.dialogs.CategoryAddDialog;
import org.eclipse.wb.internal.core.editor.palette.dialogs.CategoryEditDialog;
import org.eclipse.wb.internal.core.editor.palette.dialogs.ComponentAddDialog;
import org.eclipse.wb.internal.core.editor.palette.dialogs.ComponentEditDialog;
import org.eclipse.wb.internal.core.editor.palette.dialogs.ImportArchiveDialog;
import org.eclipse.wb.internal.core.editor.palette.dialogs.PaletteManagerDialog;
import org.eclipse.wb.internal.core.editor.palette.dialogs.PalettePreferencesDialog;
import org.eclipse.wb.internal.core.editor.palette.dialogs.factory.FactoriesAddDialog;
import org.eclipse.wb.internal.core.editor.palette.dialogs.factory.FactoryAddDialog;
import org.eclipse.wb.internal.core.editor.palette.dialogs.factory.FactoryEditDialog;
import org.eclipse.wb.internal.core.editor.palette.model.entry.IDefaultEntryInfo;
import org.eclipse.wb.internal.core.editor.palette.model.entry.InstanceFactoryEntryInfo;
import org.eclipse.wb.internal.core.editor.palette.model.entry.StaticFactoryEntryInfo;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentPresentationHelper;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.gef.core.EditDomain;
import org.eclipse.wb.internal.gef.core.IDefaultToolProvider;

import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Palette implementation for {@link DesignPage}.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette.ui
 */
public class DesignerPalette {
  public static final String FLAG_NO_PALETTE = "FLAG_NO_PALETTE"; // Don't load palette during testing
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final boolean m_isMainPalette;
  private final PluginPalettePreferences m_preferences;
  private final PaletteComposite m_paletteComposite;
  private final DesignerPaletteOperations m_operations;
  private IEditPartViewer m_editPartViewer;
  private JavaInfo m_rootJavaInfo;
  private PaletteManager m_manager;
  private IEntry m_defaultEntry;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DesignerPalette(Composite parent, int style, boolean isMainPalette) {
    m_isMainPalette = isMainPalette;
    m_operations = new DesignerPaletteOperations();
    m_preferences = new PluginPalettePreferences(DesignerPlugin.getPreferences());
    m_paletteComposite = new PaletteComposite(parent, SWT.NONE);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public Control getControl() {
    return m_paletteComposite;
  }

  /**
   * Sets information about editor.
   */
  public void setInput(IEditPartViewer editPartViewer, final JavaInfo rootJavaInfo, String toolkitId) {
    m_editPartViewer = editPartViewer;
    m_rootJavaInfo = rootJavaInfo;
    //
    if (m_rootJavaInfo != null) {
      // cancel cache pre-loading jobs possibly scheduled and/or running
      {
        IJobManager manager = Job.getJobManager();
        manager.cancel(ComponentPresentationHelper.PALETTE_PRELOAD_JOBS);
        try {
          manager.join(ComponentPresentationHelper.PALETTE_PRELOAD_JOBS, null);
        } catch (Throwable e) {
          // don't care
        }
      }
      // configure palette
      m_manager = new PaletteManager(m_rootJavaInfo, toolkitId);
      reloadPalette();
      // configure preferences
      {
        m_preferences.setPrefix(toolkitId);
        m_paletteComposite.setPreferences(m_preferences);
      }
      // set site
      IPaletteSite.Helper.setSite(m_rootJavaInfo, m_paletteSite);
      // refresh palette on JavaInfo hierarchy refresh
      rootJavaInfo.addBroadcastListener(new ObjectEventListener() {
        @Override
        public void refreshed() throws Exception {
          if (m_paletteComposite.isDisposed()) {
            rootJavaInfo.removeBroadcastListener(this);
            return;
          }
          refreshVisualPalette();
        }
      });
    }
  }

  /**
   * @return the {@link PaletteEventListener} for hierarchy.
   */
  private PaletteEventListener getBroadcastPalette() {
    return m_rootJavaInfo.getBroadcast(PaletteEventListener.class);
  }

  /**
   * Loads new base palette, applies commands and shows.
   */
  private void reloadPalette() {
    m_manager.reloadPalette();
    showPalette();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds given {@link Command} to the list and writes commands.
   */
  private void commands_addWrite(Command command) {
    commands_addWrite(ImmutableList.of(command));
  }

  /**
   * Adds given {@link Command}s to the list and writes commands.
   */
  private void commands_addWrite(List<Command> commands) {
    for (Command command : commands) {
      m_manager.commands_add(command);
    }
    m_manager.commands_write();
    refreshVisualPalette();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IPaletteSite
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IPaletteSite m_paletteSite = new IPaletteSite() {
    public Shell getShell() {
      return getOperations().getShell();
    }

    public PaletteInfo getPalette() {
      return m_manager.getPalette();
    }

    public void addCommand(Command command) {
      commands_addWrite(command);
    }

    public void editPalette() {
      getOperations().editPalette();
    }
  };
  ////////////////////////////////////////////////////////////////////////////
  //
  // Palette displaying
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Map<CategoryInfo, ICategory> m_categoryInfoToVisual = Maps.newHashMap();
  private final Map<ICategory, CategoryInfo> m_visualToCategoryInfo = Maps.newHashMap();
  private final Map<String, Boolean> m_openCategories = Maps.newHashMap();
  private final Set<EntryInfo> m_knownEntryInfos = Sets.newHashSet();
  private final Set<EntryInfo> m_goodEntryInfos = Sets.newHashSet();
  private final Map<EntryInfo, IEntry> m_entryInfoToVisual = Maps.newHashMap();
  private final Map<IEntry, EntryInfo> m_visualToEntryInfo = Maps.newHashMap();

  /**
   * Clears all caches for {@link EntryInfo}, {@link IEntry}, etc.
   */
  private void clearEntryCaches() {
    m_categoryInfoToVisual.clear();
    m_visualToCategoryInfo.clear();
    m_knownEntryInfos.clear();
    m_goodEntryInfos.clear();
    m_entryInfoToVisual.clear();
    m_visualToEntryInfo.clear();
    m_defaultEntry = null;
  }

  /**
   * @return the {@link IEntry} for given {@link EntryInfo}.
   */
  private IEntry getVisualEntry(final EntryInfo entryInfo) {
    IEntry entry = m_entryInfoToVisual.get(entryInfo);
    if (entry == null && !m_knownEntryInfos.contains(entryInfo)) {
      m_knownEntryInfos.add(entryInfo);
      if (entryInfo.initialize(m_editPartViewer, m_rootJavaInfo)) {
        entry = new IEntry() {
          ////////////////////////////////////////////////////////////////////////////
          //
          // Access
          //
          ////////////////////////////////////////////////////////////////////////////
          public boolean isEnabled() {
            return entryInfo.isEnabled();
          }

          public Image getIcon() {
            return entryInfo.getIcon();
          }

          public String getText() {
            return entryInfo.getName();
          }

          public String getToolTipText() {
            return entryInfo.getDescription();
          }

          ////////////////////////////////////////////////////////////////////////////
          //
          // Activation
          //
          ////////////////////////////////////////////////////////////////////////////
          public boolean activate(boolean reload) {
            return entryInfo.activate(reload);
          }
        };
        m_goodEntryInfos.add(entryInfo);
        m_entryInfoToVisual.put(entryInfo, entry);
        m_visualToEntryInfo.put(entry, entryInfo);
        // initialize default entry
        if (m_defaultEntry == null) {
          if (entryInfo instanceof IDefaultEntryInfo) {
            m_defaultEntry = entry;
          }
        }
      }
    }
    return entry;
  }

  /**
   * @return the {@link ICategory} for given {@link CategoryInfo}.
   */
  private ICategory getVisualCategory(final CategoryInfo categoryInfo) {
    ICategory category = m_categoryInfoToVisual.get(categoryInfo);
    if (category == null) {
      final String categoryId = categoryInfo.getId();
      category = new ICategory() {
        private boolean m_open;

        public List<IEntry> getEntries() {
          final List<EntryInfo> entryInfoList = Lists.newArrayList(categoryInfo.getEntries());
          // add new EntryInfo's using broadcast
          ExecutionUtils.runIgnore(new RunnableEx() {
            public void run() throws Exception {
              getBroadcastPalette().entries(categoryInfo, entryInfoList);
            }
          });
          // convert EntryInfo's into IEntry's
          List<IEntry> entries = Lists.newArrayList();
          for (EntryInfo entryInfo : entryInfoList) {
            if (entryInfo.isVisible()) {
              IEntry entry = getVisualEntry(entryInfo);
              if (entry != null) {
                entries.add(entry);
              }
            }
          }
          return entries;
        }

        public String getText() {
          return categoryInfo.getName();
        }

        public String getToolTipText() {
          return categoryInfo.getDescription();
        }

        public boolean isOpen() {
          return m_open;
        }

        public void setOpen(boolean open) {
          m_open = open;
          m_openCategories.put(categoryId, open);
        }
      };
      m_categoryInfoToVisual.put(categoryInfo, category);
      m_visualToCategoryInfo.put(category, categoryInfo);
      // set "open" state: from map, or default
      if (m_openCategories.containsKey(categoryId)) {
        category.setOpen(m_openCategories.get(categoryId));
      } else {
        category.setOpen(categoryInfo.isOpen());
      }
    }
    return category;
  }

  /**
   * Shows current {@link PaletteInfo}.
   */
  private void showPalette() {
    clearEntryCaches();
    // set IPalette
    IPalette palette = new IPalette() {
      public List<ICategory> getCategories() {
        // check for skipping palette during tests
        if (System.getProperty(FLAG_NO_PALETTE) != null) {
          return ImmutableList.of();
        }
        // get categories for palette model
        final List<CategoryInfo> categoryInfoList;
        {
          List<CategoryInfo> pristineCategories = m_manager.getPalette().getCategories();
          categoryInfoList = Lists.newArrayList(pristineCategories);
        }
        // add new CategoryInfo's using broadcast
        ExecutionUtils.runLog(new RunnableEx() {
          public void run() throws Exception {
            getBroadcastPalette().categories(categoryInfoList);
            getBroadcastPalette().categories2(categoryInfoList);
          }
        });
        // convert CategoryInfo's into ICategory's
        List<ICategory> categories = Lists.newArrayList();
        for (CategoryInfo categoryInfo : categoryInfoList) {
          if (shouldBeDisplayed(categoryInfo)) {
            ICategory category = getVisualCategory(categoryInfo);
            categories.add(category);
          }
        }
        return categories;
      }

      public void addPopupActions(IMenuManager menuManager, Object target) {
        new DesignerPalettePopupActions(getOperations()).addPopupActions(menuManager, target);
      }

      public void selectDefault() {
        m_editPartViewer.getEditDomain().loadDefaultTool();
      }

      public void moveCategory(ICategory _category, ICategory _nextCategory) {
        CategoryInfo category = m_visualToCategoryInfo.get(_category);
        CategoryInfo nextCategory = m_visualToCategoryInfo.get(_nextCategory);
        commands_addWrite(new CategoryMoveCommand(category, nextCategory));
      }

      public void moveEntry(IEntry _entry, ICategory _targetCategory, IEntry _nextEntry) {
        EntryInfo entry = m_visualToEntryInfo.get(_entry);
        CategoryInfo category = m_visualToCategoryInfo.get(_targetCategory);
        EntryInfo nextEntry = m_visualToEntryInfo.get(_nextEntry);
        commands_addWrite(new EntryMoveCommand(entry, category, nextEntry));
      }
    };
    m_paletteComposite.setPalette(palette);
    configure_EditDomain_DefaultTool();
  }

  private DesignerPaletteOperations getOperations() {
    return m_operations;
  }

  /**
   * @return <code>true</code> if given {@link CategoryInfo} should be displayed.
   */
  private boolean shouldBeDisplayed(CategoryInfo category) {
    if (!category.isVisible()) {
      return false;
    }
    if (category.isOptional()) {
      return !getVisualCategory(category).getEntries().isEmpty();
    }
    return true;
  }

  private void configure_EditDomain_DefaultTool() {
    if (m_isMainPalette) {
      final EditDomain editDomain = m_editPartViewer.getEditDomain();
      editDomain.setDefaultToolProvider(new IDefaultToolProvider() {
        public void loadDefaultTool() {
          if (m_defaultEntry != null) {
            m_paletteComposite.selectEntry(m_defaultEntry, false);
          } else {
            editDomain.setActiveTool(new SelectionTool());
          }
        }
      });
      editDomain.loadDefaultTool();
    }
  }

  /**
   * Refreshes visual palette.
   */
  private void refreshVisualPalette() {
    m_paletteComposite.refreshPalette();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Palette operations
  //
  ////////////////////////////////////////////////////////////////////////////
  final class DesignerPaletteOperations {
    /**
     * @return <code>true</code> if palette can be edited.
     */
    public boolean canEditPalette() {
      boolean[] canEdit = {true};
      getBroadcastPalette().canEdit(canEdit);
      return canEdit[0];
    }

    /**
     * Edits palette using {@link PaletteManagerDialog}.
     */
    public void editPalette() {
      PaletteManagerDialog dialog =
          new PaletteManagerDialog(m_rootJavaInfo.getEditor(),
              m_manager.getPalette(),
              m_goodEntryInfos);
      // reload in any case
      reloadPalette();
      // add commands used to update palette in dialog
      if (dialog.open() == Window.OK) {
        commands_addWrite(dialog.getCommands());
      }
    }

    public void defaultPalette() {
      m_manager.commands_clear();
      m_manager.commands_write();
      m_openCategories.clear();
      reloadPalette();
    }

    /**
     * Changes palette preferences.
     */
    public void editPreferences() {
      PalettePreferencesDialog dialog = new PalettePreferencesDialog(getShell(), m_preferences);
      if (dialog.open() == Window.OK) {
        dialog.commit();
        m_paletteComposite.setPreferences(m_preferences);
      }
    }

    public EntryInfo getEntry(Object target) {
      return m_visualToEntryInfo.get(target);
    }

    public CategoryInfo getCategory(Object target) {
      return m_visualToCategoryInfo.get(target);
    }

    public void addComponent(CategoryInfo category) {
      ComponentAddDialog dialog =
          new ComponentAddDialog(getShell(),
              m_rootJavaInfo.getEditor(),
              m_manager.getPalette(),
              category);
      if (dialog.open() == Window.OK) {
        commands_addWrite(dialog.getCommand());
      }
    }

    public void addFactory(CategoryInfo category, boolean forStatic) {
      FactoryAddDialog dialog =
          new FactoryAddDialog(getShell(),
              m_rootJavaInfo.getEditor(),
              forStatic,
              m_manager.getPalette(),
              category);
      if (dialog.open() == Window.OK) {
        commands_addWrite(dialog.getCommand());
      }
    }

    public void addFactories(CategoryInfo category, boolean forStatic) {
      // prepare dialog
      FactoriesAddDialog dialog =
          new FactoriesAddDialog(getShell(),
              m_rootJavaInfo.getEditor(),
              m_manager.getPalette(),
              category,
              forStatic);
      // open dialog
      if (dialog.open() == Window.OK) {
        commands_addWrite(dialog.getCommands());
      }
    }

    public void editEntry(ToolEntryInfo targetEntry) {
      AbstractPaletteElementDialog dialog = null;
      // prepare editing dialog
      AstEditor editor = m_rootJavaInfo.getEditor();
      if (targetEntry instanceof ComponentEntryInfo) {
        ComponentEntryInfo entryInfo = (ComponentEntryInfo) targetEntry;
        dialog = new ComponentEditDialog(getShell(), editor, entryInfo);
      } else if (targetEntry instanceof StaticFactoryEntryInfo) {
        StaticFactoryEntryInfo entryInfo = (StaticFactoryEntryInfo) targetEntry;
        dialog = new FactoryEditDialog(getShell(), editor, true, entryInfo);
      } else if (targetEntry instanceof InstanceFactoryEntryInfo) {
        InstanceFactoryEntryInfo entryInfo = (InstanceFactoryEntryInfo) targetEntry;
        dialog = new FactoryEditDialog(getShell(), editor, false, entryInfo);
      }
      // execute dialog
      if (dialog != null && dialog.open() == Window.OK) {
        // remove visual for component, so re-initialize them
        m_entryInfoToVisual.remove(targetEntry);
        m_knownEntryInfos.remove(targetEntry);
        m_goodEntryInfos.remove(targetEntry);
        // do updates
        commands_addWrite(dialog.getCommand());
      }
    }

    public void removeEntry(EntryInfo targetEntry) {
      commands_addWrite(new EntryRemoveCommand(targetEntry));
    }

    public void addCategory(CategoryInfo nextCategory) {
      CategoryAddDialog dialog =
          new CategoryAddDialog(getShell(), m_manager.getPalette(), nextCategory);
      if (dialog.open() == Window.OK) {
        commands_addWrite(dialog.getCommand());
      }
    }

    public void editCategory(CategoryInfo targetCategory) {
      CategoryEditDialog dialog = new CategoryEditDialog(getShell(), targetCategory);
      if (dialog.open() == Window.OK) {
        commands_addWrite(dialog.getCommand());
      }
    }

    public void removeCategory(CategoryInfo targetCategory) {
      commands_addWrite(new CategoryRemoveCommand(targetCategory));
    }

    public void importJar(CategoryInfo nextCategory) {
      ImportArchiveDialog dialog =
          new ImportArchiveDialog(getShell(), m_manager.getPalette(), nextCategory);
      if (dialog.open() == Window.OK) {
        for (Command command : dialog.getCommands()) {
          commands_addWrite(command);
        }
      }
    }

    public void importPalette(String path) throws Exception {
      m_manager.importFrom(path);
      reloadPalette();
    }

    public void exportPalette(String path) throws Exception {
      m_manager.exportTo(path);
    }

    public String getToolkitId() {
      return m_manager.getToolkitId();
    }

    public Shell getShell() {
      return m_paletteComposite.getShell();
    }
  }
}
