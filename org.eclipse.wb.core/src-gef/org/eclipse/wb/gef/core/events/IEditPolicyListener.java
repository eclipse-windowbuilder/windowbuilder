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
package org.eclipse.wb.gef.core.events;

import org.eclipse.wb.gef.core.policies.EditPolicy;

/**
 * The listener interface for receiving basic events from an {@link EditPolicy}.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public interface IEditPolicyListener {
  /**
   * Called when given {@link EditPolicy} has activate.
   */
  void activatePolicy(EditPolicy policy);

  /**
   * Called when given {@link EditPolicy} has deactivate.
   */
  void deactivatePolicy(EditPolicy policy);
}