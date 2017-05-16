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

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.model.order.MethodOrder;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;

import java.util.List;

/**
 * The {@link Rule} that {@link MethodOrder} for multiple {@link MethodDescription}s.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class MethodOrderMethodsRule extends AbstractDesignerRule {
  private MethodOrder m_order;
  private List<String> m_signatures;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Rule
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void begin(String namespace, String name, Attributes attributes) throws Exception {
    // prepare order
    {
      String specification = getRequiredAttribute(name, attributes, "order");
      m_order = MethodOrder.parse(specification);
    }
    // push List for signatures
    {
      m_signatures = Lists.newArrayList();
      digester.push(m_signatures);
    }
  }

  @Override
  public void end(String namespace, String name) throws Exception {
    digester.pop();
    ComponentDescription componentDescription = (ComponentDescription) digester.peek();
    for (String signature : m_signatures) {
      // prepare method
      MethodDescription methodDescription;
      {
        methodDescription = componentDescription.getMethod(signature);
        Assert.isNotNull(
            methodDescription,
            "Can not find method %s for %s.",
            signature,
            componentDescription);
      }
      // set order
      methodDescription.setOrder(m_order);
    }
    // clean up
    m_order = null;
    m_signatures = null;
  }
}
