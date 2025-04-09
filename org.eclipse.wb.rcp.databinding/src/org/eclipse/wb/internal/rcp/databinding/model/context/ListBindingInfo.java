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
package org.eclipse.wb.internal.rcp.databinding.model.context;

import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.strategies.UpdateListStrategyInfo;

/**
 * Model for <code>DataBindingContext.bindList()</code>.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.context
 */
public final class ListBindingInfo extends BindingInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public ListBindingInfo(ObservableInfo target, ObservableInfo model) {
		this(target, model, null, null);
	}

	public ListBindingInfo(ObservableInfo target,
			ObservableInfo model,
			UpdateListStrategyInfo targetStrategy,
			UpdateListStrategyInfo modelStrategy) {
		super(target, model);
		m_targetStrategy = targetStrategy == null ? new UpdateListStrategyInfo() : targetStrategy;
		m_modelStrategy = modelStrategy == null ? new UpdateListStrategyInfo() : modelStrategy;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Code generation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getBindingMethod() {
		return "bindList";
	}
}