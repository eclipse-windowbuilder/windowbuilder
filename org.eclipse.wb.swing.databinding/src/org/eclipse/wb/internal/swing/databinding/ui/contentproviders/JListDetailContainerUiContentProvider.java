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
import org.eclipse.wb.internal.swing.databinding.model.bindings.DetailBindingInfo;
import org.eclipse.wb.internal.swing.databinding.model.bindings.JListBindingInfo;

import org.eclipse.swt.custom.CTabItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Editor for {@link JListBindingInfo} detail binding.
 *
 * @author lobas_av
 * @coverage bindings.swing.ui
 */
public class JListDetailContainerUiContentProvider extends TabContainerUiContentProvider {
	private final JListBindingInfo m_binding;
	private final List<BindingInfo> m_bindings;
	private final DatabindingsProvider m_provider;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public JListDetailContainerUiContentProvider(TabContainerConfiguration configuration,
			JListBindingInfo binding,
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
		return new UIContentContainer<>(m_binding.createDefaultDetailBinding(),
				m_bindings,
				Messages.JListDetailContainerUiContentProvider_detail1,
				m_provider);
	}

	@Override
	protected void configute(CTabItem tabItem, int index, IUiContentProvider provider) {
		tabItem.setText(Messages.JListDetailContainerUiContentProvider_detail);
	}

	@Override
	protected void postDelete() throws Exception {
		createPage(-1, createNewPageContentProvider(), true);
		configure();
		m_listener.calculateFinish();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Update
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void updateFromObject() throws Exception {
		List<IUiContentProvider> providers = new ArrayList<>();
		providers.add(new UIContentContainer<>(m_binding.getDetailBinding(),
				m_bindings,
				Messages.JListDetailContainerUiContentProvider_detail2,
				m_provider));
		updateFromObject(providers);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void saveToObject(List<IUiContentProvider> providers) throws Exception {
		UIContentContainer<DetailBindingInfo> container =
				(UIContentContainer<DetailBindingInfo>) providers.get(0);
		m_binding.setDetailBinding(container.getBinding(), m_bindings);
	}
}