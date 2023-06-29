/*******************************************************************************
 * Copyright (c) 2011, 2022 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import org.apache.commons.lang.StringUtils;
import org.osgi.service.prefs.BackingStoreException;

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
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public LayoutsPreferencePage(ToolkitDescription toolkit) {
		super(toolkit);
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
			InstanceScope.INSTANCE.getNode(IEditorPreferenceConstants.P_AVAILABLE_LAYOUTS_NODE).flush();
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
		public ContentsComposite(Composite parent,
				DataBindManager bindManager,
				IPreferenceStore preferences) {
			super(parent, bindManager, preferences);
			int gridLayoutColumns = 2;
			GridLayoutFactory.create(this).noMargins().columns(gridLayoutColumns);
			// default layout
			{
				new Label(this, SWT.NONE).setText(UiMessages.LayoutsPreferencePage_defaultLayout);
				final Combo layoutCombo = new Combo(this, SWT.READ_ONLY);
				GridDataFactory.create(layoutCombo).grabH().fillH();
				UiUtils.setVisibleItemCount(layoutCombo, 15);
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

				layoutCombo.addSelectionListener(new SelectionListener() {

					@Override
					public void widgetSelected(SelectionEvent e) {
						// If a layout is specified as a default layout but the layout is not specified
						// to be available in the table
						// the default layout is set back to implicit layout
						int index = layoutCombo.getSelectionIndex();
						LayoutDescription layout = layouts.get(index - 1);
						if (!isLayoutAvailable(layout)) {
							layoutCombo.select(0);
						}
					}
					@Override
					public void widgetDefaultSelected(SelectionEvent e) {

					}

				});
				// bind
				m_bindManager.bind(new IDataEditor() {
					@Override
					public void setValue(Object value) {
						String id = (String) value;
						// implicit layout
						if (StringUtils.isEmpty(id)) {
							layoutCombo.select(0);
							return;
						}
						// find layout by id
						for (int index = 0; index < layouts.size(); index++) {
							LayoutDescription layout = layouts.get(index);
							if (layout.getId().equals(id)) {
								layoutCombo.select(1 + index);
							}
						}
					}

					@Override
					public Object getValue() {
						int index = layoutCombo.getSelectionIndex();
						if (index <= 0) {
							// implicit layout
							return null;
						} else {
							LayoutDescription layout = layouts.get(index - 1);
							if (isLayoutAvailable(layout)) {
								return layout.getId();
							}
							return null;

						}
					}
				},
						new StringPreferenceProvider(m_preferences, IPreferenceConstants.P_LAYOUT_DEFAULT), true);
				new Label(this, SWT.NONE).setText(UiMessages.LayoutsPreferencePage_availableLayouts);
				Table table = new Table(this, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
				for (LayoutDescription layout : layouts) {
					TableItem checkItem = new TableItem(table, SWT.NONE);
					checkItem.setText(layout.getName());
					checkItem.setData(layout);
					checkItem.setChecked(
							InstanceScope.INSTANCE.getNode(
									IEditorPreferenceConstants.P_AVAILABLE_LAYOUTS_NODE).getBoolean(
											layout.getLayoutClassName(),
											true));
				}
				table.addListener(SWT.Selection, event -> {
					if (event.detail == SWT.CHECK) {
						InstanceScope.INSTANCE.getNode(
								IEditorPreferenceConstants.P_AVAILABLE_LAYOUTS_NODE).putBoolean(
										((LayoutDescription) ((TableItem) event.item).getData()).getLayoutClassName(),
										((TableItem) event.item).getChecked());
					}
					// If default was set to a layoiut that is deselcted from available layouts. The
					// default layout is set back to implicit layout
					if (!((TableItem) event.item).getChecked()
							&& ((LayoutDescription) ((TableItem) event.item).getData()).getName()
							.contentEquals(layoutCombo.getText())) {
						layoutCombo.select(0);

					}
				});
				GridDataFactory.create(table).fillH().spanH(gridLayoutColumns);
			}
			// boolean preferences
			checkButton(
					this,
					2,
					UiMessages.LayoutsPreferencePage_inheritLayout,
					IPreferenceConstants.P_LAYOUT_OF_PARENT);
		}
	}

	private static boolean isLayoutAvailable(LayoutDescription layout) {
		return InstanceScope.INSTANCE.getNode(IEditorPreferenceConstants.P_AVAILABLE_LAYOUTS_NODE)
				.getBoolean(layout.getLayoutClassName(), false);

	}
}