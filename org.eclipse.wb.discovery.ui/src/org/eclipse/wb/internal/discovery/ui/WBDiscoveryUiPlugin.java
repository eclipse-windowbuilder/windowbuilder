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
package org.eclipse.wb.internal.discovery.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.wb.internal.discovery.core.WBToolkit;
import org.eclipse.wb.internal.discovery.core.WBToolkitRegistry;
import org.eclipse.wb.internal.discovery.ui.util.ToolkitPingJob;
import org.eclipse.wb.internal.discovery.ui.wizard.DynamicRegistryHelper;
import org.osgi.framework.BundleContext;

import java.util.List;

/**
 * The activator class controls the plug-in life cycle.
 * <p>
 * Note: this plugin has been disabled.
 */
public class WBDiscoveryUiPlugin extends AbstractUIPlugin {
  /** The plugin identifier. */
  public static final String PLUGIN_ID = "org.eclipse.wb.discovery.ui";
  /** Set the system property "com.google.usageprofiler.debug" to "true" to enable debugging. */
  public static final boolean DEBUG = Boolean.getBoolean("org.eclipse.wb.discovery.debug");
  /** The preference key to contribute new wizard entries. */
  public static final String CONTRIBUTE_WIZARD_ENTRIES_PREF = "contributeWizardEntries";
  
  // The shared instance
  private static WBDiscoveryUiPlugin plugin;
  private static LocalResourceManager resourceManager;

  private WBToolkitRegistry.IRegistryChangeListener registryListener;
  
  /**
   * The constructor
   */
  public WBDiscoveryUiPlugin() {
  }

  /**
	 * 
	 */
  public void start(BundleContext context) throws Exception {
    super.start(context);
    plugin = this;
    initializeDefaultPreferences(getPreferenceStore());
    try {
      contributeWizardEntries();
      listenForToolkitUpdates();
    } catch (Throwable t) {
      logError(t);
    }
  }

  /**
	 * 
	 */
  public void stop(BundleContext context) throws Exception {
    if (registryListener != null) {
      WBToolkitRegistry.getRegistry().removeRegistryListener(registryListener);
      registryListener = null;
    }
    plugin = null;
    super.stop(context);
  }

  protected void initializeDefaultPreferences(IPreferenceStore store) {
    store.setDefault(CONTRIBUTE_WIZARD_ENTRIES_PREF, true);
  }

  /**
   * Returns the shared instance
   * 
   * @return the shared instance
   */
  public static WBDiscoveryUiPlugin getPlugin() {
    return plugin;
  }

  /**
   * Log an exception to the Eclipse log.
   * 
   * @param t
   *          the exception to log
   */
  public static void logError(Throwable t) {
    getPlugin().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, t.toString(), t));
  }

  public static Image getImage(ImageDescriptor imageDescriptor) {
    if (resourceManager == null) {
      resourceManager = new LocalResourceManager(JFaceResources.getResources());
    }
    return resourceManager.createImage(imageDescriptor);
  }

  /**
   * Returns an image descriptor for the image file at the given plug-in relative path.
   * 
   * @param path
   *          the path
   * @return the image descriptor
   */
  public static ImageDescriptor getBundledImageDescriptor(String path) {
    return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
  }

  /**
   * Install the given toolkits.
   * 
   * @param toolkits
   *          the toolkits to install
   * @param monitor
   *          a progress monitor for reporting progress
   * @throws ProvisionException
   *           a P2 exception
   * @throws OperationCanceledException
   *           thrown if the user canceled
   */
  public void installToolkits(List<WBToolkit> toolkits, IProgressMonitor monitor)
      throws ProvisionException, OperationCanceledException {
    P2Provisioner provisioner = new P2Provisioner(toolkits);
    ping(toolkits);
    provisioner.installToolkits(monitor);
  }

  /**
   * Uninstall the given toolkits.
   * 
   * @param toolkits
   *          the toolkits to uninstall
   * @param monitor
   *          a progress monitor for reporting progress
   * @throws ProvisionException
   *           a P2 exception
   * @throws OperationCanceledException
   *           thrown if the user canceled
   */
  public void uninstallToolkits(List<WBToolkit> toolkits, IProgressMonitor monitor)
      throws ProvisionException, OperationCanceledException {
    P2Provisioner provisioner = new P2Provisioner(toolkits);
    provisioner.uninstallToolkits(monitor);
  }

  /**
   * @return the preference value to contribute new wizard entries
   */
  public boolean getContributeToWizards() {
    return getPreferenceStore().getBoolean(CONTRIBUTE_WIZARD_ENTRIES_PREF);
  }

  /**
   * Initiate a check for updates to the registered toolkits.
   */
  protected void listenForToolkitUpdates() {
    registryListener = new WBToolkitRegistry.IRegistryChangeListener() {
      public void handleRegistryChange() {
        DynamicRegistryHelper.getRegistryHelper().removeRegistrations();
        DynamicRegistryHelper.getRegistryHelper().registerWizards();
      }
    };    
  }

  /**
   * Register new wizard entries for toolkits that are not installed.
   */
  protected void contributeWizardEntries() {
    if (getContributeToWizards()) {
      DynamicRegistryHelper.getRegistryHelper().registerWizards();
    }
  }

  private void ping(List<WBToolkit> toolkits) {
    ToolkitPingJob job = new ToolkitPingJob(toolkits);
    job.schedule();
  }
}
