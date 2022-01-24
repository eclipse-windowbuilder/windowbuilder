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
}
