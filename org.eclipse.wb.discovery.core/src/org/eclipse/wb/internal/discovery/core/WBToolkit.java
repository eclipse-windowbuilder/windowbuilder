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

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.QueryUtil;

import org.osgi.framework.ServiceReference;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The representation of a WindowBuilder user interface toolkit.
 */
public class WBToolkit {
  private static IProfile installedProfile;
  private String name;
  private String title;
  private String id;
  private String description;
  private URL parentPath;
  private String iconPath;
  private String updateSite;
  private String auxiliaryUpdateSite;
  private List<WBToolkitFeature> features = new ArrayList<WBToolkitFeature>();
  private String providerName;
  private String licenseDescription;
  private String moreInfoURL;
  private List<String> osList = new ArrayList<String>();

  /**
   * Create a new WindowBuilder toolkit.
   */
  protected WBToolkit() {
  }

  /**
   * @return the name of the toolkit
   */
  public String getName() {
    return name;
  }

  /**
   * Set the name of the toolkit.
   * 
   * @param name
   *          the toolkit name
   */
  protected void setName(String name) {
    this.name = name;
  }

  /**
   * @return the title of the toolkit
   */
  public String getTitle() {
    if (title != null) {
      return title;
    } else {
      return getName();
    }
  }

  /**
   * Set the name of the toolkit.
   * 
   * @param title
   *          the toolkit title
   */
  protected void setTitle(String title) {
    this.title = title;
  }

  /**
   * @return the toolkit's unique ID
   */
  public String getId() {
    return id;
  }

  /**
   * Set the toolkit's unique ID.
   * 
   * @param id
   *          the unique ID
   */
  protected void setId(String id) {
    this.id = id;
  }

  /**
   * @return the toolkit's icon path
   */
  protected String getIconPath() {
    return iconPath;
  }

  /**
   * @return the URL for the toolkit's icon; can be <code>null</code>
   */
  public URL getIconURL() {
    if (iconPath == null) {
      return null;
    } else {
      try {
    	String urlString = parentPath.toString();
    	if (!urlString.endsWith("/")) {
    		urlString = urlString.substring(0, urlString.length());
    	}
    	int index = urlString.lastIndexOf('/');
    	urlString = urlString.substring(0, index);
    	return new URL(urlString + "/" + iconPath);
      } catch (MalformedURLException e) {
        return null;
      }
    }
  }

  /**
   * Set the toolkit's icon path.
   * 
   * @param parentPath
   *          the parent path
   * @param iconPath
   *          the icon's path
   */
  protected void setIconPath(URL parentPath, String iconPath) {
    this.parentPath = parentPath;
    this.iconPath = iconPath;
  }

  /**
   * @return the toolkit's license description, if any
   */
  public String getLicenseDescription() {
    return licenseDescription;
  }

  /**
   * Set the toolkit's license description.
   * 
   * @param licenseDescription
   *          the license description
   */
  protected void setLicenseDescription(String licenseDescription) {
    this.licenseDescription = licenseDescription;
  }

  /**
   * @return get the update site URL
   */
  public String getUpdateSite() {
    return updateSite;
  }

  /**
   * Set the update set URL.
   * 
   * @param updateSite
   *          the update site
   */
  protected void setUpdateSite(String updateSite) {
    this.updateSite = updateSite;
  }
  
  /**
   * @return the update site URI
   */
  public URI getUpdateSiteURI() {
    try {
      if (getUpdateSite() == null) {
        return null;
      }
      return new URI(getUpdateSite());
    } catch (URISyntaxException e) {
      return null;
    }
  }

  /**
   * @return get the update site URL
   */
  public String getAuxiliaryUpdateSite() {
    return auxiliaryUpdateSite;
  }

  /**
   * Set the update set URL.
   * 
   * @param updateSite
   *          the update site
   */
  protected void setAuxiliaryUpdateSite(String updateSite) {
    this.auxiliaryUpdateSite = updateSite;
  }
  
  /**
   * @return the update site URI
   */
  public URI getAuxiliaryUpdateSiteURI() {
    try {
    	if (getAuxiliaryUpdateSite() == null) {
        return null;
      }
      return new URI(getAuxiliaryUpdateSite());
    } catch (URISyntaxException e) {
      return null;
    }
  }
  
  /**
   * @return the list of features necessary to install this toolkit
   */
  public List<WBToolkitFeature> getFeatures() {
    return Collections.unmodifiableList(features);
  }

  /**
   * Add a feature identifier to the toolkit.
   * 
   * @param featureId
   *          the feature identifier
   */
  protected void addFeature(String featureId, boolean optional) {
    if (featureId != null && featureId.length() > 0) {
      features.add(new WBToolkitFeature(featureId, optional));
    }
  }

  /**
   * @return the provider name (source company) for this toolkit
   */
  public String getProviderName() {
    return providerName;
  }

  /**
   * Set the provider name for this toolkit.
   * 
   * @param providerName
   *          the provider name
   */
  protected void setProviderName(String providerName) {
    this.providerName = providerName;
  }

  /**
   * @return a concatenation of the provider name and license description
   */
  public String getProviderDescription() {
    if (licenseDescription != null) {
      return MessageFormat.format(
          Messages.WBToolkit_fromProviderLicense,
          getProviderName(),
          getLicenseDescription());
    } else {
      return MessageFormat.format(Messages.WBToolkit_fromProvider, getProviderName());
    }
  }

  /**
   * @return the URL to visit for more info about this toolkit, if any
   */
  public String getMoreInfoURL() {
    return moreInfoURL;
  }

  /**
   * Set the info URL.
   * 
   * @param moreInfoURL
   *          the info URL
   */
  protected void setMoreInfoURL(String moreInfoURL) {
    this.moreInfoURL = moreInfoURL;
  }

  /**
   * @return the toolkit's description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Set the toolkit's description.
   * 
   * @param description
   *          the description
   */
  protected void setDescription(String description) {
    this.description = description;
  }

  /**
   * @return the list of operating systems this toolkit can be installed on
   */
  public List<String> getOsList() {
    return Collections.unmodifiableList(osList);
  }
  
  /**
   * Set the list of supported operating systems.
   * @param oses
   */
  protected void setOsList(String[] oses) {
    osList.clear();
    osList.addAll(Arrays.asList(oses));
  }
  
  /**
   * @return whether the current operating system is supported by this toolkit
   */
  public boolean supportsCurrentOS() {
    if (osList.size() == 0) {
      return true;
    }
    
    for (String os : osList) {
      if (WBDiscoveryCorePlugin.getCurrentOS().equals(os)) {
        return true;
      }
    }
    
    return false;
  }
  
  /**
   * @return whether this toolkit is installed or not
   */
  public boolean isInstalled() {
    if (getFeatures().size() == 0) {
      return false;
    }
    for (WBToolkitFeature feature : getFeatures()) {
      if (!feature.isOptional()) {
        if (!isFeatureInstalled(feature)) {
          return false;
        }
      }
    }
    return true;
  }

  private boolean isFeatureInstalled(WBToolkitFeature feature) {
    IProfile profile = getCurrentProfile();
    if (profile == null) {
      return false;
    }
    IQueryResult<IInstallableUnit> results =
        installedProfile.available(
            QueryUtil.createIUQuery(feature.getFeatureId() + ".feature.group"),
            new NullProgressMonitor());
    return !results.isEmpty();
  }

  private IProfile getCurrentProfile() {
    if (installedProfile == null) {
      // get the agent
      ServiceReference sr =
          WBDiscoveryCorePlugin.getBundleContext().getServiceReference(
              IProvisioningAgentProvider.SERVICE_NAME);
      if (sr == null) {
        return null;
      }
      IProvisioningAgentProvider agentProvider =
          (IProvisioningAgentProvider) WBDiscoveryCorePlugin.getBundleContext().getService(sr);
      try {
        // null == the current Eclipse installation
        IProvisioningAgent agent = agentProvider.createAgent(null);
        IProfileRegistry profileRegistry =
            (IProfileRegistry) agent.getService(IProfileRegistry.SERVICE_NAME);
        installedProfile = profileRegistry.getProfile(IProfileRegistry.SELF);
      } catch (ProvisionException e) {
        return null;
      }
    }
    return installedProfile;
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof WBToolkit) {
      WBToolkit other = (WBToolkit) object;
      return getName().equals(other.getName());
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return getName().hashCode();
  }

  @Override
  public String toString() {
    return getName();
  }

}
