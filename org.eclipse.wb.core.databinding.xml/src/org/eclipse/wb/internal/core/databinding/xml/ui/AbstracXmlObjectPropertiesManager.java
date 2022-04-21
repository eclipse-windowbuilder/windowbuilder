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
package org.eclipse.wb.internal.core.databinding.xml.ui;

import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectAddProperties;

import java.util.List;

/**
 * @author lobas_av
 */
public abstract class AbstracXmlObjectPropertiesManager
    extends
      org.eclipse.wb.internal.core.databinding.ui.property.AbstractJavaInfoPropertiesManager {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstracXmlObjectPropertiesManager(IDatabindingsProvider provider,
      XmlObjectInfo xmlObjectRoot) {
    super(provider);
    xmlObjectRoot.addBroadcastListener(new XmlObjectAddProperties() {
      @Override
      public void invoke(XmlObjectInfo object, List<Property> properties) throws Exception {
        addBindingsProperty(object, properties);
      }
    });
  }
}