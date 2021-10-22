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
package org.eclipse.wb.internal.core.xml.model.association;

import java.util.Map;

/**
 * Factory for {@link Association}s.
 *
 * @author scheglov_ke
 * @coverage XML.model.association
 */
public final class Associations {
  /**
   * @return the {@link DirectAssociation}.
   */
  public static Association direct() {
    return DirectAssociation.INSTANCE;
  }

  /**
   * @return the {@link PropertyAssociation}.
   */
  public static Association property(String property) {
    return new PropertyAssociation(property);
  }

  /**
   * @return the {@link IntermediateAssociation} without attributes.
   */
  public static Association name(String name) {
    return new IntermediateAssociation(name);
  }

  /**
   * @return the {@link IntermediateAssociation} with attributes.
   */
  public static Association intermediate(String name, Map<String, String> attributes) {
    IntermediateAssociation association = new IntermediateAssociation(name);
    if (attributes != null && !attributes.isEmpty()) {
      association.setAttributes(attributes);
    }
    return association;
  }
}
