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
