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
package org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.standard;

import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.SingleSelectionObservableInfo;

import java.util.List;

/**
 * Model for observable object <code>ViewersObservables.observeSingleSelection(...)</code>.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public class SingleSelectionObservableCodeSupport extends ObservableCodeSupport {
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
					observable.getBindableProperty().getReference()));
		}
		SingleSelectionObservableInfo selectionObservable = (SingleSelectionObservableInfo) observable;
		if (selectionObservable.getDelayValue() == 0) {
			// no delay
			lines.add("org.eclipse.core.databinding.observable.value.IObservableValue "
					+ observable.getVariableIdentifier()
					+ " = org.eclipse.jface.databinding.viewers.ViewersObservables."
					+ observable.getBindableProperty().getReference()
					+ "("
					+ observable.getBindableObject().getReference()
					+ ");");
		} else {
			// with delay
			lines.add("org.eclipse.core.databinding.observable.value.IObservableValue "
					+ observable.getVariableIdentifier()
					+ " = org.eclipse.jface.databinding.viewers.ViewersObservables.observeDelayedValue("
					+ Integer.toString(selectionObservable.getDelayValue())
					+ ", org.eclipse.jface.databinding.viewers.ViewersObservables."
					+ observable.getBindableProperty().getReference()
					+ "("
					+ observable.getBindableObject().getReference()
					+ "));");
		}
	}
}