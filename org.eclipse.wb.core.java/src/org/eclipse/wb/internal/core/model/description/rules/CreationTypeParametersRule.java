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

import org.eclipse.wb.internal.core.model.description.CreationDescription;

import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;

/**
 * The {@link Rule} that adds type parameter (generic) descriptions for {@link CreationDescription}.
 *
 * @author sablin_aa
 * @coverage core.model.description
 */
public final class CreationTypeParametersRule extends AbstractDesignerRule {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Rule
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void begin(String namespace, String elementName, Attributes attributes) throws Exception {
    String name = getRequiredAttribute(elementName, attributes, "name");
    String type = getRequiredAttribute(elementName, attributes, "type");
    String description = getRequiredAttribute(elementName, attributes, "title");
    CreationDescription creationDescription = (CreationDescription) digester.peek();
    creationDescription.setTypeParameter(name, type, description);
  }
}
