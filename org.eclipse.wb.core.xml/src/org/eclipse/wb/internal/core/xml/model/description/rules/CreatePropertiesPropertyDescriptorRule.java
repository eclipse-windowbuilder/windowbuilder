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
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.description.DescriptionPropertiesHelper;
import org.eclipse.wb.internal.core.xml.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.xml.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.xml.model.property.accessor.MethodExpressionAccessor;
import org.eclipse.wb.internal.core.xml.model.property.converter.ExpressionConverter;

import org.apache.commons.digester.Rule;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.List;

/**
 * The {@link Rule} that adds {@link GenericPropertyDescription}s for {@link PropertyDescriptor}s.
 * 
 * @author scheglov_ke
 * @coverage XML.model.description
 */
public final class CreatePropertiesPropertyDescriptorRule extends Rule {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Rule
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void begin(String namespace, String name, Attributes attributes) throws Exception {
    ComponentDescription componentDescription = (ComponentDescription) digester.peek();
    List<PropertyDescriptor> descriptors =
        ReflectionUtils.getPropertyDescriptors(null, componentDescription.getComponentClass());
    for (PropertyDescriptor propertyDescriptor : descriptors) {
      addSingleProperty(componentDescription, propertyDescriptor);
    }
  }

  private void addSingleProperty(ComponentDescription componentDescription,
      PropertyDescriptor propertyDescriptor) throws Exception {
    Method setMethod = propertyDescriptor.getWriteMethod();
    if (setMethod == null) {
      return;
    }
    if (!ReflectionUtils.isPublic(setMethod)) {
      return;
    }
    // prepare description parts
    String title = propertyDescriptor.getName();
    String attribute = StringUtils.substringBeforeLast(StringUtils.uncapitalize(title), "(");
    Method getMethod = propertyDescriptor.getReadMethod();
    Class<?> propertyType = propertyDescriptor.getPropertyType();
    // prepare property parts
    String id = ReflectionUtils.getMethodSignature(setMethod);
    ExpressionAccessor accessor = new MethodExpressionAccessor(attribute, setMethod, getMethod);
    ExpressionConverter converter = DescriptionPropertiesHelper.getConverterForType(propertyType);
    PropertyEditor editor = DescriptionPropertiesHelper.getEditorForType(propertyType);
    // create property
    GenericPropertyDescription property =
        new GenericPropertyDescription(id, title, propertyType, accessor);
    property.setConverter(converter);
    property.setEditor(editor);
    // add property
    componentDescription.addProperty(property);
  }
}