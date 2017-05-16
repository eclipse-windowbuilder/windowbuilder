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
 * The {@link Rule} that adds some tag for {@link CreationDescription}.
 *
 * @author sablin_aa
 * @coverage core.model.description
 */
public final class CreationTagRule extends AbstractDesignerRule {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Rule
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void begin(String namespace, String name, Attributes attributes) throws Exception {
    String tag = getRequiredAttribute(name, attributes, "name");
    String value = getRequiredAttribute(name, attributes, "value");
    CreationDescription creationDescription = (CreationDescription) digester.peek();
    creationDescription.putTag(tag, value);
  }
}
