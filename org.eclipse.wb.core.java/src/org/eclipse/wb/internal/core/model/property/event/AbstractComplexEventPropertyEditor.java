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
package org.eclipse.wb.internal.core.model.property.event;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDisplayPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.complex.IComplexPropertyEditor;

/**
 * Abstract implementation of {@link PropertyEditor} and {@link IComplexPropertyEditor} for event
 * properties .
 *
 * @author scheglov_ke
 * @coverage core.model.property.events
 */
public abstract class AbstractComplexEventPropertyEditor extends TextDisplayPropertyEditor
    implements
      IComplexPropertyEditor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // TextDisplayPropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected final String getText(Property property) throws Exception {
    StringBuffer sb = new StringBuffer();
    // append properties
    Property[] subProperties = getProperties(property);
    for (int i = 0; i < subProperties.length; i++) {
      Property subProperty = subProperties[i];
      if (subProperty.isModified()) {
        if (sb.length() != 0) {
          sb.append(", ");
        }
        sb.append(subProperty.getTitle());
      }
    }
    // append square brackets
    sb.insert(0, "[");
    sb.append("]");
    // return final text
    return sb.toString();
  }
}
