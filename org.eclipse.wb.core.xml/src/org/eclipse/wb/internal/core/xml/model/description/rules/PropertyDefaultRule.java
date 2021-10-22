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

import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.xml.model.description.GenericPropertyDescription;

import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;

/**
 * {@link Rule} that sets the default value of current {@link GenericPropertyDescription}.
 *
 * @author scheglov_ke
 * @coverage XML.model.description
 */
public final class PropertyDefaultRule extends Rule {
  private final ClassLoader m_classLoader;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PropertyDefaultRule(ClassLoader classLoader) {
    m_classLoader = classLoader;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Rule
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void begin(String namespace, String name, Attributes attributes) throws Exception {
    GenericPropertyDescription propertyDescription = (GenericPropertyDescription) digester.peek();
    if (propertyDescription != null) {
      String text = attributes.getValue("value");
      Object value = ScriptUtils.evaluate(m_classLoader, text);
      propertyDescription.setDefaultValue(value);
    }
  }
}
