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
 * {@link Rule} that adds xml property attribute namespace for {@link GenericPropertyDescription}.
 * 
 * @author mitin_aa
 * @coverage XML.model.description
 */
public final class PropertyAttributeNamespaceRule extends AbstractDesignerRule {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Rule
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void begin(String namespace, String name, Attributes attributes) throws Exception {
    String xmlnsName = getRequiredAttribute(name, attributes, "url");
    // store
    ComponentDescription componentDescription = (ComponentDescription) digester.peek();
    componentDescription.setPropertyAttributeXmlns(xmlnsName);
  }
}
