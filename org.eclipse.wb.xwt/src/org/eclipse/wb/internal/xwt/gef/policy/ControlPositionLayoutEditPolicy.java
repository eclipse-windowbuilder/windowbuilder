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
package org.eclipse.wb.internal.xwt.gef.policy;

import org.eclipse.wb.core.gef.policy.layout.position.ObjectPositionLayoutEditPolicy;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.internal.swt.gef.ControlsLayoutRequestValidator;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;

/**
 * {@link ObjectPositionLayoutEditPolicy} for XWT toolkit.
 *
 * @author sablin_aa
 * @coverage XWT.gef.policy
 */
public abstract class ControlPositionLayoutEditPolicy<D>
    extends
      ObjectPositionLayoutEditPolicy<ControlInfo, D> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ControlPositionLayoutEditPolicy(ObjectInfo host) {
    super(host);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Requests
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected ILayoutRequestValidator getRequestValidator() {
    return ControlsLayoutRequestValidator.INSTANCE;
  }
}
