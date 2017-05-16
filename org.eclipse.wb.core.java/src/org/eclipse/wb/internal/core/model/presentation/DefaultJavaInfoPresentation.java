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
package org.eclipse.wb.internal.core.model.presentation;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.creation.factory.AbstractExplicitFactoryCreationSupport;

import org.eclipse.swt.graphics.Image;

/**
 * Default {@link IObjectPresentation} for {@link JavaInfo}
 *
 * @author scheglov_ke
 * @coverage core.model.presentation
 */
public class DefaultJavaInfoPresentation extends DefaultObjectPresentation {
  protected final JavaInfo m_javaInfo;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DefaultJavaInfoPresentation(JavaInfo javaInfo) {
    super(javaInfo);
    m_javaInfo = javaInfo;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IObjectPresentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Image getIcon() throws Exception {
    // for factory try to get "factory method" specific icon
    if (m_javaInfo.getCreationSupport() instanceof AbstractExplicitFactoryCreationSupport) {
      AbstractExplicitFactoryCreationSupport factoryCreationSupport =
          (AbstractExplicitFactoryCreationSupport) m_javaInfo.getCreationSupport();
      Image icon = factoryCreationSupport.getDescription().getIcon();
      if (icon != null) {
        return icon;
      }
    }
    // by default use "component type" specific icon
    return m_javaInfo.getDescription().getIcon();
  }

  public String getText() throws Exception {
    return m_javaInfo.getVariableSupport().getTitle();
  }
}
