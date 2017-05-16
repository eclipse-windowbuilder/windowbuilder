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
import org.eclipse.wb.internal.core.model.description.ConstructorDescription;

import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;

/**
 * The {@link Rule} for parsing single constructor.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class ConstructorRule extends Rule {
  private ComponentDescription componentDescription;
  private ConstructorDescription constructorDescription;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Rule
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void begin(String namespace, String name, Attributes attributes) throws Exception {
    componentDescription = (ComponentDescription) digester.peek();
    createConstructorDescription();
    digester.push(constructorDescription);
  }

  private void createConstructorDescription() {
    Class<?> componentClass = componentDescription.getComponentClass();
    constructorDescription = new ConstructorDescription(componentClass);
  }

  @Override
  public void end(String namespace, String name) throws Exception {
    digester.pop();
    constructorDescription.postProcess();
    // add constructor only if we are parsing final component class
    if (componentDescription.getCurrentClass() == componentDescription.getComponentClass()) {
      componentDescription.addConstructor(constructorDescription);
    }
  }

  @Override
  public void finish() throws Exception {
    super.finish();
    componentDescription = null;
    constructorDescription = null;
  }
}
