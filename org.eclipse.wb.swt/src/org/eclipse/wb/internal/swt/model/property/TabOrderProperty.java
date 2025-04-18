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
package org.eclipse.wb.internal.swt.model.property;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildTree;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.swt.model.ModelMessages;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;

import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Property for editing controls tab order.
 *
 * @author lobas_av
 * @coverage swt.property.order
 */
public class TabOrderProperty
extends
org.eclipse.wb.internal.core.model.property.order.TabOrderProperty {
	private static final String TAB_METHOD_SIGNATURE =
			"setTabList(org.eclipse.swt.widgets.Control[])";
	private static final String TITLE_TOOLTIP = ModelMessages.TabOrderProperty_tooltip;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public TabOrderProperty(CompositeInfo composite) {
		super(composite);
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
			Object expression = invocation.arguments().get(0);
			if (expression instanceof ArrayCreation creation) {
				return creation.getInitializer();
			}
		}
		return null;
	}

	@Override
	protected void removePropertyAssociation() throws Exception {
		m_container.removeMethodInvocations(TAB_METHOD_SIGNATURE);
	}

	@Override
	protected void setOrderedArraySource(String source) throws Exception {
		String newSource = "new org.eclipse.swt.widgets.Control[]" + source;
		MethodInvocation invocation = getMethodInvocation();
		if (invocation == null) {
			m_container.addMethodInvocation(TAB_METHOD_SIGNATURE, newSource);
		} else {
			Expression argument = DomGenerics.arguments(invocation).get(0);
			m_container.replaceExpression(argument, newSource);
		}
	}

	@Override
	protected MethodInvocation getMethodInvocation() {
		return m_container.getMethodInvocation(TAB_METHOD_SIGNATURE);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Children
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected List<? extends AbstractComponentInfo> getTabPossibleChildren() throws Exception {
		CompositeInfo composite = (CompositeInfo) m_container;
		List<AbstractComponentInfo> children = new ArrayList<>();
		for (AbstractComponentInfo child : composite.getChildrenControls()) {
			// ask listeners if child should be displayed
			boolean[] visible = new boolean[]{true};
			visible[0] &= child.getPresentation().isVisible();
			m_container.getBroadcast(ObjectInfoChildTree.class).invoke(child, visible);
			// check if we can add this child
			if (visible[0]) {
				children.add(child);
			}
		}
		return children;
	}

	@Override
	protected boolean isDefaultOrdered(AbstractComponentInfo component) throws Exception {
		return !JavaInfoUtils.hasTrueParameter(component, "tabOrder.isNotOrdered");
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