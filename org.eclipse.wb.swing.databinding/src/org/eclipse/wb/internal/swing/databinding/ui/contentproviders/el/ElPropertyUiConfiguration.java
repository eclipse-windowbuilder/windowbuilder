/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swing.databinding.ui.contentproviders.el;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.PlatformUI;

/**
 * Configuration for {@link ElPropertyUiContentProvider}.
 *
 * @author lobas_av
 * @coverage bindings.swing.ui
 */
public class ElPropertyUiConfiguration {
	private String m_title;
	private int m_rows = 4;
	private Color m_stringsColor = new Color(42, 0, 255);
	private Color m_keywordsColor = new Color(127, 0, 85);
	private Color m_numbersColor = PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_BLACK);
	private Color m_operatorsColor = new Color(0, 57, 29);
	private Color m_propertiesColor = new Color(130, 0, 0);

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public String getTitle() {
		return m_title == null ? "" : m_title;
	}

	public void setTitle(String title) {
		m_title = title;
	}

	public int getRows() {
		return m_rows;
	}

	public void setRows(int rows) {
		m_rows = rows;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Colors
	//
	////////////////////////////////////////////////////////////////////////////
	public Color getStringsColor() {
		return m_stringsColor;
	}

	public void setStringsColor(Color textColor) {
		m_stringsColor = textColor;
	}

	public Color getKeywordsColor() {
		return m_keywordsColor;
	}

	public void setKeywordsColor(Color wordColor) {
		m_keywordsColor = wordColor;
	}

	public Color getNumbersColor() {
		return m_numbersColor;
	}

	public void setNumbersColor(Color digitsColor) {
		m_numbersColor = digitsColor;
	}

	public Color getOperatorsColor() {
		return m_operatorsColor;
	}

	public void setOperatorsColor(Color operatorsColor) {
		m_operatorsColor = operatorsColor;
	}

	public Color getPropertiesColor() {
		return m_propertiesColor;
	}

	public void setPropertiesColor(Color propertyColor) {
		m_propertiesColor = propertyColor;
	}
}