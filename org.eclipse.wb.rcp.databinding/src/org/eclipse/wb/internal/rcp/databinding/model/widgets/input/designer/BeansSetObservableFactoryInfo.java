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
package org.eclipse.wb.internal.rcp.databinding.model.widgets.input.designer;

import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassConfiguration;

/**
 * Model for {@link org.eclipse.wb.rcp.databinding.BeansSetObservableFactory}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public final class BeansSetObservableFactoryInfo extends BeansObservableFactoryInfo {
	private static final String FACTORY_CLASS =
			"org.eclipse.wb.rcp.databinding.BeansSetObservableFactory";

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public BeansSetObservableFactoryInfo(String className) {
		super(className);
	}

	public BeansSetObservableFactoryInfo() {
		super(FACTORY_CLASS);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Editing
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void configure(ChooseClassConfiguration configuration) {
		configuration.setValueScope(FACTORY_CLASS);
		configuration.setClearValue(FACTORY_CLASS);
		configuration.setBaseClassName(FACTORY_CLASS);
		configuration.setConstructorParameters(new Class[]{Class.class, String.class});
	}
}