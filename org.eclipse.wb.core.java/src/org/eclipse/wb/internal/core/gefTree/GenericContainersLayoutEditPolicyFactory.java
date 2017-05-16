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
package org.eclipse.wb.internal.core.gefTree;

import org.eclipse.wb.core.gefTree.policy.layout.ILayoutEditPolicyFactory;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.gefTree.policy.generic.FlowContainerLayoutEditPolicy;
import org.eclipse.wb.internal.core.gefTree.policy.generic.SimpleContainerLayoutEditPolicy;
import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.generic.FlowContainerFactory;
import org.eclipse.wb.internal.core.model.generic.SimpleContainer;
import org.eclipse.wb.internal.core.model.generic.SimpleContainerFactory;

import java.util.List;

/**
 * {@link ILayoutEditPolicyFactory} for generic simple/flow policies.
 *
 * @author scheglov_ke
 * @coverage core.gefTree
 */
public final class GenericContainersLayoutEditPolicyFactory implements ILayoutEditPolicyFactory {
  ////////////////////////////////////////////////////////////////////////////
  //
  // ILayoutEditPolicyFactory
  //
  ////////////////////////////////////////////////////////////////////////////
  public LayoutEditPolicy createLayoutEditPolicy(EditPart context, Object model) {
    if (model instanceof JavaInfo) {
      JavaInfo layout = (JavaInfo) model;
      // simple containers
      {
        List<SimpleContainer> containers = new SimpleContainerFactory(layout, true).get();
        for (SimpleContainer container : containers) {
          return new SimpleContainerLayoutEditPolicy(layout, container);
        }
      }
      // flow containers
      {
        List<FlowContainer> containers = new FlowContainerFactory(layout, false).get();
        for (FlowContainer container : containers) {
          return new FlowContainerLayoutEditPolicy(layout, container);
        }
      }
    }
    // not found
    return null;
  }
}
