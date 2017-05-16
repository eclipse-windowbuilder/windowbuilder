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
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;

import java.lang.reflect.Method;

/**
 * The {@link Rule} for parsing single {@link Method} into {@link MethodDescription}.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class MethodRule extends AbstractDesignerRule {
  private ComponentDescription componentDescription;
  private MethodDescription methodDescription;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Rule.begin
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void begin(String namespace, String name, Attributes attributes) throws Exception {
    componentDescription = (ComponentDescription) digester.peek();
    pushNewMethodDescription();
    configureBegin(attributes);
  }

  private void pushNewMethodDescription() {
    Class<?> componentClass = componentDescription.getComponentClass();
    methodDescription = new MethodDescription(componentClass);
    digester.push(methodDescription);
  }

  private void configureBegin(Attributes attributes) {
    methodDescription.setName(getRequiredAttribute("method", attributes, "name"));
    setOptionalOrder(attributes);
    setExecutableFlag(attributes);
  }

  private void setOptionalOrder(Attributes attributes) {
    String orderSpecification = attributes.getValue("order");
    if (orderSpecification != null) {
      methodDescription.setOrderSpecification(orderSpecification);
    }
  }

  private void setExecutableFlag(Attributes attributes) {
    String executableString = attributes.getValue("executable");
    if (executableString != null) {
      boolean executable = !"false".equalsIgnoreCase(executableString);
      methodDescription.setExecutable(executable);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Rule.end
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void end(String namespace, String name) throws Exception {
    popMethodDescription();
    configureEnd();
    componentDescription.addMethod(methodDescription);
  }

  private void popMethodDescription() {
    digester.pop();
  }

  private void configureEnd() {
    methodDescription.postProcess();
    configureWithReflectionMethod();
  }

  private void configureWithReflectionMethod() {
    Method method = getReflectionMethod();
    methodDescription.setReturnClass(method.getReturnType());
  }

  private Method getReflectionMethod() {
    Class<?> componentClass = methodDescription.getDeclaringClass();
    String signature = methodDescription.getSignature();
    Method method = ReflectionUtils.getMethodBySignature(componentClass, signature);
    Assert.isNotNull2(
        method,
        "No such method {0}.{1} during parsing {2}",
        componentClass.getName(),
        signature,
        componentDescription.getCurrentClass().getName());
    return method;
  }
}
