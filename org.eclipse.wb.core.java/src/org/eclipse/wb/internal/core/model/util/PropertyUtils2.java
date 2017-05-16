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
package org.eclipse.wb.internal.core.model.util;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.Property;

/**
 * Utils for {@link Property}.
 *
 * @author scheglov_ke
 * @coverage core.model.util
 */
public final class PropertyUtils2 {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private PropertyUtils2() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Create
  //
  ////////////////////////////////////////////////////////////////////////////
  public static GenericPropertyImpl createGenericPropertyImpl(JavaInfo javaInfo,
      GenericPropertyDescription description) {
    GenericPropertyImpl property =
        new GenericPropertyImpl(javaInfo,
            description.getTitle(),
            description.getAccessorsArray(),
            description.getDefaultValue(),
            description.getConverter(),
            description.getEditor());
    property.setDescription(description);
    property.setCategory(description.getCategory());
    return property;
  }
}
