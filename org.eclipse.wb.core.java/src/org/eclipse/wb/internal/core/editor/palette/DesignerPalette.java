/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *    Daten- und Systemtechnik Aachen - Addition of icons type styling
 *******************************************************************************/
package org.eclipse.wb.internal.core.editor.palette;

import org.eclipse.wb.core.controls.palette.DesignerContainer;
import org.eclipse.wb.core.controls.palette.DesignerEntry;
import org.eclipse.wb.core.controls.palette.DesignerRoot;
import org.eclipse.wb.core.controls.palette.ICategory;
import org.eclipse.wb.core.controls.palette.IEntry;
import org.eclipse.wb.core.controls.palette.PaletteComposite;
import org.eclipse.wb.core.editor.constants.IEditorPreferenceConstants;
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
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.gef.graphical.tools.SelectionTool;
import org.eclipse.wb.internal.core.EnvironmentUtils;
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
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.gef.ui.palette.PaletteContextMenuProvider;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
	private final String ENTRYINFO_CATEGORY = "org.eclipse.wb.swing.layouts";
	private final boolean m_isMainPalette;
	private final PluginPalettePreferences m_preferences;
	private final PaletteComposite m_legacyPaletteComposite;
	private final DesignerPaletteOperations m_operations;
	private final PaletteViewer m_paletteViewer;
	private final FigureCanvas m_paletteComposite;
	private final DesignerPaletteEditDomain m_paletteDomain;
	private IEditPartViewer m_editPartViewer;
	private JavaInfo m_rootJavaInfo;
	private PaletteManager m_manager;
	private DesignerEntry m_defaultEntry;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DesignerPalette(Composite parent, int style, boolean isMainPalette) {
		m_isMainPalette = isMainPalette;
		m_operations = new DesignerPaletteOperations();
		m_preferences = new PluginPalettePreferences();
		if (EnvironmentUtils.isGefPalette()) {
			m_legacyPaletteComposite = null;
			m_paletteDomain = new DesignerPaletteEditDomain();
			m_paletteViewer = new PaletteViewer();
			m_paletteViewer.enableVerticalScrollbar(true);
			m_paletteViewer.setEditDomain(m_paletteDomain);
			m_paletteViewer.setPaletteViewerPreferences(m_preferences);
			m_paletteViewer.setContextMenu(new PaletteContextMenuProvider(m_paletteViewer));
			m_paletteViewer.setColorProvider(new DesignerColorProvider());
			m_paletteViewer.setEditPartFactory(new DesignerPaletteEditPartFactory());
			m_paletteComposite = (FigureCanvas) m_paletteViewer.createControl(parent);
			m_paletteComposite.setScrollbarsMode(SWT.NONE);
		} else {
			m_legacyPaletteComposite = new PaletteComposite(parent, SWT.NONE);
			m_paletteDomain = null;
			m_paletteViewer = null;
			m_paletteComposite = null;
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public Control getControl() {
		return m_legacyPaletteComposite != null ? m_legacyPaletteComposite : m_paletteComposite;
	}

	/**
	 * Sets information about editor.
	 */
	public void setInput(IEditPartViewer editPartViewer,
			final JavaInfo rootJavaInfo,
			String toolkitId) {
		m_editPartViewer = editPartViewer;
		m_rootJavaInfo = rootJavaInfo;
		if (m_legacyPaletteComposite == null) {
			editPartViewer.getEditDomain().setPaletteViewer(m_paletteViewer);
		}
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
				if (m_legacyPaletteComposite != null) {
					m_legacyPaletteComposite.setPreferences(m_preferences);
				}
			}
			// set site
			IPaletteSite.Helper.setSite(m_rootJavaInfo, m_paletteSite);
			// refresh palette on JavaInfo hierarchy refresh
			rootJavaInfo.addBroadcastListener(new ObjectEventListener() {
				@Override
				public void refreshed() throws Exception {
					if (m_legacyPaletteComposite != null && m_legacyPaletteComposite.isDisposed()) {
						rootJavaInfo.removeBroadcastListener(this);
						return;
					}
					if (m_paletteComposite != null && m_paletteComposite.isDisposed()) {
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
		commands_addWrite(List.of(command));
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
		@Override
		public Shell getShell() {
			return getOperations().getShell();
		}

		@Override
		public PaletteInfo getPalette() {
			return m_manager.getPalette();
		}

		@Override
		public void addCommand(Command command) {
			commands_addWrite(command);
		}

		@Override
		public void editPalette() {
			getOperations().editPalette();
		}
	};
	////////////////////////////////////////////////////////////////////////////
	//
	// Palette displaying
	//
	////////////////////////////////////////////////////////////////////////////
	private final Map<CategoryInfo, DesignerContainer> m_categoryInfoToVisual = new HashMap<>();
	private final Map<DesignerContainer, CategoryInfo> m_visualToCategoryInfo = new HashMap<>();
	private final Map<String, Boolean> m_openCategories = new HashMap<>();
	private final Set<EntryInfo> m_knownEntryInfos = new HashSet<>();
	private final Set<EntryInfo> m_goodEntryInfos = new HashSet<>();
	private final Map<EntryInfo, DesignerEntry> m_entryInfoToVisual = new HashMap<>();
	private final Map<DesignerEntry, EntryInfo> m_visualToEntryInfo = new HashMap<>();

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
	 * @return the {@link DesignerEntry} for given {@link EntryInfo}.
	 */
	private DesignerEntry getVisualEntry(final EntryInfo entryInfo) {
		DesignerEntry entry = m_entryInfoToVisual.get(entryInfo);
		if (entry == null && !m_knownEntryInfos.contains(entryInfo)) {
			m_knownEntryInfos.add(entryInfo);
			if (entryInfo.initialize(m_editPartViewer, m_rootJavaInfo)) {
				entry = new DesignerEntry(entryInfo.getName(), entryInfo.getDescription(), entryInfo.getIcon()) {
					////////////////////////////////////////////////////////////////////////////
					//
					// Access
					//
					////////////////////////////////////////////////////////////////////////////
					@Override
					public boolean isEnabled() {
						return entryInfo.isEnabled();
					}

					////////////////////////////////////////////////////////////////////////////
					//
					// Activation
					//
					////////////////////////////////////////////////////////////////////////////
					@Override
					public boolean activate(boolean reload) {
						return entryInfo.createTool(reload) != null;
					}

					@Override
					public Tool createTool() {
						if (m_paletteDomain == null) {
							return entryInfo.createTool(false);
						}
						return entryInfo.createTool(m_paletteDomain.isReload());
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
	private DesignerContainer getVisualCategory(final CategoryInfo categoryInfo) {
		DesignerContainer category = m_categoryInfoToVisual.get(categoryInfo);
		if (category == null) {
			final String categoryId = categoryInfo.getId();
			category = new DesignerContainer(categoryInfo.getName(), categoryInfo.getDescription()) {
				private boolean m_open;

				@Override
				public List<DesignerEntry> getChildren() {
					final List<EntryInfo> entryInfoList = new ArrayList<>(categoryInfo.getEntries());
					// add new EntryInfo's using broadcast
					ExecutionUtils.runIgnore(new RunnableEx() {
						@Override
						public void run() throws Exception {
							getBroadcastPalette().entries(categoryInfo, entryInfoList);
						}
					});
					// convert EntryInfo's into IEntry's
					List<DesignerEntry> entries = new ArrayList<>();
					for (EntryInfo entryInfo : entryInfoList) {
						if (entryInfo.isVisible()) {
							if (categoryId.equals(ENTRYINFO_CATEGORY)) {
								if (InstanceScope.INSTANCE.getNode(
										IEditorPreferenceConstants.P_AVAILABLE_LAYOUTS_NODE).getBoolean(
												entryInfo.getId().substring(entryInfo.getId().indexOf(' ') + 1),
												true)) {
									DesignerEntry entry = getVisualEntry(entryInfo);
									if (entry != null) {
										entries.add(entry);
									}
								}
							} else {
								DesignerEntry entry = getVisualEntry(entryInfo);
								if (entry != null) {
									entries.add(entry);
								}
							}
						}
					}
					return entries;
				}

				@Override
				public boolean isOpen() {
					return m_open;
				}

				@Override
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
		DesignerRoot palette = new DesignerRoot() {
			@Override
			public void addPopupActions(IMenuManager menuManager, Object target, int iconsType) {
				new DesignerPalettePopupActions(getOperations()).addPopupActions(
						menuManager,
						target,
						iconsType);
			}

			@Override
			public void selectDefault() {
				m_editPartViewer.getEditDomain().loadDefaultTool();
			}

			@Override
			public void moveCategory(ICategory _category, ICategory _nextCategory) {
				CategoryInfo category = m_visualToCategoryInfo.get(_category);
				CategoryInfo nextCategory = m_visualToCategoryInfo.get(_nextCategory);
				commands_addWrite(new CategoryMoveCommand(category, nextCategory));
			}

			@Override
			public void moveEntry(IEntry _entry, ICategory _targetCategory, IEntry _nextEntry) {
				EntryInfo entry = m_visualToEntryInfo.get(_entry);
				CategoryInfo category = m_visualToCategoryInfo.get(_targetCategory);
				EntryInfo nextEntry = m_visualToEntryInfo.get(_nextEntry);
				commands_addWrite(new EntryMoveCommand(entry, category, nextEntry));
			}
		};

		// check for skipping palette during tests
		if (System.getProperty(FLAG_NO_PALETTE) == null) {
			// get categories for palette model
			final List<CategoryInfo> categoryInfoList;
			{
				List<CategoryInfo> pristineCategories = m_manager.getPalette().getCategories();
				categoryInfoList = new ArrayList<>(pristineCategories);
			}
			// add new CategoryInfo's using broadcast
			ExecutionUtils.runLog(() -> {
				getBroadcastPalette().categories(categoryInfoList);
				getBroadcastPalette().categories2(categoryInfoList);
			});
			// convert CategoryInfo's into ICategory's
			for (CategoryInfo categoryInfo : categoryInfoList) {
				if (shouldBeDisplayed(categoryInfo)) {
					DesignerContainer category = getVisualCategory(categoryInfo);
					palette.add(category);
				}
			}
		}
		if (m_legacyPaletteComposite != null) {
			m_legacyPaletteComposite.setPalette(palette);
		} else {
			m_paletteViewer.setPaletteRoot(palette);
		}
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
				@Override
				public void loadDefaultTool() {
					if (m_defaultEntry != null) {
						if (m_legacyPaletteComposite != null) {
							m_legacyPaletteComposite.selectEntry(m_defaultEntry, false);
						} else {
							m_paletteViewer.setActiveTool(m_defaultEntry);
						}
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
		if (m_legacyPaletteComposite != null) {
			m_legacyPaletteComposite.refreshPalette();
		}
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
			PaletteManagerDialog dialog = new PaletteManagerDialog(m_rootJavaInfo.getEditor(),
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
				if (m_legacyPaletteComposite != null) {
					m_legacyPaletteComposite.setPreferences(m_preferences);
				}
			}
		}

		public void setIconsType(int iconsType) {
			m_preferences.setLayoutSetting(iconsType);
			if (m_legacyPaletteComposite != null) {
				m_legacyPaletteComposite.setLayoutType(iconsType);
				m_legacyPaletteComposite.setPreferences(m_preferences);
				m_legacyPaletteComposite.refreshComposite();
			}
		}

		public EntryInfo getEntry(Object target) {
			return m_visualToEntryInfo.get(target);
		}

		public CategoryInfo getCategory(Object target) {
			return m_visualToCategoryInfo.get(target);
		}

		public void addComponent(CategoryInfo category) {
			ComponentAddDialog dialog = new ComponentAddDialog(getShell(),
					m_rootJavaInfo.getEditor(),
					m_manager.getPalette(),
					category);
			if (dialog.open() == Window.OK) {
				commands_addWrite(dialog.getCommand());
			}
		}

		public void addFactory(CategoryInfo category, boolean forStatic) {
			FactoryAddDialog dialog = new FactoryAddDialog(getShell(),
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
			FactoriesAddDialog dialog = new FactoriesAddDialog(getShell(),
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
			if (targetEntry instanceof ComponentEntryInfo entryInfo) {
				dialog = new ComponentEditDialog(getShell(), editor, entryInfo);
			} else if (targetEntry instanceof StaticFactoryEntryInfo entryInfo) {
				dialog = new FactoryEditDialog(getShell(), editor, true, entryInfo);
			} else if (targetEntry instanceof InstanceFactoryEntryInfo entryInfo) {
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
