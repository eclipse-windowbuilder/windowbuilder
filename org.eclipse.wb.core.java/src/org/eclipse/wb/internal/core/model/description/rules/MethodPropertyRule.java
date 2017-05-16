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

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.model.property.accessor.MethodInvocationAccessor;
import org.eclipse.wb.internal.core.model.property.accessor.MethodInvocationArgumentAccessor;
import org.eclipse.wb.internal.core.model.property.editor.MethodPropertyPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.IJavaProject;

import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;

import java.lang.reflect.Method;
import java.util.List;

/**
 * The {@link Rule} that adds {@link GenericPropertyDescription} for method with one or more
 * parameters.
 * <p>
 * For example "Forms API" <code>FormText</code> has method
 * <code>setText(String,boolean,boolean)</code>. So, we need some way to describe in
 * <code>*.wbp-component.xml</code> that we need to create property for this method with
 * sub-properties for each parameter.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class MethodPropertyRule extends AbstractDesignerRule {
  private final IJavaProject m_javaProject;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MethodPropertyRule(IJavaProject javaProject) {
    m_javaProject = javaProject;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Rule
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void begin(String namespace, String name, Attributes attributes) throws Exception {
    ComponentDescription componentDescription = (ComponentDescription) digester.peek();
    Class<?> componentClass = componentDescription.getComponentClass();
    // prepare method attributes
    String propertyTitle = getRequiredAttribute(name, attributes, "title");
    String methodSignature = getRequiredAttribute(name, attributes, "method");
    String propertyId = methodSignature;
    // prepare Method
    Method method = ReflectionUtils.getMethodBySignature(componentClass, methodSignature);
    Assert.isNotNull(method, "Method %s in %s.", methodSignature, componentClass);
    // prepare executable MethodDescription
    MethodDescription methodDescription;
    {
      methodDescription = componentDescription.addMethod(method);
      ComponentDescriptionHelper.ensureInitialized(m_javaProject, methodDescription);
    }
    // prepare accessor
    ExpressionAccessor accessor = new MethodInvocationAccessor(method);
    // prepare editor
    PropertyEditor editor;
    {
      List<GenericPropertyDescription> descriptions = Lists.newArrayList();
      for (ParameterDescription parameter : methodDescription.getParameters()) {
        GenericPropertyDescription description =
            createPropertyDescription(method, propertyId, parameter);
        if (description != null) {
          descriptions.add(description);
          componentDescription.registerProperty(description);
        }
      }
      editor = new MethodPropertyPropertyEditor("(properties)", descriptions);
    }
    // create property
    GenericPropertyDescription propertyDescription =
        new GenericPropertyDescription(propertyId, propertyTitle);
    propertyDescription.addAccessor(accessor);
    propertyDescription.setEditor(editor);
    // add property
    componentDescription.addProperty(propertyDescription);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link GenericPropertyDescription} for editing given {@link ParameterDescription},
   *         may be <code>null</code> if it can not be edited.
   */
  private static GenericPropertyDescription createPropertyDescription(Method method,
      String methodPropertyId,
      ParameterDescription parameter) {
    if (parameter.getEditor() != null) {
      String propertyId = methodPropertyId + " " + parameter.getIndex();
      String propertyTitle = parameter.getName();
      GenericPropertyDescription propertyDescription =
          new GenericPropertyDescription(propertyId, propertyTitle);
      // fill GenericPropertyDescription
      ExpressionAccessor accessor =
          new MethodInvocationArgumentAccessor(method, parameter.getIndex());
      propertyDescription.addAccessor(accessor);
      propertyDescription.setConverter(parameter.getConverter());
      propertyDescription.setEditor(parameter.getEditor());
      // OK, we have GenericPropertyDescription
      return propertyDescription;
    }
    return null;
  }
}