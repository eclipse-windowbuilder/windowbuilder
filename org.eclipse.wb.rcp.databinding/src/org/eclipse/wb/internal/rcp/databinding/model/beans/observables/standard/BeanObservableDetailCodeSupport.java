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
package org.eclipse.wb.internal.rcp.databinding.model.beans.observables.standard;

import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailBeanObservableInfo;

import java.util.List;

/**
 * Abstract model for all detail observable.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
public abstract class BeanObservableDetailCodeSupport extends ObservableCodeSupport {
	////////////////////////////////////////////////////////////////////////////
	//
	// Code generation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void addSourceCode(ObservableInfo observable,
			List<String> lines,
			CodeGenerationSupport generationSupport) throws Exception {
		DetailBeanObservableInfo detailObservable = (DetailBeanObservableInfo) observable;
		ObservableInfo masterObservable = detailObservable.getMasterObservable();
		// prepare variable
		if (observable.getVariableIdentifier() == null) {
			observable.setVariableIdentifier(generationSupport.generateLocalName(
					observable.getBindableObject().getReference(),
					detailObservable.getDetailPropertyReference(),
					"ObserveDetail" + detailObservable.getPresentationPrefix()));
		}
		// add master observable code
		generationSupport.addSourceCode(masterObservable, lines);
		// add code
		addDetailSourceCode(lines, generationSupport, detailObservable, masterObservable);
	}

	/**
	 * @return the source code for create this observable.
	 */
	protected abstract void addDetailSourceCode(List<String> lines,
			CodeGenerationSupport generationSupport,
			DetailBeanObservableInfo observable,
			ObservableInfo masterObservable) throws Exception;
}