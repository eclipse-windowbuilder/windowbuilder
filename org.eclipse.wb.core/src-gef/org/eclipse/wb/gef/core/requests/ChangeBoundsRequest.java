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
package org.eclipse.wb.gef.core.requests;

import org.eclipse.gef.EditPart;

/**
 * A {@link Request} to change the bounds of the {@link EditPart}(s).
 *
 * @author lobas_av
 * @coverage gef.core
 * @deprecated Use {@link org.eclipse.gef.requests.ChangeBoundsRequest
 *             ChangeBoundsRequest} directly.
 */
@Deprecated(forRemoval = true, since = "2026-06")
public class ChangeBoundsRequest extends org.eclipse.gef.requests.ChangeBoundsRequest {
}