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
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.standard.BeanObservableDetailCodeSupport;

import java.util.List;

/**
 *
 * @author lobas_av
 *
 */
public class EmfObservableDetailListCodeSupport extends BeanObservableDetailCodeSupport {
	////////////////////////////////////////////////////////////////////////////
	//
	// BeanObservableDetailCodeSupport
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void addDetailSourceCode(List<String> lines,
			CodeGenerationSupport generationSupport,
			DetailBeanObservableInfo observable,
			ObservableInfo masterObservable) throws Exception {
		DetailEmfObservableInfo emfObservable = (DetailEmfObservableInfo) observable;
		//
		lines.add("org.eclipse.core.databinding.observable.list.IObservableList "
				+ observable.getVariableIdentifier()
				+ emfObservable.getPropertiesSupport().getEMFObservablesCode(
						"observeDetailList(org.eclipse.core.databinding.observable.Realm.getDefault(), ")
				+ masterObservable.getVariableIdentifier()
				+ ", "
				+ emfObservable.getDetailPropertyReference()
				+ ");");
	}
}