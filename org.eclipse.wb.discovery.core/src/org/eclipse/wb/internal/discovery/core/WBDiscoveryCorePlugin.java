/*******************************************************************************
 * Copyright (c) 2011 Google, Inc. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.discovery.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.Util;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class for the org.eclipse.wb.discovery.core plugin.
 */
public class WBDiscoveryCorePlugin extends AbstractUIPlugin {

  /** The plugin identifier. */
  public static final String PLUGIN_ID = "org.eclipse.wb.discovery.core";

  /** Set the system property "com.google.usageprofiler.debug" to "true" to enable debugging. */
  public static final boolean DEBUG = Boolean.getBoolean("org.eclipse.wb.discovery.debug");
  
  // The shared instance
  private static WBDiscoveryCorePlugin plugin;

  /**
   * Returns the bundle context for this plugin.
   * 
   * @return the bundle context
   */
  public static BundleContext getBundleContext() {
    return getPlugin().getBundle().getBundleContext();
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
    getPlugin().getLog().log(
        new Status(IStatus.ERROR, PLUGIN_ID, t.toString(), t));
  }

  /**
   * Log an exception to the Eclipse log.
   * 
   * @param message the log message
   * @param t the exception to log
   */
  public static void logError(String message, Throwable t) {
    getPlugin().getLog().log(
        new Status(IStatus.ERROR, PLUGIN_ID, message, t));
  }
  
  /**
   * @return a string representing the current operating system
   */
  protected static String getCurrentOS() {
    if (Util.isWindows()) {
      return "win32";
    } else if (Util.isMac()) {
      return "mac";
    } else if (Util.isLinux()) {
      return "linux";
    } else {
    	return "unknown";
    }
  }

  //private WBToolkitRegistryUpdateJob updateJob;

  /**
   * The constructor.
   */
  public WBDiscoveryCorePlugin() {

  }

  public void checkForRegistryUpdates() {
    //updateJob.cancel();
    //updateJob.schedule();
    
    WBToolkitRegistryUpdateJob updateJob = new WBToolkitRegistryUpdateJob();
    updateJob.schedule(1000);
  }

  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);

    plugin = this;

    //updateJob = new WBToolkitRegistryUpdateJob();
    //updateJob.startJob();
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    //updateJob.cancel();
    //updateJob = null;

    plugin = null;

    super.stop(context);
  }

}
