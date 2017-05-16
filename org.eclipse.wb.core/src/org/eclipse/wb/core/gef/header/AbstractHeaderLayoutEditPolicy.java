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
package org.eclipse.wb.core.gef.header;

import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;

/**
 * Abstract implementation of {@link LayoutEditPolicy} for headers. It provides additional utilities
 * for interacting with main {@link LayoutEditPolicy} and main {@link IEditPartViewer}.
 *
 * @author scheglov_ke
 * @coverage core.gef.header
 */
public abstract class AbstractHeaderLayoutEditPolicy extends LayoutEditPolicy {
  private final LayoutEditPolicy m_mainPolicy;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractHeaderLayoutEditPolicy(LayoutEditPolicy mainPolicy) {
    m_mainPolicy = mainPolicy;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Target
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean isRequestCondition(Request request) {
    return request.getType() == Request.REQ_MOVE;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Feedback utilities
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Layer} from main {@link IEditPartViewer} with given id.
   */
  protected final Layer getMainLayer(String layerId) {
    return getMainViewer().getLayer(layerId);
  }

  /**
   * @return the main {@link IEditPartViewer}.
   */
  private IEditPartViewer getMainViewer() {
    return m_mainPolicy.getHost().getViewer();
  }
}
