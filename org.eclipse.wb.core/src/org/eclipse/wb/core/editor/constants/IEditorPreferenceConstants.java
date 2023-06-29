/*******************************************************************************
 * Copyright (c) 2021, 2022 DSA Daten- und Systemtechnik GmbH. (https://www.dsa.de)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel du Preez   - initial implementation
 *********************************************************************************/
package org.eclipse.wb.core.editor.constants;

/**
 * This interface contains the constants used to alter preferences for Windowbuilder.
 *
 */
public interface IEditorPreferenceConstants {
	//The node to use for the Windowbuilder basic preference
	public static String WB_BASIC_UI_PREFERENCE_NODE =
			"org.eclipse.wb.core.editor.constants.preferences";
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
}
