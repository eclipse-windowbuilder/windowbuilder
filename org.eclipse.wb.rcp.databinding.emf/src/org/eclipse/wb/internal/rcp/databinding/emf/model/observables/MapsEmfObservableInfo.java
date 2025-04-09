/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
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
package org.eclipse.wb.internal.rcp.databinding.emf.model.observables;

import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.rcp.databinding.emf.model.bindables.HierarchySupport;
import org.eclipse.wb.internal.rcp.databinding.emf.model.bindables.PropertiesSupport;
import org.eclipse.wb.internal.rcp.databinding.emf.model.bindables.PropertiesSupport.ClassInfo;
import org.eclipse.wb.internal.rcp.databinding.emf.model.bindables.PropertiesSupport.PropertyInfo;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.MapsBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.KnownElementsObservableInfo;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Model for observable object <code>EMFObservables.observeMaps(...)</code>.
 *
 * @author lobas_av
 * @coverage bindings.rcp.emf.model
 */
public class MapsEmfObservableInfo extends MapsBeanObservableInfo {
	private final PropertiesSupport m_propertiesSupport;
	private List<PropertyInfo> m_emfProperties;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public MapsEmfObservableInfo(ObservableInfo domainObservable, PropertiesSupport propertiesSupport) {
		super(domainObservable, null, null);
		m_propertiesSupport = propertiesSupport;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void setProperties(String[] properties) throws Exception {
		super.setProperties(properties);
		// prepare EMF properties
		List<PropertyInfo> emfProperties = m_propertiesSupport.getProperties(getElementType());
		Assert.isTrue(!CollectionUtils.isEmpty(emfProperties));
		// bind properties to EMF properties
		m_emfProperties = new ArrayList<>();
		//
		for (String propertyName : properties) {
			for (PropertyInfo property : emfProperties) {
				if (propertyName.equals(property.name)) {
					m_emfProperties.add(property);
					break;
				}
			}
		}
		//
		Assert.equals(properties.length, m_emfProperties.size());
	}

	public void setEMFProperties(List<String> referenceProperties) throws Exception {
		Assert.isTrue(!CollectionUtils.isEmpty(referenceProperties));
		// prepare EMF class info
		Object[] firstResult = m_propertiesSupport.getClassInfoForProperty(referenceProperties.get(0));
		Assert.isNotNull(firstResult);
		//
		m_emfProperties = new ArrayList<>();
		List<String> properties = new ArrayList<>();
		HierarchySupport hierarchySupport = new HierarchySupport(m_propertiesSupport, false);
		// prepare EMF properties
		for (String propertyReference : referenceProperties) {
			Object[] result = m_propertiesSupport.getClassInfoForProperty(propertyReference);
			Assert.isNotNull(result);
			ClassInfo classInfo = (ClassInfo) result[0];
			Assert.isNotNull(classInfo.thisClass);
			hierarchySupport.addClass(classInfo);
			//
			for (PropertyInfo property : classInfo.properties) {
				if (propertyReference.equals(property.reference)) {
					m_emfProperties.add(property);
					properties.add(property.name);
					break;
				}
			}
		}
		// prepare element type
		ClassInfo lastClassInfo = hierarchySupport.getLastClass();
		setElementType(lastClassInfo.thisClass);
		//
		Assert.equals(referenceProperties.size(), m_emfProperties.size());
		super.setProperties(properties.toArray(new String[properties.size()]));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Code generation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void addSourceCode(List<String> lines, CodeGenerationSupport generationSupport)
			throws Exception {
		// prepare variable
		if (getVariableIdentifier() == null) {
			setVariableIdentifier(generationSupport.generateLocalName("observeMaps"));
		}
		// prepare properties
		StringBuffer properties = new StringBuffer();
		for (Iterator<PropertyInfo> I = m_emfProperties.iterator(); I.hasNext();) {
			PropertyInfo property = I.next();
			properties.append(property.reference);
			if (I.hasNext()) {
				properties.append(", ");
			}
		}
		// add code
		KnownElementsObservableInfo domainObservable =
				(KnownElementsObservableInfo) getDomainObservable();
		//
		lines.add("org.eclipse.core.databinding.observable.map.IObservableMap[] "
				+ getVariableIdentifier()
				+ m_propertiesSupport.getEMFObservablesCode("observeMaps(")
				+ domainObservable.getSourceCode()
				+ ", new org.eclipse.emf.ecore.EStructuralFeature[]{"
				+ properties.toString()
				+ "});");
	}
}