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

import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.requests.IDropRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.tree.TreeEditPart;
import org.eclipse.wb.internal.gef.tree.TreeViewer;

import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Implementation of {@link EditPolicy} for {@link TreeViewer} and {@link TreeEditPart}'s, that
 * automatically expands {@link TreeItem}'s, when {@link Request#REQ_CREATE} or
 * {@link Request#REQ_PASTE} are received.
 *
 * @author scheglov_ke
 * @coverage gef.tree
 */
public final class AutoExpandEditPolicy extends EditPolicy {
  @Override
  public boolean understandsRequest(Request request) {
    return request.getType() == Request.REQ_CREATE || request.getType() == Request.REQ_PASTE;
  }

  @Override
  public EditPart getTargetEditPart(Request request) {
    // prepare host widget
    final TreeEditPart host = (TreeEditPart) getHost();
    final TreeItem hostWidget = host.getWidget();
    final Tree tree = hostWidget.getParent();
    // prepare target widget
    TreeItem targetWidget;
    {
      IDropRequest dropRequest = (IDropRequest) request;
      Point location = dropRequest.getLocation();
      targetWidget = tree.getItem(location.getSwtPoint());
    }
    // if mouse cursor is above our "host", expand it
    if (targetWidget == hostWidget && !hostWidget.getExpanded() && hostWidget.getItemCount() != 0) {
      tree.getShell().getDisplay().asyncExec(new Runnable() {
        public void run() {
          if (!hostWidget.isDisposed()) {
            hostWidget.setExpanded(true);
          }
        }
      });
    }
    // we don't perform any real checks
    return null;
  }
}