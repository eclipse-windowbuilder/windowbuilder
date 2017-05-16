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
package org.eclipse.wb.internal.core.utils.platform;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PluginVersionIdentifier;

import org.osgi.framework.Version;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Central place for Platform information
 *
 * @author Dan Rubel
 */
public class PlatformInfo {
  /**
   * The name displayed for an unknown IDE
   */
  public static final String UNKNOWN_IDE_NAME = "Unknown IDE";
  /**
   * Constant representing unknown version of Eclipse or IDE
   */
  public static final PluginVersionIdentifier UNKNOWN_VERSION = new PluginVersionIdentifier(100,
      1,
      4);
  /**
   * Constant representing unknown version string of Eclipse or IDE
   */
  public static final String UNKNOWN_VERSION_STRING = UNKNOWN_VERSION.toString();
  /**
   * The version displayed for an unknown IDE version
   */
  public static final String UNKNOWN_BUILD_ID = "?BUILD-ID?";
  /**
   * The NL value displayed for an unknown NL
   */
  public static final String UNKNOWN_NL = "?NL?";
  /**
   * The full name for the IDE
   */
  private static String ideName;
  /**
   * The development environment version string or <code>null</code> if not initialized yet by
   * {@link #getIDEVersionString()}
   */
  private static String ideVersionString;
  /**
   * The development environment's NL (not null, not empty, no leading or trailing spaces)
   */
  private static String ideNL;
  /**
   * The version of Eclipse upon which the development environment is based or <code>null</code> if
   * not initialized yet by {@link #getEclipseVersion()}
   */
  private static Version eclipseVersion;
  /**
   * The build identifier for Eclipse (e.g. "v20061503") or <code>null</code> if not initialized yet
   * by {@link #getEclipseBuildId()}. For Eclipse 3.2, this is the same as the Eclipse version
   * qualifier segment. For older versions of Eclipse, this is read from the JDT plugin
   * about.properties file.
   */
  private static String eclipseBuildId;
  /**
   * The name for this particular Eclipse build (e.g. "M6", "M7", "RC1", "", etc) or
   * <code>null</code> if not initialized yet by {@link #getEclipseBuildName()}. This may contain
   * the same value as {@link #getEclipseBuildId()} if the build is not a milestone, release
   * candidate, or GA.
   */
  private static String eclipseBuildName;
  /**
   * The build date for this particular instance of Eclipse as derived from
   * {@link #getEclipseBuildId()} or <code>null</code> if not initialized yet by
   * {@link #getEclipseBuildDate()}.
   */
  private static GregorianCalendar eclipseBuildDate;

  /**
   * No instances
   */
  private PlatformInfo() {
  }

  // //////////////////////////////////////////////////////////////////////////
  //
  // Accessors
  //
  // //////////////////////////////////////////////////////////////////////////
  /**
   * Answer the development environment full name
   *
   * @return the full name (not null, not empty, no leading or trailing spaces)
   */
  public static String getIDEName() {
    if (ideName == null) {
      try {
        if (Platform.getProduct() != null) {
          ideName = Platform.getProduct().getName();
        }
        if (ideName == null) {
          ideName = UNKNOWN_IDE_NAME;
        } else {
          ideName = ideName.trim();
          if (ideName.length() == 0) {
            ideName = UNKNOWN_IDE_NAME;
          } else if (ideName.startsWith("Eclipse Pl")) {
            ideName = "Eclipse";
          } else if (ideName.startsWith("Common OS-independent base of the Eclipse platform")) {
            ideName = "Eclipse";
          }
        }
      } catch (Exception ex) {
        // Logger may not be open, so cannot log exception
        ideName = UNKNOWN_IDE_NAME;
      }
    }
    return ideName;
  }

  /**
   * Answer the development environment version string
   *
   * @return the version string (not null, not empty, no leading or trailing spaces)
   */
  public static String getIDEVersionString() {
    if (ideVersionString == null) {
      try {
        String pluginId = null;
        if (Platform.getProduct() != null) {
          pluginId = Platform.getProduct().getDefiningBundle().getSymbolicName();
        }
        ideVersionString = PluginUtilities.getVersionString(pluginId);
      } catch (Exception ex) {
        // Logger may not be open, so cannot log exception
      }
      if (ideVersionString == null || ideVersionString.length() == 0) {
        ideVersionString = UNKNOWN_VERSION_STRING;
      }
    }
    return ideVersionString;
  }

  /**
   * Returns the string name of the current locale for use in finding files whose path starts with
   * <code>$nl$</code>.
   *
   * @return the national language being used (not null, not empty, no leading or trailing spaces)
   */
  public static String getIDENL() {
    if (ideNL == null) {
      try {
        ideNL = Platform.getNL();
      } catch (Exception e) {
        // Logger may not be open, so cannot log exception
      }
      if (ideNL == null) {
        ideNL = UNKNOWN_NL;
      }
    }
    return ideNL;
  }

  /**
   * Answer the version of Eclipse upon which the development environment is based.
   *
   * @return the version (not <code>null</code>)
   */
  public static Version getEclipseVersion() {
    if (eclipseVersion == null) {
      try {
        eclipseVersion = PluginUtilities.getVersion("org.eclipse.core.runtime");
        // Use org.eclipse.jdt.ui to tell the difference between E-3.5 and E-3.6 M#
        if (eclipseVersion.getMajor() == 3 && eclipseVersion.getMinor() == 5) {
          Version version = PluginUtilities.getVersion("org.eclipse.jdt.ui");
          if (version.getMajor() == 3 && version.getMinor() == 6) {
            eclipseVersion = version;
          }
        }
        // For older versions of Eclipse, read the build from the JDT
        if (eclipseVersion.getQualifier().length() == 0) {
          eclipseVersion =
              new Version(eclipseVersion.getMajor(),
                  eclipseVersion.getMinor(),
                  eclipseVersion.getMicro(),
                  "v" + readBuildId("org.eclipse.jdt", "about.mappings"));
        }
      } catch (Exception e) {
        // Logger may not be open, so cannot log exception
      }
      if (eclipseVersion == null) {
        eclipseVersion = Version.emptyVersion;
      }
    }
    return eclipseVersion;
  }

  /**
   * Answer the build identifier for Eclipse (e.g. "200408122000"). This is read from the JDT plugin
   * about.properties file.
   *
   * @return the build id or {@link #UNKNOWN_BUILD_ID} if it cannot be determined
   */
  public static String getEclipseBuildId() {
    if (eclipseBuildId == null) {
      try {
        eclipseBuildId = readBuildId("org.eclipse.jdt", "about.mappings");
      } catch (Exception e) {
        // Logger may not be open, so cannot log exception
      }
      if (eclipseBuildId == null) {
        eclipseBuildId = UNKNOWN_BUILD_ID;
      }
    }
    return eclipseBuildId;
  }

  /**
   * Answer the name for this particular Eclipse build (e.g. "M6", "M7", "RC1", "", etc). This may
   * return the same value as {@link #getEclipseBuildId()} if the build is not a milestone, release
   * candidate, or GA.
   *
   * @return the build name (not <code>null</code>, but may be empty if it is a GA)
   */
  public static String getEclipseBuildName() {
    if (eclipseBuildName == null) {
      eclipseBuildName = "";
      if (eclipseBuildName == null) {
        eclipseBuildName = getEclipseBuildId();
      }
    }
    return eclipseBuildName;
  }

  /**
   * Answer the build date for this particular instance of Eclipse as derived from
   * {@link #getEclipseBuildId()} or Jan 1, 2003 if it cannot be determined.
   *
   * @return the build date (not <code>null</code>)
   */
  public static Calendar getEclipseBuildDate() {
    if (eclipseBuildDate == null) {
      try {
        String ymd = getEclipseBuildId();
        if (Character.isLetter(ymd.charAt(0))) {
          ymd = ymd.substring(1);
        }
        int y = Integer.parseInt(ymd.substring(0, 4));
        int m = Integer.parseInt(ymd.substring(4, 6)) - 1;
        int d = Integer.parseInt(ymd.substring(6, 8));
        eclipseBuildDate = new GregorianCalendar(y, m, d);
      } catch (Exception e) {
        // Logger may not be open, so cannot log exception
      }
      if (eclipseBuildDate == null) {
        eclipseBuildDate = new GregorianCalendar(2003, 0, 1);
      }
    }
    return eclipseBuildDate;
  }

  /**
   * Read the build identifier from the specified plugin file.
   *
   * @param pluginId
   *          the plugin's unique identifier
   * @param fileName
   *          the name of the file containing the build id
   * @return the build identifier or <code>null</code> if it cannot be determined
   */
  private static String readBuildId(String pluginId, String fileName) {
    InputStream stream = null;
    try {
      URL url = PluginUtilities.getUrl(pluginId, fileName);
      stream = url.openStream();
      LineNumberReader reader = new LineNumberReader(new InputStreamReader(stream));
      while (true) {
        String line = reader.readLine();
        if (line == null) {
          return null;
        }
        if (line.startsWith("0=")) {
          return line.substring(2).trim();
        }
      }
    } catch (Exception e) {
      // Logger may not be open, so cannot log exception
    } finally {
      try {
        if (stream != null) {
          stream.close();
        }
      } catch (IOException e) {
        // Logger may not be open, so cannot log exception
      }
    }
    return null;
  }
}
