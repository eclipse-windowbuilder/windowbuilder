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

import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.description.DescriptionPropertiesHelper;
import org.eclipse.wb.internal.core.xml.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.xml.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.xml.model.property.accessor.FieldExpressionAccessor;
import org.eclipse.wb.internal.core.xml.model.property.converter.ExpressionConverter;

import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * {@link Rule} that adds for public fields.
 * 
 * @author scheglov_ke
 * @coverage XML.model.description
 */
public final class CreatePropertiesFieldRule extends Rule {
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
    String id = field.getName();
    String propertyName = field.getName();
    Class<?> propertyType = field.getType();
    // prepare property parts
    ExpressionAccessor accessor = new FieldExpressionAccessor(field);
    ExpressionConverter converter = DescriptionPropertiesHelper.getConverterForType(propertyType);
    PropertyEditor editor = DescriptionPropertiesHelper.getEditorForType(propertyType);
    // create property
    GenericPropertyDescription property =
        new GenericPropertyDescription(id, propertyName, propertyType, accessor);
    property.setConverter(converter);
    property.setEditor(editor);
    // add property
    componentDescription.addProperty(property);
  }
}