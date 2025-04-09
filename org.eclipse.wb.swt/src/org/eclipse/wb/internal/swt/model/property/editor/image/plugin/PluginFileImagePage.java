/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swt.model.property.editor.image.plugin;

import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.AbstractImageDialog;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.AbstractImagePage;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.AbstractBrowseImagePage;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IImageContainer;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IImageElement;
import org.eclipse.wb.internal.swt.Activator;
import org.eclipse.wb.internal.swt.model.ModelMessages;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.dialogs.SearchPattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Implementation of {@link AbstractImagePage} that selects image as plugin resource.
 *
 * @author lobas_av
 * @coverage swt.property.editor.plugin
 */
public class PluginFileImagePage extends AbstractBrowseImagePage {
	public static final String ID = "PLUGIN";
	private final FilterConfigurer m_filterConfigurer;
	private final PaletteTreeFilter m_filter = new PaletteTreeFilter();
	private boolean m_enabledFilter;
	private final Text m_filterText;
	private final ToolItem m_clearButton;
	private final ToolItem m_wButton;
	private final ToolItem m_rButton;
	private final ToolItem m_uiButton;
	private final ToolItem m_allButton;
	private final PluginImagesRoot m_root;
	protected Timer m_uiFilterTimer;

	////////////////////////////////////////////////////////////////////////////
	//
	// Creation
	//
	////////////////////////////////////////////////////////////////////////////
	public static AbstractBrowseImagePage createPage(Composite parent,
			int style,
			AbstractImageDialog imageDialog,
			IProject project) {
		Composite pageComposite = new Composite(parent, style);
		GridLayoutFactory.create(pageComposite);
		// create filter bar
		Composite filterBarComposite = new Composite(pageComposite, SWT.NONE);
		GridDataFactory.create(filterBarComposite).fillH().grabH();
		// create page
		FilterConfigurer filterConfigurer = new FilterConfigurer(true, true, true, false);
		return new PluginFileImagePage(pageComposite,
				SWT.NONE,
				imageDialog,
				project,
				filterBarComposite,
				filterConfigurer,
				new PluginImagesRoot(project, filterConfigurer));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	private PluginFileImagePage(Composite parent,
			int style,
			AbstractImageDialog imageDialog,
			IProject project,
			Composite filterBarComposite,
			FilterConfigurer filterConfigurer,
			PluginImagesRoot root) {
		super(parent, style, imageDialog, root);
		m_root = root;
		GridLayoutFactory.modify(this).noMargins();
		GridDataFactory.create(this).fill().grab();
		m_filterConfigurer = filterConfigurer;
		addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				stopUIFilterTimer();
			}
		});
		//
		GridLayoutFactory.create(filterBarComposite).columns(3).noMargins();
		// filter text
		m_filterText = new Text(filterBarComposite, SWT.SINGLE | SWT.BORDER | SWT.SEARCH);
		GridDataFactory.create(m_filterText).fillH().grabH();
		m_filterText.setText(ModelMessages.PluginFileImagePage_emptyFilterText);
		m_filterText.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				m_filterText.selectAll();
			}
		});
		m_filterText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				stopUIFilterTimer();
				m_uiFilterTimer = new Timer();
				m_uiFilterTimer.schedule(new TimerTask() {
					@Override
					public void run() {
						Display.getDefault().syncExec(new Runnable() {
							@Override
							public void run() {
								handleModifyFilter();
							}
						});
						m_uiFilterTimer = null;
					}
				}, 600);
			}
		});
		// filter text toolbar
		ToolBar filterTextToolBar = new ToolBar(filterBarComposite, SWT.RIGHT | SWT.FLAT);
		// clear button
		m_clearButton = new ToolItem(filterTextToolBar, SWT.PUSH);
		m_clearButton.setImage(Activator.getImage("clear_new.gif"));
		m_clearButton.setToolTipText(ModelMessages.PluginFileImagePage_clearButton);
		m_clearButton.setEnabled(false);
		m_clearButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				m_filterText.setText("");
			}
		});
		// filter toolbar
		ToolBar filterToolBar = new ToolBar(filterBarComposite, SWT.RIGHT | SWT.FLAT);
		GridDataFactory.create(filterToolBar).alignHR();
		// workspace button
		m_wButton = new ToolItem(filterToolBar, SWT.CHECK);
		m_wButton.setImage(Activator.getImage("workspace_projects.png"));
		m_wButton.setToolTipText(ModelMessages.PluginFileImagePage_showWorkspacePluginsButton);
		m_wButton.setSelection(true);
		m_wButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				m_filterConfigurer.showWorkspacePlugins(m_wButton.getSelection());
				if (m_filterConfigurer.isDirty()) {
					refresh();
				}
			}
		});
		// required button
		m_rButton = new ToolItem(filterToolBar, SWT.CHECK);
		m_rButton.setImage(Activator.getImage("required_projects.png"));
		m_rButton.setToolTipText(ModelMessages.PluginFileImagePage_showRequiredPluginsButton);
		m_rButton.setSelection(true);
		m_rButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				m_filterConfigurer.showRequiredPlugins(m_rButton.getSelection());
				if (m_filterConfigurer.isDirty()) {
					refresh();
				}
			}
		});
		// separator
		new ToolItem(filterToolBar, SWT.SEPARATOR);
		// UI button
		m_uiButton = new ToolItem(filterToolBar, SWT.RADIO);
		m_uiButton.setImage(Activator.getImage("ui_projects.png"));
		m_uiButton.setToolTipText(ModelMessages.PluginFileImagePage_showUiPluginsButton);
		m_uiButton.setSelection(true);
		m_uiButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				m_filterConfigurer.showUIPlugins(m_uiButton.getSelection());
				if (m_filterConfigurer.isDirty()) {
					refresh();
				}
			}
		});
		// "all" button
		m_allButton = new ToolItem(filterToolBar, SWT.RADIO);
		m_allButton.setImage(Activator.getImage("all_projects.png"));
		m_allButton.setToolTipText(ModelMessages.PluginFileImagePage_showAllPluginsButton);
		m_allButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				m_filterConfigurer.showAllPlugins(m_allButton.getSelection());
				if (m_filterConfigurer.isDirty()) {
					refresh();
				}
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// GUI
	//
	////////////////////////////////////////////////////////////////////////////
	protected final void handleModifyFilter() {
		String filterText = m_filterText.getText();
		boolean canEmptyFilterText = filterText.length() == 0;
		m_clearButton.setEnabled(!canEmptyFilterText);
		//
		if (m_enabledFilter) {
			if (canEmptyFilterText) {
				m_enabledFilter = false;
				resetFilter();
			} else {
				refreshFilter(filterText, false);
			}
		} else if (!canEmptyFilterText) {
			m_enabledFilter = true;
			refreshFilter(filterText, true);
		}
	}

	private void resetFilter() {
		TreeViewer viewer = getViewer();
		try {
			viewer.getControl().setRedraw(false);
			viewer.resetFilters();
			viewer.collapseAll();
		} finally {
			viewer.getControl().setRedraw(true);
		}
	}

	private void refreshFilter(String filterText, boolean canAddFilter) {
		TreeViewer viewer = getViewer();
		try {
			viewer.getControl().setRedraw(false);
			m_filter.setPattern(filterText);
			if (canAddFilter) {
				viewer.addFilter(m_filter);
			} else {
				viewer.refresh();
			}
			viewer.expandAll();
		} finally {
			viewer.getControl().setRedraw(true);
		}
	}

	protected final void stopUIFilterTimer() {
		if (m_uiFilterTimer != null) {
			m_uiFilterTimer.cancel();
			m_uiFilterTimer = null;
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// AbstractImagePage
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getTitle() {
		return ModelMessages.PluginFileImagePage_title;
	}

	@Override
	protected Control getPageControl() {
		return getParent();
	}

	@Override
	public void init(Object data) {
		m_root.init(data);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Filter
	//
	////////////////////////////////////////////////////////////////////////////
	private static class PaletteTreeFilter extends ViewerFilter {
		private SearchPattern m_pattern;

		public void setPattern(String pattern) {
			if (pattern.indexOf('*') == -1) {
				pattern += '*';
			}
			m_pattern = new SearchPattern();
			m_pattern.setPattern(pattern);
		}

		@Override
		public boolean select(Viewer viewer, Object parent, Object element) {
			IImageElement imageElement = (IImageElement) element;
			if (m_pattern.matches(imageElement.getName())) {
				return true;
			}
			if (element instanceof IImageContainer container) {
				IImageElement[] elements = container.elements();
				List<IImageContainer> containers = new ArrayList<>();
				// check only children
				for (IImageElement subElement : elements) {
					if (m_pattern.matches(subElement.getName())) {
						return true;
					}
					if (subElement instanceof IImageContainer) {
						containers.add((IImageContainer) subElement);
					}
				}
				// check sub children
				for (IImageContainer subContainer : containers) {
					if (select(viewer, element, subContainer)) {
						return true;
					}
				}
			}
			return false;
		}
	}
}