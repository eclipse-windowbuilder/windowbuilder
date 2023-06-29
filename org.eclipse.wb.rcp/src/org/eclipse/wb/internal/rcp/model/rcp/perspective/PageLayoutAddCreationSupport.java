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
package org.eclipse.wb.internal.rcp.model.rcp.perspective;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.VoidInvocationCreationSupport;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.ui.IPageLayout;

/**
 * {@link CreationSupport} for {@link IRenderableInfo}, created using {@link IPageLayout#addXXX()}
 * methods.
 *
 * @author scheglov_ke
 * @coverage rcp.model.rcp
 */
public final class PageLayoutAddCreationSupport extends VoidInvocationCreationSupport {
	private String m_addSource;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public PageLayoutAddCreationSupport(JavaInfo hostJavaInfo, MethodInvocation invocation) {
		super(hostJavaInfo, getMethodDescription(hostJavaInfo, invocation), invocation);
	}

	public PageLayoutAddCreationSupport(JavaInfo hostJavaInfo,
			MethodDescription description,
			String addSource) {
		super(hostJavaInfo, description);
		m_addSource = addSource;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private static MethodDescription getMethodDescription(JavaInfo hostJavaInfo,
			MethodInvocation invocation) {
		String signature = AstNodeUtils.getMethodSignature(invocation);
		return hostJavaInfo.getDescription().getMethod(signature);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Object getObject(Object hostObject) throws Exception {
		return ((IRenderableInfo) m_javaInfo).render();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean canReorder() {
		return true;
	}

	@Override
	public boolean canReparent() {
		return true;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Adding
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String add_getMethodSource() throws Exception {
		Assert.isNotNull(m_addSource);
		return m_addSource;
	}
}
