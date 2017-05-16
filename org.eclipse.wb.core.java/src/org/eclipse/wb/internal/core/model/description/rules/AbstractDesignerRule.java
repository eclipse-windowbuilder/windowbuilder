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
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;

/**
 * Abstract {@link Rule} for parsing {@link ComponentDescription}.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public abstract class AbstractDesignerRule extends Rule {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the value of required attribute or throws exception if attribute is missing.
   */
  protected static String getRequiredAttribute(String tagName,
      Attributes attributes,
      String attributeName) {
    String value = attributes.getValue(attributeName);
    Assert.isNotNull(value, "Attribute '" + attributeName + "' for <" + tagName + "> required.");
    return value;
  }
}
