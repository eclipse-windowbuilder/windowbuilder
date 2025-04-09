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
package org.eclipse.wb.internal.rcp.databinding.model.widgets.input;

import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;

import java.util.List;

/**
 * Model for {@link org.eclipse.jface.databinding.viewers.ObservableListContentProvider}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public final class ObservableListContentProviderInfo
extends
ObservableCollectionContentProviderInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public ObservableListContentProviderInfo() {
		super("org.eclipse.jface.databinding.viewers.ObservableListContentProvider");
	}

	public ObservableListContentProviderInfo(String className) {
		super(className);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Code generation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void addSourceCode(List<String> lines, CodeGenerationSupport generationSupport)
			throws Exception {
		// prepare variable
		if (getVariableIdentifier() == null) {
			setVariableIdentifier(generationSupport.generateLocalName("listContentProvider"));
		}
		// add code
		lines.add("org.eclipse.jface.databinding.viewers.ObservableListContentProvider "
				+ getVariableIdentifier()
				+ " = new "
				+ m_className
				+ "();");
	}
}