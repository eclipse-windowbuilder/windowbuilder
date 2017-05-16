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
import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.model.property.accessor.SetterAccessor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;

import java.lang.reflect.Method;

/**
 * The {@link Rule} to set "getter" for {@link SetterAccessor}.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class PropertyGetterRule extends Rule {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Rule
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void begin(String namespace, String name, Attributes attributes) throws Exception {
    ComponentDescription componentDescription = (ComponentDescription) digester.peek(1);
    GenericPropertyDescription propertyDescription = (GenericPropertyDescription) digester.peek();
    String getterName = attributes.getValue("name");
    Method getter = ReflectionUtils.getMethod(componentDescription.getComponentClass(), getterName);
    for (ExpressionAccessor accessor : propertyDescription.getAccessorsList()) {
      if (accessor instanceof SetterAccessor) {
        SetterAccessor setterAccessor = (SetterAccessor) accessor;
        setterAccessor.setGetter(getter);
      }
    }
  }
}
