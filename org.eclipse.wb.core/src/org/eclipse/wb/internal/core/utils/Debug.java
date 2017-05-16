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
package org.eclipse.wb.internal.core.utils;

import org.eclipse.wb.internal.core.EnvironmentUtils;

/**
 * Debug output support.
 *
 * @author scheglov_ke
 * @coverage core.util
 */
public class Debug {
  /**
   * Prints debug output on console, only if developers host.
   */
  public static void print(Object s) {
    if (EnvironmentUtils.DEVELOPER_HOST) {
      System.out.print(s);
    }
  }

  /**
   * Prints debug output on console, only if developers host.
   */
  public static void println(Object s) {
    if (EnvironmentUtils.DEVELOPER_HOST) {
      System.out.println(s);
    }
  }

  /**
   * Prints new line on console, only if developers host.
   */
  public static void println() {
    if (EnvironmentUtils.DEVELOPER_HOST) {
      System.out.println();
    }
  }
}
