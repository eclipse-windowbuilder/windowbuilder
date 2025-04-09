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

import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.AstObjectInfoVisitor;
import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.properties.ValuePropertyCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.context.DataBindingContextInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetBindableInfo;

import java.util.List;

/**
 * Model for handle
 * {@code ObservableValueEditingSupport.create(viewer, dbc, cellEditor, cellEditorProperty,
 * elementProperty)}.
 *
 * @author lobas_av
 */
public final class EditingSupportInfo extends AstObjectInfo {
	private WidgetBindableInfo m_viewerColumn;
	private final AbstractViewerInputBindingInfo m_viewerBinding;
	private final CellEditorInfo m_cellEditorInfo;
	private final CellEditorValuePropertyCodeSupport m_cellEditorProperty;
	private final ValuePropertyCodeSupport m_elementProperty;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public EditingSupportInfo(AbstractViewerInputBindingInfo viewerBinding,
			CellEditorInfo cellEditorInfo,
			CellEditorValuePropertyCodeSupport cellEditorProperty,
			ValuePropertyCodeSupport elementProperty) {
		m_viewerBinding = viewerBinding;
		m_cellEditorInfo = cellEditorInfo;
		m_cellEditorProperty = cellEditorProperty;
		m_elementProperty = elementProperty;
	}

	public EditingSupportInfo(AbstractViewerInputBindingInfo viewerBinding,
			WidgetBindableInfo viewerColumn,
			String cellEditorClassName,
			String cellEditorProperty,
			String elementProperty) throws Exception {
		m_viewerBinding = viewerBinding;
		m_viewerColumn = viewerColumn;
		m_cellEditorInfo = new CellEditorInfo(m_viewerBinding, cellEditorClassName);
		m_cellEditorProperty = new CellEditorValuePropertyCodeSupport(cellEditorProperty);
		m_elementProperty = new ValuePropertyCodeSupport();
		m_elementProperty.setParserPropertyReference(elementProperty);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public WidgetBindableInfo getViewerColumn() {
		return m_viewerColumn;
	}

	public void setViewerColumn(WidgetBindableInfo viewerColumn) {
		m_viewerColumn = viewerColumn;
		m_viewerBinding.getEditingSupports().add(this);
		m_cellEditorInfo.setViewerBinding(m_viewerBinding);
	}

	public AbstractViewerInputBindingInfo getViewerBinding() {
		return m_viewerBinding;
	}

	public CellEditorInfo getCellEditorInfo() {
		return m_cellEditorInfo;
	}

	public CellEditorValuePropertyCodeSupport getCellEditorProperty() {
		return m_cellEditorProperty;
	}

	public ValuePropertyCodeSupport getElementProperty() {
		return m_elementProperty;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Visiting
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void accept(AstObjectInfoVisitor visitor) throws Exception {
		super.accept(visitor);
		m_cellEditorInfo.accept(visitor);
		m_cellEditorProperty.accept(visitor);
		m_elementProperty.accept(visitor);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Code generation
	//
	////////////////////////////////////////////////////////////////////////////
	public void addSourceCode(DataBindingContextInfo context,
			List<String> lines,
			CodeGenerationSupport generationSupport) throws Exception {
		m_cellEditorInfo.addSourceCode(lines, generationSupport);
		m_cellEditorProperty.addSourceCode(null, lines, generationSupport);
		m_elementProperty.addSourceCode(null, lines, generationSupport);
		//
		String sourceCode =
				"org.eclipse.jface.databinding.viewers.ObservableValueEditingSupport.create("
						+ m_viewerBinding.getViewer().getReference()
						+ ", "
						+ context.getVariableIdentifier()
						+ ", "
						+ m_cellEditorInfo.getVariableIdentifier()
						+ ", "
						+ m_cellEditorProperty.getVariableIdentifier()
						+ ", "
						+ m_elementProperty.getVariableIdentifier()
						+ ")";
		//
		if (getVariableIdentifier() == null) {
			lines.add(m_viewerColumn.getReference() + ".setEditingSupport(" + sourceCode + ");");
		} else {
			lines.add("org.eclipse.jface.viewers.EditingSupport "
					+ getVariableIdentifier()
					+ " = "
					+ sourceCode
					+ ";");
			lines.add(m_viewerColumn.getReference()
					+ ".setEditingSupport("
					+ getVariableIdentifier()
					+ "));");
		}
	}
}