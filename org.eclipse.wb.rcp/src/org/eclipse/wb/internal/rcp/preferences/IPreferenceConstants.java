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
package org.eclipse.wb.internal.rcp.preferences;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.widgets.Control;

/**
 * Contains various preference constants for RCP.
 *
 * @author scheglov_ke
 * @coverage rcp.preferences
 */
public interface IPreferenceConstants {
  String TOOLKIT_ID = "org.eclipse.wb.rcp";
  ////////////////////////////////////////////////////////////////////////////
  //
  // Forms API
  //
  ////////////////////////////////////////////////////////////////////////////
  String FORMS_PAINT_BORDERS = TOOLKIT_ID + ".forms: paint borders for";
  String FORMS_ADAPT_CONTROL = TOOLKIT_ID + ".forms: adapt new controls";
  ////////////////////////////////////////////////////////////////////////////
  //
  // PreferencePage
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Specifies that {@link FieldEditor}s should use same code generation settings as {@link Control}
   * s.
   */
  String PREF_FIELD_USUAL_CODE = TOOLKIT_ID + ".preferencePage: use usual code generation style";
}
