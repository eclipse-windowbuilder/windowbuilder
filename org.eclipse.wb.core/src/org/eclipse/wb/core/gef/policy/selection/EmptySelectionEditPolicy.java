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
