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
package org.eclipse.wb.internal.core.wizards.palette;

import org.osgi.framework.Version;

/**
 * Utilities to work with plugin elements - id, version, name, etc.
 *
 * @author scheglov_ke
 * @coverage core.wizards.ui
 */
public final class Utils {
  ////////////////////////////////////////////////////////////////////////////
  //
  // ID
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Valid composite ID contains only alpha-numeric characters and <code>'.'</code> as parts
   * separator.
   *
   * @return <code>true</code> if given composite ID is valid or <code>false</code> in other case.
   */
  public static boolean isValidCompositeID(String id) {
    if (id.length() <= 0) {
      return false;
    }
    // check separate characters
    for (int i = 0; i < id.length(); i++) {
      char c = id.charAt(i);
      if ((c < 'A' || 'Z' < c)
          && (c < 'a' || 'z' < c)
          && (c < '0' || '9' < c)
          && c != '_'
          && c != '.') {
        return false;
      }
    }
    // OK, valid
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Version
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if given version string is valid.
   */
  public static boolean isValidVersion(String version) {
    try {
      new Version(version.trim());
    } catch (IllegalArgumentException e) {
      return false;
    }
    // OK, valid
    return true;
  }
}
