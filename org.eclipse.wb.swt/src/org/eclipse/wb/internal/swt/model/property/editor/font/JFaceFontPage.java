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
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.swt.model.ModelMessages;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link AbstractFontPage} for constructing {@link Font} using JFace constants.
 *
 * @author lobas_av
 * @coverage swt.property.editor
 */
public final class JFaceFontPage extends AbstractFontPage {
	public static final String NAME = "JFace";
	//
	private final Table m_fontTable;
	private final List<FontInfo> m_fonts;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public JFaceFontPage(Composite parent, int style, FontDialog fontDialog, JavaInfo javaInfo) {
		super(parent, style, fontDialog);
		GridLayoutFactory.create(this);
		//
		{
			new Label(this, SWT.NONE).setText(ModelMessages.JFaceFontPage_selectFont);
		}
		{
			m_fontTable = new Table(this, SWT.BORDER | SWT.FULL_SELECTION);
			GridDataFactory.create(m_fontTable).hintVC(15).grab().fill();
			m_fontTable.setHeaderVisible(true);
			m_fontTable.setLinesVisible(true);
			// create columns
			{
				new TableColumn(m_fontTable, SWT.NONE).setText(ModelMessages.JFaceFontPage_nameColumn);
				new TableColumn(m_fontTable, SWT.NONE).setText(ModelMessages.JFaceFontPage_valueColumn);
			}
			// add items
			List<FontInfo> fonts;
			try {
				fonts = getJFaceFonts();
			} catch (Throwable e) {
				DesignerPlugin.log(e);
				fonts = Collections.emptyList();
			}
			m_fonts = fonts;
			//
			for (FontInfo fontInfo : m_fonts) {
				TableItem tableItem = new TableItem(m_fontTable, SWT.NONE);
				tableItem.setText(0, fontInfo.getName());
				try {
					FontData fontData = fontInfo.getFont().getFontData()[0];
					tableItem.setText(
							1,
							"" + fontData.getName() + " " + fontData.getHeight());
				} catch (Throwable e) {
					tableItem.setText(1, "???");
				}
			}
			// pack columns
			for (int i = 0; i < m_fontTable.getColumnCount(); i++) {
				m_fontTable.getColumn(i).pack();
			}
			// add listeners
			m_fontTable.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					FontInfo fontInfo = m_fonts.get(m_fontTable.getSelectionIndex());
					m_fontDialog.setFontInfo(fontInfo);
				}
			});
			m_fontTable.addListener(SWT.MouseDoubleClick, new Listener() {
				@Override
				public void handleEvent(Event event) {
					m_fontDialog.closeOk();
				}
			});
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	//
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void setFont(FontInfo fontInfo) {
	}

	/**
	 * @return all JFace fonts.
	 */
	public static List<FontInfo> getJFaceFonts() throws Exception {
		List<FontInfo> jfaceFonts = new ArrayList<>();
		Method[] methods = JFaceResources.class.getMethods();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			int modifiers = method.getModifiers();
			// check public static
			if (!Modifier.isPublic(modifiers) || !Modifier.isStatic(modifiers)) {
				continue;
			}
			// check getXXXFont name
			String name = method.getName();
			if (!name.startsWith("get") || !name.endsWith("Font")) {
				continue;
			}
			// check empty parameters method
			if (method.getParameterTypes().length != 0) {
				continue;
			}
			// check return type
			if (!method.getReturnType().getName().equals("org.eclipse.swt.graphics.Font")) {
				continue;
		}
			// create font info
			Font font = (Font) method.invoke(null);
			jfaceFonts.add(
					new FontInfo(name + "()", font, "org.eclipse.jface.resource.JFaceResources." + name + "()", false));
	}
		return jfaceFonts;
	}
}