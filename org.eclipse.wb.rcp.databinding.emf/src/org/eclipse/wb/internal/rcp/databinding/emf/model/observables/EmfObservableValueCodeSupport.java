/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
import org.eclipse.wb.internal.rcp.databinding.emf.model.bindables.PropertiesSupport;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;

import java.util.List;

/**
 *
 * @author lobas_av
 *
 */
public class EmfObservableValueCodeSupport extends ObservableCodeSupport {
	////////////////////////////////////////////////////////////////////////////
	//
	// ObservableCodeSupport
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
		// add code
		lines.add("org.eclipse.core.databinding.observable.value.IObservableValue "
				+ observable.getVariableIdentifier()
				+ PropertiesSupport.getEMFObservablesCode(observable.getBindableObject(), "observeValue(")
				+ observable.getBindableObject().getReference()
				+ ", "
				+ observable.getBindableProperty().getReference()
				+ ");");
	}
}