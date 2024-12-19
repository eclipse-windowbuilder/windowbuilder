/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swt.model.property.editor.font;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.swt.model.ModelMessages;
import org.eclipse.wb.internal.swt.model.jface.resource.FontRegistryInfo;
import org.eclipse.wb.internal.swt.model.jface.resource.KeyFieldInfo;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import java.util.List;

/**
 * Implementation of {@link AbstractFontPage} for constructing {@link Font} using
 * {@link org.eclipse.jface.resource.FontRegistry}.
 *
 * @author lobas_av
 * @coverage swt.property.editor
 */
public final class RegistryFontPage extends AbstractFontPage {
	public static final String NAME = "FontRegistry";
	//
	public static final int FONT_STYLE_NONE = 0;
	public static final int FONT_STYLE_BOLD = 1;
	public static final int FONT_STYLE_ITALIC = 2;
	//
	private static final RegistryLabelProvider REGISTRY_LABEL_PROVIDER = new RegistryLabelProvider();
	private static final KeyLabelProvider KEY_LABEL_PROVIDER = new KeyLabelProvider();
	private static final KeyFieldInfo NO_VALUE_KEY = new KeyFieldInfo(Object.class,
			ModelMessages.RegistryFontPage_selectRegistry,
			null);
	private static final KeyFieldInfo[] NO_VALUE_KEY_INPUT = {NO_VALUE_KEY};
	//
	private final Text m_registryText;
	private final ListViewer m_registryList;
	//
	private final Text m_keyText;
	private final ListViewer m_keyList;
	//
	private final Text m_styleText;
	private final org.eclipse.swt.widgets.List m_methodList;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public RegistryFontPage(Composite parent,
			int style,
			FontDialog fontDialog,
			List<FontRegistryInfo> registries) {
		super(parent, style, fontDialog);
		GridLayoutFactory.create(this).columns(3);
		// labels
		{
			new Label(this, SWT.NONE).setText(ModelMessages.RegistryFontPage_registry);
			new Label(this, SWT.NONE).setText(ModelMessages.RegistryFontPage_key);
			new Label(this, SWT.NONE).setText(ModelMessages.RegistryFontPage_method);
		}
		// text's
		{
			{
				m_registryText = new Text(this, SWT.BORDER | SWT.READ_ONLY);
				GridDataFactory.create(m_registryText).fill();
			}
			{
				m_keyText = new Text(this, SWT.BORDER | SWT.READ_ONLY);
				GridDataFactory.create(m_keyText).fill();
			}
			{
				m_styleText = new Text(this, SWT.BORDER | SWT.READ_ONLY);
				GridDataFactory.create(m_styleText).fill();
			}
		}
		// list's
		{
			{
				m_registryList = new ListViewer(this, SWT.BORDER | SWT.V_SCROLL);
				GridDataFactory.create(m_registryList.getControl()).hintVC(12).grab().fill();
				// add items
				m_registryList.setContentProvider(new ArrayContentProvider());
				m_registryList.setLabelProvider(REGISTRY_LABEL_PROVIDER);
				m_registryList.setInput(registries);
				// add listener
				m_registryList.addPostSelectionChangedListener(new ISelectionChangedListener() {
					FontRegistryInfo m_registryInfo;

					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						FontRegistryInfo registryInfo = getSelectionRegistry();
						if (registryInfo != null && m_registryInfo != registryInfo) {
							m_registryInfo = registryInfo;
							m_registryText.setText(REGISTRY_LABEL_PROVIDER.getText(registryInfo));
							List<KeyFieldInfo> keyFields = registryInfo.getKeyFields();
							m_keyList.setInput(keyFields);
							if (!keyFields.isEmpty()) {
								m_keyList.setSelection(new StructuredSelection(keyFields.get(0)));
							}
						}
					}
				});
			}
			{
				m_keyList = new ListViewer(this, SWT.BORDER);
				GridDataFactory.create(m_keyList.getControl()).hintHC(30).fill().grab();
				// add items
				m_keyList.setContentProvider(new ArrayContentProvider());
				m_keyList.setLabelProvider(KEY_LABEL_PROVIDER);
				m_keyList.setInput(NO_VALUE_KEY_INPUT);
				// add listener
				m_keyList.addPostSelectionChangedListener(new ISelectionChangedListener() {
					KeyFieldInfo m_keyFieldInfo;

					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						KeyFieldInfo keyFieldInfo = getKeyInfo();
						if (keyFieldInfo != null
								&& keyFieldInfo != NO_VALUE_KEY
								&& m_keyFieldInfo != keyFieldInfo) {
							m_keyFieldInfo = keyFieldInfo;
							m_keyText.setText(keyFieldInfo.keyName);
							if (m_methodList.getSelectionIndex() == -1) {
								m_methodList.setSelection(0);
							}
							updateFont();
						}
					}
				});
			}
			{
				m_methodList = new org.eclipse.swt.widgets.List(this, SWT.BORDER | SWT.V_SCROLL);
				GridDataFactory.create(m_methodList).hintC(10, 12).fill();
				// add items
				m_methodList.add("get()");
				m_methodList.add("getBold()");
				m_methodList.add("getItalic()");
				// add listener
				m_methodList.addListener(SWT.Selection, new Listener() {
					@Override
					public void handleEvent(Event event) {
						int selectionIndex = m_methodList.getSelectionIndex();
						if (selectionIndex != -1) {
							m_styleText.setText(m_methodList.getItem(selectionIndex));
							updateFont();
						}
					}
				});
			}
		}
	}

	protected final FontRegistryInfo getSelectionRegistry() {
		IStructuredSelection selection = (IStructuredSelection) m_registryList.getSelection();
		if (selection != null) {
			return (FontRegistryInfo) selection.getFirstElement();
		}
		return null;
	}

	protected final KeyFieldInfo getKeyInfo() {
		IStructuredSelection selection = (IStructuredSelection) m_keyList.getSelection();
		if (selection != null) {
			return (KeyFieldInfo) selection.getFirstElement();
		}
		return null;
	}

	/**
	 * Updates current font in {@link FontDialog} based on selection in controls.
	 */
	protected final void updateFont() {
		try {
			FontRegistryInfo registryInfo = getSelectionRegistry();
			KeyFieldInfo keyFieldInfo = getKeyInfo();
			int selectionIndex = m_methodList.getSelectionIndex();
			if (registryInfo == null || keyFieldInfo == null || selectionIndex == -1) {
				return;
			}
			//
			String methodSignature = "get";
			if (selectionIndex == FONT_STYLE_BOLD) {
				methodSignature += "Bold";
			} else if (selectionIndex == FONT_STYLE_ITALIC) {
				methodSignature += "Italic";
			}
			//
			String source =
					TemplateUtils.format(
							"{0}.{1}({2})",
							registryInfo,
							methodSignature,
							keyFieldInfo.keySource);
			methodSignature += "(java.lang.String)";
			//
			Font fontValue = (Font) ReflectionUtils.invokeMethod(
							registryInfo.getObject(),
							methodSignature,
							keyFieldInfo.keyValue);
			m_fontDialog.setFontInfo(new FontInfo(null, fontValue, source, false));
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
					if (fontInfo.getData() instanceof Object[]) {
						Object[] data = (Object[]) fontInfo.getData();
						m_registryList.setSelection(new StructuredSelection(data[0]));
						m_keyList.setSelection(new StructuredSelection(data[1]));
						m_methodList.setSelection((Integer) data[2]);
					}
				}
			});
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Label Provider
	//
	////////////////////////////////////////////////////////////////////////////
	private static class RegistryLabelProvider extends LabelProvider {
		@Override
		public String getText(Object element) {
			try {
				FontRegistryInfo registryInfo = (FontRegistryInfo) element;
				return registryInfo.getVariableSupport().getTitle();
			} catch (Throwable e) {
				DesignerPlugin.log(e);
				return "???";
			}
		}
	}
	private static class KeyLabelProvider extends LabelProvider {
		@Override
		public String getText(Object element) {
			KeyFieldInfo keyFieldInfo = (KeyFieldInfo) element;
			return keyFieldInfo.keyName;
		}
	}
}