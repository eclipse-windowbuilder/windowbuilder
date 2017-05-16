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

import org.eclipse.wb.internal.core.utils.check.Assert;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;

/**
 * The {@link Rule} that allows sets properties with given names.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class SetListedPropertiesRule extends Rule {
  private final String[] m_attributeNames;
  private final String[] m_propertyNames;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public SetListedPropertiesRule(String[] propertyNames) {
    this(propertyNames, propertyNames);
  }

  public SetListedPropertiesRule(String[] attributeNames, String[] propertyNames) {
    m_attributeNames = attributeNames;
    m_propertyNames = propertyNames;
    Assert.isTrue(m_attributeNames.length == m_propertyNames.length);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Rule
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void begin(Attributes attributes) throws Exception {
    for (int i = 0; i < m_attributeNames.length; i++) {
      String attributeName = m_attributeNames[i];
      String value = attributes.getValue(attributeName);
      if (value != null) {
        BeanUtils.setProperty(digester.peek(), m_propertyNames[i], value);
      }
    }
  }
}
