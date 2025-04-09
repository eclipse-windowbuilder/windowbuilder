/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
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
package org.eclipse.wb.gef.core.policies;

import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;

import org.eclipse.gef.EditPart;

/**
 * The listener interface for receiving decorate/undecorate events from {@link LayoutEditPolicy}.
 *
 * @author scheglov_ke
 * @coverage gef.core
 */
public interface IEditPartDecorationListener {
	/**
	 * Notifies that {@link EditPart} should be decorated.
	 */
	void decorate(EditPart child);

	/**
	 * Notifies that {@link EditPart} should be undecorated.
	 */
	void undecorate(EditPart child);
}