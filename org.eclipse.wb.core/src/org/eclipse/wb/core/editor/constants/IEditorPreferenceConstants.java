/*******************************************************************************
 * Copyright (c) 2021, 2026 DSA Daten- und Systemtechnik GmbH.
 *                          (https://www.dsa.de) and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marcel du Preez   - initial implementation
 *********************************************************************************/
package org.eclipse.wb.core.editor.constants;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

/**
 * This interface contains the constants used to alter preferences for Windowbuilder.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IEditorPreferenceConstants {
	//The node to use for the Windowbuilder basic preference
	/**
	 * Don't reference this constant directly. Use {@link getPreferences()} instead.
	 * This field will be removed after the 2028-12 release.
	 *
	 * @deprecated Access preferences using {@link #getPreferences()} instead.
	 */
	@Deprecated(since = "2026-12", forRemoval = true)
	public static String WB_BASIC_UI_PREFERENCE_NODE = "org.eclipse.wb.core.editor.constants.preferences";
	//Windowbuilder basic is a simplified version of Windowbuilder, containing fewer UI elements.
	public static String WB_BASIC_UI = "basicUserInterface";
	public static String WB_CLASSPATH_ICONS = "iconsClasspaths";
	//Sets the root object name in the Components Tree view
	public static String WB_ROOT_OBJ_NAME = "rootObjectDisplayName";
	/**
	 * This node is used to store the preferences of which layouts should be available in
	 * Windowbuilder Swing and SWT layout preferences both use the same node. If the preferences on
	 * this node are <code>true</true> they
	 * will be available for use as normal. If it is <code>false</code> then the specified layouts
	 * will be hidden from layout comboxes as well as the layout container in the designer palette.
	 */
	public static String P_AVAILABLE_LAYOUTS_NODE = "layout.available";

	/**
	 * Returns the preference node that contains the preferences listed in this
	 * interface.
	 *
	 * @since 1.26
	 */
	static IEclipsePreferences getPreferences() {
		return InstanceScope.INSTANCE.getNode(WB_BASIC_UI_PREFERENCE_NODE);
	}
}
