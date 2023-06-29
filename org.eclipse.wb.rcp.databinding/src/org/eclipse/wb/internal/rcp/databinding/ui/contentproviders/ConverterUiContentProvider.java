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
package org.eclipse.wb.internal.rcp.databinding.ui.contentproviders;

import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassConfiguration;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassUiContentProvider;
import org.eclipse.wb.internal.rcp.databinding.model.context.strategies.ConverterInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.strategies.UpdateStrategyInfo;

/**
 * Content provider for edit (choose class over dialog and combo) {@link UpdateStrategyInfo}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.ui
 */
public final class ConverterUiContentProvider extends ChooseClassUiContentProvider {
	private final UpdateStrategyInfo m_strategy;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ConverterUiContentProvider(ChooseClassConfiguration configuration,
			UpdateStrategyInfo strategy) {
		super(configuration);
		m_strategy = strategy;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Update
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void updateFromObject() {
		ConverterInfo converter = m_strategy.getConverter();
		setClassName(converter == null ? "N/S" : converter.getClassName());
	}

	@Override
	public void saveToObject() {
		String className = getClassName();
		// check set or clear value
		if ("N/S".equals(className)) {
			m_strategy.setConverter(null);
		} else {
			ConverterInfo converter = m_strategy.getConverter();
			// check new converter or edit value
			if (converter == null) {
				m_strategy.setConverter(new ConverterInfo(className));
			} else {
				converter.setClassName(className);
			}
		}
	}
}