/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swt.model.layout;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaInfoSetObjectAfter;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.IClipboardImplicitCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.IImplicitCreationSupport;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.swt.widgets.Control;

/**
 * Implementation of {@link CreationSupport} for implicit {@link LayoutDataInfo}.
 *
 * @author lobas_av
 * @coverage swt.model.layout
 */
public final class ImplicitLayoutDataCreationSupport extends CreationSupport
implements
IImplicitCreationSupport {
	private final ControlInfo m_controlInfo;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ImplicitLayoutDataCreationSupport(ControlInfo controlInfo) {
		m_controlInfo = controlInfo;
		m_controlInfo.addBroadcastListener(new JavaInfoSetObjectAfter() {
			@Override
			public void invoke(JavaInfo target, Object object) throws Exception {
				if (target == m_controlInfo) {
					m_javaInfo.setObject(((Control) object).getLayoutData());
				}
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		Class<?> layoutClass = getComponentClass();
		return "implicit-layout-data: " + layoutClass.getName();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean isJavaInfo(ASTNode node) {
		if (node instanceof MethodInvocation invocation) {
			return invocation.arguments().isEmpty()
					&& invocation.getName().getIdentifier().equals("getLayoutData")
					&& m_controlInfo.isRepresentedBy(invocation.getExpression());
		}
		return false;
	}

	@Override
	public ASTNode getNode() {
		return m_controlInfo.getCreationSupport().getNode();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Add
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String add_getSource(NodeTarget target) throws Exception {
		String layoutClassName = m_javaInfo.getDescription().getComponentClass().getName();
		return TemplateUtils.format("({0}) {1}.getLayoutData()", layoutClassName, m_controlInfo);
	}

	@Override
	public void add_setSourceExpression(Expression expression) throws Exception {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Delete
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean canDelete() {
		return true;
	}

	@Override
	public void delete() throws Exception {
		JavaInfoUtils.deleteJavaInfo(m_javaInfo, false);
		// if implicit layout data was materialized, so has real variable, restore implicit variable
		if (!(m_javaInfo.getVariableSupport() instanceof ImplicitLayoutDataVariableSupport)) {
			m_javaInfo.setVariableSupport(new ImplicitLayoutDataVariableSupport(m_javaInfo));
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IClipboardImplicitCreationSupport
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public IClipboardImplicitCreationSupport getImplicitClipboard() {
		return null;
	}
}