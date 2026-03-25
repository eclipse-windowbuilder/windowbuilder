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
package org.eclipse.wb.core.gef.policy;

/**
 * The listener interface for receiving basic events from an
 * {@link IDesignEditPolicy}.
 *
 * @author lobas_av
 * @coverage gef.core
 * @since 1.24
 */
public interface IEditPolicyListener {
	/**
	 * Called when given {@link IDesignEditPolicy} has activate.
	 */
	void activatePolicy(IDesignEditPolicy policy);

	/**
	 * Called when given {@link IDesignEditPolicy} has deactivate.
	 */
	void deactivatePolicy(IDesignEditPolicy policy);
}