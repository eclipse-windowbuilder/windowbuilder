/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.internal.gef.tree.policies;

import org.eclipse.wb.gef.core.policies.EditPolicy;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartListener;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;

/**
 * A {@link EditPolicy} that is sensitive to the host's selection.
 * <P>
 * This {@link EditPolicy} adds itself as an {@link EditPartListener} so that it can
 * observe selection. When selection or focus changes, the {@link EditPolicy} will update itself and
 * call the appropriate methods.
 *
 * @author scheglov_ke
 * @coverage gef.tree
 */
public final class SelectionEditPolicy extends EditPolicy {
	@Override
	public boolean understandsRequest(Request request) {
		return request.getType() == RequestConstants.REQ_SELECTION;
	}

	@Override
	public EditPart getTargetEditPart(Request request) {
		return getHost();
	}
}
