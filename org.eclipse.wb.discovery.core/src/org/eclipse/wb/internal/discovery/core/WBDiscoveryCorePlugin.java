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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class for the org.eclipse.wb.discovery.core plugin.
 */
public class WBDiscoveryCorePlugin extends AbstractUIPlugin {

  /** The plugin identifier. */
  public static final String PLUGIN_ID = "org.eclipse.wb.discovery.core";
  
  // The shared instance
  private static WBDiscoveryCorePlugin plugin;
  
  /**
   * The constructor.
   */
  public WBDiscoveryCorePlugin() {
    
  }

  public void start(BundleContext context) throws Exception {
    super.start(context);
    
    plugin = this;
  }
  
  public void stop(BundleContext context) throws Exception {
    plugin = null;
    
    super.stop(context);
  }

  /**
   * Returns the shared instance.
   *
   * @return the shared instance
   */
  public static WBDiscoveryCorePlugin getPlugin() {
    return plugin;
  }
  
  /**
   * Log an exception to the Eclipse log.
   * 
   * @param t the exception to log
   */
  public static void logError(Throwable t) {
    getPlugin().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, t.toString(), t));
  }
  
  /**
   * Returns the bundle context for this plugin.
   * 
   * @return the bundle context
   */
  public static BundleContext getBundleContext() {
    return getPlugin().getBundle().getBundleContext();
  }
  
}
