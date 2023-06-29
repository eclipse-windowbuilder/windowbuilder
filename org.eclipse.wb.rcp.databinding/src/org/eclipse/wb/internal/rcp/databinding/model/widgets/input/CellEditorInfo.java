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
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.rcp.databinding.model.SimpleClassObjectInfo;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;

import java.util.List;

/**
 * Model for {@link org.eclipse.jface.viewers.CellEditor}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public class CellEditorInfo extends SimpleClassObjectInfo {
	private AbstractViewerInputBindingInfo m_viewerBinding;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public CellEditorInfo(AstEditor editor, ClassInstanceCreation creation, String className) {
		if (creation != null && !creation.arguments().isEmpty()) {
			String source = editor.getSource(creation);
			int index = source.indexOf('(');
			className += source.substring(index);
		}
		setClassName(className);
	}

	public CellEditorInfo(AbstractViewerInputBindingInfo viewerBinding, String className)
			throws Exception {
		m_viewerBinding = viewerBinding;
		setClassName0(className);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public void setViewerBinding(AbstractViewerInputBindingInfo viewerBinding) {
		m_viewerBinding = viewerBinding;
	}

	public void setClassName0(String className) throws Exception {
		if (className.indexOf('(') == -1) {
			// prepare viewer control accessor
			String viewerControlAccessCode = m_viewerBinding.getViewer().getReference();
			if (m_viewerBinding instanceof ViewerInputBindingInfo) {
				viewerControlAccessCode += ".getTable()";
			} else {
				viewerControlAccessCode += ".getTree()";
			}
			// find constructor with SWT parent
			ClassLoader classLoader = JavaInfoUtils.getClassLoader(EditorState.getActiveJavaInfo());
			Class<?> cellClass = CoreUtils.load(classLoader, className);
			if (ReflectionUtils.getConstructorBySignature(
					cellClass,
					"<init>(org.eclipse.swt.widgets.Composite)") != null) {
				className += "(" + viewerControlAccessCode + ")";
			} else {
				Class<?> comboCellClass =
						classLoader.loadClass("org.eclipse.jface.viewers.ComboBoxCellEditor");
				if (comboCellClass.isAssignableFrom(cellClass)
						&& ReflectionUtils.getConstructorBySignature(
								cellClass,
								"<init>(org.eclipse.swt.widgets.Composite,java.lang.String[])") != null) {
					className += "(" + viewerControlAccessCode + ", new String[0])";
				}
			}
		}
		setClassName(className);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Source code
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void addSourceCode(List<String> lines, CodeGenerationSupport generationSupport)
			throws Exception {
		if (getVariableIdentifier() == null) {
			setVariableIdentifier(generationSupport.generateLocalName("cellEditor"));
		}
		String defaultCostructor = m_className.indexOf('(') == -1 ? "()" : "";
		lines.add("org.eclipse.jface.viewers.CellEditor "
				+ getVariableIdentifier()
				+ " = new "
				+ m_className
				+ defaultCostructor
				+ ";");
	}
}