/*******************************************************************************
 * Copyright (c) 2007 SAS Institute. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
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
