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
package org.eclipse.wb.internal.rcp.model.jface;

import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.TopBoundsSupport;
import org.eclipse.wb.internal.core.model.property.converter.IntegerConverter;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.swt.model.widgets.CompositeTopBoundsSupport;
import org.eclipse.wb.internal.swt.support.ControlSupport;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.swt.graphics.Point;

import java.util.List;

/**
 * Implementation of {@link TopBoundsSupport} for {@link WindowInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.jface
 */
public class WindowTopBoundsSupport extends TopBoundsSupport {
	private final WindowInfo m_window;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public WindowTopBoundsSupport(WindowInfo window) {
		super(window);
		m_window = window;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// TopBoundsSupport
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void apply() throws Exception {
		// check for getInitialSize()
		if (get_getInitialSize_Point() != null) {
			return;
		}
		// set size from resource properties (or default)
		{
			Dimension size = getResourceSize();
			ControlSupport.setSize(m_window.getShell(), size.width, size.height);
		}
	}

	@Override
	public void setSize(int width, int height) throws Exception {
		// check for getInitialSize()
		{
			ClassInstanceCreation pointCreation = get_getInitialSize_Point();
			if (pointCreation != null) {
				String widthSource = IntegerConverter.INSTANCE.toJavaSource(m_window, width);
				String heightSource = IntegerConverter.INSTANCE.toJavaSource(m_window, height);
				AstEditor editor = m_window.getEditor();
				List<Expression> arguments = DomGenerics.arguments(pointCreation);
				editor.replaceExpression(arguments.get(0), widthSource);
				editor.replaceExpression(arguments.get(1), heightSource);
			}
		}
		// remember size in resource properties
		setResourceSize(width, height);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Show
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean show() throws Exception {
		CompositeTopBoundsSupport.show(m_window, m_window.getShell());
		return true;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Size utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link ClassInstanceCreation} for new {@link Point} in
	 *         <code>getInitialSize()</code>, or <code>null</code> if something different is returned.
	 */
	private ClassInstanceCreation get_getInitialSize_Point() {
		TypeDeclaration typeDeclaration = JavaInfoUtils.getTypeDeclaration(m_window);
		MethodDeclaration method =
				AstNodeUtils.getMethodBySignature(typeDeclaration, "getInitialSize()");
		if (method != null) {
			List<Statement> statements = DomGenerics.statements(method.getBody());
			Statement lastStatement = statements.get(statements.size() - 1);
			if (lastStatement instanceof ReturnStatement returnStatement) {
				if (returnStatement.getExpression() instanceof ClassInstanceCreation) {
					return (ClassInstanceCreation) returnStatement.getExpression();
				}
			}
		}
		// unknown state
		return null;
	}
}