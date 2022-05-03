/*******************************************************************************
 * Copyright (c) 2021, 2021 DSA Daten- und Systemtechnik GmbH. (https://www.dsa.de)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel du Preez   - initial implementation
 *******************************************************************************/
package org.eclipse.wb.core.editor.constants;

import org.eclipse.wb.internal.core.model.ModelMessages;

public interface IColorChooserPreferenceConstants {
  String PREFERENCE_NODE = "org.eclipse.wb.colorchooser.preferences";
  String PREFERENCE_NODE_1 = "org.eclipse.wb.colorchooser.preferences.swing";
  String P_CUSTOM_COLORS = "customColor";
  String P_SYSTEM_COLORS = "sysColor";
  String P_AWT_COLORS = "awtColor";
  String P_SWING_COLORS = "swingColor";
  String P_NAMED_COLORS = "namedColor";
  String P_WEB_SAFE_COLORS = "webSafeColor";
  String CUSTOM_COLORS = ModelMessages.ColorPropertyEditor_pageCustomColors;
  String SYSTEM_COLORS = ModelMessages.ColorPropertyEditor_pageSystemColors;
  String AWT_COLORS = ModelMessages.ColorPropertyEditor_pageAwtColors;
  String SWING_COLORS = ModelMessages.ColorPropertyEditor_pageSwingColors;
  String NAMED_COLORS = ModelMessages.ColorPropertyEditor_pageNamedColors;
  String WEBSAFE_COLORS = ModelMessages.ColorPropertyEditor_pageWebColors;
}
