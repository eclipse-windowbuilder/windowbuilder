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
import org.eclipse.wb.internal.rcp.databinding.model.context.strategies.UpdateValueStrategyInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.strategies.ValidatorInfo;

/**
 * Content provider foe edit (choose class over dialog and combo) {@link UpdateValueStrategyInfo}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.ui
 */
public final class ValidatorUiContentProvider extends ChooseClassUiContentProvider {
	private final UpdateValueStrategyInfo m_strategy;
	private final String m_validatorName;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ValidatorUiContentProvider(ChooseClassConfiguration configuration,
			UpdateValueStrategyInfo strategy,
			String validatorName) {
		super(configuration);
		m_strategy = strategy;
		m_validatorName = validatorName;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Update
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void updateFromObject() {
		ValidatorInfo validator = m_strategy.getValidator(m_validatorName);
		setClassName(validator == null ? "N/S" : validator.getClassName());
	}

	@Override
	public void saveToObject() {
		String className = getClassName();
		// check set or clear value
		if ("N/S".equals(className)) {
			m_strategy.setValidator(m_validatorName, null);
		} else {
			ValidatorInfo validator = m_strategy.getValidator(m_validatorName);
			// check new validator or edit value
			if (validator == null) {
				m_strategy.setValidator(m_validatorName, new ValidatorInfo(className));
			} else {
				validator.setClassName(className);
			}
		}
	}
}