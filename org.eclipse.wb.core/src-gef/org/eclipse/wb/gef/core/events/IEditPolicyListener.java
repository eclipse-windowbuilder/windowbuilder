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