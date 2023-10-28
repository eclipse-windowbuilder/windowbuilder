/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swing.gef.part;

import org.eclipse.wb.core.editor.constants.IEditorPreferenceConstants;
import org.eclipse.wb.core.gef.policy.TabOrderContainerEditPolicy;
import org.eclipse.wb.core.gef.policy.layout.LayoutPolicyUtils;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.policies.IRefreshableEditPolicy;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.swing.gef.policy.layout.DropLayoutEditPolicy;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.LayoutInfo;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

/**
 * {@link EditPart} for {@link ContainerInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.gef.part
 */
public final class ContainerEditPart extends ComponentEditPart {
	private final ContainerInfo m_container;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ContainerEditPart(ContainerInfo container) {
		super(container);
		m_container = container;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Figure
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void drawCustomBorder(Figure figure, Graphics graphics) {
		try {
			if (m_container.shouldDrawDotsBorder()) {
				graphics.setForegroundColor(IColorConstants.gray);
				graphics.setLineStyle(SWT.LINE_DOT);
				Rectangle area = figure.getClientArea();
				graphics.drawRectangle(0, 0, area.width - 1, area.height - 1);
			}
		} catch (Throwable e) {
		}
	}

	@Override
	protected void addChildVisual(org.eclipse.gef.EditPart childPart, int index) {
		super.addChildVisual(childPart, getFigure().getChildren().size() - index);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Policies
	//
	////////////////////////////////////////////////////////////////////////////
	private LayoutInfo m_currentLayout;

	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
		// support for dropping LayoutInfo's
		if (m_container.canSetLayout()) {
			installEditPolicy(new DropLayoutEditPolicy(m_container));
		}
		// support tab ordering for children
		installEditPolicy(
				TabOrderContainerEditPolicy.TAB_CONTAINER_ROLE,
				new TabOrderContainerEditPolicy());
	}

	@Override
	protected void refreshEditPolicies() {
		super.refreshEditPolicies();
		// support for dropping components
		if (m_container.hasLayout()) {
			LayoutInfo layout = m_container.getLayout();
			if (m_currentLayout != layout) {
				m_currentLayout = layout;

				if (layout.getDescription().getComponentClass() != null) {
					if (!InstanceScope.INSTANCE.getNode(IEditorPreferenceConstants.P_AVAILABLE_LAYOUTS_NODE)
							.getBoolean(layout.getDescription().getComponentClass().getName(), true)) {
						try {
							m_currentLayout = m_container.getDefaultContainerInfo();
							m_container.setLayout(m_currentLayout);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				LayoutEditPolicy policy = LayoutPolicyUtils.createLayoutEditPolicy(this, layout);
				installEditPolicy(EditPolicy.LAYOUT_ROLE, policy);

			} else {
				EditPolicy policy = getEditPolicy(EditPolicy.LAYOUT_ROLE);
				if (policy instanceof IRefreshableEditPolicy) {
					((IRefreshableEditPolicy) policy).refreshEditPolicy();
				}
			}
		}
	}
}
