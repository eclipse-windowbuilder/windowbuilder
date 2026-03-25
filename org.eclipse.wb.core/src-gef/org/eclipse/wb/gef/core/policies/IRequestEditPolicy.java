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
package org.eclipse.wb.gef.core.policies;

import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;

/**
 * Optional interface for edit policies to notify them if a {@link Request} was
 * sent to an {@link EditPart}.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public interface IRequestEditPolicy extends EditPolicy {
	/**
	 * Performs the specified Request. This method can be used to send a generic
	 * message to an EditPolicy.
	 */
	void performRequest(Request request);
}
