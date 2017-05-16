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
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.model.description.helpers.DescriptionPropertiesHelper;
import org.eclipse.wb.internal.core.model.property.accessor.SetterAccessor;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.converter.ExpressionConverter;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.List;

/**
 * The {@link Rule} that adds standard properties for <code>set/getXXX</code> methods.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class StandardBeanPropertiesRule extends Rule {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Rule
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void begin(String namespace, String name, Attributes attributes) throws Exception {
    ComponentDescription componentDescription = (ComponentDescription) digester.peek();
    List<PropertyDescriptor> descriptors =
        ReflectionUtils.getPropertyDescriptors(
            componentDescription.getBeanInfo(),
            componentDescription.getComponentClass());
    componentDescription.setPropertyDescriptors(descriptors);
    for (PropertyDescriptor propertyDescriptor : descriptors) {
      // prepare property parts
      String propertyName = propertyDescriptor.getName();
      Method setMethod = ReflectionUtils.getWriteMethod(propertyDescriptor);
      Method getMethod = ReflectionUtils.getReadMethod(propertyDescriptor);
      // add property
      if (setMethod != null) {
        GenericPropertyDescription propertyDescription =
            addSingleProperty(componentDescription, propertyName, setMethod, getMethod);
        tryToSetPropertyEditorAWT(propertyDescriptor, propertyDescription);
        if (propertyDescriptor.isPreferred()) {
          propertyDescription.setCategory(PropertyCategory.PREFERRED);
        }
      }
    }
  }

  private static void tryToSetPropertyEditorAWT(PropertyDescriptor propertyDescriptor,
      GenericPropertyDescription propertyDescription) throws Exception {
    // check for editor based on attributes of PropertyDescriptor
    {
      PropertyEditor editor =
          DescriptionPropertiesHelper.getEditorForPropertyDescriptor(propertyDescriptor);
      if (editor != null) {
        propertyDescription.setEditor(editor);
        return;
      }
    }
    // check for java.beans.PropertyEditor
    Class<?> propertyEditorType = propertyDescriptor.getPropertyEditorClass();
    if (propertyEditorType != null) {
      PropertyEditor editor =
          DescriptionPropertiesHelper.getEditorForEditorType(propertyEditorType);
      propertyDescription.setEditor(editor);
    }
  }

  /**
   * Adds single {@link GenericPropertyDescription} for given methods.
   */
  static GenericPropertyDescription addSingleProperty(ComponentDescription componentDescription,
      String title,
      Method setMethod,
      Method getMethod) throws Exception {
    // make setMethod() executable
    MethodDescription methodDescription = componentDescription.addMethod(setMethod);
    ParameterDescription parameter = methodDescription.getParameter(0);
    parameter.setName(title);
    // prepare property parts
    String id = getId(setMethod);
    Class<?> type = parameter.getType();
    SetterAccessor accessor = new SetterAccessor(setMethod, getMethod);
    ExpressionConverter converter = parameter.getConverter();
    PropertyEditor editor = parameter.getEditor();
    // create property
    GenericPropertyDescription propertyDescription =
        new GenericPropertyDescription(id, title, type);
    propertyDescription.addAccessor(accessor);
    propertyDescription.setConverter(converter);
    propertyDescription.setEditor(editor);
    // add property
    componentDescription.addProperty(propertyDescription);
    return propertyDescription;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the id for standard bean property with given setter {@link Method}.
   */
  public static String getId(Method setMethod) {
    return ReflectionUtils.getMethodSignature(setMethod);
  }
}