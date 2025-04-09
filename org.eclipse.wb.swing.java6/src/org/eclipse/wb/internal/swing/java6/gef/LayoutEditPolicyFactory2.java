/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.internal.swing.java6.gef;

import org.eclipse.wb.core.gef.policy.layout.ILayoutEditPolicyFactory;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.model.creation.IImplicitCreationSupport;
import org.eclipse.wb.internal.layout.group.model.IGroupLayoutInfo;
import org.eclipse.wb.internal.swing.java6.model.GroupLayoutInfo2;

import org.eclipse.core.runtime.IAdaptable;

/**
 * Implementation of {@link ILayoutEditPolicyFactory} for Java6 Swing. "2" is for developers to not
 * confuse this with main Swing factory.
 *
 * @author mitin_aa
 * @coverage swing.gef.policy
 */
public final class LayoutEditPolicyFactory2 implements ILayoutEditPolicyFactory {
	////////////////////////////////////////////////////////////////////////////
	//
	// ILayoutEditPolicyFactory
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public LayoutEditPolicy createLayoutEditPolicy(EditPart context, Object model) {
		if (model instanceof GroupLayoutInfo2) {
			if (!(((GroupLayoutInfo2) model).getCreationSupport() instanceof IImplicitCreationSupport)) {
				IAdaptable adaptable = (IAdaptable) model;
				IGroupLayoutInfo layoutInfo = adaptable.getAdapter(IGroupLayoutInfo.class);
				if (layoutInfo != null) {
					return new SwingGroupLayoutEditPolicy2(layoutInfo);
				}
			}
		}
		// not found
		return null;
	}
}
