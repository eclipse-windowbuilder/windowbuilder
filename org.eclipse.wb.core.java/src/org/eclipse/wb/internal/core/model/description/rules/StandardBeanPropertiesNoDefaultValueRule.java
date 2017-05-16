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
import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;

import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;

/**
 * The {@link Rule} that sets {@link ExpressionAccessor#NO_DEFAULT_VALUE_TAG} for standard bean
 * properties.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class StandardBeanPropertiesNoDefaultValueRule
    extends
      StandardBeanPropertiesFlaggedRule {
  @Override
  protected void configure(GenericPropertyDescription propertyDescription, Attributes attributes) {
    propertyDescription.putTag(ExpressionAccessor.NO_DEFAULT_VALUE_TAG, "true");
  }
}
