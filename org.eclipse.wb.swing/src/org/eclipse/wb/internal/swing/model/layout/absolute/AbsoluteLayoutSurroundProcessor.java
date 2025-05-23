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
package org.eclipse.wb.internal.swing.model.layout.absolute;

import org.eclipse.wb.internal.core.model.util.surround.ISurroundProcessor;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import java.util.List;

/**
 * {@link ISurroundProcessor} that places enclosing {@link ComponentInfo}'s into same visual
 * positions as they were before enclosing. It works only if source {@link ContainerInfo} has
 * {@link AbsoluteLayoutInfo} and sets also {@link AbsoluteLayoutInfo} on target
 * {@link ContainerInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.model.layout
 */
public final class AbsoluteLayoutSurroundProcessor
implements
ISurroundProcessor<ContainerInfo, ComponentInfo> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final Object INSTANCE = new AbsoluteLayoutSurroundProcessor();

	private AbsoluteLayoutSurroundProcessor() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ISurroundProcessor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean filter(ContainerInfo sourceContainer, ContainerInfo targetContainer)
			throws Exception {
		String targetClassName = targetContainer.getDescription().getComponentClass().getName();
		boolean isJPanel = targetClassName.equals("javax.swing.JPanel");
		return sourceContainer.hasLayout()
				&& sourceContainer.getLayout() instanceof AbsoluteLayoutInfo
				&& isJPanel;
	}

	@Override
	public void move(ContainerInfo sourceContainer,
			ContainerInfo targetContainer,
			List<ComponentInfo> components) throws Exception {
		// set absolute layout for target
		AbsoluteLayoutInfo targetLayout;
		{
			targetLayout = AbsoluteLayoutInfo.createExplicit(targetContainer.getEditor());
			targetContainer.setLayout(targetLayout);
		}
		// prepare expanded bounds for "targetContainer"
		Point locationOffset;
		{
			Rectangle targetBounds =
					(Rectangle) targetContainer.getArbitraryValue(AbsoluteLayoutSurroundSupport.BOUNDS_KEY);
			targetBounds.expand(targetContainer.getInsets());
			// prepare offset for components
			locationOffset = targetBounds.getLocation().getNegated();
			// set for "targetContainer" expanded bounds
			AbsoluteLayoutInfo sourceLayout = (AbsoluteLayoutInfo) sourceContainer.getLayout();
			sourceLayout.command_BOUNDS(
					targetContainer,
					targetBounds.getLocation(),
					targetBounds.getSize());
		}
		// move components
		for (ComponentInfo component : components) {
			Rectangle bounds = component.getModelBounds().getTranslated(locationOffset);
			targetLayout.command_MOVE(component, null);
			targetLayout.command_BOUNDS(component, bounds.getLocation(), bounds.getSize());
		}
	}
}
