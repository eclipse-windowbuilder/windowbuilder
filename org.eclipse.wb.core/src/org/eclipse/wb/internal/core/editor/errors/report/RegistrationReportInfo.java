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
package org.eclipse.wb.internal.core.editor.errors.report;

import org.eclipse.wb.core.branding.BrandingUtils;
import org.eclipse.wb.internal.core.utils.XmlWriter;
import org.eclipse.wb.internal.core.utils.product.ProductInfo;

/**
 * Intended to gather user registration info and represents it as xml and html.
 * 
 * @author mitin_aa
 * @coverage core.editor.errors.report
 */
public final class RegistrationReportInfo implements IReportInfo {
  // constants
  private static final String CR = "\n";
  private static final String XML_TAG_REGISTRATION = "registration";
  // fields
  private String name = "";
  private String email = "";
  private final String productName;
  //private final String m_productVersion;
  //private final String m_expectedEclipse;
  private final String productVersion;

  //private final List<String> m_serials = Lists.newArrayList();
  //private final List<String> m_keys = Lists.newArrayList();
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RegistrationReportInfo() {
    //IUserInfo userInfo = License.getUserInfo();
    //m_name = userInfo.getName();
    //m_email = userInfo.getEmail();
    // 
    // removed the printing out of the expected eclipse and product version
    // when we moved Shared out of the WB build path
    //m_expectedEclipse = ProductInfo.getProduct().getExpectedEclipseText();
    productName = BrandingUtils.getBranding().getProductName(); // + " " + product.getModeName();
    productVersion =
        ProductInfo.getProduct().getVersion() + "[" + ProductInfo.getProduct().getBuild() + "]";
    //
    //for (IProduct prod : support.getRelatedProducts()) {
    //ISerialNumber serial = prod.getSerialNumber();
    //if (serial != null) {
    //m_serials.add(serial.getText());
    //}
    //IActivationKey key = prod.getActivationKey();
    //if (key != null) {
    //m_keys.add(key.getText());
    //}
    //}
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IReportInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  public void writeXML(XmlWriter xmlWriter) {
    xmlWriter.openTag(XML_TAG_REGISTRATION);
    String regInfo = "";
    regInfo += "Name: " + name + CR;
    regInfo += "E-mail: " + email + CR;
    regInfo += "Product: " + productName + CR;
    regInfo += "Product Version: " + productVersion + CR;
    //regInfo += "For Eclipse Version: " + m_expectedEclipse + CR;
    //regInfo += "Registration:" + CR;
    //for (String serial : m_serials) {
    //regInfo += "\t" + serial + CR;
    //}
    //for (String key : m_keys) {
    //regInfo += "\t" + key + CR;
    //}
    xmlWriter.write(regInfo);
    xmlWriter.closeTag(); // XML_TAG_REGISTRATION
  }

  public String getHTML() {
    String html = "<table cellspacing=\"2\" border=\"0\">";
    //html += "<tr><td>Name:</td><td>" + m_name + "</td></tr>";
    //html += "<tr><td>E-mail:</td><td>" + m_email + "</td></tr>";
    html += "<tr><td>Product:</td><td>" + productName + "</td></tr>";
    html += "<tr><td>Product Version:</td><td>" + productVersion + "</td></tr>";
    //html += "<tr><td>For Eclipse Version:</td><td>" + m_expectedEclipse + "</td></tr>";
    //html += "<tr><td style=\"vertical-align:top;\">Registration:</td><td><table border=\"0\">";
    //for (String serial : m_serials) {
    //html += "<tr><td>" + serial + "</td></tr>";
    //}
    //for (String key : m_keys) {
    //html += "<tr><td>" + key + "</td></tr>";
    //}
    //html += "</table></td></tr>";
    return html + "</table>";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public final String getName() {
    return name;
  }

  public final void setName(String name) {
    this.name = name;
  }

  public final String getEmail() {
    return email;
  }

  public final void setEmail(String email) {
    this.email = email;
  }
}