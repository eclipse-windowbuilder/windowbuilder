/*******************************************************************************
 * Copyright (c) 2007 SAS Institute. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies
 * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SAS Institute - initial API and implementation
 *******************************************************************************/
package swingintegration.example;

import org.eclipse.swt.SWT;

class Platform {
  private static String platformString = SWT.getPlatform();

  // prevent instantiation
  private Platform() {
  }

  public static boolean isWin32() {
    return "win32".equals(platformString); //$NON-NLS-1$
  }

  public static boolean isGtk() {
    return "gtk".equals(platformString); //$NON-NLS-1$
  }
}
