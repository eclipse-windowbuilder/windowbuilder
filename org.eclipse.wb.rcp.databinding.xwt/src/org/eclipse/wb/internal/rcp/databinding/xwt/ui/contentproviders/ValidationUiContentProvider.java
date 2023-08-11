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
package org.eclipse.wb.internal.rcp.databinding.xwt.ui.contentproviders;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.databinding.ui.UiUtils;
import org.eclipse.wb.internal.core.databinding.ui.editor.DialogFieldUiContentProvider;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.IDialogFieldListener;
import org.eclipse.wb.internal.core.utils.dialogfields.IListAdapter;
import org.eclipse.wb.internal.core.utils.dialogfields.ListDialogField;
import org.eclipse.wb.internal.rcp.databinding.xwt.Activator;
import org.eclipse.wb.internal.rcp.databinding.xwt.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.xwt.Messages;
import org.eclipse.wb.internal.rcp.databinding.xwt.model.ValidationInfo;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ClassUtils;

import java.lang.reflect.Modifier;
import java.util.List;

/**
 *
 * @author lobas_av
 *
 */
public class ValidationUiContentProvider extends DialogFieldUiContentProvider {
	private static final Image CLASS_IMAGE = Activator.getImage("class_obj.gif");
	private static final Image ERROR_IMAGE = Activator.getImage("error_obj.gif");
	//
	private final ListDialogField m_dialogField;
	//
	private final DatabindingsProvider m_provider;
	private final ValidationInfo m_validator;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ValidationUiContentProvider(DatabindingsProvider provider, ValidationInfo validator) {
		m_provider = provider;
		m_validator = validator;
		m_dialogField =
				new ListDialogField(m_listAdapter, new String[]{
						Messages.ValidationUiContentProvider_addButton,
						Messages.ValidationUiContentProvider_removeButton}, new LabelProvider());
		m_dialogField.setRemoveButtonIndex(1);
		m_dialogField.setLabelText(Messages.ValidationUiContentProvider_title);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	//
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public DialogField getDialogField() {
		return m_dialogField;
	}

	@Override
	public void createContent(Composite parent, int columns) {
		super.createContent(parent, columns);
		GridData data = (GridData) m_dialogField.getListControl(null).getLayoutData();
		data.verticalAlignment = GridData.BEGINNING;
		data.grabExcessVerticalSpace = false;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	//
	//
	////////////////////////////////////////////////////////////////////////////
	private final IListAdapter m_listAdapter = new IListAdapter() {
		@Override
		public void selectionChanged(ListDialogField field) {
		}

		@Override
		public void doubleClicked(ListDialogField field) {
		}

		@Override
		public void customButtonPressed(ListDialogField field, int index) {
			if (index == 0) {
				handleAdd();
			}
		}
	};
	private final IDialogFieldListener m_changeListener = new IDialogFieldListener() {
		@Override
		public void dialogFieldChanged(DialogField field) {
			calculateFinish();
		}
	};

	private void handleAdd() {
		try {
			String className =
					UiUtils.chooseType(
							getShell(),
							getJavaProject(),
							"org.eclipse.xwt.IValidationRule",
							IJavaElementSearchConstants.CONSIDER_CLASSES);
			if (className != null) {
				m_dialogField.setDialogFieldListener(null);
				//
				int size = m_dialogField.getSize();
				for (int i = 0; i < size; i++) {
					ClassInfo info = (ClassInfo) m_dialogField.getElement(i);
					if (className.equals(info.className)) {
						m_dialogField.selectElements(new StructuredSelection(info));
						return;
					}
				}
				//
				ClassInfo info = createInfo(className);
				m_dialogField.addElement(info);
				m_dialogField.selectElements(new StructuredSelection(info));
			}
		} catch (Throwable e) {
			DesignerPlugin.log(e);
		} finally {
			m_dialogField.setDialogFieldListener(m_changeListener);
		}
	}

	private ClassInfo createInfo(String className) {
		ClassInfo info = new ClassInfo();
		info.className = className;
		//
		if (className.length() == 0) {
			info.message = Messages.ValidationUiContentProvider_noClass;
		} else {
			if (className.startsWith("{") && className.endsWith("}")) {
				return info;
			}
			//
			try {
				// check load class
				Class<?> testClass = loadClass(className);
				// check permissions
				int modifiers = testClass.getModifiers();
				if (!Modifier.isPublic(modifiers)) {
					info.message = Messages.ValidationUiContentProvider_notPublicClass;
					return info;
				}
				if (Modifier.isAbstract(modifiers)) {
					info.message = Messages.ValidationUiContentProvider_abstractClass;
					return info;
				}
				// check constructor
				boolean noConstructor = true;
				try {
					testClass.getConstructor(ArrayUtils.EMPTY_CLASS_ARRAY);
					noConstructor = false;
				} catch (SecurityException e) {
				} catch (NoSuchMethodException e) {
				}
				// prepare error message for constructor
				if (noConstructor) {
					info.message =
							Messages.ValidationUiContentProvider_noPublicConstructor
							+ ClassUtils.getShortClassName(className)
							+ "().";
				}
			} catch (ClassNotFoundException e) {
				info.message = Messages.ValidationUiContentProvider_notExistClass;
			}
		}
		return info;
	}

	private void calculateFinish() {
		int size = m_dialogField.getSize();
		for (int i = 0; i < size; i++) {
			ClassInfo info = (ClassInfo) m_dialogField.getElement(i);
			if (info.message != null) {
				setErrorMessage(info.message);
				return;
			}
		}
		setErrorMessage(null);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	//
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void updateFromObject() throws Exception {
		List<ClassInfo> infos = Lists.newArrayList();
		for (String className : m_validator.getClassNames()) {
			infos.add(createInfo(className));
		}
		//
		m_dialogField.setElements(infos);
		m_dialogField.setDialogFieldListener(m_changeListener);
	}

	@Override
	public void saveToObject() throws Exception {
		List<String> classNames = Lists.newArrayList();
		//
		int size = m_dialogField.getSize();
		for (int i = 0; i < size; i++) {
			ClassInfo info = (ClassInfo) m_dialogField.getElement(i);
			classNames.add(info.className);
		}
		//
		m_validator.setClassNames(classNames);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	//
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Class<?> loadClass(String className) throws ClassNotFoundException {
		return CoreUtils.load(m_provider.getXmlObjectRoot().getContext().getClassLoader(), className);
	}

	@Override
	protected IJavaProject getJavaProject() {
		return m_provider.getXmlObjectRoot().getContext().getJavaProject();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	//
	//
	////////////////////////////////////////////////////////////////////////////
	private static class ClassInfo {
		String message;
		String className;
	}
	////////////////////////////////////////////////////////////////////////////
	//
	//
	//
	////////////////////////////////////////////////////////////////////////////
	private static class LabelProvider extends org.eclipse.jface.viewers.LabelProvider {
		@Override
		public Image getImage(Object element) {
			ClassInfo info = (ClassInfo) element;
			return info.message == null ? CLASS_IMAGE : ERROR_IMAGE;
		}

		@Override
		public String getText(Object element) {
			ClassInfo info = (ClassInfo) element;
			return info.className;
		}
	}
}