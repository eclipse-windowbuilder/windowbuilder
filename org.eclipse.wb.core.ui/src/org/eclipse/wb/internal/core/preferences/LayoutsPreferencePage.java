/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *    Marcel du Preez - Added table to select available layouts
 *                    - Override PerformOk to save preferences
 *******************************************************************************/
package org.eclipse.wb.internal.core.preferences;

import org.eclipse.wb.core.editor.constants.IEditorPreferenceConstants;
import org.eclipse.wb.internal.core.UiMessages;
import org.eclipse.wb.internal.core.model.description.LayoutDescription;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.description.helpers.LayoutDescriptionHelper;
import org.eclipse.wb.internal.core.preferences.bind.AbstractBindingPreferencesPage;
import org.eclipse.wb.internal.core.utils.binding.DataBindManager;
import org.eclipse.wb.internal.core.utils.binding.IDataEditor;
import org.eclipse.wb.internal.core.utils.binding.providers.StringPreferenceProvider;
import org.eclipse.wb.internal.core.utils.ui.AbstractBindingComposite;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.prefs.BackingStoreException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Main {@link PreferencePage} for Layout Support.
 *
 * @author scheglov_ke
 * @coverage core.preferences.ui
 */
public abstract class LayoutsPreferencePage extends AbstractBindingPreferencesPage {
	private final IEclipsePreferences m_layoutPreferences;
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public LayoutsPreferencePage(ToolkitDescription toolkit) {
		super(toolkit);
		m_layoutPreferences = InstanceScope.INSTANCE.getNode(IEditorPreferenceConstants.P_AVAILABLE_LAYOUTS_NODE);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// GUI
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected AbstractBindingComposite createBindingComposite(Composite parent) {
		return new ContentsComposite(parent, m_bindManager, m_preferences);
	}

	@Override
	public boolean performOk() {
		try {
			m_layoutPreferences.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
		return super.performOk();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Contents
	//
	////////////////////////////////////////////////////////////////////////////
	protected class ContentsComposite extends AbstractBindingComposite {
		private final CheckboxTableViewer m_table;

		public ContentsComposite(Composite parent,
				DataBindManager bindManager,
				IPreferenceStore preferences) {
			super(parent, bindManager, preferences);
			int gridLayoutColumns = 2;
			GridLayoutFactory.create(this).noMargins().columns(gridLayoutColumns);
			// default layout
			{
				new Label(this, SWT.NONE).setText(UiMessages.LayoutsPreferencePage_defaultLayout);
				final ISelection implicitLayoutSelection = new StructuredSelection(
						UiMessages.LayoutsPreferencePage_implicitLayout);
				final ComboViewer layoutCombo = new ComboViewer(this, SWT.READ_ONLY);
				layoutCombo.setContentProvider(ArrayContentProvider.getInstance());
				layoutCombo.setLabelProvider(ColumnLabelProvider.createTextProvider(o -> {
					if (o instanceof LayoutDescription layout) {
						return layout.getName();
					}
					return (String) o;
				}));
				GridDataFactory.create(layoutCombo.getCombo()).grabH().fillH();
				UiUtils.setVisibleItemCount(layoutCombo.getCombo(), 15);
				// prepare layouts
				final List<LayoutDescription> layouts = LayoutDescriptionHelper.get(m_toolkit);
				Collections.sort(layouts, new Comparator<LayoutDescription>() {
					@Override
					public int compare(LayoutDescription layout_1, LayoutDescription layout_2) {
						return layout_1.getName().compareTo(layout_2.getName());
					}
				});
				// add items for layouts
				{
					layoutCombo.add(UiMessages.LayoutsPreferencePage_implicitLayout);
					for (LayoutDescription layoutDescription : layouts) {
						layoutCombo.add(layoutDescription.getName());
					}
				}
				new Label(this, SWT.NONE).setText(UiMessages.LayoutsPreferencePage_availableLayouts);
				m_table = CheckboxTableViewer.newCheckList(this, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
				m_table.setContentProvider(ArrayContentProvider.getInstance());
				m_table.setLabelProvider(
						ColumnLabelProvider.createTextProvider(o -> ((LayoutDescription) o).getName()));
				m_table.setCheckStateProvider(new ICheckStateProvider() {
					@Override
					public boolean isChecked(Object element) {
						return m_layoutPreferences.getBoolean(((LayoutDescription) element).getLayoutClassName(), true);
					}

					@Override
					public boolean isGrayed(Object element) {
						return false;
					}
				});
				m_table.setInput(layouts);
				m_table.addCheckStateListener(event -> {
					LayoutDescription layout = (LayoutDescription) event.getElement();
					m_layoutPreferences.putBoolean(layout.getLayoutClassName(), event.getChecked());
					// If default was set to a layout that is de-selected from available layouts.
					// The default layout is set back to implicit layout
					List<Object> input = getLayoutItems();
					IStructuredSelection selection = layoutCombo.getStructuredSelection();
					layoutCombo.setInput(input);
					if (!input.contains(selection.getFirstElement())) {
						layoutCombo.setSelection(implicitLayoutSelection);
					} else {
						layoutCombo.setSelection(selection);
					}
				});
				GridDataFactory.create(m_table.getTable()).fillH().spanH(gridLayoutColumns);
				// bind
				layoutCombo.setInput(getLayoutItems());
				m_bindManager.bind(new IDataEditor() {
					@Override
					public void setValue(Object value) {
						String id = (String) value;
						// implicit layout
						if (StringUtils.isEmpty(id)) {
							layoutCombo.setSelection(implicitLayoutSelection);
							return;
						}
						// find layout by id
						for (int index = 0; index < layouts.size(); index++) {
							LayoutDescription layout = layouts.get(index);
							if (layout.getId().equals(id)) {
								layoutCombo.setSelection(new StructuredSelection(layout));
							}
						}
					}

					@Override
					public Object getValue() {
						if (layoutCombo.getStructuredSelection()
								.getFirstElement() instanceof LayoutDescription layout) {
							return layout.getId();
					}
						// implicit layout
						return "";
					}
				}, new StringPreferenceProvider(m_preferences, IPreferenceConstants.P_LAYOUT_DEFAULT), true);
			}
			// boolean preferences
			checkButton(
					this,
					2,
					UiMessages.LayoutsPreferencePage_inheritLayout,
					IPreferenceConstants.P_LAYOUT_OF_PARENT);
		}

		private List<Object> getLayoutItems() {
			List<Object> layoutItems = new ArrayList<>();
			layoutItems.add(UiMessages.LayoutsPreferencePage_implicitLayout);
			layoutItems.addAll(List.of(m_table.getCheckedElements()));
			return Collections.unmodifiableList(layoutItems);
		}
	}
}