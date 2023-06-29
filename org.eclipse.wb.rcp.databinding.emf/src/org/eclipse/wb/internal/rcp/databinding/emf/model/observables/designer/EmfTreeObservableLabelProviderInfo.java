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
package org.eclipse.wb.internal.rcp.databinding.emf.model.observables.designer;

import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassConfiguration;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.rcp.databinding.emf.model.bindables.PropertiesSupport;
import org.eclipse.wb.internal.rcp.databinding.emf.model.bindables.PropertiesSupport.PropertyInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.KnownElementsObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.designer.TreeObservableLabelProviderInfo;

import java.util.List;

/**
 * Model for {@link org.eclipse.wb.rcp.databinding.EMFTreeObservableLabelProvider}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.emf.model
 */
public class EmfTreeObservableLabelProviderInfo extends TreeObservableLabelProviderInfo {
	private static final String PROVIDER_CLASS =
			"org.eclipse.wb.rcp.databinding.EMFTreeObservableLabelProvider";
	private final PropertiesSupport m_propertiesSupport;
	private PropertyInfo m_textProperty;
	private PropertyInfo m_imageProperty;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public EmfTreeObservableLabelProviderInfo(String className,
			KnownElementsObservableInfo allElementsObservable,
			PropertiesSupport propertiesSupport) {
		super(className, allElementsObservable);
		m_propertiesSupport = propertiesSupport;
	}

	public EmfTreeObservableLabelProviderInfo(KnownElementsObservableInfo allElementsObservable,
			PropertiesSupport propertiesSupport) {
		this(PROVIDER_CLASS, allElementsObservable, propertiesSupport);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void setTextProperty(String textProperty) throws Exception {
		super.setTextProperty(textProperty);
		//
		m_textProperty = m_propertiesSupport.getProperty(getElementType(), textProperty);
		Assert.isNotNull(m_textProperty);
	}

	public void setEMFTextProperty(String textPropertyReference) throws Exception {
		if (textPropertyReference == null) {
			m_textProperty = null;
			super.setTextProperty(null);
		} else {
			Object[] result = m_propertiesSupport.getClassInfoForProperty(textPropertyReference);
			Assert.isNotNull(result);
			//
			m_textProperty = (PropertyInfo) result[1];
			super.setTextProperty(m_textProperty.name);
		}
	}

	@Override
	public void setImageProperty(String imageProperty) throws Exception {
		super.setImageProperty(imageProperty);
		//
		m_imageProperty = m_propertiesSupport.getProperty(getElementType(), imageProperty);
		Assert.isNotNull(m_imageProperty);
	}

	public void setEMFImageProperty(String imagePropertyReference) throws Exception {
		if (imagePropertyReference == null) {
			m_imageProperty = null;
			super.setImageProperty(null);
		} else {
			Object[] result = m_propertiesSupport.getClassInfoForProperty(imagePropertyReference);
			Assert.isNotNull(result);
			//
			m_imageProperty = (PropertyInfo) result[1];
			super.setImageProperty(m_imageProperty.name);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Editing
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void configure(ChooseClassConfiguration configuration, boolean useClear) {
		configuration.setValueScope(PROVIDER_CLASS);
		if (useClear) {
			configuration.setClearValue(PROVIDER_CLASS);
		}
		configuration.setBaseClassName(PROVIDER_CLASS);
		//
		Class<?> iObservableSetClass = m_propertiesSupport.getIObservableSetClass();
		if (iObservableSetClass != null) {
			Class<?> eStructuralFeature = m_propertiesSupport.getEStructuralFeature();
			configuration.setConstructorParameters(new Class[]{
					iObservableSetClass,
					eStructuralFeature,
					eStructuralFeature});
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Code generation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getSourceCode(List<String> lines, CodeGenerationSupport generationSupport)
			throws Exception {
		String textProperty = m_textProperty == null ? "null" : m_textProperty.reference;
		String imageProperty = m_imageProperty == null ? "null" : m_imageProperty.reference;
		return "new "
		+ m_className
		+ "("
		+ getAllElementsObservable().getSourceCode()
		+ ", "
		+ textProperty
		+ ", "
		+ imageProperty
		+ ")";
	}
}