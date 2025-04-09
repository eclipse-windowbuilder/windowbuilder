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
package org.eclipse.wb.internal.rcp.databinding.ui.contentproviders;

import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassConfiguration;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassUiContentProvider;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailBeanObservableInfo;

/**
 * Additional content provider for choose collection element type for
 * {@link DetailBeanObservableInfo} (over selection, multi selection, checkable) viewer input.
 *
 * @author lobas_av
 * @coverage bindings.rcp.ui
 */
public final class TreeDetailUiContentProvider extends ChooseClassUiContentProvider {
	private final DetailBeanObservableInfo m_observable;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public TreeDetailUiContentProvider(ChooseClassConfiguration configuration,
			DetailBeanObservableInfo observable) {
		super(configuration);
		m_observable = observable;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Update
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void updateFromObject() throws Exception {
	}

	@Override
	public void saveToObject() throws Exception {
		m_observable.setDetailPropertyType(getChoosenClass());
	}
}