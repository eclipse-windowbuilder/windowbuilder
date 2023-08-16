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
package org.eclipse.wb.internal.swing.databinding.ui.contentproviders;

import com.google.common.collect.Lists;

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
		return new UIContentContainer<ColumnBindingInfo>(m_binding.createNewColumnBinding(-1),
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
		List<IUiContentProvider> providers = Lists.newArrayList();
		for (ColumnBindingInfo binding : m_binding.getColumns()) {
			providers.add(new UIContentContainer<ColumnBindingInfo>(binding,
					m_bindings,
					Messages.JTableColumnContainerUiContentProvider_column,
					m_provider));
		}
		updateFromObject(providers);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void saveToObject(List<IUiContentProvider> providers) throws Exception {
		List<ColumnBindingInfo> columns = Lists.newArrayList();
		for (IUiContentProvider provider : providers) {
			UIContentContainer<ColumnBindingInfo> container =
					(UIContentContainer<ColumnBindingInfo>) provider;
			columns.add(container.getBinding());
		}
		m_binding.setColumns(columns, m_bindings);
	}
}