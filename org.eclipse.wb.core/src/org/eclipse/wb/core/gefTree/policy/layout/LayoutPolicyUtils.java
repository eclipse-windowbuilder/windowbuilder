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
package org.eclipse.wb.core.gefTree.policy.layout;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;

import java.util.List;

/**
 * Contains utilities for {@link LayoutEditPolicy}'s.
 *
 * @author scheglov_ke
 * @coverage core.gefTree.policy
 */
public class LayoutPolicyUtils {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	private LayoutPolicyUtils() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// LayoutEditPolicy creation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link LayoutEditPolicy} for given model.
	 */
	public static LayoutEditPolicy createLayoutEditPolicy(EditPart context, Object model) {
		List<ILayoutEditPolicyFactory> factories =
				ExternalFactoriesHelper.getElementsInstances(
						ILayoutEditPolicyFactory.class,
						"org.eclipse.wb.core.treeEditPolicyFactories",
						"factory");
		// try to create policy
		for (ILayoutEditPolicyFactory factory : factories) {
			LayoutEditPolicy layoutEditPolicy = factory.createLayoutEditPolicy(context, model);
			if (layoutEditPolicy != null) {
				return layoutEditPolicy;
			}
		}
		// not found
		return null;
	}
}
