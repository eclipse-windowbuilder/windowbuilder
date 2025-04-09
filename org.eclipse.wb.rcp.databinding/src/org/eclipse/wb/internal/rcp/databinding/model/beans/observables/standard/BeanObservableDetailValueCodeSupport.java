/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
import org.eclipse.wb.internal.rcp.databinding.model.DataBindingsCodeUtils;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailBeanObservableInfo;

import java.util.List;

/**
 * Model for observable object {@code BeanProperties.value(...).observeDetail(...)}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
public class BeanObservableDetailValueCodeSupport extends BeanObservableDetailCodeSupport {
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
		String masterTypeCode =
				observable.getDetailBeanClass() == null || observable.isPojoBindable0()
				? ""
						: CoreUtils.getClassName(observable.getDetailBeanClass()) + ".class, ";
		lines.add("org.eclipse.core.databinding.observable.value.IObservableValue "
				+ observable.getVariableIdentifier()
				+ " = "
				+ DataBindingsCodeUtils.getObservableClass(observable)
				+ ".value("
				+ masterTypeCode
				+ observable.getDetailPropertyReference()
				+ ", "
				+ CoreUtils.getClassName(observable.getDetailPropertyType())
				+ ".class).observeDetail("
				+ masterObservable.getVariableIdentifier()
				+ ");");

	}
}