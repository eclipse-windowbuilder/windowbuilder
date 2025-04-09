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
package org.eclipse.wb.internal.swing.preferences;

/**
 * Contains various preference constants for Swing.
 *
 * @author scheglov_ke
 * @coverage swing.preferences
 */
public interface IPreferenceConstants
extends
org.eclipse.wb.internal.core.preferences.IPreferenceConstants {
	String TOOLKIT_ID = "org.eclipse.wb.swing";
	/**
	 * The template for <code>Layout</code> variable name.
	 */
	String P_LAYOUT_NAME_TEMPLATE = "templateLayoutName";
}
