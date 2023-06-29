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
import org.eclipse.wb.internal.rcp.databinding.model.ObservableCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.properties.ValuePropertyCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.SwtProperties;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.properties.WidgetPropertiesCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.properties.WidgetPropertyTextCodeSupport;

import org.eclipse.swt.SWT;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Source code generator for observe CellEditor properties.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public final class CellEditorValuePropertyCodeSupport extends ObservableCodeSupport {
	private CellEditorControlPropertyCodeSupport m_master;
	private WidgetPropertiesCodeSupport m_detail;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public CellEditorValuePropertyCodeSupport(CellEditorControlPropertyCodeSupport master,
			WidgetPropertiesCodeSupport detail) {
		m_master = master;
		m_detail = detail;
	}

	public CellEditorValuePropertyCodeSupport(ValuePropertyCodeSupport value) {
		this(value.getParserPropertyReference());
	}

	public CellEditorValuePropertyCodeSupport(String parsePropertyReference) {
		setParsePropertyReference(parsePropertyReference);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public String getParsePropertyReference() {
		return (m_master == null ? "\"" : "\"control.")
				+ (m_detail == null ? "null" : getParsePropertyReference(m_detail.getPropertyReference()))
				+ "\"";
	}

	public void setParsePropertyReference(String parsePropertyReference) {
		if (getParsePropertyReference().equals(parsePropertyReference)) {
			return;
		}
		if (parsePropertyReference.startsWith("\"control.")) {
			if (m_master == null) {
				m_master = new CellEditorControlPropertyCodeSupport();
			}
			String propertyReference =
					getParsePropertyReference(StringUtils.substringBetween(parsePropertyReference, ".", "\""));
			m_detail = createDetail(propertyReference);
		} else {
			m_master = null;
			String propertyReference =
					getParsePropertyReference(StringUtils.substringBetween(parsePropertyReference, "\"", "\""));
			m_detail = createDetail(propertyReference);
		}
	}

	private WidgetPropertiesCodeSupport createDetail(String propertyReference) {
		return "observeText".equals(propertyReference)
				? new WidgetPropertyTextCodeSupport(new int[]{SWT.Modify})
						: new WidgetPropertiesCodeSupport(propertyReference);
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
		if (getVariableIdentifier() == null) {
			setVariableIdentifier(generationSupport.generateLocalName("cellEditorProperty"));
		}
		if (m_master == null) {
			// simple property
			lines.add("org.eclipse.core.databinding.property.value.IValueProperty "
					+ getVariableIdentifier()
					+ " = org.eclipse.core.databinding.beans.typed.BeanProperties.value("
					+ getParsePropertyReference()
					+ ");");
		} else {
			// control.YYY property
			String sourceCode;
			if (m_master.getVariableIdentifier() == null) {
				sourceCode = "org.eclipse.jface.databinding.viewers.CellEditorProperties.control()";
			} else {
				m_master.addSourceCode(null, lines, generationSupport);
				sourceCode = m_master.getVariableIdentifier();
			}
			lines.add("org.eclipse.core.databinding.property.value.IValueProperty "
					+ getVariableIdentifier()
					+ " = "
					+ sourceCode
					+ ".value("
					+ m_detail.getSourceCode()
					+ ");");
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utilities
	//
	////////////////////////////////////////////////////////////////////////////
	public static String getParsePropertyReference(String reference) {
		String parseReference = SwtProperties.SWT_OBSERVABLES_TO_WIDGET_PROPERTIES.get(reference);
		return parseReference == null ? reference : parseReference;
	}
}