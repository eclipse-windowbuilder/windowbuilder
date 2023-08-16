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

import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassConfiguration;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassUiContentProvider;
import org.eclipse.wb.internal.swing.databinding.model.bindings.VirtualBindingInfo;
import org.eclipse.wb.internal.swing.databinding.model.generic.ClassGenericType;
import org.eclipse.wb.internal.swing.databinding.model.generic.IGenericType;

/**
 * Editor for {@link VirtualBindingInfo}.
 *
 * @author lobas_av
 * @coverage bindings.swing.ui
 */
public final class ElementTypeUiContentProvider extends ChooseClassUiContentProvider {
	private final VirtualBindingInfo m_binding;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ElementTypeUiContentProvider(ChooseClassConfiguration configuration,
			VirtualBindingInfo binding) {
		super(configuration);
		m_binding = binding;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Update
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void updateFromObject() throws Exception {
		IGenericType elementType = m_binding.getElementType();
		if (elementType == null) {
			calculateFinish();
		} else {
			setClassName(elementType.getFullTypeName());
		}
	}

	@Override
	public void saveToObject() throws Exception {
		m_binding.setElementType(new ClassGenericType(getChoosenClass(), null, null));
	}
}