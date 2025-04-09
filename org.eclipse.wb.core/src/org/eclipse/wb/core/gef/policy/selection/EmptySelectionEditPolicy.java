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
package org.eclipse.wb.core.gef.policy.selection;

import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;

import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link SelectionEditPolicy} that does not show any selection feedback.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy
 */
public final class EmptySelectionEditPolicy extends SelectionEditPolicy {
	@Override
	protected List<Handle> createSelectionHandles() {
		return Collections.emptyList();
	}
}
