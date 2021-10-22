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
package org.eclipse.wb.internal.rcp.databinding;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingFactory;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.parser.DatabindingParser;

import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * {@link IDatabindingFactory} factory for support JFace bindings API.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model
 */
public final class JFaceDatabindingsFactory implements IDatabindingFactory {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IDatabindingFactory
  //
  ////////////////////////////////////////////////////////////////////////////
  public IDatabindingsProvider createProvider(JavaInfo javaInfoRoot) throws Exception {
    // check root
    if (isRCPRootObject(javaInfoRoot)) {
      // create provider
      DatabindingsProvider provider = new DatabindingsProvider(javaInfoRoot);
      // parse
      DatabindingParser.parse(provider);
      // events
      provider.hookJavaInfoEvents();
      return provider;
    }
    return null;
  }

  public AbstractUIPlugin getPlugin() {
    return Activator.getDefault();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static boolean isRCPRootObject(JavaInfo javaInfoRoot) throws Exception {
    return javaInfoRoot.getDescription().getToolkit().getId() == org.eclipse.wb.internal.rcp.preferences.IPreferenceConstants.TOOLKIT_ID;
  }
}