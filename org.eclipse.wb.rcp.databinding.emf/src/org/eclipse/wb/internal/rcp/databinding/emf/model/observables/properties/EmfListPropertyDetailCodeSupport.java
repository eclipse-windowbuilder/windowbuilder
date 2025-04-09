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
package org.eclipse.wb.internal.rcp.databinding.emf.model.observables.properties;

import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.rcp.databinding.emf.model.observables.DetailEmfObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.standard.BeanObservableDetailCodeSupport;

import java.util.List;

/**
 *
 *
 * @author lobas_av
 *
 */
public class EmfListPropertyDetailCodeSupport extends BeanObservableDetailCodeSupport {
	////////////////////////////////////////////////////////////////////////////
	//
	// Code generation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void addDetailSourceCode(List<String> lines,
			CodeGenerationSupport generationSupport,
			DetailBeanObservableInfo observable,
			ObservableInfo masterObservable) throws Exception {
		DetailEmfObservableInfo emfObservable = (DetailEmfObservableInfo) observable;
		//
		String sourceCode =
				emfObservable.getPropertiesSupport().getEMFPropertiesCode("list(")
				+ emfObservable.getDetailPropertyReference()
				+ ")";
		if (getVariableIdentifier() != null) {
			lines.add("org.eclipse.core.databinding.beans.IBeanListProperty "
					+ getVariableIdentifier()
					+ sourceCode
					+ ";");
			sourceCode = " = " + getVariableIdentifier();
		}
		// add code
		lines.add("org.eclipse.core.databinding.observable.list.IObservableList "
				+ observable.getVariableIdentifier()
				+ sourceCode
				+ ".observeDetail("
				+ masterObservable.getVariableIdentifier()
				+ ");");
	}
}