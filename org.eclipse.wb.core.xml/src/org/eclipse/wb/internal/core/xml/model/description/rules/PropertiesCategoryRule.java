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

import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.xml.model.description.GenericPropertyDescription;

import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;

/**
 * {@link Rule} that sets {@link PropertyCategory} for {@link GenericPropertyDescription}.
 * 
 * @author scheglov_ke
 * @coverage XML.model.description
 */
public final class PropertiesCategoryRule extends PropertiesFlagRule {
  private final PropertyCategory m_category;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PropertiesCategoryRule(PropertyCategory category) {
    m_category = category;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Configure
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void configure(GenericPropertyDescription propertyDescription, Attributes attributes) {
    propertyDescription.setCategory(m_category);
  }
}
