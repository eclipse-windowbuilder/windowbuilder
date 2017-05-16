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

import com.google.common.base.Predicate;

import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.apache.commons.digester.Rule;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 * The {@link Rule} for including/excluding methods. For example sometimes we want prevent execution
 * of some methods.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class MethodsOperationRule extends AbstractDesignerRule {
  private final boolean m_include;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MethodsOperationRule(boolean include) {
    m_include = include;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Rule
  //
  ////////////////////////////////////////////////////////////////////////////
  private ComponentDescription componentDescription;

  @Override
  public void begin(String namespace, String name, Attributes attributes) throws Exception {
    componentDescription = (ComponentDescription) digester.peek();
    String signature = getRequiredAttribute(name, attributes, "signature");
    if (isRegexpSignature(signature)) {
      processRegexp(signature);
    } else {
      processSingleSignature(signature);
    }
  }

  private boolean isRegexpSignature(String signature) {
    return signature.startsWith("/") && signature.endsWith("/");
  }

  private void processRegexp(final String signature) throws Exception {
    final Pattern pattern = Pattern.compile(StringUtils.substring(signature, 1, -1));
    process(new Predicate<String>() {
      public boolean apply(String t) {
        return pattern.matcher(t).matches();
      }
    });
  }

  private void processSingleSignature(final String signature) throws Exception {
    process(new Predicate<String>() {
      public boolean apply(String t) {
        return signature.equals(t);
      }
    });
  }

  private void process(Predicate<String> signaturePredicate) throws Exception {
    if (m_include) {
      processInclude(signaturePredicate);
    } else {
      processExclude(signaturePredicate);
    }
  }

  private void processInclude(Predicate<String> signaturePredicate) throws Exception {
    Method[] methods = componentDescription.getComponentClass().getMethods();
    for (Method method : methods) {
      String methodSignature = ReflectionUtils.getMethodSignature(method);
      if (signaturePredicate.apply(methodSignature)) {
        componentDescription.addMethod(method);
      }
    }
  }

  private void processExclude(Predicate<String> signaturePredicate) {
    for (Iterator<MethodDescription> I = componentDescription.getMethods().iterator(); I.hasNext();) {
      MethodDescription methodDescription = I.next();
      String methodSignature = methodDescription.getSignature();
      if (signaturePredicate.apply(methodSignature)) {
        I.remove();
      }
    }
  }
}