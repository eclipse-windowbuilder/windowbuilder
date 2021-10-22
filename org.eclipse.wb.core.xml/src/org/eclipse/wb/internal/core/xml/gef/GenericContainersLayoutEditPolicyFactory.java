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
package org.eclipse.wb.internal.core.xml.gef;

import org.eclipse.wb.core.gef.policy.layout.ILayoutEditPolicyFactory;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.gef.policy.layout.generic.FlowContainerLayoutEditPolicy;
import org.eclipse.wb.internal.core.gef.policy.layout.generic.SimpleContainerLayoutEditPolicy;
import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.generic.SimpleContainer;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.generic.FlowContainerFactory;
import org.eclipse.wb.internal.core.xml.model.generic.SimpleContainerFactory;

import java.util.List;

/**
 * {@link ILayoutEditPolicyFactory} for generic simple/flow policies.
 *
 * @author scheglov_ke
 * @coverage XML.gef
 */
public final class GenericContainersLayoutEditPolicyFactory implements ILayoutEditPolicyFactory {
  ////////////////////////////////////////////////////////////////////////////
  //
  // ILayoutEditPolicyFactory
  //
  ////////////////////////////////////////////////////////////////////////////
  public LayoutEditPolicy createLayoutEditPolicy(EditPart context, Object model) {
    if (model instanceof XmlObjectInfo) {
      XmlObjectInfo layout = (XmlObjectInfo) model;
      // simple containers
      {
        List<SimpleContainer> containers = new SimpleContainerFactory(layout, true).get();
        for (SimpleContainer container : containers) {
          return new SimpleContainerLayoutEditPolicy(layout, container);
        }
      }
      // flow containers
      {
        List<FlowContainer> containers = new FlowContainerFactory(layout, true).get();
        for (FlowContainer container : containers) {
          return new FlowContainerLayoutEditPolicy(layout, container);
        }
      }
    }
    // not found
    return null;
  }
}
