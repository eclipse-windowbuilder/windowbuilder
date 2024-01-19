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
package org.eclipse.wb.internal.rcp.databinding.model.widgets.input;

import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Source code generator for owner {@link ViewerInputBindingInfo}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public class ViewerCodeSupport extends CodeSupport {
	private final ViewerInputBindingInfo m_binding;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ViewerCodeSupport(ViewerInputBindingInfo binding) {
		m_binding = binding;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Code generation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void addSourceCode(List<String> lines, CodeGenerationSupport generationSupport)
			throws Exception {
		generationSupport.addSourceCode(m_binding.getInputObservable(), lines);
		//
		Class<?> elementType = m_binding.getElementType();
		String[] properties = m_binding.getLabelProvider().getMapsObservable().getProperties();
		String propertiesSourceCode =
				ObservableInfo.isPojoBean(elementType)
				? "org.eclipse.core.databinding.beans.typed.PojoProperties"
						: "org.eclipse.core.databinding.beans.typed.BeanProperties";
		//
		lines.add("org.eclipse.jface.databinding.viewers.ViewerSupport.bind("
				+ m_binding.getViewer().getReference()
				+ ", "
				+ m_binding.getInputObservable().getVariableIdentifier()
				+ ", "
				+ propertiesSourceCode
				+ ".values("
				+ CoreUtils.getClassName(elementType)
				+ ".class, new java.lang.String[]{\""
				+ StringUtils.join(properties, "\", \"")
				+ "\"}));");
	}
}