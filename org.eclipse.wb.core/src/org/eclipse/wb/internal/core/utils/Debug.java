/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
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
