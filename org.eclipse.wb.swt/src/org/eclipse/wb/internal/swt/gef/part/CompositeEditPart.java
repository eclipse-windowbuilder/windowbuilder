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
 *    Marcel du Preez - Updated refreshEditPolicies to include a preference check
 *                      and set a default should the original layout not be allowed
 *******************************************************************************/
package org.eclipse.wb.internal.swt.gef.part;

import org.eclipse.wb.core.editor.constants.IEditorPreferenceConstants;
import org.eclipse.wb.core.gef.policy.TabOrderContainerEditPolicy;
import org.eclipse.wb.core.gef.policy.layout.LayoutPolicyUtils;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.IRefreshableEditPolicy;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.swt.gef.policy.layout.DefaultLayoutEditPolicy;
import org.eclipse.wb.internal.swt.gef.policy.layout.DropLayoutEditPolicy;
import org.eclipse.wb.internal.swt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPolicy;
import org.eclipse.swt.SWT;

/**
 * {@link EditPart} for {@link CompositeInfo}.
 *
 * @author lobas_av
 * @coverage swt.gef.part
 */
public class CompositeEditPart extends ControlEditPart {
	private final CompositeInfo m_composite;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public CompositeEditPart(CompositeInfo composite) {
		super(composite);
		m_composite = composite;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Figure
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void drawCustomBorder(IFigure figure, Graphics graphics) {
		try {
			if (m_composite.shouldDrawDotsBorder()) {
				graphics.setForegroundColor(ColorConstants.gray);
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
		if (m_composite.hasLayout()) {
			installEditPolicy(new DropLayoutEditPolicy(m_composite));
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

		if (m_composite.hasLayout()) {
			LayoutInfo layout = m_composite.getLayout();
			if (layout != m_currentLayout) {
				try {
					m_currentLayout = layout;
					if (layout.getDescription().getComponentClass() != null) {
						if (!InstanceScope.INSTANCE.getNode(
								IEditorPreferenceConstants.P_AVAILABLE_LAYOUTS_NODE).getBoolean(
										layout.getDescription().getComponentClass().getName(),
										true)) {
							// Gets the default layout if the layout was originally set with a layout
							// that is no longer available due to preference settings
							m_currentLayout = m_composite.getDefaultCompositeInfo();
							m_composite.setLayout(m_currentLayout);
						}
					}
					LayoutEditPolicy policy = LayoutPolicyUtils.createLayoutEditPolicy(this, m_currentLayout);
					if (policy == null) {
						policy = new DefaultLayoutEditPolicy();
					}
					installEditPolicy(EditPolicy.LAYOUT_ROLE, policy);

				} catch (Exception e) {

					e.printStackTrace();
				}

			} else {
				EditPolicy policy = getEditPolicy(EditPolicy.LAYOUT_ROLE);
				if (policy instanceof IRefreshableEditPolicy) {
					((IRefreshableEditPolicy) policy).refreshEditPolicy();
				}
			}

		}
	}
}
