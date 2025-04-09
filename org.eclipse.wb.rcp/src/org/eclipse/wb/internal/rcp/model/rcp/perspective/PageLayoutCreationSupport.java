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
package org.eclipse.wb.internal.rcp.model.rcp.perspective;

import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.eval.ExecutionFlowUtils.ExecutionFlowFrameVisitor;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesNoArguments;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.StubMethod;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Implementation of {@link CreationSupport} for {@link IPageLayout} parameter of
 * {@link IPerspectiveFactory}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.rcp
 */
public final class PageLayoutCreationSupport extends CreationSupport {
	private final SingleVariableDeclaration m_parameter;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PageLayoutCreationSupport(SingleVariableDeclaration parameter) {
		m_parameter = parameter;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		return "parameter: " + m_parameter.getName().getIdentifier();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public ASTNode getNode() {
		return m_parameter;
	}

	@Override
	public boolean isJavaInfo(ASTNode node) {
		return node == m_parameter;
	}

	@Override
	public boolean canBeEvaluated() {
		return false;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean canDelete() {
		return false;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Creation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Object create(EvaluationContext context, ExecutionFlowFrameVisitor visitor)
			throws Exception {
		((PageLayoutInfo) m_javaInfo).render();
		//
		return new ByteBuddy() //
				.subclass(m_javaInfo.getDescription().getComponentClass()) //
				.method(ElementMatchers.any()) //
				.intercept(StubMethod.INSTANCE) //
				.method(named("getEditorArea").and(takesNoArguments())) //
				.intercept(FixedValue.value(IPageLayout.ID_EDITOR_AREA)) //
				.method(named("isEditorAreaVisible").and(takesNoArguments())) //
				.intercept(FixedValue.value(true)) //
				.method(named("isFixed").and(takesNoArguments())) //
				.intercept(FixedValue.value(false)) //
				.make() //
				.load(JavaInfoUtils.getClassLoader(m_javaInfo)) //
				.getLoaded() //
				.getConstructor() //
				.newInstance();
	}
}
