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
import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.model.description.helpers.DescriptionPropertiesHelper;
import org.eclipse.wb.internal.core.model.property.accessor.FieldAccessor;
import org.eclipse.wb.internal.core.model.property.converter.ExpressionConverter;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;

import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * The {@link Rule} that adds standard properties for <code>.xxxx</code> public fields.
 *
 * @author lobas_av
 * @coverage core.model.description
 */
public final class PublicFieldPropertiesRule extends Rule {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Rule
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void begin(String namespace, String name, Attributes attributes) throws Exception {
    ComponentDescription componentDescription = (ComponentDescription) digester.peek();
    Class<?> componentClass = componentDescription.getComponentClass();
    for (Field field : componentClass.getFields()) {
      int modifiers = field.getModifiers();
      if (!Modifier.isStatic(modifiers) && !Modifier.isFinal(modifiers)) {
        addSingleProperty(componentDescription, field);
      }
    }
  }

  /**
   * Adds single {@link GenericPropertyDescription} for given field.
   */
  private static void addSingleProperty(ComponentDescription componentDescription, Field field)
      throws Exception {
    String title = field.getName();
    String id = title;
    Class<?> type = field.getType();
    // prepare property parts
    PropertyEditor editor = DescriptionPropertiesHelper.getEditorForType(type);
    ExpressionConverter converter = DescriptionPropertiesHelper.getConverterForType(type);
    FieldAccessor accessor = new FieldAccessor(field);
    // create property
    GenericPropertyDescription propertyDescription =
        new GenericPropertyDescription(id, title, type);
    propertyDescription.addAccessor(accessor);
    propertyDescription.setConverter(converter);
    propertyDescription.setEditor(editor);
    // add property
    componentDescription.addProperty(propertyDescription);
  }
}