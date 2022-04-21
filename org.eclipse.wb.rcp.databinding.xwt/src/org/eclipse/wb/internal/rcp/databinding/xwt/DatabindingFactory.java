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
package org.eclipse.wb.internal.rcp.databinding.xwt;

import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.xml.model.IDatabindingFactory;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.rcp.databinding.xwt.parser.DatabindingParser;

import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * {@link IDatabindingFactory} factory for support XWT bindings API.
 *
 * @author lobas_av
 * @coverage bindings.xwt.model
 */
public final class DatabindingFactory implements IDatabindingFactory {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IDatabindingFactory
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public IDatabindingsProvider createProvider(XmlObjectInfo xmlObjectRoot) throws Exception {
    // check root
    if (isRCPRootObject(xmlObjectRoot)) {
      // create provider
      DatabindingsProvider provider = new DatabindingsProvider(xmlObjectRoot);
      // parse
      DatabindingParser.parse(provider);
      // events
      provider.hookXmlObjectEvents();
      //
      return provider;
    }
    return null;
  }

  @Override
  public AbstractUIPlugin getPlugin() {
    return Activator.getDefault();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static boolean isRCPRootObject(XmlObjectInfo xmlObjectRoot) throws Exception {
    return xmlObjectRoot.getDescription().getToolkit().getId() == org.eclipse.wb.internal.rcp.preferences.IPreferenceConstants.TOOLKIT_ID;
  }
}