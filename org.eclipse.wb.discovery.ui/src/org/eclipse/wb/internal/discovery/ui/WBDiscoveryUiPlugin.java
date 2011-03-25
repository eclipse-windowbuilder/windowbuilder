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
import org.eclipse.wb.internal.discovery.ui.wizard.DynamicRegistryHelper;
import org.osgi.framework.BundleContext;

import java.util.List;

/**
 * The activator class controls the plug-in life cycle.
 */
public class WBDiscoveryUiPlugin extends AbstractUIPlugin {

	/** The plugin identifier. */
	public static final String PLUGIN_ID = "org.eclipse.wb.discovery.ui";
	
	/** The preference key to contribute new wizard entries. */
	public static final String CONTRIBUTE_WIZARD_ENTRIES_PREF = "contributeWizardEntries";
	
	// The shared instance
	private static WBDiscoveryUiPlugin plugin;
	
	private static LocalResourceManager resourceManager;
	
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
      
      checkForUpdates();
		} catch (Throwable t) {
		  logError(t);
		}
	}
	
	/**
	 * 
	 */
	public void stop(BundleContext context) throws Exception {
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
   * @param t the exception to log
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
   * Returns an image descriptor for the image file at the given plug-in
   * relative path.
   * 
   * @param path the path
   * @return the image descriptor
   */
  public static ImageDescriptor getBundledImageDescriptor(String path) {
    return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
  }

  /**
   * Install the given toolkits.
   * 
   * @param toolkits the toolkits to install
   * @param monitor a progress monitor for reporting progress
   * @throws ProvisionException a P2 exception
   * @throws OperationCanceledException thrown if the user canceled
   */
  public void installToolkits(List<WBToolkit> toolkits, IProgressMonitor monitor)
    throws ProvisionException, OperationCanceledException {
    P2Provisioner provisioner = new P2Provisioner(toolkits);
    
    provisioner.installToolkits(monitor);
  }
  
  /**
   * Uninstall the given toolkits.
   * 
   * @param toolkits the toolkits to uninstall
   * @param monitor a progress monitor for reporting progress
   * @throws ProvisionException a P2 exception
   * @throws OperationCanceledException thrown if the user canceled
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
  protected void checkForUpdates() {
    // TODO:
    
    // http://dev.eclipse.org/svnroot/tools/org.eclipse.windowbuilder/trunk/org.eclipse.wb.discovery.core/resources/toolkits.xml
    
    // if the toolkits changed, then call
    // EclipseRegistryTools.getRegisitryTools().removeRegistrations();
    // EclipseRegistryTools.getRegisitryTools().registerWizards();
  }
  
  /**
   * Register new wizard entries for toolkits that are not installed. 
   */
  protected void contributeWizardEntries() {
    if (getContributeToWizards()) {
      DynamicRegistryHelper.getRegistryHelper().registerWizards();
    }
  }

}
