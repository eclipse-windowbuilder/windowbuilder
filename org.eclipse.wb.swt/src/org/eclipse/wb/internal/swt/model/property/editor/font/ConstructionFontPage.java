/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.internal.swt.model.property.editor.font;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.swt.model.ModelMessages;
import org.eclipse.wb.internal.swt.support.ToolkitSupport;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * Implementation of {@link AbstractFontPage} for constructing {@link Font} using family, style and
 * size.
 *
 * @author lobas_av
 * @coverage swt.property.editor
 */
public final class ConstructionFontPage extends AbstractFontPage {
	private static final String[] m_styleTitles = {"NORMAL", "BOLD", "ITALIC", "BOLD | ITALIC"};
	private static final int[] m_styleValues = {
			SWT.NORMAL,
			SWT.BOLD,
			SWT.ITALIC,
			SWT.BOLD | SWT.ITALIC };
	//
	private final String[] m_families;
	//
	private final Text m_familyText;
	private final List m_familyList;
	//
	private final Text m_styleText;
	private final List m_styleList;
	//
	private final Text m_sizeText;
	private final List m_sizeList;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ConstructionFontPage(JavaInfo javaInfo, Composite parent, int style, FontDialog fontDialog) {
		super(parent, style, fontDialog);
		GridLayoutFactory.create(this).columns(3);
		// labels
		{
			new Label(this, SWT.NONE).setText(ModelMessages.ConstructionFontPage_family);
			new Label(this, SWT.NONE).setText(ModelMessages.ConstructionFontPage_style);
			new Label(this, SWT.NONE).setText(ModelMessages.ConstructionFontPage_size);
		}
		// text's
		{
			{
				m_familyText = new Text(this, SWT.BORDER | SWT.READ_ONLY);
				GridDataFactory.create(m_familyText).fill();
			}
			{
				m_styleText = new Text(this, SWT.BORDER | SWT.READ_ONLY);
				GridDataFactory.create(m_styleText).fill();
			}
			{
				m_sizeText = new Text(this, SWT.BORDER | SWT.READ_ONLY);
				GridDataFactory.create(m_sizeText).fill();
			}
		}
		// list's
		{
			{
				m_familyList = new List(this, SWT.BORDER | SWT.V_SCROLL);
				GridDataFactory.create(m_familyList).hintVC(12).grab().fill();
				// add items
				String[] families;
				try {
					families = getFontFamilies();
				} catch (Throwable e) {
					DesignerPlugin.log(e);
					families = ArrayUtils.EMPTY_STRING_ARRAY;
				}
				m_families = families;
				m_familyList.setItems(m_families);
				// add listener
				m_familyList.addListener(SWT.Selection, new Listener() {
					@Override
					public void handleEvent(Event event) {
						int index = m_familyList.getSelectionIndex();
						String family = m_families[index];
						m_familyText.setText(family);
						updateFont();
					}
				});
			}
			{
				m_styleList = new List(this, SWT.BORDER);
				GridDataFactory.create(m_styleList).hintHC(20).fill();
				// add items
				for (int i = 0; i < m_styleTitles.length; i++) {
					String styleTitle = m_styleTitles[i];
					m_styleList.add(styleTitle);
				}
				// add listener
				m_styleList.addListener(SWT.Selection, new Listener() {
					@Override
					public void handleEvent(Event event) {
						int index = m_styleList.getSelectionIndex();
						String fontStyle = m_styleTitles[index];
						m_styleText.setText(fontStyle);
						updateFont();
					}
				});
			}
			{
				m_sizeList = new List(this, SWT.BORDER | SWT.V_SCROLL);
				GridDataFactory.create(m_sizeList).hintC(10, 12).fill();
				// add items
				for (int i = 5; i < 100; i++) {
					m_sizeList.add(Integer.toString(i));
				}
				// add listener
				m_sizeList.addListener(SWT.Selection, new Listener() {
					@Override
					public void handleEvent(Event event) {
						String size = m_sizeList.getSelection()[0];
						m_sizeText.setText(size);
						updateFont();
					}
				});
			}
		}
	}

	/**
	 * Updates current font in {@link FontDialog} based on selection in controls.
	 */
	private void updateFont() {
		String family = m_familyText.getText();
		// prepare style
		int style = m_styleValues[0];
		{
			int index = m_styleList.getSelectionIndex();
			if (index != -1) {
				style = m_styleValues[index];
			}
		}
		// prepare size
		int size;
		try {
			size = Integer.parseInt(m_sizeText.getText());
		} catch (Throwable e) {
			size = 12;
		}
		//
		try {
			Font font = new Font(DesignerPlugin.getStandardDisplay(), family, size, style);
			m_fontDialog.setFontInfo(new FontInfo(null, font, null, true));
		} catch (Throwable e) {
			DesignerPlugin.log(e);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void setFont(final FontInfo fontInfo) {
		if (fontInfo != null) {
			ExecutionUtils.runLog(new RunnableEx() {
				@Override
				public void run() throws Exception {
					Font font = fontInfo.getFont();
					FontData fontData = font.getFontData()[0];
					// family
					{
						String family = fontData.getName();
						m_familyList.setSelection(new String[]{family});
						m_familyText.setText(family);
					}
					// style
					{
						int style = fontData.getStyle();
						for (int i = 0; i < m_styleValues.length; i++) {
							if (style == m_styleValues[i]) {
								m_styleList.select(i);
								m_styleText.setText(m_styleTitles[i]);
							}
						}
					}
					// size
					{
						String text = "" + fontData.getHeight();
						m_sizeList.setSelection(new String[]{text});
						m_sizeText.setText(text);
					}
				}
			});
		}
	}

	/**
	 * @return names of all fonts into system.
	 */
	public static String[] getFontFamilies() throws Exception {
		Set<String> families = new TreeSet<>();
		// add all font families
		Collections.addAll(families, ToolkitSupport.getFontFamilies(false));
		Collections.addAll(families, ToolkitSupport.getFontFamilies(true));
		// add default font
		families.add(DesignerPlugin.getStandardDisplay().getSystemFont().getFontData()[0].getName());
		// sort names
		String[] sortFamilies = families.toArray(new String[families.size()]);
		Arrays.sort(sortFamilies);
		return sortFamilies;
	}
}