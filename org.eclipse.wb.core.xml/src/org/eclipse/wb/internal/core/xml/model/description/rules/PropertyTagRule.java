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
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.description.GenericPropertyDescription;

import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;

/**
 * {@link Rule} that adds some tag for {@link GenericPropertyDescription}.
 * 
 * @author scheglov_ke
 * @coverage XML.model.description
 */
public final class PropertyTagRule extends AbstractDesignerRule {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Rule
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void begin(String namespace, String name, Attributes attributes) throws Exception {
    if (name.equals("tag")) {
      GenericPropertyDescription propertyDescription = (GenericPropertyDescription) digester.peek();
      String tagName = getRequiredAttribute(name, attributes, "name");
      String tagValue = getRequiredAttribute(name, attributes, "value");
      propertyDescription.putTag(tagName, tagValue);
    } else {
      String propertyName = getRequiredAttribute(name, attributes, "name");
      ComponentDescription componentDescription = (ComponentDescription) digester.peek();
      // check all properties
      for (GenericPropertyDescription propertyDescription : componentDescription.getProperties()) {
        String id = propertyDescription.getId();
        if (PropertiesFlagRule.matchPropertyId(id, propertyName)) {
          String tag = getRequiredAttribute(name, attributes, "tag");
          String value = getRequiredAttribute(name, attributes, "value");
          propertyDescription.putTag(tag, value);
        }
      }
    }
  }
}
