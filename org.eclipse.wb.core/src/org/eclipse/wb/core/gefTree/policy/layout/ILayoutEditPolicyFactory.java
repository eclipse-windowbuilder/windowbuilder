/*******************************************************************************
 * Copyright (c) 2011, 2026 Google, Inc. and others.
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

import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;

import org.eclipse.gef.EditPart;

/**
 * Factory for creating {@link LayoutEditPolicy}'s for layout models.
 *
 * @author scheglov_ke
 * @coverage core.gefTree.policy
 */
public interface ILayoutEditPolicyFactory {
	/**
	 * @return the {@link LayoutEditPolicy} for given model.
	 */
	LayoutEditPolicy createLayoutEditPolicy(EditPart context, Object model);
}
