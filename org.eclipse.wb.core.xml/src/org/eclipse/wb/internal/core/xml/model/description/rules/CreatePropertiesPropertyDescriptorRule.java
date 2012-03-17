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
import org.eclipse.wb.internal.core.utils.GenericTypeResolver;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
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
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
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
    Class<?> propertyType = resolvePropertyType(componentDescription, setMethod);
    // prepare property parts
    String id =
        setMethod.getName()
            + "("
            + ReflectionUtils.getFullyQualifiedName(propertyType, false)
            + ")";
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

  private static Class<?> resolvePropertyType(ComponentDescription componentDescription,
      Method setMethod) {
    Class<?> propertyType = setMethod.getParameterTypes()[0];
    final Type genericPropertyType = setMethod.getGenericParameterTypes()[0];
    if (genericPropertyType instanceof TypeVariable<?>) {
      final Class<?> declaringClass = setMethod.getDeclaringClass();
      final Class<?> actualClass = componentDescription.getComponentClass();
      return ExecutionUtils.runObjectIgnore(new RunnableObjectEx<Class<?>>() {
        public Class<?> runObject() throws Exception {
          String typeName =
              GenericsUtils.getTypeName(GenericTypeResolver.superClass(
                  GenericTypeResolver.EMPTY,
                  actualClass,
                  declaringClass), genericPropertyType);
          return actualClass.getClassLoader().loadClass(typeName);
        }
      },
          propertyType);
    }
    return propertyType;
  }
}