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

package org.eclipse.wb.internal.discovery.ui.wizard;

import org.eclipse.core.internal.registry.ExtensionRegistry;
import org.eclipse.core.runtime.ContributorFactoryOSGi;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.spi.IDynamicExtensionRegistry;
import org.eclipse.wb.internal.discovery.core.WBToolkit;
import org.eclipse.wb.internal.discovery.core.WBToolkitRegistry;
import org.eclipse.wb.internal.discovery.ui.WBDiscoveryUiPlugin;
import org.osgi.framework.Bundle;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

/**
 * A helper class to dynamically register and unregister new Eclipse
 * registry entries.
 */
@SuppressWarnings("restriction")
public class DynamicRegistryHelper {
  private static DynamicRegistryHelper registryHelper;
  
  /**
   * @return the singleton instance of the DynamicRegistryHelper
   */
  public static DynamicRegistryHelper getRegistryHelper() {
    if (registryHelper == null) {
      registryHelper = new DynamicRegistryHelper();
    }
    
    return registryHelper;
  }
  
  private IContributor contributor;
  private Object userToken;
  
  /**
   * Dynamically register new wizard entries for any WindowBuilder toolkits
   * which are not installed.
   */
  public void registerWizards() {
    if (contributor == null) {
      Bundle bundle = WBDiscoveryUiPlugin.getPlugin().getBundle();
      contributor = ContributorFactoryOSGi.createContributor(bundle);
      
      IExtensionRegistry registry = Platform.getExtensionRegistry();
      userToken = ((ExtensionRegistry) registry).getTemporaryUserToken();
      
      boolean success = false;
      
      byte[] xmlData = createContributionXML();
      
      if (xmlData != null && xmlData.length > 0) {
        success = registry.addContribution(
          new ByteArrayInputStream(xmlData),
          contributor,
          false,
          "WindowBuilder dynamic contributions",
          null,
          userToken);
      }
      
      if (!success) {
        contributor = null;
        userToken = null;
      }
    }
  }
  
  /**
   * Remove our previous dynamic registry entries.
   */
  public void removeRegistrations() {
    if (contributor != null) {
      IExtensionRegistry registry = Platform.getExtensionRegistry();
      
      if (contributor != null && registry instanceof IDynamicExtensionRegistry) {
        IDynamicExtensionRegistry dRegistry = (IDynamicExtensionRegistry)registry;
        
        dRegistry.removeContributor(contributor, userToken);
        
        contributor = null;
        userToken = null;
      }
    }
  }
  
/*
<?xml version="1.0" encoding="UTF-8"?>
<?eclipse version="3.4"?>
<plugin>

  <extension point="org.eclipse.ui.newWizards">
      <wizard
        category="org.eclipse.wb"
        class="org.eclipse.wb.internal.discovery.ui.wizard.InstallToolkitWizard:org.eclipse.swing"
          icon="icons/wizard.gif"
          id="org.eclipse.wb.internal.discovery.ui.wizard.InstallToolkitWizard"
          name="Swing User Interface">
            <description>Create a new Swing user interface. Requires loading additional WindowBuilder toolkits.</description>
      </wizard>
  </extension>

</plugin>
*/
  
  private byte[] createContributionXML() {
    StringBuilder builder = new StringBuilder();
    
    builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    builder.append("<?eclipse version=\"3.4\"?>\n");
    builder.append("<plugin>\n");
    
    for (WBToolkit toolkit : WBToolkitRegistry.getRegistry().getToolkits()) {
      if (toolkit.isInstalled() || toolkit.getWizardContributionTitle() == null) {
        continue;
      }
      
      builder.append("<extension point=\"org.eclipse.ui.newWizards\">\n");
      builder.append("<wizard\n");
      builder.append("category=\"org.eclipse.wb\"\n");
      builder.append("class=\"org.eclipse.wb.internal.discovery.ui.wizard.InstallToolkitWizard:" + toolkit.getId() + "\"\n");
      builder.append("icon=\"icons/wizard.gif\"\n");
      builder.append("id=\"org.eclipse.wb.internal.discovery.ui.wizard.InstallToolkitWizard\"\n");
      builder.append("name=\"" + toolkit.getWizardContributionTitle() + "\">\n");
      builder.append("<description>" + toolkit.getWizardContributionDescription() + "</description>\n");
      builder.append("</wizard>\n");
      builder.append("</extension>\n");
    }
    
    builder.append("</plugin>\n");
    
    try {
      return builder.toString().getBytes("UTF-8");
    } catch (UnsupportedEncodingException e) {
      return builder.toString().getBytes();
    }
  }

}
