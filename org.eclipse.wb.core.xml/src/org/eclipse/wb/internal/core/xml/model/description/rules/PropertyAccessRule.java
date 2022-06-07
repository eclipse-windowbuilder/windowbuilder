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

import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.description.GenericPropertyDescription;

import org.apache.commons.digester3.Rule;
import org.xml.sax.Attributes;

/**
 * {@link Rule} that push's {@link GenericPropertyDescription} for property with given "id".
 *
 * @author scheglov_ke
 * @coverage XML.model.description
 */
public final class PropertyAccessRule extends Rule {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Rule
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void begin(String namespace, String name, Attributes attributes) throws Exception {
    String id = attributes.getValue("id");
    ComponentDescription componentDescription = (ComponentDescription) getDigester().peek();
    GenericPropertyDescription propertyDescription = componentDescription.getProperty(id);
    getDigester().push(propertyDescription);
  }

  @Override
  public void end(String namespace, String name) throws Exception {
    getDigester().pop();
  }
}
