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
package org.eclipse.wb.internal.core.databinding.ui.property;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.TextDisplayPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.complex.IComplexPropertyEditor;

/**
 * Editor for complex bindings property.
 *
 * @author lobas_av
 * @coverage bindings.ui.properties
 */
public final class BindingsPropertyEditor extends TextDisplayPropertyEditor
    implements
      IComplexPropertyEditor {
  public static final BindingsPropertyEditor EDITOR = new BindingsPropertyEditor();

  ////////////////////////////////////////////////////////////////////////////
  //
  // IComplexPropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  public Property[] getProperties(Property property) throws Exception {
    AbstractBindingsProperty bindingsProperty = (AbstractBindingsProperty) property;
    return bindingsProperty.getProperties();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // TextDisplayPropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getText(Property property) throws Exception {
    StringBuffer text = new StringBuffer("[");
    for (Property subProperty : getProperties(property)) {
      if (subProperty.isModified()) {
        if (text.length() > 1) {
          text.append(", ");
        }
        text.append(subProperty.getTitle());
      }
    }
    text.append("]");
    return text.toString();
  }
}