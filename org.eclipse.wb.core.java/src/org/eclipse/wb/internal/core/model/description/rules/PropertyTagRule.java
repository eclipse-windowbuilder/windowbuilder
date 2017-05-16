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

import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;

import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;

/**
 * The {@link Rule} that sets a tag for current {@link GenericPropertyDescription}.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class PropertyTagRule extends AbstractDesignerRule {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Rule
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void begin(String namespace, String name, Attributes attributes) throws Exception {
    GenericPropertyDescription propertyDescription = (GenericPropertyDescription) digester.peek();
    String tagName = getRequiredAttribute(name, attributes, "name");
    String tagValue = getRequiredAttribute(name, attributes, "value");
    propertyDescription.putTag(tagName, tagValue);
  }
}
