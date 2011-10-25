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
package org.eclipse.wb.internal.swing.gef.policy;

import org.eclipse.wb.core.gef.policy.layout.flow.ObjectFlowLayoutEditPolicy;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.swing.gef.ComponentsLayoutRequestValidator;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

/**
 * Abstract {@link LayoutEditPolicy} for Swing flow containers.
 * 
 * @author sablin_aa
 * @coverage swing.gef.policy
 */
public abstract class ComponentFlowLayoutEditPolicy
    extends
      ObjectFlowLayoutEditPolicy<ComponentInfo> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ComponentFlowLayoutEditPolicy(JavaInfo container) {
    super(container);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Requests
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected ILayoutRequestValidator getRequestValidator() {
    return ComponentsLayoutRequestValidator.INSTANCE;
  }
}