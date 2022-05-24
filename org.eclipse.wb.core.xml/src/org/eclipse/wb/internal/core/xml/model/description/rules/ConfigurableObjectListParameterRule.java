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
package org.eclipse.wb.internal.core.xml.model.description.rules;

import org.eclipse.wb.internal.core.model.description.rules.AbstractDesignerRule;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.xml.model.description.internal.AbstractConfigurableDescription;

import org.apache.commons.digester3.Rule;
import org.xml.sax.Attributes;

/**
 * The {@link Rule} that sets value of {@link AbstractConfigurableDescription} parameter.
 *
 * @author scheglov_ke
 * @coverage XML.model.description
 */
public final class ConfigurableObjectListParameterRule extends AbstractDesignerRule {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Rule
  //
  ////////////////////////////////////////////////////////////////////////////
  private String m_name;
  private String m_value;

  @Override
  public void begin(String namespace, String name, Attributes attributes) throws Exception {
    m_name = getRequiredAttribute(name, attributes, "name");
  }

  @Override
  public void body(String namespace, String name, String text) throws Exception {
    m_value = text;
    Assert.isNotNull(m_value, "Body text for <" + name + "> required.");
  }

  @Override
  public void end(String namespace, String name) throws Exception {
    AbstractConfigurableDescription description =
        (AbstractConfigurableDescription) getDigester().peek();
    description.addListParameter(m_name, m_value);
  }
}
