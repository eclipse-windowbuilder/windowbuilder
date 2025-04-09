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

/**
 * Configuration for {@link ChooseClassAndPropertiesUiContentProvider}.
 *
 * @author lobas_av
 * @coverage bindings.swing.ui
 */
public class ChooseClassAndPropertiesConfiguration
extends
org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassAndPropertiesConfiguration {
	private boolean m_workWithELProperty = true;

	////////////////////////////////////////////////////////////////////////////
	//
	// ELProperty
	//
	////////////////////////////////////////////////////////////////////////////
	public boolean isWorkWithELProperty() {
		return m_workWithELProperty;
	}

	public void setWorkWithELProperty(boolean workWithElProperty) {
		m_workWithELProperty = workWithElProperty;
	}
}