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
package org.eclipse.wb.internal.swing.preferences.code;

import org.eclipse.wb.internal.swing.ToolkitProvider;

/**
 * Implementation of {@link org.eclipse.wb.internal.core.preferences.code.VariablesPreferencePage}
 * for Swing.
 *
 * @author scheglov_ke
 * @coverage swing.preferences.ui
 */
public final class VariablesPreferencePage
    extends
      org.eclipse.wb.internal.core.preferences.code.VariablesPreferencePage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public VariablesPreferencePage() {
    super(ToolkitProvider.DESCRIPTION);
  }
}
