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
import org.eclipse.wb.internal.core.model.description.MorphingTargetDescription;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;

/**
 * The {@link Rule} that adds {@link MorphingTargetDescription} to {@link ComponentDescription}.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class MorphingTargetRule extends Rule {
  private final EditorState m_state;
  private final ClassLoader m_classLoader;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public MorphingTargetRule(EditorState state) {
    m_state = state;
    m_classLoader = m_state.getEditorLoader();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Rule
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void begin(String namespace, String name, Attributes attributes) throws Exception {
    try {
      addTarget(attributes);
    } catch (ClassNotFoundException e) {
    }
  }

  private void addTarget(Attributes attributes) throws ClassNotFoundException {
    String creationId = attributes.getValue("creationId");
    // prepare class
    Class<?> clazz;
    {
      String className = attributes.getValue("class");
      Assert.isNotNull(className);
      clazz = m_classLoader.loadClass(className);
    }
    // add morphing target
    ComponentDescription componentDescription = (ComponentDescription) digester.peek();
    componentDescription.addMorphingTarget(new MorphingTargetDescription(clazz, creationId));
  }
}