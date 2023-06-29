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

import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.parser.IModelResolver;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.properties.WidgetPropertiesCodeSupport;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.util.List;

/**
 * Model for observable object <code>CellEditorProperties.control()</code>.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public final class CellEditorControlPropertyCodeSupport extends ObservableCodeSupport {
	private static final String VALUE_SIGNATURE =
			"org.eclipse.core.databinding.property.value.IValueProperty.value(org.eclipse.core.databinding.property.value.IValueProperty)";

	////////////////////////////////////////////////////////////////////////////
	//
	// Parser
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public AstObjectInfo parseExpression(AstEditor editor,
			String signature,
			MethodInvocation invocation,
			Expression[] arguments,
			IModelResolver resolver,
			IDatabindingsProvider provider) throws Exception {
		if (VALUE_SIGNATURE.equals(signature)) {
			WidgetPropertiesCodeSupport detailCodeSupport =
					(WidgetPropertiesCodeSupport) resolver.getModel(arguments[0]);
			Assert.isNotNull(detailCodeSupport);
			return new CellEditorValuePropertyCodeSupport(this, detailCodeSupport);
		}
		return null;
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
		lines.add("org.eclipse.jface.databinding.viewers.CellEditorProperties "
				+ getVariableIdentifier()
				+ " = org.eclipse.jface.databinding.viewers.CellEditorProperties.control();");
	}
}