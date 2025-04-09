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
package org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders;

import org.eclipse.wb.internal.core.databinding.ui.editor.UiContentProviderAdapter;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Content provider that represented horizontal separator line.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public final class SeparatorUiContentProvider extends UiContentProviderAdapter {
	////////////////////////////////////////////////////////////////////////////
	//
	// GUI
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public int getNumberOfControls() {
		return 1;
	}

	@Override
	public void createContent(Composite parent, int columns) {
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridDataFactory.create(separator).fillH().grabH().spanH(columns);
	}
}