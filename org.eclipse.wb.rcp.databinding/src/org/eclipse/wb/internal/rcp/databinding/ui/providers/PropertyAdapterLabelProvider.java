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
package org.eclipse.wb.internal.rcp.databinding.ui.providers;

import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.PropertyAdapter;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Implementation of {@link LabelProvider} for {@link PropertyAdapter}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.ui
 */
public final class PropertyAdapterLabelProvider extends LabelProvider {
	////////////////////////////////////////////////////////////////////////////
	//
	// LabelProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getText(Object element) {
		PropertyAdapter adapter = (PropertyAdapter) element;
		return adapter.getName();
	}

	@Override
	public Image getImage(Object element) {
		PropertyAdapter adapter = (PropertyAdapter) element;
		return TypeImageProvider.getImage(adapter.getType());
	}
}