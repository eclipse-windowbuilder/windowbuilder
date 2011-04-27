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
package org.eclipse.wb.internal.discovery.core;

import org.eclipse.osgi.util.NLS;

/**
 * The I18N class for the org.eclipse.wb.discovery.core plugin.
 */
public class Messages extends NLS {
  private static final String BUNDLE_NAME = "org.eclipse.wb.internal.discovery.core.messages"; //$NON-NLS-1$
  public static String WBToolkit_fromProvider;
  public static String WBToolkit_fromProviderLicense;
  public static String WBToolkitRegistryUpdateJob_updateJobTitle;
  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }

  private Messages() {
  }
}
