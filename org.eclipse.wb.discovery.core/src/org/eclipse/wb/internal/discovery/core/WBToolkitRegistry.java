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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.resolver.VersionRange;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * A registry of available WindowBuilder toolkits.
 */
public class WBToolkitRegistry {
  private static WBToolkitRegistry registry;
  
  public interface IRegistryChangeListener {
    public void handleRegistryChange();
  };
  
  /**
   * Returns the singleton instance of the WBToolkitRegistry.
   * 
   * @return the singleton instance of the WBToolkitRegistry
   */
  public static WBToolkitRegistry getRegistry() {
    if (registry == null) {
      registry = new WBToolkitRegistry();
    }
    return registry;
  }

  private List<WBToolkit> toolkits = new ArrayList<WBToolkit>();
  
  private List<IRegistryChangeListener> listeners = new ArrayList<WBToolkitRegistry.IRegistryChangeListener>();
  
  private WBToolkitRegistry() {
    initRegistry();
  }

  /**
   * Return the WindowBuilder toolkit with the given id.
   * 
   * @param toolkitId
   *          a unique identifier for a WindowBuilder toolkit
   * @return the WindowBuilder toolkit with the given id
   */
  public WBToolkit getToolkit(String toolkitId) {
    for (WBToolkit toolkit : getToolkits()) {
      if (toolkitId.equals(toolkit.getId())) {
        return toolkit;
      }
    }
    return null;
  }

  /**
   * Return all the available WindowBuilder toolkits.
   * 
   * @return all the available WindowBuilder toolkits
   */
  public List<WBToolkit> getToolkits() {
    return Collections.unmodifiableList(toolkits);
  }
  
  public void addRegistryListener(IRegistryChangeListener listener) {
    if (!listeners.contains(listener)) {
      listeners.add(listener);
    }
  }
  
  public void removeRegistryListener(IRegistryChangeListener listener) {
    listeners.remove(listener);
  }
  
  protected long getLastCachedModified() {
    File toolkitsFile = getCacheLocation().append("toolkits.xml").toFile();
    
    if (toolkitsFile.exists() && toolkitsFile.canRead()) {
      return toolkitsFile.lastModified();
    } else {
      return 0;
    }
  }

  protected void updateCacheFrom(URL toolkitsUrl) {
    IPath cacheDirectory = getCacheLocation();
    
    // copy toolkitsUrl to cacheDirectory
    copy(toolkitsUrl, cacheDirectory);
    
    // copy referenced images to cacheDirectory
    try {
      //URL toolkitsFileURL = getCacheLocation().append("toolkits.xml").toFile().toURL();
      
      for (WBToolkit toolkit : parseToolkits(toolkitsUrl)) {
        URL iconURL = toolkit.getIconURL();
        
        if (iconURL != null) {
          copy(iconURL, cacheDirectory);
        }
      }
    } catch (Throwable t) {
      WBDiscoveryCorePlugin.logError(t);
    }
    
    parseToolkitsFromCache();
  }

  private void copy(URL fileURL, IPath parentDirectory) {
    String fileName = fileURL.getPath();
    
    if (fileName.indexOf('/') != -1) {
      fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
    }
    
    IPath filePath = parentDirectory.append(fileName);
    
    try {
      URLConnection connection = fileURL.openConnection();
      File outFile = filePath.toFile();
      
      copy(connection.getInputStream(), new FileOutputStream(outFile));
      
      long lastModified = connection.getHeaderFieldDate("Last-Modified", 0); //$NON-NLS-1$
      
      outFile.setLastModified(lastModified);
    } catch (IOException ioe) {
      WBDiscoveryCorePlugin.logError(ioe);
    }
  }

  private void copy(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[4096];
    
    int count = in.read(buffer);
    
    while (count != -1) {
      out.write(buffer, 0, count);
      
      count = in.read(buffer);
    }
    
    in.close();
    out.close();
  }

  private void initRegistry() {
    if (!cacheExists()) {
      URL toolkitsUrl =
        WBDiscoveryCorePlugin.getPlugin().getBundle().getEntry("resources/toolkits.xml");
      
      updateCacheFrom(toolkitsUrl);
    } else {
      parseToolkitsFromCache();
    }
  }

  @SuppressWarnings("deprecation")
  private void parseToolkitsFromCache() {
    try {
      URL toolkitsFileURL = getCacheLocation().append("toolkits.xml").toFile().toURL();
      
      try {
        toolkits = parseToolkits(toolkitsFileURL);
      } catch (Throwable t) {
        WBDiscoveryCorePlugin.logError(t);
      }
      
      Collections.sort(toolkits, new Comparator<WBToolkit>() {
        public int compare(WBToolkit toolkit1, WBToolkit toolkit2) {
          return toolkit1.getName().compareToIgnoreCase(toolkit2.getName());
        }
      });
      
      for (IRegistryChangeListener listener : listeners) {
        listener.handleRegistryChange();
      }
    } catch (MalformedURLException exception) {
      WBDiscoveryCorePlugin.logError(exception);
    }
  }

  private boolean cacheExists() {
    File toolkitsFile = getCacheLocation().append("toolkits.xml").toFile();
    
    return toolkitsFile.exists();
  }

  private List<WBToolkit> parseToolkits(URL toolkitsData) throws IOException, Throwable {
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    dbFactory.setIgnoringComments(true);
    dbFactory.setIgnoringElementContentWhitespace(true);
    List<WBToolkit> results = new ArrayList<WBToolkit>();
    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    Document doc = dBuilder.parse(toolkitsData.openStream());
    doc.getDocumentElement().normalize();
    NodeList nodeList = doc.getElementsByTagName("toolkit");
    for (int s = 0; s < nodeList.getLength(); s++) {
      Node node = nodeList.item(s);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        Element element = (Element) node;
        WBToolkit entry = new WBToolkit();
        entry.setId(getAttributeText(element, "id"));
        entry.setName(getAttributeText(element, "name"));
        entry.setTitle(getAttributeText(element, "title"));
        entry.setDescription(getNodeText(element, "description"));
        entry.setLicenseDescription(getAttributeText(element, "licenseDescription"));
        entry.setIconPath(toolkitsData, getAttributeText(element, "icon"));
        parseUpdateSiteInfo(element.getElementsByTagName("updateSite"), entry);
        entry.setProviderName(getAttributeText(element, "providerName"));
        entry.setMoreInfoURL(getAttributeText(element, "moreInfoURL"));
        results.add(entry);
      }
    }
    return results;
  }

  private IPath getCacheLocation() {
    return WBDiscoveryCorePlugin.getPlugin().getStateLocation();
  }
  
  private void parseUpdateSiteInfo(NodeList nodes, WBToolkit entry) {
    //<updateSite version="[3.6,3.7)" url="http://download.eclipse.org/windowbuilder/WB/integration/3.7">
    //  <feature id="org.eclipse.wb.xwt.feature"/>
    //</updateSite>
    for (int i = 0; i < nodes.getLength(); i++) {
      Node node = nodes.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        Element element = (Element) node;
        if (isInCurrentVersionRange(getAttributeText(element, "version"))) {
          entry.setUpdateSite(getAttributeText(element, "url"));
          NodeList features = element.getElementsByTagName("feature");
          for (int j = 0; j < features.getLength(); j++) {
            Node featureNode = features.item(j);
            if (featureNode.getNodeType() == Node.ELEMENT_NODE) {
              Element featureElement = (Element) featureNode;
              String featureId = getAttributeText(featureElement, "id");
              boolean optional = "true".equals(getAttributeText(featureElement, "optional"));
              entry.addFeature(featureId, optional);
            }
          }
        }
      }
    }
  }

  private boolean isInCurrentVersionRange(String versionText) {
    // An empty versionText matches all versions.
    if (versionText == null || versionText.length() == 0) {
      return true;
    }
    Bundle bundle = Platform.getBundle("org.eclipse.core.runtime");
    Version currentVersion = bundle.getVersion();
    VersionRange range = new VersionRange(versionText);
    return range.isIncluded(currentVersion);
  }

  private String getAttributeText(Element element, String attributeName) {
    if (element.hasAttribute(attributeName)) {
      return element.getAttribute(attributeName);
    }
    return null;
  }

  private String getNodeText(Element parent, String nodeName) {
    NodeList list = parent.getElementsByTagName(nodeName);
    if (list != null && list.getLength() > 0) {
      return list.item(0).getTextContent();
    }
    return null;
  }

}
