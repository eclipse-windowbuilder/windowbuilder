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
package org.eclipse.wb.internal.rcp.databinding.emf.model.observables.properties;

import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.rcp.databinding.emf.model.bindables.EObjectBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.emf.model.bindables.EPropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.emf.model.bindables.PropertiesSupport;
import org.eclipse.wb.internal.rcp.databinding.emf.model.observables.DetailValueEmfObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.emf.model.observables.ValueEmfObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;

import java.util.List;

/**
 *
 * @author lobas_av
 *
 */
public class EmfValuePropertyCodeSupport extends EmfPropertiesCodeSupport {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public EmfValuePropertyCodeSupport() {
		super("org.eclipse.core.databinding.property.value.IValueProperty");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Parser
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected ObservableInfo createObservable(EObjectBindableInfo eObject,
			EPropertyBindableInfo eProperty) {
		return new ValueEmfObservableInfo(eObject, eProperty);
	}

	@Override
	protected ObservableInfo createDetailObservable(ObservableInfo masterObservable,
			PropertiesSupport propertiesSupport) throws Exception {
		Assert.isNotNull(m_parserPropertyReference);
		//
		DetailValueEmfObservableInfo observeDetailValue =
				new DetailValueEmfObservableInfo(masterObservable, propertiesSupport);
		observeDetailValue.setDetailPropertyReference(null, m_parserPropertyReference);
		observeDetailValue.setCodeSupport(new EmfValuePropertyDetailCodeSupport());
		//
		return observeDetailValue;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Code generation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void addSourceCode(ObservableInfo observable,
			List<String> lines,
			CodeGenerationSupport generationSupport) throws Exception {
		// prepare variable
		if (observable.getVariableIdentifier() == null) {
			observable.setVariableIdentifier(generationSupport.generateLocalName(
					observable.getBindableObject().getReference(),
					observable.getBindableProperty().getPresentation().getText(),
					"ObserveValue"));
		}
		//
		String sourceCode =
				PropertiesSupport.getEMFPropertiesCode(observable.getBindableObject(), "value(")
				+ observable.getBindableProperty().getReference()
				+ ")";
		if (getVariableIdentifier() != null) {
			if (generationSupport.addModel(this)) {
				if (generationSupport.addModel(this)) {
					lines.add("org.eclipse.core.databinding.beans.IBeanValueProperty "
							+ getVariableIdentifier()
							+ sourceCode
							+ ";");
				}
			}
			sourceCode = getVariableIdentifier();
		}
		// add code
		lines.add("org.eclipse.core.databinding.observable.value.IObservableValue "
				+ observable.getVariableIdentifier()
				+ sourceCode
				+ ".observe("
				+ observable.getBindableObject().getReference()
				+ ");");
	}
}