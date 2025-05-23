/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.properties;

import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetPropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.ItemsSwtObservableInfo;

import java.util.List;

/**
 * Model for observable object <code>WidgetProperties.items()</code>.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public class WidgetPropertyItemsCodeSupport extends AbstractWidgetPropertiesCodeSupport {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public WidgetPropertyItemsCodeSupport() {
		super("observeItems",
				"org.eclipse.jface.databinding.swt.IWidgetListProperty.observe(org.eclipse.swt.widgets.Control)",
				null);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Parser
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected ObservableInfo createObservable(WidgetBindableInfo bindableWidget,
			WidgetPropertyBindableInfo bindableProperty,
			int delayValue) throws Exception {
		return new ItemsSwtObservableInfo(bindableWidget, bindableProperty);
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
					"Items",
					observable.getBindableObject().getReference(),
					"ObserveWidget"));
		}
		// add code
		String code = " = org.eclipse.jface.databinding.swt.typed.WidgetProperties.items()";
		if (getVariableIdentifier() != null) {
			if (generationSupport.addModel(this)) {
				lines.add("org.eclipse.jface.databinding.viewers.IViewerValueProperty "
						+ getVariableIdentifier()
						+ code
						+ ";");
			}
			code = " = " + getVariableIdentifier();
		}
		lines.add("org.eclipse.core.databinding.observable.list.IObservableList "
				+ observable.getVariableIdentifier()
				+ code
				+ ".observe("
				+ observable.getBindableObject().getReference()
				+ ");");
	}
}