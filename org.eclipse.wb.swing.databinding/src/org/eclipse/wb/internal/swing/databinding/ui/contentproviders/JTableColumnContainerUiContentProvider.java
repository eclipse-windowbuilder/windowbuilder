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
package org.eclipse.wb.internal.swing.databinding.ui.contentproviders;

import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.TabContainerConfiguration;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.TabContainerUiContentProvider;
import org.eclipse.wb.internal.swing.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.swing.databinding.Messages;
import org.eclipse.wb.internal.swing.databinding.model.bindings.BindingInfo;
import org.eclipse.wb.internal.swing.databinding.model.bindings.ColumnBindingInfo;
import org.eclipse.wb.internal.swing.databinding.model.bindings.JTableBindingInfo;

import org.eclipse.swt.custom.CTabItem;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Editor for {@link JTableBindingInfo} columns.
 *
 * @author lobas_av
 * @coverage bindings.swing.ui
 */
public class JTableColumnContainerUiContentProvider extends TabContainerUiContentProvider {
	private final JTableBindingInfo m_binding;
	private final List<BindingInfo> m_bindings;
	private final DatabindingsProvider m_provider;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public JTableColumnContainerUiContentProvider(TabContainerConfiguration configuration,
			JTableBindingInfo binding,
			List<BindingInfo> bindings,
			DatabindingsProvider provider) {
		super(configuration);
		m_binding = binding;
		m_bindings = bindings;
		m_provider = provider;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Pages
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected IUiContentProvider createNewPageContentProvider() throws Exception {
		return new UIContentContainer<>(m_binding.createNewColumnBinding(-1),
				m_bindings,
				Messages.JTableColumnContainerUiContentProvider_column,
				m_provider);
	}

	@Override
	protected void configute(CTabItem tabItem, int index, IUiContentProvider provider) {
		tabItem.setText(MessageFormat.format(
				Messages.JTableColumnContainerUiContentProvider_columnIndex,
				index));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Update
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void updateFromObject() throws Exception {
		List<IUiContentProvider> providers = new ArrayList<>();
		for (ColumnBindingInfo binding : m_binding.getColumns()) {
			providers.add(new UIContentContainer<>(binding,
					m_bindings,
					Messages.JTableColumnContainerUiContentProvider_column,
					m_provider));
		}
		updateFromObject(providers);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void saveToObject(List<IUiContentProvider> providers) throws Exception {
		List<ColumnBindingInfo> columns = new ArrayList<>();
		for (IUiContentProvider provider : providers) {
			UIContentContainer<ColumnBindingInfo> container =
					(UIContentContainer<ColumnBindingInfo>) provider;
			columns.add(container.getBinding());
		}
		m_binding.setColumns(columns, m_bindings);
	}
}