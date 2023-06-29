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
package org.eclipse.wb.internal.rcp.databinding.model.context.strategies;

import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassConfiguration;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.rcp.databinding.Activator;
import org.eclipse.wb.internal.rcp.databinding.Messages;
import org.eclipse.wb.internal.rcp.databinding.model.context.BindingUiContentProviderContext;
import org.eclipse.wb.internal.rcp.databinding.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.rcp.databinding.ui.contentproviders.ConverterUiContentProvider;
import org.eclipse.wb.internal.rcp.databinding.ui.contentproviders.UpdateStrategyPropertiesUiContentProvider;
import org.eclipse.wb.internal.rcp.databinding.ui.contentproviders.UpdateStrategyUiContentProvider;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;

import java.util.List;

/**
 * Model for <code>org.eclipse.core.databinding.UpdateSetStrategy</code>.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.context
 */
public final class UpdateSetStrategyInfo extends UpdateStrategyInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public UpdateSetStrategyInfo() {
		setStringValue(Activator.getStore().getString(IPreferenceConstants.UPDATE_SET_STRATEGY_DEFAULT));
	}

	public UpdateSetStrategyInfo(ClassInstanceCreation creation, Expression[] arguments) {
		super(creation, arguments);
	}

	/**
	 * Note: this constructor used only for tests.
	 */
	public UpdateSetStrategyInfo(StrategyType strategyType,
			Object strategyValue,
			ConverterInfo converter) {
		super(strategyType, strategyValue, converter);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getStrategyClass() {
		return "org.eclipse.core.databinding.UpdateSetStrategy";
	}

	@Override
	protected Object getStrategyValue(String value) {
		if (value == null) {
			return Value.POLICY_UPDATE;
		}
		if (value.endsWith("POLICY_NEVER")) {
			return Value.POLICY_NEVER;
		}
		if (value.endsWith("POLICY_ON_REQUEST")) {
			return Value.POLICY_ON_REQUEST;
		}
		if (value.endsWith("POLICY_UPDATE")) {
			return Value.POLICY_UPDATE;
		}
		//
		Assert.fail(Messages.UpdateSetStrategyInfo_errUndefinedStratery + value);
		return null;
	}

	@Override
	protected String getStrategyStringValue() {
		switch ((Value) m_strategyValue) {
		case POLICY_NEVER :
			return "POLICY_NEVER";
		case POLICY_ON_REQUEST :
			return "POLICY_ON_REQUEST";
		case POLICY_UPDATE :
			return "POLICY_UPDATE";
		}
		Assert.fail(Messages.UpdateSetStrategyInfo_errUndefinedStratery + m_strategyValue);
		return null;
	}

	@Override
	public void setStringValue(String value) {
		if (value.endsWith("POLICY_NEVER")) {
			m_strategyType = StrategyType.IntConstructor;
			m_strategyValue = Value.POLICY_NEVER;
		} else if (value.endsWith("POLICY_ON_REQUEST")) {
			m_strategyType = StrategyType.IntConstructor;
			m_strategyValue = Value.POLICY_ON_REQUEST;
		} else if (value.endsWith("POLICY_UPDATE")) {
			m_strategyType = StrategyType.Null;
			m_strategyValue = Value.POLICY_UPDATE;
		} else {
			m_strategyType = StrategyType.ExtendetClass;
			m_strategyValue = value;
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Editing
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected ChooseClassConfiguration createConfiguration(BindingUiContentProviderContext context) {
		ChooseClassConfiguration configuration = super.createConfiguration(context);
		configuration.setDialogFieldLabel(Messages.UpdateSetStrategyInfo_strategyLabel);
		configuration.setDefaultValues(new String[]{
				"POLICY_UPDATE",
				"POLICY_NEVER",
		"POLICY_ON_REQUEST"});
		return configuration;
	}

	@Override
	public void createContentProviders(List<IUiContentProvider> providers,
			BindingUiContentProviderContext context) throws Exception {
		// self editor
		providers.add(new UpdateStrategyUiContentProvider(createConfiguration(context), this));
		// properties editor
		UpdateStrategyPropertiesUiContentProvider propertiesUIContentProvider =
				new UpdateStrategyPropertiesUiContentProvider(context.getDirection());
		// converter
		propertiesUIContentProvider.addProvider(new ConverterUiContentProvider(createConverterConfiguration(context),
				this));
		//
		providers.add(propertiesUIContentProvider);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Values
	//
	////////////////////////////////////////////////////////////////////////////
	public static enum Value {
		POLICY_NEVER, POLICY_ON_REQUEST, POLICY_UPDATE
	}
}