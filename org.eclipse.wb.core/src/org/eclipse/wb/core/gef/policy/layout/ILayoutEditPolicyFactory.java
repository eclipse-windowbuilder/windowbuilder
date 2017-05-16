/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
