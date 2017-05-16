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
package org.eclipse.wb.internal.core.utils.product;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.platform.PluginUtilities;

import org.eclipse.core.runtime.Platform;

import org.osgi.framework.Version;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Properties;

/**
 *
 */
public final class ProductInfo {
  // product info
  private static final String[] CORE_PRODUCT = {"org.eclipse.wb.core"};
  private static final String[] SWTD_PRODUCT = {"org.eclipse.wb.swt"};
  private static final String[] SWINGD_PRODUCT = {"org.eclipse.wb.swing"};
  private static final String[] GWTD_PRODUCT = {"com.google.gdt.eclipse.designer"};
  private static final String[] ERCP_PRODUCT = {"org.eclipse.wb.ercp"};
  // build info
  private static final String UNKNOWN_BUILD = "UNKNOWN";
  // eclipse info
  private static final String ECLIPSE_VERSION_KEY = "eclipse.version";
  private static final String TARGET_PROPERTIES = "target.properties";
  private static final String PLUGIN_PROPERTIES = "plugin.properties";
  private static final String BUILD_KEY = "build";
  private static final String GRACE_GIVEN = ".graceGiven";
  private static final String DEVELOPMENT_BUILD_NUM = "${build_num}";
  private static final String DOT_INTERNAL = ".internal";
  // instance info
  private final String[] product_info;
  private static ProductInfo INSTANCE;
  private Version version;
  private String build;

  private ProductInfo() {
    boolean isInstalledERCP = Platform.getBundle(ERCP_PRODUCT[0]) != null;
    boolean isInstalledSWT = Platform.getBundle(SWTD_PRODUCT[0]) != null;
    boolean isInstalledSwing = Platform.getBundle(SWINGD_PRODUCT[0]) != null;
    boolean isInstalledGWT = Platform.getBundle(GWTD_PRODUCT[0]) != null;
    if (isInstalledSWT && isInstalledSwing) {
      product_info = CORE_PRODUCT; //core
    } else if (isInstalledSWT) {
      product_info = SWTD_PRODUCT; // swt
    } else if (isInstalledSwing) {
      product_info = SWINGD_PRODUCT; // swing
    } else if (isInstalledGWT) {
      product_info = GWTD_PRODUCT; // gwt
    } else if (isInstalledERCP) {
      product_info = ERCP_PRODUCT; // ercp
    } else {
      product_info = CORE_PRODUCT; // core...should not get here
    }
  }

  public String getPluginId() {
    return product_info[0];
  }

  public static ProductInfo getProduct() {
    if (INSTANCE == null) {
      INSTANCE = new ProductInfo();
    }
    return INSTANCE;
  }

  public Version getVersion() {
    if (version != null) {
      return version;
    }
    // Determine if the platform is running
    boolean isEclipseRunning;
    try {
      isEclipseRunning = Platform.isRunning();
    } catch (NoClassDefFoundError e) {
      version = Version.emptyVersion;
      return version;
    }
    // If Eclipse is running, get the version from the plugin
    if (isEclipseRunning) {
      version = PluginUtilities.getVersion(getPluginId());
      return version;
    }
    version = Version.emptyVersion;
    return version;
  }

  /**
   * Determine if the specified plugin is installed in the currently executing development
   * environment without actually loading or starting the plugin
   *
   * @param pluginId
   *          the plugin identifier
   * @return <code>true</code> if installed, else <code>false</code>
   */
  public static boolean isInstalled(String pluginId) {
    return PluginUtilities.getInstallUrl(pluginId) != null;
  }

  public String getBuild() {
    if (build != null) {
      return build;
    }
    // Determine if the platform is running
    boolean isEclipseRunning;
    try {
      isEclipseRunning = Platform.isRunning();
    } catch (NoClassDefFoundError e) {
      build = UNKNOWN_BUILD;
      return build;
    }
    // If Eclipse is running, get the build from the plugin
    if (isEclipseRunning) {
      build = getPluginBuild(getPluginId());
      return build;
    }
    build = UNKNOWN_BUILD;
    return build;
  }

  /**
   * Answer the build for the specified plugin. This method ASSUMES that we are executing inside an
   * Eclipse based application.
   *
   * @param id
   *          the unique plugin identifier (not <code>null</code>)
   * @return the build for the plugin or UNKNOWN if it could not be determined
   */
  public static String getPluginBuild(String pluginId) {
    URL url = PluginUtilities.getUrl(pluginId, PLUGIN_PROPERTIES);
    if (url != null) {
      Properties properties = new Properties();
      InputStream stream = null;
      try {
        stream = url.openStream();
        properties.load(stream);
      } catch (IOException e) {
        DesignerPlugin.log(e);
      } finally {
        try {
          if (stream != null) {
            stream.close();
          }
        } catch (Exception e) {
          DesignerPlugin.log(e);
        }
      }
      String build = properties.getProperty(BUILD_KEY);
      // If this is a code under development in a runtime workbench, then return today's date
      if (build == null || build.equals(DEVELOPMENT_BUILD_NUM)) {
        return getStartDateTimeString();
      }
      if (build.length() > 0) {
        return build;
      }
    }
    return UNKNOWN_BUILD;
  }

  /**
   * Answer a string in the format "yyyyMMddHHmm" indicating the session start time
   *
   * @return a string (not null)
   */
  public static String getStartDateTimeString() {
    if (startTimeString == null) {
      startTimeString = getCurrentDateTimeString();
    }
    return startTimeString;
  }

  private static String startTimeString;

  /**
   * Answer a string in the format "yyyyMMddHHmm" indicating the current time
   *
   * @return a string (not null)
   */
  public static String getCurrentDateTimeString() {
    return new SimpleDateFormat("yyyyMMddHHmm").format(new GregorianCalendar().getTime());
  }
}
