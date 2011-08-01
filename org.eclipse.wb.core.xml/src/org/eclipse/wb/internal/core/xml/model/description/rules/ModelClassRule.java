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

import org.eclipse.wb.internal.core.model.description.helpers.DescriptionHelper;
import org.eclipse.wb.internal.core.xml.model.UseModelIfNotAlready;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;

import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;

/**
 * The {@link Rule} that sets model class for {@link ComponentDescription}.
 * 
 * @author scheglov_ke
 * @coverage XML.model.description
 */
public final class ModelClassRule extends Rule {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Rule
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void begin(String namespace, String name, Attributes attributes) throws Exception {
    // prepare model Class
    String className = attributes.getValue("class");
    Class<?> modelClass = DescriptionHelper.loadModelClass(className);
    // set model Class
    {
      ComponentDescription componentDescription = (ComponentDescription) digester.peek();
      // check, may be this is "secondary" model
      {
        UseModelIfNotAlready annotation = modelClass.getAnnotation(UseModelIfNotAlready.class);
        if (annotation != null) {
          Class<?> currentModelClass = componentDescription.getModelClass();
          Class<?> baseModelClass = annotation.value();
          if (baseModelClass.isAssignableFrom(currentModelClass)) {
            return;
          }
        }
      }
      // OK, do set
      componentDescription.setModelClass(modelClass);
    }
  }
}