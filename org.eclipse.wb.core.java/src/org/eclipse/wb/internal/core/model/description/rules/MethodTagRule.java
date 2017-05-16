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

import org.eclipse.wb.internal.core.model.description.MethodDescription;

import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;

/**
 * The {@link Rule} that adds some tag for {@link MethodDescription}.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class MethodTagRule extends AbstractDesignerRule {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Rule
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void begin(String namespace, String name, Attributes attributes) throws Exception {
    String tag = getRequiredAttribute(name, attributes, "name");
    String value = getRequiredAttribute(name, attributes, "value");
    MethodDescription methodDescription = (MethodDescription) digester.peek();
    methodDescription.putTag(tag, value);
  }
}
