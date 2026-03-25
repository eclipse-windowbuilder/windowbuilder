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

import org.eclipse.gef.EditPolicy;

/**
 * @since 1.24
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IDesignEditPolicy extends EditPolicy {

	/**
	 * Adds a listener to the {@link IDesignEditPolicy}.
	 */
	void addEditPolicyListener(IEditPolicyListener listener);

	/**
	 * Removes the first occurrence of the specified listener from the list of
	 * listeners. Does nothing if the listener was not present.
	 */
	void removeEditPolicyListener(IEditPolicyListener listener);
}
