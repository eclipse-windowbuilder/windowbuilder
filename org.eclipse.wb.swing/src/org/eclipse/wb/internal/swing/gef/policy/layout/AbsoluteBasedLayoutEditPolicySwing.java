/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.internal.swing.gef.policy.layout;

import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.AbsoluteBasedLayoutEditPolicy;
import org.eclipse.wb.internal.core.laf.BaselineSupportHelper;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.swing.ToolkitProvider;
import org.eclipse.wb.internal.swing.gef.ComponentsLayoutRequestValidator;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.layout.LayoutInfo;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic {@link LayoutEditPolicy} for absolute based Swing layouts.
 *
 * @author mitin_aa
 * @author scheglov_ke
 * @coverage swing.gef.policy
 */
public abstract class AbsoluteBasedLayoutEditPolicySwing
extends
AbsoluteBasedLayoutEditPolicy<ComponentInfo> {
	private final LayoutInfo m_layout;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AbsoluteBasedLayoutEditPolicySwing(LayoutInfo layout) {
		super(layout);
		m_layout = layout;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Requests
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected ILayoutRequestValidator getRequestValidator() {
		return ComponentsLayoutRequestValidator.INSTANCE;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IVisualDataProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public List<ComponentInfo> getAllComponents() {
		List<ComponentInfo> components = new ArrayList<>();
		components.addAll(m_layout.getContainer().getChildrenComponents());
		return components;
	}

	@Override
	public int getBaseline(IAbstractComponentInfo component) {
		return BaselineSupportHelper.getBaseline(component.getObject());
	}

	@Override
	public Dimension getComponentPreferredSize(IAbstractComponentInfo component) {
		ComponentInfo componentInfo = (ComponentInfo) component;
		return componentInfo.getPreferredSize();
	}

	@Override
	public Dimension getContainerSize() {
		IAbstractComponentInfo composite = m_layout.getContainer();
		Rectangle compositeBounds = composite.getModelBounds().getCopy();
		Insets clientAreaInsets = composite.getClientAreaInsets();
		return compositeBounds.shrink(clientAreaInsets).getSize();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Misc
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected ToolkitDescription getToolkit() {
		return ToolkitProvider.DESCRIPTION;
	}
}