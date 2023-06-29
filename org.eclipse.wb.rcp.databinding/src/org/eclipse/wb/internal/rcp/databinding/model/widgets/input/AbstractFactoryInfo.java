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
package org.eclipse.wb.internal.rcp.databinding.model.widgets.input;

import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassConfiguration;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.rcp.databinding.model.DataBindingsCodeUtils;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.designer.BeansObservableFactoryInfo;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ClassUtils;

import java.util.List;

/**
 * Abstract model for factory observable object <code>BeansObservables.XXXFactory(...)</code>.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public abstract class AbstractFactoryInfo extends BeansObservableFactoryInfo {
	private final String m_method;
	protected boolean m_isPojoBindable;
	private boolean m_cancel;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AbstractFactoryInfo(String method) {
		super(null);
		m_method = method;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public void setPojoBindable(boolean isPojoBindable) {
		m_isPojoBindable = isPojoBindable;
	}

	@Override
	public boolean isDesignerMode() {
		return false;
	}

	public String getOriginalClassName() {
		return m_className;
	}

	@Override
	public String getClassName() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("(");
		if (m_propertyName == null) {
			buffer.append("?????, ");
		} else {
			buffer.append(m_propertyName + ", ");
		}
		if (m_elementType == null) {
			buffer.append("?????");
		} else {
			buffer.append(ClassUtils.getShortClassName(m_elementType));
		}
		buffer.append(".class)");
		return m_method + buffer.toString();
	}

	@Override
	public void setClassName(String className) {
		if (!className.startsWith(m_method)) {
			m_cancel = true;
			super.setClassName(className);
		}
	}

	public boolean isCancel() {
		return m_cancel;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getPresentationText() throws Exception {
		return getClassName();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Editing
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void configure(ChooseClassConfiguration configuration) {
		configuration.setValueScope(m_method);
		configuration.addDefaultStart(getClassName());
		configuration.setBaseClassName("org.eclipse.core.databinding.observable.masterdetail.IObservableFactory");
		configuration.setConstructorParameters(ArrayUtils.EMPTY_CLASS_ARRAY);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Code generation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void addSourceCode(List<String> lines) throws Exception {
		String beansClassName =
				m_isPojoBindable
				? DataBindingsCodeUtils.getPojoObservablesClass()
						: "org.eclipse.core.databinding.beans.BeansObservables";
		lines.add("org.eclipse.core.databinding.observable.masterdetail.IObservableFactory "
				+ getVariableIdentifier()
				+ " = "
				+ beansClassName
				+ "."
				+ m_method
				+ "(org.eclipse.core.databinding.observable.Realm.getDefault(), "
				+ m_propertyName
				+ ", "
				+ CoreUtils.getClassName(m_elementType)
				+ ".class"
				+ ");");
	}
}