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
package org.eclipse.wb.internal.rcp.databinding.model.beans.observables.properties;

import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.standard.BeanObservableDetailCodeSupport;

import java.util.List;

/**
 * Model for observable object {@code ISetProperty.observeDetail(...)}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
public class SetPropertyDetailCodeSupport extends BeanObservableDetailCodeSupport {
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
		String sourceCode =
				observable.isPojoBindable0()
				? "org.eclipse.core.databinding.beans.typed.PojoProperties"
						: "org.eclipse.core.databinding.beans.typed.BeanProperties";
		String beanClassCode =
				observable.getDetailBeanClass() == null
				? ""
						: CoreUtils.getClassName(observable.getDetailBeanClass()) + ".class, ";
		sourceCode +=
				".set("
						+ beanClassCode
						+ observable.getDetailPropertyReference()
						+ ", "
						+ CoreUtils.getClassName(observable.getDetailPropertyType())
						+ ".class)";
		if (getVariableIdentifier() != null) {
			lines.add("org.eclipse.core.databinding.beans.IBeanSetProperty "
					+ getVariableIdentifier()
					+ " = "
					+ sourceCode
					+ ";");
			sourceCode = getVariableIdentifier();
		}
		// add code
		lines.add("org.eclipse.core.databinding.observable.set.IObservableSet "
				+ observable.getVariableIdentifier()
				+ " = "
				+ sourceCode
				+ ".observeDetail("
				+ masterObservable.getVariableIdentifier()
				+ ");");
	}
}