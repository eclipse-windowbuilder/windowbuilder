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
package org.eclipse.wb.core.gef.policy.layout.generic;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.gef.policy.layout.generic.FlowContainerLayoutEditPolicy;
import org.eclipse.wb.internal.core.model.generic.FlowContainer;

/**
 * Factory for creating {@link LayoutEditPolicy} for generic containers.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy
 * @deprecated No longer used. This class will be removed after the 2028-12
 *             release.
 */
@Deprecated(since = "2026-12", forRemoval = true)
public final class GenericEditPolicyFactory {
	/**
	 * @return the {@link LayoutEditPolicy} for {@link FlowContainer}.
	 */
	@Deprecated(since = "2026-12", forRemoval = true)
	public static LayoutEditPolicy createFlow(ObjectInfo model, FlowContainer container) {
		return new FlowContainerLayoutEditPolicy(model, container);
	}
}