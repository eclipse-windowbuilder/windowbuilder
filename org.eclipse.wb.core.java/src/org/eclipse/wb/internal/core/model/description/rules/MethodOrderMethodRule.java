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
package org.eclipse.wb.internal.core.model.description.rules;

import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.model.order.MethodOrder;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;

/**
 * The {@link Rule} that {@link MethodOrder} for single {@link MethodDescription}.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class MethodOrderMethodRule extends AbstractDesignerRule {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Rule
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void begin(String namespace, String name, Attributes attributes) throws Exception {
    // prepare order
    MethodOrder order;
    {
      String specification = getRequiredAttribute(name, attributes, "order");
      order = MethodOrder.parse(specification);
    }
    // prepare method
    MethodDescription methodDescription;
    {
      String signature = getRequiredAttribute(name, attributes, "signature");
      ComponentDescription componentDescription = (ComponentDescription) digester.peek();
      methodDescription = componentDescription.getMethod(signature);
      Assert.isNotNull(
          methodDescription,
          "Can not find method %s for %s.",
          signature,
          componentDescription);
    }
    // set order
    methodDescription.setOrder(order);
  }
}
