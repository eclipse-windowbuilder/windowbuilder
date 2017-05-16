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
import org.eclipse.wb.internal.core.model.description.ConfigurablePropertyDescription;

import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;

/**
 * The {@link Rule} for adding new {@link ConfigurablePropertyDescription}.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class ConfigurablePropertyRule extends AbstractDesignerRule {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Rule
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void begin(String namespace, String name, Attributes attributes) throws Exception {
    String id = getRequiredAttribute(name, attributes, "id");
    String title = getRequiredAttribute(name, attributes, "title");
    // create property
    ConfigurablePropertyDescription propertyDescription = new ConfigurablePropertyDescription();
    propertyDescription.setId(id);
    propertyDescription.setTitle(title);
    // add property
    {
      ComponentDescription componentDescription = (ComponentDescription) digester.peek();
      componentDescription.addConfigurableProperty(propertyDescription);
    }
    // push property
    digester.push(propertyDescription);
  }

  @Override
  public void end(String namespace, String name) throws Exception {
    digester.pop();
  }
}
