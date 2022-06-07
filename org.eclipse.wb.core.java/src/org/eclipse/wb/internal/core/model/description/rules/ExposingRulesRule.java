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
import org.eclipse.wb.internal.core.model.description.ExposingMethodRule;
import org.eclipse.wb.internal.core.model.description.ExposingPackageRule;
import org.eclipse.wb.internal.core.model.description.ExposingRule;

import org.apache.commons.digester3.Rule;
import org.xml.sax.Attributes;

/**
 * The {@link Rule} that adds include/exclude rules for exposed children.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class ExposingRulesRule extends AbstractDesignerRule {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Rule
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void begin(String namespace, String name, Attributes attributes) throws Exception {
    ComponentDescription componentDescription = (ComponentDescription) getDigester().peek();
    // prepare attributes
    boolean include = "include".equals(name);
    String packageName = attributes.getValue("package");
    String methodName = attributes.getValue("method");
    // add expose rules
    if (packageName != null) {
      ExposingRule rule = new ExposingPackageRule(include, packageName);
      componentDescription.addExposingRule(rule);
    }
    if (methodName != null) {
      ExposingRule rule = new ExposingMethodRule(include, methodName);
      componentDescription.addExposingRule(rule);
    }
  }
}
