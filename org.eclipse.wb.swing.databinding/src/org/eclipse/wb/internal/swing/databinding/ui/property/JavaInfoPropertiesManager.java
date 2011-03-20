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
package org.eclipse.wb.internal.swing.databinding.ui.property;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.ui.property.AbstractBindingsProperty;
import org.eclipse.wb.internal.core.databinding.ui.property.AbstractJavaInfoPropertiesManager;
import org.eclipse.wb.internal.core.databinding.ui.property.Context;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.nonvisual.NonVisualBeanContainerInfo;
import org.eclipse.wb.internal.swing.databinding.Activator;
import org.eclipse.wb.internal.swing.databinding.model.DataBindingsCodeUtils;
import org.eclipse.wb.internal.swing.databinding.model.components.JavaInfoReferenceProvider;

/**
 * @author lobas_av
 * @coverage bindings.swing.ui.properties
 */
public class JavaInfoPropertiesManager extends AbstractJavaInfoPropertiesManager {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JavaInfoPropertiesManager(IDatabindingsProvider provider, JavaInfo javaInfoRoot) {
    super(provider, javaInfoRoot);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractJavaInfoPropertiesManager
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean isCreateProperty(ObjectInfo objectInfo) throws Exception {
    JavaInfo javaInfo = (JavaInfo) objectInfo;
    if (JavaInfoUtils.hasTrueParameter(javaInfo, "databinding.disable")) {
      return false;
    }
    if (!DataBindingsCodeUtils.isDBAvailable(javaInfo.getEditor().getJavaProject())) {
      return false;
    }
    return (javaInfo instanceof AbstractComponentInfo || javaInfo.getParent() instanceof NonVisualBeanContainerInfo)
        && JavaInfoReferenceProvider.getReference(javaInfo) != null;
  }

  @Override
  protected AbstractBindingsProperty createProperty(ObjectInfo objectInfo) throws Exception {
    return new BindingsProperty(new Context(Activator.getDefault(), m_provider, objectInfo));
  }
}