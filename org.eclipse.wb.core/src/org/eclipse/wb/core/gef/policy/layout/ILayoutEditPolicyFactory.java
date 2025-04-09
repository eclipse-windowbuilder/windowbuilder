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
package org.eclipse.wb.core.gef.policy.layout;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;

/**
 * Factory for creating {@link LayoutEditPolicy}'s for layout models.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy
 */
public interface ILayoutEditPolicyFactory {
	/**
	 * @return the {@link LayoutEditPolicy} for given model.
	 */
	LayoutEditPolicy createLayoutEditPolicy(EditPart context, Object model);
}
