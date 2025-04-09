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
package org.eclipse.wb.internal.rcp.databinding.model.beans.observables.standard;

import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.CollectionObservableInfo;

import java.util.List;

/**
 * Model for observable object {@link org.eclipse.core.databinding.observable.set.WritableSet}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
public class WritableSetCodeSupport extends ObservableCodeSupport {
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
			observable.setVariableIdentifier(generationSupport.generateLocalName("WritableSet"));
		}
		// add code
		CollectionObservableInfo collectionObservable = (CollectionObservableInfo) observable;
		lines.add("org.eclipse.core.databinding.observable.set.WritableSet "
				+ observable.getVariableIdentifier()
				+ " = new org.eclipse.core.databinding.observable.set.WritableSet("
				+ observable.getBindableObject().getReference()
				+ ", "
				+ CoreUtils.getClassName(collectionObservable.getElementType())
				+ ".class);");
	}
}