/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Content provider for view two labels:
 * <p>
 * title <b>value</b>
 * </p>
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public final class LabelUiContentProvider extends UiContentProviderAdapter {
	private final String m_title;
	private final String m_value;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public LabelUiContentProvider(String title, String value) {
		m_title = title;
		m_value = value;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// GUI
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public int getNumberOfControls() {
		return 2;
	}

	@Override
	public void createContent(Composite parent, int columns) {
		// create title label
		Label titleLable = new Label(parent, SWT.NONE);
		titleLable.setText(m_title);
		// create value bold label
		Label valueLabel = new Label(parent, SWT.NONE);
		GridDataFactory.create(valueLabel).fillH().grabH().spanH(columns - 1);
		Font boldFont = FontDescriptor.createFrom(valueLabel.getFont()) //
				.setStyle(SWT.BOLD) //
				.createFont(null);
		valueLabel.setFont(boldFont);
		valueLabel.addDisposeListener(event -> boldFont.dispose());
		valueLabel.setText(m_value);
	}
}