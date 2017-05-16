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
package org.eclipse.wb.internal.gef.tree.policies;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.events.IEditPartSelectionListener;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.requests.Request;

/**
 * A {@link EditPolicy} that is sensitive to the host's selection.
 * <P>
 * This {@link EditPolicy} adds itself as an {@link IEditPartSelectionListener} so that it can
 * observe selection. When selection or focus changes, the {@link EditPolicy} will update itself and
 * call the appropriate methods.
 *
 * @author scheglov_ke
 * @coverage gef.tree
 */
public final class SelectionEditPolicy extends EditPolicy {
  @Override
  public boolean understandsRequest(Request request) {
    return request.getType() == Request.REQ_SELECTION;
  }

  @Override
  public EditPart getTargetEditPart(Request request) {
    return getHost();
  }
}
