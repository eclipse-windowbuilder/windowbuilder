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
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;

import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;

/**
 * The {@link Rule} that sets {@link PropertyCategory} of current {@link GenericPropertyDescription}
 * .
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class PropertyCategoryRule extends Rule {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Rule
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void begin(String namespace, String name, Attributes attributes) throws Exception {
    // prepare category
    PropertyCategory category;
    {
      String categoryTitle = attributes.getValue("value");
      if ("preferred".equals(categoryTitle)) {
        category = PropertyCategory.PREFERRED;
      } else if ("normal".equals(categoryTitle)) {
        category = PropertyCategory.NORMAL;
      } else if ("advanced".equals(categoryTitle)) {
        category = PropertyCategory.ADVANCED;
      } else if ("hidden".equals(categoryTitle)) {
        category = PropertyCategory.HIDDEN;
      } else {
        throw new IllegalArgumentException("Unknown category " + categoryTitle);
      }
    }
    // set category
    GenericPropertyDescription propertyDescription = (GenericPropertyDescription) digester.peek();
    propertyDescription.setCategory(category);
  }
}
