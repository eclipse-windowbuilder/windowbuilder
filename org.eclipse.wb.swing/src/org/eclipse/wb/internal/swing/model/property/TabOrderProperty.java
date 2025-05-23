/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.internal.swing.model.property;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.ObjectInfoVisitor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.swing.model.ModelMessages;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Property for editing components tab order.
 *
 * @author lobas_av
 * @coverage swing.property.order
 */
public class TabOrderProperty
extends
org.eclipse.wb.internal.core.model.property.order.TabOrderProperty {
	private static final String FOCUS_TRAVERSAL_CLASS = "org.eclipse.wb.swing.FocusTraversalOnArray";
	private static final String FOCUS_TRAVERSAL_METHOD_SIGNATURE =
			"setFocusTraversalPolicy(java.awt.FocusTraversalPolicy)";
	private static final String TITLE_TOOLTIP = ModelMessages.TabOrderProperty_tooltip;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public TabOrderProperty(ContainerInfo container) {
		super(container);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Value
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected ArrayInitializer getOrderedArray() throws Exception {
		MethodInvocation invocation = getMethodInvocation();
		if (invocation != null) {
			Object traversalPolicy = invocation.arguments().get(0);
			if (traversalPolicy instanceof ClassInstanceCreation traversalPolicyCreation) {
				if (FOCUS_TRAVERSAL_CLASS.equals(AstNodeUtils.getFullyQualifiedName(
						traversalPolicyCreation,
						false))) {
					Object focusTraversalOnArray = traversalPolicyCreation.arguments().get(0);
					if (focusTraversalOnArray instanceof ArrayCreation creation) {
						return creation.getInitializer();
					}
				}
			}
		}
		return null;
	}

	@Override
	protected void removePropertyAssociation() throws Exception {
		m_container.removeMethodInvocations(FOCUS_TRAVERSAL_METHOD_SIGNATURE);
	}

	@Override
	protected void setOrderedArraySource(String source) throws Exception {
		String newSource =
				"new org.eclipse.wb.swing.FocusTraversalOnArray(new java.awt.Component[]" + source + ")";
		MethodInvocation invocation = getMethodInvocation();
		if (invocation == null) {
			ProjectUtils.ensureResourceType(
					m_container.getEditor().getJavaProject(),
					m_container.getDescription().getToolkit().getBundle(),
					FOCUS_TRAVERSAL_CLASS);
			m_container.addMethodInvocation(FOCUS_TRAVERSAL_METHOD_SIGNATURE, newSource);
		} else {
			Expression argument = DomGenerics.arguments(invocation).get(0);
			m_container.replaceExpression(argument, newSource);
		}
	}

	@Override
	protected MethodInvocation getMethodInvocation() {
		return m_container.getMethodInvocation(FOCUS_TRAVERSAL_METHOD_SIGNATURE);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Children
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected List<? extends AbstractComponentInfo> getTabPossibleChildren() throws Exception {
		final List<AbstractComponentInfo> children = new ArrayList<>();
		m_container.accept(new ObjectInfoVisitor() {
			@Override
			public boolean visit(ObjectInfo objectInfo) throws Exception {
				if (objectInfo != m_container && objectInfo instanceof ComponentInfo) {
					children.add((AbstractComponentInfo) objectInfo);
				}
				return true;
			}
		});
		return children;
	}

	@Override
	protected boolean isDefaultOrdered(AbstractComponentInfo component) throws Exception {
		return true;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Delete
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected boolean hasOrderElement(ObjectInfo parent, ObjectInfo child) throws Exception {
		while (parent != null) {
			if (parent == m_container) {
				return true;
			}
			parent = parent.getParent();
		}
		return false;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tooltip
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getPropertyTooltipText() {
		return TITLE_TOOLTIP;
	}
}