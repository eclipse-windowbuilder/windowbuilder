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
package org.eclipse.wb.internal.rcp.databinding.model.beans.observables;

import org.eclipse.wb.internal.core.databinding.model.AstObjectInfoVisitor;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassAndPropertiesConfiguration;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.Messages;
import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.BindingUiContentProviderContext;
import org.eclipse.wb.internal.rcp.databinding.ui.contentproviders.ObservableDetailUiContentProvider;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Abstract model for observable objects <code>BeansObservables.observeDetailXXX(...)</code>.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
public abstract class DetailBeanObservableInfo extends ObservableInfo {
	private static ChooseClassAndPropertiesConfiguration m_configuration;
	protected final ObservableInfo m_masterObservable;
	protected Class<?> m_detailBeanClass;
	protected String m_detailPropertyReference;
	protected Class<?> m_detailPropertyType;
	protected boolean m_isPojoBindable;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DetailBeanObservableInfo(ObservableInfo masterObservable,
			Class<?> detailBeanClass,
			String detailPropertyReference,
			Class<?> detailPropertyType) {
		m_masterObservable = masterObservable;
		m_detailBeanClass = detailBeanClass;
		m_detailPropertyReference = detailPropertyReference;
		m_detailPropertyType = detailPropertyType;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public final ObservableInfo getMasterObservable() {
		return m_masterObservable;
	}

	public final Class<?> getDetailBeanClass() {
		return m_detailBeanClass;
	}

	public final String getDetailPropertyReference() {
		return m_detailPropertyReference;
	}

	public void setDetailPropertyReference(Class<?> detailBeanClass, String detailPropertyReference)
			throws Exception {
		if (!detailBeanClass.equals(m_detailBeanClass)) {
			m_detailBeanClass = detailBeanClass;
			m_isPojoBindable = isPojoBean(m_detailBeanClass);
		}
		m_detailPropertyReference = detailPropertyReference;
	}

	public final Class<?> getDetailPropertyType() {
		return m_detailPropertyType;
	}

	public final void setDetailPropertyType(Class<?> detailPropertyType) {
		m_detailPropertyType = detailPropertyType;
	}

	public final boolean isPojoBindable0() {
		return m_isPojoBindable;
	}

	public void setPojoBindable(boolean isPojoBindable) {
		m_isPojoBindable = isPojoBindable;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ObservableInfo
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final BindableInfo getBindableObject() {
		return m_masterObservable.getBindableObject();
	}

	@Override
	public final BindableInfo getBindableProperty() {
		return m_masterObservable.getBindableProperty();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getPresentationText() throws Exception {
		String presentationProperty = StringUtils.defaultIfEmpty(m_detailPropertyReference, "?????");
		String presentationPropertyType =
				m_detailPropertyType == null ? "?????" : ClassUtils.getShortClassName(m_detailPropertyType);
		return m_masterObservable.getPresentationText()
				+ ".detail"
				+ getPresentationPrefix()
				+ "("
				+ presentationProperty
				+ ", "
				+ presentationPropertyType
				+ ".class)";
	}

	/**
	 * @return presentation prefix for this observable.
	 */
	public abstract String getPresentationPrefix();

	////////////////////////////////////////////////////////////////////////////
	//
	// Editing
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void createContentProviders(List<IUiContentProvider> providers,
			BindingUiContentProviderContext context,
			DatabindingsProvider provider) throws Exception {
		m_masterObservable.createContentProviders(providers, context, provider);
		providers.add(new ObservableDetailUiContentProvider(getConfiguration(), this, provider));
	}

	protected ChooseClassAndPropertiesConfiguration getConfiguration() {
		if (m_configuration == null) {
			m_configuration = new ChooseClassAndPropertiesConfiguration();
			m_configuration.setDialogFieldLabel(Messages.DetailBeanObservableInfo_objectLabel);
			m_configuration.setValueScope("beans");
			m_configuration.setChooseInterfaces(true);
			m_configuration.setEmptyClassErrorMessage(Messages.DetailBeanObservableInfo_errorMessage);
			m_configuration.setErrorMessagePrefix(Messages.DetailBeanObservableInfo_errorMessagePrefix);
			m_configuration.setPropertiesLabel(Messages.DetailBeanObservableInfo_propertiesLabel);
			m_configuration.setPropertiesErrorMessage(Messages.DetailBeanObservableInfo_propertiesErrorMessage);
		}
		return m_configuration;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Visiting
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final void accept(AstObjectInfoVisitor visitor) throws Exception {
		super.accept(visitor);
		m_masterObservable.accept(visitor);
	}
}