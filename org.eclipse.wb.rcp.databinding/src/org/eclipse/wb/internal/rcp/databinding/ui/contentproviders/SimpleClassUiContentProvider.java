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
import org.eclipse.wb.internal.rcp.databinding.model.SimpleClassObjectInfo;

/**
 * Content provider for edit (choose class over dialog and combo) {@link SimpleClassObjectInfo}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.ui
 */
public final class SimpleClassUiContentProvider extends ChooseClassUiContentProvider {
	private final SimpleClassObjectInfo m_object;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SimpleClassUiContentProvider(ChooseClassConfiguration configuration,
			SimpleClassObjectInfo object) {
		super(configuration);
		m_object = object;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Update
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void updateFromObject() throws Exception {
		setClassName(m_object.getClassName());
	}

	@Override
	public void saveToObject() throws Exception {
		m_object.setClassName(getClassName());
	}
}