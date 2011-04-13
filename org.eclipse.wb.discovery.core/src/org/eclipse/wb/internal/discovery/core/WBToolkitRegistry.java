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

import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.resolver.VersionRange;

import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.net.URL;
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

  public void checkForUpdates() {
    // TODO:
  }

  private void initRegistry() {
    // TODO: read from some some cached location
    // TODO: else read from the static location
    URL toolkitsData =
        WBDiscoveryCorePlugin.getPlugin().getBundle().getEntry("resources/toolkits.xml");
    try {
      toolkits = parseToolkits(toolkitsData);
    } catch (Throwable t) {
      WBDiscoveryCorePlugin.logError(t);
    }
    Collections.sort(toolkits, new Comparator<WBToolkit>() {
      public int compare(WBToolkit toolkit1, WBToolkit toolkit2) {
        //if (toolkit1.isInstalled() != toolkit2.isInstalled()) {
        //  return toolkit1.isInstalled() ? 1 : -1;
        //}
        return toolkit1.getName().compareToIgnoreCase(toolkit2.getName());
      }
    });
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
