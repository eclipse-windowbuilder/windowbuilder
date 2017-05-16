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
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;

import java.lang.reflect.Method;

/**
 * The {@link Rule} that adds property for method with single parameter. We need this for cases when
 * method is not "setXXX".
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class MethodSinglePropertyRule extends AbstractDesignerRule {
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
    // prepare method
    Method method = ReflectionUtils.getMethodBySignature(componentClass, methodSignature);
    Assert.isTrue(
        method.getParameterTypes().length == 1,
        "Method with single parameter expected: %s",
        method);
    // add property
    GenericPropertyDescription property =
        StandardBeanPropertiesRule.addSingleProperty(
            componentDescription,
            propertyTitle,
            method,
            null);
    digester.push(property);
  }

  @Override
  public void end(String namespace, String name) throws Exception {
    digester.pop();
  }
}